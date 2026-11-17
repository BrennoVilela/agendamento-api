package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;

public class ConflitoDeHorarioException extends RegraNegocioException {
    public ConflitoDeHorarioException() {
        super("Profissional já possui agendamento nesse horário");
    }
}