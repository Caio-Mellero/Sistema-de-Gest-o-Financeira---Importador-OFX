package com.example.sistemafinancas.repository;

import com.example.sistemafinancas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca o usuário pelo e-mail para validar o login
    Optional<Usuario> findByEmail(String email);

    // Verifica se o e-mail já está cadastrado (usado no registro)
    boolean existsByEmail(String email);
}
