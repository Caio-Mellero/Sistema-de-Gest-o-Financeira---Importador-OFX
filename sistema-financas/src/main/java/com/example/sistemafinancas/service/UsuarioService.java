package com.example.sistemafinancas.service;

import com.example.sistemafinancas.model.Usuario;
import com.example.sistemafinancas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra um novo usuário, fazendo o hash da senha com SHA-256 + salt antes de salvar.
     * Retorna false se o e-mail já estiver em uso.
     */
    public boolean registrar(String nome, String email, String senhaPlana) {
        if (usuarioRepository.existsByEmail(email)) {
            return false;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email.toLowerCase().trim());
        usuario.setSenha(hashSenha(senhaPlana));
        usuarioRepository.save(usuario);
        return true;
    }

    /**
     * Autentica um usuário. Retorna o objeto Usuario se as credenciais forem válidas,
     * ou Optional.empty() caso contrário.
     */
    public Optional<Usuario> autenticar(String email, String senhaPlana) {
        return usuarioRepository.findByEmail(email.toLowerCase().trim())
                .filter(u -> verificarSenha(senhaPlana, u.getSenha()));
    }

    // -------------------------------------------------------------------------
    // Utilitários internos de hashing
    // -------------------------------------------------------------------------

    /**
     * Gera um hash SHA-256 com salt aleatório.
     * Formato armazenado: BASE64(salt) + ":" + BASE64(hash)
     */
    private String hashSenha(String senhaPlana) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(senhaPlana.getBytes());

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }

    /**
     * Verifica se a senha plana bate com o hash armazenado.
     */
    private boolean verificarSenha(String senhaPlana, String hashArmazenado) {
        try {
            String[] partes = hashArmazenado.split(":");
            if (partes.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(partes[0]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashCalculado = md.digest(senhaPlana.getBytes());

            return MessageDigest.isEqual(hashCalculado, hashEsperado);
        } catch (Exception e) {
            return false;
        }
    }
}
