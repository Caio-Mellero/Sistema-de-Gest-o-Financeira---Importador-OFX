package com.example.finsight.controller;

import com.example.finsight.model.TipoTransacao;
import com.example.finsight.model.Transacao;
import com.example.finsight.model.Usuario;
import com.example.finsight.repository.TransacaoRepository;
import com.example.finsight.service.TransacaoService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TransacaoController {

    private final TransacaoRepository repository;
    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoRepository repository, TransacaoService transacaoService) {
        this.repository = repository;
        this.transacaoService = transacaoService;
    }

    // =========================================================================
    // LISTAGEM PRINCIPAL
    // =========================================================================

    @GetMapping("/app")
    public String listarTransacoes(@RequestParam(required = false, defaultValue = "data") String ordem,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {

        Sort sort = switch (ordem) {
            case "valor" -> Sort.by(Sort.Direction.DESC, "valor");
            case "texto" -> Sort.by(Sort.Direction.ASC, "descricao");
            default -> Sort.by(Sort.Direction.DESC, "data");
        };

        List<Transacao> todas = repository.findByUsuario(usuario, sort);
        model.addAttribute("transacoes", todas);
        model.addAttribute("filtroAtual", ordem);
        model.addAttribute("nomeUsuario", usuario.getNome());

        calcularTotais(todas, model);

        model.addAttribute("categoriasPreset", Arrays.asList(
                "Alimentação", "Lazer", "Transporte", "Fatura", "Saúde", "Educação", "Moradia", "Outros"));

        return "index";
    }

    // =========================================================================
    // DASHBOARD
    // =========================================================================

    @GetMapping("/dashboard")
    public String carregarGraficos(@AuthenticationPrincipal Usuario usuario, Model model) {
        List<Transacao> todas = repository.findByUsuario(usuario);

        model.addAttribute("nomeUsuario", usuario.getNome());
        calcularTotais(todas, model);

        // --- Gráfico de Rosca: gastos por categoria ---
        Map<String, BigDecimal> gastosPorCategoria = todas.stream()
                .filter(t -> t.getTipo() == TipoTransacao.SAIDA)
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria() != null ? t.getCategoria() : "Não categorizado",
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)));

        List<String> graficoNomes = new ArrayList<>();
        List<BigDecimal> graficoValores = new ArrayList<>();
        gastosPorCategoria.forEach((cat, val) -> {
            graficoNomes.add(cat);
            graficoValores.add(val.abs());
        });

        model.addAttribute("graficoNomes", graficoNomes);
        model.addAttribute("graficoValores", graficoValores);

        // --- Gráfico de Barras: histórico mensal real dos últimos 6 meses ---
        List<String> labelsMeses = new ArrayList<>();
        List<BigDecimal> dadosEntradas = new ArrayList<>();
        List<BigDecimal> dadosSaidas = new ArrayList<>();
        Locale ptBr = Locale.forLanguageTag("pt-BR");

        LocalDate hoje = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoje.minusMonths(i);
            int ano = mes.getYear();
            int numMes = mes.getMonthValue();

            BigDecimal entradas = todas.stream()
                    .filter(t -> t.getTipo() == TipoTransacao.ENTRADA
                            && t.getData().getYear() == ano
                            && t.getData().getMonthValue() == numMes)
                    .map(Transacao::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saidas = todas.stream()
                    .filter(t -> t.getTipo() == TipoTransacao.SAIDA
                            && t.getData().getYear() == ano
                            && t.getData().getMonthValue() == numMes)
                    .map(Transacao::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String nomeMes = mes.getMonth().getDisplayName(TextStyle.SHORT, ptBr);
            labelsMeses.add(nomeMes + "/" + String.valueOf(ano).substring(2));
            dadosEntradas.add(entradas);
            dadosSaidas.add(saidas.abs());
        }

        model.addAttribute("labelsMeses", labelsMeses);
        model.addAttribute("dadosEntradas", dadosEntradas);
        model.addAttribute("dadosSaidas", dadosSaidas);

        return "dashboard";
    }

    // =========================================================================
    // IMPORTAR OFX
    // =========================================================================

    @PostMapping("/importar")
    public String importar(@RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes attr) {
        try {
            int total = transacaoService.importarOFX(arquivo.getInputStream(), usuario);
            attr.addFlashAttribute("mensagem",
                    total > 0 ? total + " transaç" + (total == 1 ? "ão importada" : "ões importadas") + "!"
                            : "Nada novo para importar.");
        } catch (Exception e) {
            attr.addFlashAttribute("mensagem", "Erro ao importar: " + e.getMessage());
        }
        return "redirect:/app";
    }

    // =========================================================================
    // LANÇAMENTO MANUAL
    // =========================================================================

    @PostMapping("/lancar-manualmente")
    public String lancarManual(Transacao transacao,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes attr) {
        transacao.setUsuario(usuario);

        if (transacao.getTipo() == TipoTransacao.SAIDA
                && transacao.getValor().compareTo(BigDecimal.ZERO) > 0) {
            transacao.setValor(transacao.getValor().negate());
        }
        repository.save(transacao);
        attr.addFlashAttribute("mensagem", "Lançamento salvo com sucesso!");
        return "redirect:/app";
    }

    // =========================================================================
    // ATUALIZAR CATEGORIA
    // =========================================================================

    @PostMapping("/atualizar-categoria")
    public String atualizarCategoria(@RequestParam("id") Long id,
            @RequestParam("novaCategoria") String cat,
            @AuthenticationPrincipal Usuario usuario) {
        Transacao t = repository.findById(id).orElseThrow();

        // Segurança: garante que o usuário só edita suas próprias transações
        if (!t.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/app";
        }
        t.setCategoria(cat);
        repository.save(t);
        return "redirect:/app";
    }

    // =========================================================================
    // APAGAR TRANSAÇÃO
    // =========================================================================

    @PostMapping("/apagar/{id}")
    public String apagar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        Transacao t = repository.findById(id).orElseThrow();

        // Segurança: garante que o usuário só apaga suas próprias transações
        if (t.getUsuario().getId().equals(usuario.getId())) {
            repository.deleteById(id);
        }
        return "redirect:/app";
    }

    // =========================================================================
    // UTILITÁRIOS PRIVADOS
    // =========================================================================

    private void calcularTotais(List<Transacao> transacoes, Model model) {
        BigDecimal entradas = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.ENTRADA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saidas = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.SAIDA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = entradas.add(saidas);

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        model.addAttribute("totalEntradasFmt", nf.format(entradas.abs()));
        model.addAttribute("totalSaidasFmt", nf.format(saidas.abs()));
        model.addAttribute("saldoFmt", nf.format(saldo));
    }
}
