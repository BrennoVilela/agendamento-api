package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import io.github.brennovilela.agendamento_api.seguranca.UsuarioLogado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public AgendamentoResponse agendar(@RequestBody @Valid AgendamentoRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado.de(jwt).verificarAcessoAoCliente(request.clienteId());
        return AgendamentoResponse.de(service.agendar(request.paraComando()));
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Agendamento agendamento = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));
        UsuarioLogado.de(jwt).verificarAcessoAoCliente(agendamento.getCliente().getId());
        return AgendamentoResponse.de(agendamento);
    }

    @PatchMapping("/{id}/cancelamento")
    public AgendamentoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return AgendamentoResponse.de(service.cancelar(id, UsuarioLogado.de(jwt)));
    }
}