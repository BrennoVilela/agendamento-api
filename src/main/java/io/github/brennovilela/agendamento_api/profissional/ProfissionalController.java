package io.github.brennovilela.agendamento_api.profissional;

import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import io.github.brennovilela.agendamento_api.servico.ServicoController;
import io.github.brennovilela.agendamento_api.servico.ServicoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final HorarioTrabalhoRepository horarioTrabalhoRepository;

    public record ProfissionalRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email
    ) {
    }

    public record ProfissionalResponse(Long id, String nome, String email, boolean ativo) {
        static ProfissionalResponse de(Profissional profissional) {
            return new ProfissionalResponse(profissional.getId(), profissional.getNome(),
                    profissional.getEmail(), profissional.isAtivo());
        }
    }

    public record HorarioRequest(
            @NotNull DayOfWeek diaSemana,
            @NotNull LocalTime horaInicio,
            @NotNull LocalTime horaFim
    ) {
    }

    public record HorarioResponse(Long id, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        static HorarioResponse de(HorarioTrabalho horario) {
            return new HorarioResponse(horario.getId(), horario.getDiaSemana(),
                    horario.getHoraInicio(), horario.getHoraFim());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalResponse criar(@RequestBody @Valid ProfissionalRequest request) {
        Profissional profissional = new Profissional(request.nome(), request.email());
        return ProfissionalResponse.de(profissionalRepository.save(profissional));
    }

    @GetMapping
    public List<ProfissionalResponse> listar() {
        return profissionalRepository.findAll().stream().map(ProfissionalResponse::de).toList();
    }

    @PostMapping("/{id}/horarios")
    @ResponseStatus(HttpStatus.CREATED)
    public HorarioResponse adicionarHorario(@PathVariable Long id, @RequestBody @Valid HorarioRequest request) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado"));
        HorarioTrabalho horario = new HorarioTrabalho(profissional, request.diaSemana(),
                request.horaInicio(), request.horaFim());
        return HorarioResponse.de(horarioTrabalhoRepository.save(horario));
    }

    @GetMapping("/{id}/horarios")
    public List<HorarioResponse> listarHorarios(@PathVariable Long id) {
        return horarioTrabalhoRepository.findByProfissionalId(id).stream().map(HorarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscarProfissional(@PathVariable Long id) {
        return profissionalRepository.findById(id)
                .map(ProfissionalResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado"));
    }
}