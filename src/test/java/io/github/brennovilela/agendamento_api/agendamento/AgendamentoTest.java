package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;
import io.github.brennovilela.agendamento_api.profissional.Profissional;
import io.github.brennovilela.agendamento_api.servico.Servico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendamentoTest {

    private final LocalDateTime inicio = LocalDateTime.of(2026, 11, 18, 14, 0);

    private Agendamento novoAgendamentoDeCorte() {
        return new Agendamento(
                new Cliente("Ana", "ana@email.com", null),
                new Profissional("João", "joao@email.com"),
                new Servico("Corte", 30, new BigDecimal("50.00")),
                inicio
        );
    }

    @Test
    void deveCalcularFimPelaDuracaoDoServico() {
        Agendamento agendamento = novoAgendamentoDeCorte();

        assertThat(agendamento.getFim()).isEqualTo(inicio.plusMinutes(30));
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
    }

    @Test
    void devePermitirCancelarComAntecedencia() {
        Agendamento agendamento = novoAgendamentoDeCorte();

        agendamento.cancelar(inicio.minusHours(3));

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void naoDevePermitirCancelarComMenosDeDuasHoras() {
        Agendamento agendamento = novoAgendamentoDeCorte();

        assertThatThrownBy(() -> agendamento.cancelar(inicio.minusMinutes(90)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("2 horas");
    }

    @Test
    void naoDevePermitirCancelarDuasVezes() {
        Agendamento agendamento = novoAgendamentoDeCorte();
        agendamento.cancelar(inicio.minusHours(3));

        assertThatThrownBy(() -> agendamento.cancelar(inicio.minusHours(3)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveDetectarConflitoDeHorarios() {
        Agendamento agendamento = novoAgendamentoDeCorte(); // 14:00 às 14:30

        assertThat(agendamento.conflitaCom(inicio.plusMinutes(15), inicio.plusMinutes(45))).isTrue();
        assertThat(agendamento.conflitaCom(inicio.minusMinutes(30), inicio)).isFalse();
        assertThat(agendamento.conflitaCom(inicio.plusMinutes(30), inicio.plusMinutes(60))).isFalse();
    }
}