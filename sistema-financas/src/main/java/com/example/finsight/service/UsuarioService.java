package com.example.finsight.service;

import com.example.finsight.model.Perfil;
import com.example.finsight.model.Usuario;
import com.example.finsight.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra um novo usuário com perfil USER padrão.
     * Retorna false se o e-mail já estiver em uso.
     */
    public boolean registrar(String nome, String email, String senhaPlana) {
        if (usuarioRepository.existsByEmail(email)) {
            return false;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email.toLowerCase().trim());
        usuario.setSenha(passwordEncoder.encode(senhaPlana)); // Criptografia BCrypt
        usuario.setPerfil(Perfil.USER); // Por padrão, sempre cria como USER comum
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        return true;
    }

    /**
     * Método obrigatório do Spring Security.
     * Busca o usuário no banco para injetar na sessão de autenticação.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
