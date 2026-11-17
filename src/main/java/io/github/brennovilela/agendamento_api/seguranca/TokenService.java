package io.github.brennovilela.agendamento_api.seguranca;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final Duration VALIDADE = Duration.ofHours(2);

    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public String gerar(Usuario usuario) {
        Instant agora = clock.instant();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("agendamento-api")
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE))
                .claim("email", usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name());

        if (usuario.getCliente() != null) {
            claims.claim("clienteId", usuario.getCliente().getId());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();
    }
}