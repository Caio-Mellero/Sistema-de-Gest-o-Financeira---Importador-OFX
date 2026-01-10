package com.example.sistemafinancas.controller;

import com.example.sistemafinancas.model.Transacao;
import com.example.sistemafinancas.repository.TransacaoRepository;
import com.example.sistemafinancas.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class TransacaoController {

    @Autowired
    private TransacaoRepository repository;
    @Autowired
    private TransacaoService transacaoService;

    @GetMapping("/")
    public String listarTransacoes(Model model) {
        model.addAttribute("transacoes", repository.findAll());

        // Lista de categorias pré-setadas
        List<String> categoriasPreset = Arrays.asList(
                "Alimentação", "Lazer", "Transporte", "Fatura", "Saúde", "Educação", "Moradia", "Outros"
        );
        model.addAttribute("categoriasPreset", categoriasPreset);

        // ... (mantenha os cálculos de totais que fizemos antes)
        return "index";
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

}