package com.example.finsight.config;

import com.example.finsight.model.Perfil;
import com.example.finsight.model.Usuario;
import com.example.finsight.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test") // Não roda nos testes para não sujar o H2 de teste
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Verificando se o banco precisa ser inicializado...");

        // Comportamento idempotente: só cria se não houver NENHUM usuário
        if (usuarioRepository.count() == 0) {
            log.info("Banco vazio. Populando usuários padrão...");

            // Usuário ADMIN
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("adm@gmail.com");
            admin.setSenha(passwordEncoder.encode("adm123"));
            admin.setPerfil(Perfil.ADMIN);
            admin.setAtivo(true);
            usuarioRepository.save(admin);

            // Usuário USER
            Usuario user = new Usuario();
            user.setNome("User");
            user.setEmail("user@gmail.com");
            user.setSenha(passwordEncoder.encode("user123"));
            user.setPerfil(Perfil.USER);
            user.setAtivo(true);
            usuarioRepository.save(user);

            log.info("Usuários criados com sucesso: adm@gmail.com (ADMIN) e user@gmail.com (USER)");
        } else {
            log.info("Banco já inicializado (usuários encontrados). Nenhuma alteração feita.");
        }
    }
}
