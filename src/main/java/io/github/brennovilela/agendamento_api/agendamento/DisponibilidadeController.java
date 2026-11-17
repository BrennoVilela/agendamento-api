package io.github.brennovilela.agendamento_api.agendamento;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DisponibilidadeController {

    private final DisponibilidadeService service;

    @GetMapping("/profissionais/{id}/disponibilidade")
    public List<LocalDateTime> buscar(
            @PathVariable Long id,
            @RequestParam Long servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return service.buscarDisponibilidade(new ConsultaDisponibilidade(id, servicoId, data));
    }
}