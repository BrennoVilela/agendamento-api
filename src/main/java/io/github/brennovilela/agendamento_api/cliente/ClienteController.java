package io.github.brennovilela.agendamento_api.cliente;

import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository repository;

    public record ClienteRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @Size(max = 20) String telefone
    ) {
    }

    public record ClienteResponse(Long id, String nome, String email, String telefone) {
        static ClienteResponse de(Cliente cliente) {
            return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@RequestBody @Valid ClienteRequest request) {
        Cliente cliente = new Cliente(request.nome(), request.email(), request.telefone());
        return ClienteResponse.de(repository.save(cliente));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ClienteResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }
}