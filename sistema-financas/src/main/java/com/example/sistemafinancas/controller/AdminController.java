package com.example.sistemafinancas.controller;

import com.example.sistemafinancas.model.Perfil;
import com.example.sistemafinancas.model.Usuario;
import com.example.sistemafinancas.repository.TransacaoRepository;
import com.example.sistemafinancas.repository.UsuarioRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller administrativo: gerencia usuários cadastrados no sistema.
 * Protegido pelo Spring Security — exige ROLE_ADMIN.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;

    public AdminController(UsuarioRepository usuarioRepository,
                           TransacaoRepository transacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(@AuthenticationPrincipal Usuario usuarioLogado, Model model) {
        model.addAttribute("nomeUsuario", usuarioLogado.getNome());
        model.addAttribute("idUsuarioLogado", usuarioLogado.getId()); // Usado no template para desativar os botões na própria conta

        List<Usuario> usuarios = usuarioRepository.findAll();

        // Conta quantas transações cada usuário possui
        List<Long> quantidades = usuarios.stream()
                .map(u -> transacaoRepository.findByUsuario(u).stream().count())
                .toList();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("quantidades", quantidades);
        model.addAttribute("total", usuarios.size());

        return "admin/usuarios";
    }

    @PostMapping("/usuario/{id}/toggle-ativo")
    public String alternarStatusAtivo(@PathVariable Long id,
                                      @AuthenticationPrincipal Usuario usuarioLogado,
                                      RedirectAttributes attr) {
        if (id.equals(usuarioLogado.getId())) {
            attr.addFlashAttribute("erro", "Você não pode desativar a própria conta.");
            return "redirect:/admin/usuarios";
        }

        Usuario u = usuarioRepository.findById(id).orElseThrow();
        u.setAtivo(!u.isAtivo());
        usuarioRepository.save(u);
        attr.addFlashAttribute("sucesso", "Status da conta alterado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuario/{id}/perfil")
    public String alterarPerfil(@PathVariable Long id,
                                @RequestParam String novoPerfil,
                                @AuthenticationPrincipal Usuario usuarioLogado,
                                RedirectAttributes attr) {
        if (id.equals(usuarioLogado.getId())) {
            attr.addFlashAttribute("erro", "Você não pode alterar o próprio nível de acesso.");
            return "redirect:/admin/usuarios";
        }

        Usuario u = usuarioRepository.findById(id).orElseThrow();
        try {
            u.setPerfil(Perfil.valueOf(novoPerfil));
            usuarioRepository.save(u);
            attr.addFlashAttribute("sucesso", "Nível de acesso alterado com sucesso.");
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("erro", "Perfil inválido.");
        }
        return "redirect:/admin/usuarios";
    }
}
