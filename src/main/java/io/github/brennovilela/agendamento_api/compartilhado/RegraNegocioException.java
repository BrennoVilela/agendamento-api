package io.github.brennovilela.agendamento_api.compartilhado;

public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}