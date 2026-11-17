package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;
import io.github.brennovilela.agendamento_api.profissional.HorarioTrabalho;
import io.github.brennovilela.agendamento_api.profissional.HorarioTrabalhoRepository;
import io.github.brennovilela.agendamento_api.profissional.Profissional;
import io.github.brennovilela.agendamento_api.profissional.ProfissionalRepository;
import io.github.brennovilela.agendamento_api.servico.Servico;
import io.github.brennovilela.agendamento_api.servico.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadeService {
    private static final Duration INTERVALO = Duration.ofMinutes(30);

    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final HorarioTrabalhoRepository horarioTrabalhoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<LocalDateTime> buscarDisponibilidade(ConsultaDisponibilidade consulta) {

        // PASSO 1: validar profissional e serviço
        Profissional profissional = profissionalRepository.findById(consulta.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado"));
        if (!profissional.isAtivo()) {
            throw new RegraNegocioException("Profissional inativo");
        }

        Servico servico = servicoRepository.findById(consulta.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
        if (!servico.isAtivo()) {
            throw new RegraNegocioException("Serviço inativo");
        }

        LocalDate data = consulta.data();
        Duration duracao = Duration.ofMinutes(servico.getDuracaoMinutos());
        LocalDateTime agora = LocalDateTime.now(clock);

        List<HorarioTrabalho> expediente = horarioTrabalhoRepository
                .findByProfissionalIdAndDiaSemana(consulta.profissionalId(), data.getDayOfWeek());

        List<Agendamento> agendamentos = agendamentoRepository
                .findByProfissionalIdAndStatusNotAndInicioGreaterThanEqualAndInicioLessThan(
                        consulta.profissionalId(),
                        StatusAgendamento.CANCELADO,
                        data.atStartOfDay(),
                        data.plusDays(1).atStartOfDay()
                );

        List<LocalDateTime> livres = new ArrayList<>();

        for (HorarioTrabalho faixa : expediente) {
            LocalDateTime candidato = data.atTime(faixa.getHoraInicio());
            LocalDateTime limite = data.atTime(faixa.getHoraFim());

            while (!candidato.plus(duracao).isAfter(limite)) {
                LocalDateTime inicioCandidato = candidato;
                LocalDateTime fimCandidato = candidato.plus(duracao);

                boolean noFuturo = candidato.isAfter(agora);
                boolean semConflito = agendamentos.stream()
                        .noneMatch(agendamento -> agendamento.conflitaCom(inicioCandidato, fimCandidato));

                if (noFuturo && semConflito) {
                    livres.add(candidato);
                }

                candidato = candidato.plus(INTERVALO);
            }
        }

        livres.sort(Comparator.naturalOrder());
        return livres;
    }
}