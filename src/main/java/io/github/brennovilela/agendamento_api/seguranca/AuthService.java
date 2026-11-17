package io.github.brennovilela.agendamento_api.seguranca;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import io.github.brennovilela.agendamento_api.cliente.ClienteRepository;
import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public String registrar(String nome, String email, String telefone, String senha) {
        if (usuarioRepository.existsByEmail(email) || clienteRepository.existsByEmail(email)) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }

        Cliente cliente = clienteRepository.save(new Cliente(nome, email, telefone));
        Usuario usuario = usuarioRepository.save(Usuario.cliente(cliente, passwordEncoder.encode(senha)));

        return tokenService.gerar(usuario);
    }

    @Transactional(readOnly = true)
    public String login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(senha, u.getSenha()))
                .orElseThrow(CredenciaisInvalidasException::new);

        return tokenService.gerar(usuario);
    }
}