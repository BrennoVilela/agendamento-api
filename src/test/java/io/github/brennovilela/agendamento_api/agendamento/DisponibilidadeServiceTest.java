package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC);

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 17);
    private static final LocalDate AMANHA = LocalDate.of(2026, 11, 18);

    @Mock ProfissionalRepository profissionalRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock HorarioTrabalhoRepository horarioTrabalhoRepository;
    @Mock AgendamentoRepository agendamentoRepository;

    private DisponibilidadeService service;

    private final Cliente cliente = new Cliente("Ana", "ana@email.com", null);
    private final Profissional profissional = new Profissional("João", "joao@email.com");
    private final Servico corte = new Servico("Corte", 30, new BigDecimal("50.00"));
    private final Servico corteEBarba = new Servico("Corte e barba", 60, new BigDecimal("80.00"));

    @BeforeEach
    void setUp() {
        service = new DisponibilidadeService(profissionalRepository, servicoRepository,
                horarioTrabalhoRepository, agendamentoRepository, clock);
    }


    private void dadoProfissionalEServico(Servico servico) {
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findById(2L)).thenReturn(Optional.of(servico));
    }

    private void dadoExpediente(LocalDate data, LocalTime inicio, LocalTime fim) {
        when(horarioTrabalhoRepository.findByProfissionalIdAndDiaSemana(1L, data.getDayOfWeek()))
                .thenReturn(List.of(new HorarioTrabalho(profissional, data.getDayOfWeek(), inicio, fim)));
    }

    private void dadoAgendamentos(LocalDate data, Agendamento... agendamentos) {
        when(agendamentoRepository.findByProfissionalIdAndStatusNotAndInicioGreaterThanEqualAndInicioLessThan(
                1L, StatusAgendamento.CANCELADO, data.atStartOfDay(), data.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(agendamentos));
    }

    @Test
    void servicoDe60MinutosNaoDeveInvadirAgendamentoExistente() {
        dadoProfissionalEServico(corteEBarba);
        dadoExpediente(AMANHA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        dadoAgendamentos(AMANHA, new Agendamento(cliente, profissional, corte, AMANHA.atTime(10, 0)));

        List<LocalDateTime> livres = service.buscarDisponibilidade(new ConsultaDisponibilidade(1L, 2L, AMANHA));

        assertThat(livres).containsExactly(
                AMANHA.atTime(9, 0),
                AMANHA.atTime(10, 30),
                AMANHA.atTime(11, 0)
        );
    }

    @Test
    void deveRetornarVazioQuandoNaoHaExpediente() {
        dadoProfissionalEServico(corte);

        List<LocalDateTime> livres = service.buscarDisponibilidade(new ConsultaDisponibilidade(1L, 2L, AMANHA));

        assertThat(livres).isEmpty();
    }

    @Test
    void deveListarTodosOsHorariosQuandoAgendaEstaLivre() {
        dadoProfissionalEServico(corte);
        dadoExpediente(AMANHA, LocalTime.of(9, 0), LocalTime.of(12, 0));

        List<LocalDateTime> livres = service.buscarDisponibilidade(new ConsultaDisponibilidade(1L, 2L, AMANHA));

        assertThat(livres).containsExactly(
                AMANHA.atTime(9, 0),
                AMANHA.atTime(9, 30),
                AMANHA.atTime(10, 0),
                AMANHA.atTime(10, 30),
                AMANHA.atTime(11, 0),
                AMANHA.atTime(11, 30)
        );
    }

    @Test
    void naoDeveRetornarHorariosQueJaPassaram() {
        dadoProfissionalEServico(corte);
        dadoExpediente(HOJE, LocalTime.of(9, 0), LocalTime.of(12, 0));

        List<LocalDateTime> livres = service.buscarDisponibilidade(new ConsultaDisponibilidade(1L, 2L, HOJE));

        assertThat(livres).containsExactly(
                HOJE.atTime(10, 30),
                HOJE.atTime(11, 0),
                HOJE.atTime(11, 30)
        );
    }

    @Test
    void deveFalharQuandoProfissionalNaoExiste() {
        when(profissionalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarDisponibilidade(new ConsultaDisponibilidade(1L, 2L, AMANHA)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Profissional");
    }
}