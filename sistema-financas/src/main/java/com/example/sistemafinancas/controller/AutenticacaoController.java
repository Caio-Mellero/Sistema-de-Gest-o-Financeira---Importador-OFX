package com.example.sistemafinancas.controller;

import com.example.sistemafinancas.model.Usuario;
import com.example.sistemafinancas.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AutenticacaoController {

    private final UsuarioService usuarioService;

    public AutenticacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    @GetMapping("/login")
    public String paginaLogin(HttpSession session) {
        // Se já estiver logado, redireciona direto para home
        if (session.getAttribute("usuarioLogado") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String email,
                                 @RequestParam String senha,
                                 HttpSession session,
                                 RedirectAttributes attr) {
        Optional<Usuario> usuario = usuarioService.autenticar(email, senha);

        if (usuario.isPresent()) {
            session.setAttribute("usuarioLogado", usuario.get());
            session.setMaxInactiveInterval(60 * 60 * 8); // Sessão de 8 horas
            return "redirect:/";
        } else {
            attr.addFlashAttribute("erro", "E-mail ou senha incorretos.");
            return "redirect:/login";
        }
    }

    // =========================================================================
    // CADASTRO
    // =========================================================================

    @GetMapping("/registrar")
    public String paginaRegistro(HttpSession session) {
        if (session.getAttribute("usuarioLogado") != null) {
            return "redirect:/";
        }
        return "registrar";
    }

    @PostMapping("/registrar")
    public String processarRegistro(@RequestParam String nome,
                                    @RequestParam String email,
                                    @RequestParam String senha,
                                    @RequestParam String confirmarSenha,
                                    HttpSession session,
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

    // =========================================================================
    // LOGOUT
    // =========================================================================

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destrói a sessão completamente
        return "redirect:/login";
    }
}
