package io.github.brennovilela.agendamento_api.seguranca;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdminInicial(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${app.admin.email}") String email,
                        @Value("${app.admin.senha}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.existsByEmail(email)) {
            usuarioRepository.save(Usuario.admin(email, passwordEncoder.encode(senha)));
            log.info("Usuário ADMIN criado: {}", email);
        }
    }
}