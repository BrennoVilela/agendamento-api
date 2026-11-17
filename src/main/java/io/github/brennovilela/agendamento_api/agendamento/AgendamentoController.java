package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendamentoRepository repository;

    public record AgendamentoRequest(
            @NotNull Long clienteId,
            @NotNull Long profissionalId,
            @NotNull Long servicoId,
            @NotNull LocalDateTime inicio
    ) {
        NovoAgendamento paraComando() {
            return new NovoAgendamento(clienteId, profissionalId, servicoId, inicio);
        }
    }

    public record AgendamentoResponse(
            Long id,
            Long clienteId,
            Long profissionalId,
            Long servicoId,
            LocalDateTime inicio,
            LocalDateTime fim,
            StatusAgendamento status
    ) {
        static AgendamentoResponse de(Agendamento agendamento) {
            return new AgendamentoResponse(
                    agendamento.getId(),
                    agendamento.getCliente().getId(),
                    agendamento.getProfissional().getId(),
                    agendamento.getServico().getId(),
                    agendamento.getInicio(),
                    agendamento.getFim(),
                    agendamento.getStatus()
            );
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse agendar(@RequestBody @Valid AgendamentoRequest request) {
        return AgendamentoResponse.de(service.agendar(request.paraComando()));
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(AgendamentoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));
    }

    @PatchMapping("/{id}/cancelamento")
    public AgendamentoResponse cancelar(@PathVariable Long id) {
        return AgendamentoResponse.de(service.cancelar(id));
    }
}