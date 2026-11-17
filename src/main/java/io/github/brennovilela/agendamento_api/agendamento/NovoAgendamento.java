package io.github.brennovilela.agendamento_api.agendamento;

import java.time.LocalDateTime;

public record NovoAgendamento(
        Long clienteId,
        Long profissionalId,
        Long servicoId,
        LocalDateTime inicio
) {
}