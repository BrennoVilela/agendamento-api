package io.github.brennovilela.agendamento_api.servico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoRepository repository;
    private final ServicoService service;

    public record ServicoRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotNull @Positive Integer duracaoMinutos,
            @NotNull @PositiveOrZero BigDecimal preco
    ) {
    }

    public record ServicoResponse(Long id, String nome, Integer duracaoMinutos, BigDecimal preco, boolean ativo) {
        static ServicoResponse de(Servico servico) {
            return new ServicoResponse(servico.getId(), servico.getNome(), servico.getDuracaoMinutos(),
                    servico.getPreco(), servico.isAtivo());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse criar(@RequestBody @Valid ServicoRequest request) {
        Servico servico = new Servico(request.nome(), request.duracaoMinutos(), request.preco());
        return ServicoResponse.de(repository.save(servico));
    }

    @GetMapping
    public List<ServicoResponse> listar() {
        return repository.findAll().stream().map(ServicoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ServicoResponse buscar(@PathVariable Long id) {
        return ServicoResponse.de(service.buscar(id));
    }

    @PatchMapping("/{id}/desativacao")
    public ServicoResponse desativar(@PathVariable Long id) {
        return ServicoResponse.de(service.desativar(id));
    }
}