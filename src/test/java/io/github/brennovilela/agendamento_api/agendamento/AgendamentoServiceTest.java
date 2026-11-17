package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import io.github.brennovilela.agendamento_api.cliente.ClienteRepository;
import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;
import io.github.brennovilela.agendamento_api.profissional.HorarioTrabalho;
import io.github.brennovilela.agendamento_api.profissional.HorarioTrabalhoRepository;
import io.github.brennovilela.agendamento_api.profissional.Profissional;
import io.github.brennovilela.agendamento_api.profissional.ProfissionalRepository;
import io.github.brennovilela.agendamento_api.servico.Servico;
import io.github.brennovilela.agendamento_api.servico.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    // "Agora" congelado em 17/11/2026 às 10:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC);

    private static final LocalDateTime AMANHA_14H = LocalDateTime.of(2026, 11, 18, 14, 0);

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock ProfissionalRepository profissionalRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock HorarioTrabalhoRepository horarioTrabalhoRepository;

    private AgendamentoService service;

    private final Cliente cliente = new Cliente("Ana", "ana@email.com", null);
    private final Profissional profissional = new Profissional("João", "joao@email.com");
    private final Servico corte = new Servico("Corte", 30, new BigDecimal("50.00"));

    @BeforeEach
    void setUp() {
        service = new AgendamentoService(agendamentoRepository, clienteRepository,
                profissionalRepository, servicoRepository, horarioTrabalhoRepository, clock);
    }

    private void dadoQueClienteProfissionalEServicoExistem() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(2L)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(corte));
    }

    private void dadoQueProfissionalTrabalhaDas9As18() {
        DayOfWeek dia = AMANHA_14H.getDayOfWeek();
        when(horarioTrabalhoRepository.findByProfissionalIdAndDiaSemana(2L, dia))
                .thenReturn(List.of(new HorarioTrabalho(profissional, dia, LocalTime.of(9, 0), LocalTime.of(18, 0))));
    }

    @Test
    void deveAgendarQuandoHorarioEstaLivreEDentroDoExpediente() {
        dadoQueClienteProfissionalEServicoExistem();
        dadoQueProfissionalTrabalhaDas9As18();
        when(agendamentoRepository.existeConflito(2L, AMANHA_14H, AMANHA_14H.plusMinutes(30))).thenReturn(false);
        when(agendamentoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        Agendamento agendamento = service.agendar(new NovoAgendamento(1L, 2L, 3L, AMANHA_14H));

        assertThat(agendamento.getFim()).isEqualTo(AMANHA_14H.plusMinutes(30));
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void naoDeveAgendarNoPassado() {
        LocalDateTime ontem = LocalDateTime.of(2026, 11, 16, 14, 0);

        assertThatThrownBy(() -> service.agendar(new NovoAgendamento(1L, 2L, 3L, ontem)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("passado");

        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    void naoDeveAgendarForaDoExpediente() {
        dadoQueClienteProfissionalEServicoExistem();
        dadoQueProfissionalTrabalhaDas9As18();
        LocalDateTime as17h45 = LocalDateTime.of(2026, 11, 18, 17, 45); // terminaria 18:15

        assertThatThrownBy(() -> service.agendar(new NovoAgendamento(1L, 2L, 3L, as17h45)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expediente");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void naoDeveAgendarQuandoHaConflito() {
        dadoQueClienteProfissionalEServicoExistem();
        dadoQueProfissionalTrabalhaDas9As18();
        when(agendamentoRepository.existeConflito(2L, AMANHA_14H, AMANHA_14H.plusMinutes(30))).thenReturn(true);

        assertThatThrownBy(() -> service.agendar(new NovoAgendamento(1L, 2L, 3L, AMANHA_14H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já possui agendamento");

        verify(agendamentoRepository, never()).save(any());
    }
}