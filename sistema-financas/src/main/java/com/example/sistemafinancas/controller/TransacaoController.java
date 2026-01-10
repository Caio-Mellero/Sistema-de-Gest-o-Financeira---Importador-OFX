package com.example.sistemafinancas.controller;

import com.example.sistemafinancas.model.TipoTransacao;
import com.example.sistemafinancas.model.Transacao;
import com.example.sistemafinancas.repository.TransacaoRepository;
import com.example.sistemafinancas.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

@Controller
public class TransacaoController {

    @Autowired
    private TransacaoRepository repository;
    @Autowired
    private TransacaoService transacaoService;

    @GetMapping("/")
    public String listarTransacoes(Model model) {

        List<Transacao> todas = repository.findAll();
        model.addAttribute("transacoes", todas);

        // Categorias
        List<String> categoriasPreset = Arrays.asList(
                "Alimentação", "Lazer", "Transporte", "Fatura", "Saúde", "Educação", "Moradia", "Outros"
        );
        model.addAttribute("categoriasPreset", categoriasPreset);

        // Cálculo de totais
        BigDecimal entradas = todas.stream()
                .filter(t -> t.getTipo() == TipoTransacao.ENTRADA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saidas = todas.stream()
                .filter(t -> t.getTipo() == TipoTransacao.SAIDA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        model.addAttribute("totalEntradasFmt", nf.format(entradas.abs()));
        model.addAttribute("totalSaidasFmt", nf.format(saidas.abs()));
        model.addAttribute("saldoFmt", nf.format(entradas.subtract(saidas.abs())));

        return "index"; // ou "dashboard", dependendo do nome do seu template
    }

    @PostMapping("/importar")
    public String importar(@RequestParam("arquivo") MultipartFile arquivo, RedirectAttributes attributes) {
        if (arquivo.isEmpty()) {
            attributes.addFlashAttribute("mensagem", "Por favor, selecione um arquivo OFX.");
            return "redirect:/";
        }

        try {
            int total = transacaoService.importarOFX(arquivo.getInputStream());

            if (total > 0) {
                attributes.addFlashAttribute("mensagem", total + " novas transações processadas com sucesso!");
            } else {
                attributes.addFlashAttribute("mensagem", "Nenhuma transação nova encontrada. O arquivo parece já ter sido importado.");
            }

        } catch (Exception e) {
            attributes.addFlashAttribute("mensagem", "Erro ao importar: " + e.getMessage());
        }

        return "redirect:/"; // Atualiza a página para mostrar os novos dados
    }

    @PostMapping("/atualizar-categoria")
    public String atualizarCategoria(@RequestParam("id") Long id, @RequestParam("novaCategoria") String novaCategoria) {
        Transacao t = repository.findById(id).orElseThrow();
        t.setCategoria(novaCategoria);
        repository.save(t); // Atualiza no banco
        return "redirect:/";
    }

    @PostMapping("/lancar-manualmente")
    public String lancarManual(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam("categoria") String categoria,
            @RequestParam("descricao") String descricao,
            @RequestParam("tipo") TipoTransacao tipo,
            @RequestParam("valor") BigDecimal valor,
            RedirectAttributes redirectAttributes
    ) {
        Transacao nova = new Transacao();
        nova.setData(data);
        nova.setCategoria(categoria);
        nova.setDescricao(descricao);
        nova.setTipo(tipo);
        nova.setValor(valor);

        repository.save(nova);

        redirectAttributes.addFlashAttribute("mensagem", "Transação lançada com sucesso!");
        return "redirect:/";
    }

    @PostMapping("/apagar/{id}")
    public String apagarTransacao(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }

}