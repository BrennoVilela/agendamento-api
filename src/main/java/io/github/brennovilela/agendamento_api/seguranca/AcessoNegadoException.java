package io.github.brennovilela.agendamento_api.seguranca;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Você não tem permissão para acessar este recurso");
    }
}