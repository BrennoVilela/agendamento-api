package io.github.brennovilela.agendamento_api.agendamento;

import java.time.LocalDate;

public record ConsultaDisponibilidade(
        Long profissionalId,
        Long servicoId,
        LocalDate data
) {
}