package com.example.finsight.controller;

import com.example.finsight.model.Usuario;
import com.example.finsight.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutenticacaoController {

    private final UsuarioService usuarioService;

    public AutenticacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // =========================================================================
    // LANDING PAGE E AUTH
    // =========================================================================

    @GetMapping("/")
    public String landingPage(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            return "redirect:/app";
        }
        return "landing";
    }

    @GetMapping("/login")
    public String paginaLogin(@AuthenticationPrincipal Usuario usuario) {
        // Se já estiver logado, redireciona direto para home
        if (usuario != null) {
            return "redirect:/app";
        }
        return "login";
    }

    // O POST /login e POST /logout são interceptados e processados automaticamente pelo Spring Security

    // =========================================================================
    // CADASTRO
    // =========================================================================

    @GetMapping("/registrar")
    public String paginaRegistro(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            return "redirect:/";
        }
        return "registrar";
    }

    @PostMapping("/registrar")
    public String processarRegistro(@RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            RedirectAttributes attr) {

        if (!senha.equals(confirmarSenha)) {
            attr.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/registrar";
        }

        if (senha.length() < 6) {
            attr.addFlashAttribute("erro", "A senha deve ter ao menos 6 caracteres.");
            return "redirect:/registrar";
        }

        boolean sucesso = usuarioService.registrar(nome, email, senha);

        if (sucesso) {
            attr.addFlashAttribute("sucesso", "Conta criada com sucesso! Faça o login.");
            return "redirect:/login";
        } else {
            attr.addFlashAttribute("erro", "Este e-mail já está cadastrado.");
            return "redirect:/registrar";
        }
    }
}
