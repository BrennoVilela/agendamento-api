package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import io.github.brennovilela.agendamento_api.cliente.ClienteRepository;
import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;
import io.github.brennovilela.agendamento_api.profissional.HorarioTrabalhoRepository;
import io.github.brennovilela.agendamento_api.profissional.Profissional;
import io.github.brennovilela.agendamento_api.profissional.ProfissionalRepository;
import io.github.brennovilela.agendamento_api.servico.Servico;
import io.github.brennovilela.agendamento_api.servico.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final HorarioTrabalhoRepository horarioTrabalhoRepository;
    private final Clock clock;

    @Transactional
    public Agendamento agendar(NovoAgendamento dados) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (!dados.inicio().isAfter(agora)) {
            throw new RegraNegocioException("Não é possível agendar no passado");
        }

        Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

        Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado"));
        if (!profissional.isAtivo()) {
            throw new RegraNegocioException("Profissional inativo");
        }

        Servico servico = servicoRepository.findById(dados.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
        if (!servico.isAtivo()) {
            throw new RegraNegocioException("Serviço inativo");
        }

        Agendamento agendamento = new Agendamento(cliente, profissional, servico, dados.inicio());

        validarExpediente(dados.profissionalId(), agendamento);
        validarConflito(dados.profissionalId(), agendamento);

        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento cancelar(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));
        agendamento.cancelar(LocalDateTime.now(clock));
        return agendamento;
    }

    private void validarExpediente(Long profissionalId, Agendamento agendamento) {
        LocalDateTime inicio = agendamento.getInicio();
        LocalDateTime fim = agendamento.getFim();

        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            throw new RegraNegocioException("Agendamento fora do expediente do profissional");
        }

        boolean dentroDoExpediente = horarioTrabalhoRepository
                .findByProfissionalIdAndDiaSemana(profissionalId, inicio.getDayOfWeek())
                .stream()
                .anyMatch(horario -> horario.cobre(inicio.toLocalTime(), fim.toLocalTime()));

        if (!dentroDoExpediente) {
            throw new RegraNegocioException("Agendamento fora do expediente do profissional");
        }
    }

    private void validarConflito(Long profissionalId, Agendamento agendamento) {
        if (agendamentoRepository.existeConflito(profissionalId, agendamento.getInicio(), agendamento.getFim())) {
            throw new ConflitoDeHorarioException();
        }
    }
}