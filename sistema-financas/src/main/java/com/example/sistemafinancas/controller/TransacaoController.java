package com.example.sistemafinancas.controller;

import com.example.sistemafinancas.model.TipoTransacao;
import com.example.sistemafinancas.model.Transacao;
import com.example.sistemafinancas.repository.TransacaoRepository;
import com.example.sistemafinancas.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TransacaoController {

    @Autowired
    private TransacaoRepository repository;
    @Autowired
    private TransacaoService transacaoService;

    @GetMapping("/")
    public String listarTransacoes(@RequestParam(required = false, defaultValue = "data") String ordem, Model model) {

        // Ordenação
        Sort sort;
        if ("valor".equals(ordem)) {
            sort = Sort.by(Sort.Direction.DESC, "valor");
        } else if ("texto".equals(ordem)) {
            sort = Sort.by(Sort.Direction.ASC, "descricao");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "data");
        }

        // Busca os dados
        List<Transacao> todas = repository.findAll(sort);
        model.addAttribute("transacoes", todas);
        model.addAttribute("filtroAtual", ordem);

        // Calculo total dos Cards
        calcularTotais(todas, model);

        //Lista de Categorias para o Modal
        model.addAttribute("categoriasPreset", Arrays.asList(
                "Alimentação", "Lazer", "Transporte", "Fatura", "Saúde", "Educação", "Moradia", "Outros"));

        return "index"; // Carrega o index.html com a tabela
    }

    @GetMapping("/dashboard")
    public String carregarGraficos(Model model) {
        List<Transacao> todas = repository.findAll();

        calcularTotais(todas, model);

        // Prepara dados para o Gráfico de Rosca
        Map<String, BigDecimal> gastosPorCategoria = todas.stream()
                .filter(t -> t.getTipo() == TipoTransacao.SAIDA)
                .collect(Collectors.groupingBy(
                        Transacao::getCategoria,
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ));

        List<String> nomes = new ArrayList<>();
        List<BigDecimal> valores = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : gastosPorCategoria.entrySet()) {
            nomes.add(entry.getKey());
            valores.add(entry.getValue().abs());
        }

        model.addAttribute("graficoNomes", nomes);
        model.addAttribute("graficoValores", valores);

        // Preparando dados dos últimos 3 meses (Exemplo)
        List<String> meses = Arrays.asList("Outubro", "Novembro", "Dezembro");
        List<BigDecimal> somaEntradas = Arrays.asList(new BigDecimal("5000"), new BigDecimal("4800"), new BigDecimal("5200"));
        List<BigDecimal> somaSaidas = Arrays.asList(new BigDecimal("3000"), new BigDecimal("3500"), new BigDecimal("2900"));

        model.addAttribute("labelsMeses", meses);
        model.addAttribute("dadosEntradas", somaEntradas);
        model.addAttribute("dadosSaidas", somaSaidas);

        return "dashboard";
    }

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

    @PostMapping("/importar")
    public String importar(@RequestParam("arquivo") MultipartFile arquivo, RedirectAttributes attr) {
        try {
            int total = transacaoService.importarOFX(arquivo.getInputStream());
            attr.addFlashAttribute("mensagem", total > 0 ? total + " importados!" : "Nada novo.");
        } catch (Exception e) {
            attr.addFlashAttribute("mensagem", "Erro: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/lancar-manualmente")
    public String lancarManual(Transacao transacao, RedirectAttributes attr) {
        if (transacao.getTipo() == TipoTransacao.SAIDA && transacao.getValor().compareTo(BigDecimal.ZERO) > 0) {
            transacao.setValor(transacao.getValor().negate());
        }
        repository.save(transacao);
        attr.addFlashAttribute("mensagem", "Salvo com sucesso!");
        return "redirect:/";
    }

    @PostMapping("/atualizar-categoria")
    public String atualizarCategoria(@RequestParam("id") Long id, @RequestParam("novaCategoria") String cat) {
        Transacao t = repository.findById(id).orElseThrow();
        t.setCategoria(cat);
        repository.save(t);
        return "redirect:/";
    }

    @PostMapping("/apagar/{id}")
    public String apagar(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }
}