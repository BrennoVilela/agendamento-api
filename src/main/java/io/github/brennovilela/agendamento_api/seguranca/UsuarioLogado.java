package io.github.brennovilela.agendamento_api.seguranca;

import org.springframework.security.oauth2.jwt.Jwt;

public record UsuarioLogado(Long usuarioId, Perfil perfil, Long clienteId) {

    public static UsuarioLogado de(Jwt jwt) {
        Number clienteId = jwt.getClaim("clienteId");
        return new UsuarioLogado(
                Long.valueOf(jwt.getSubject()),
                Perfil.valueOf(jwt.getClaimAsString("perfil")),
                clienteId == null ? null : clienteId.longValue()
        );
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public void verificarAcessoAoCliente(Long clienteId) {
        if (!isAdmin() && !clienteId.equals(this.clienteId)) {
            throw new AcessoNegadoException();
        }
    }
}