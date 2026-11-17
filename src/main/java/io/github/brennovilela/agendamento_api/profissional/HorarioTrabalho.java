package io.github.brennovilela.agendamento_api.profissional;

import io.github.brennovilela.agendamento_api.compartilhado.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "horario_trabalho")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HorarioTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @Convert(converter = DiaSemanaConverter.class)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    public HorarioTrabalho(Profissional profissional, DayOfWeek diaSemana,
                           LocalTime horaInicio, LocalTime horaFim) {
        if (!horaFim.isAfter(horaInicio)) {
            throw new RegraNegocioException("Hora de fim deve ser depois da hora de início");
        }
        this.profissional = profissional;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public boolean cobre(LocalTime inicio, LocalTime fim) {
        return !inicio.isBefore(horaInicio) && !fim.isAfter(horaFim);
    }
}