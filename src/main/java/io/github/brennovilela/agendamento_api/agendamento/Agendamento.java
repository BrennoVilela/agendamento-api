package io.github.brennovilela.agendamento_api.agendamento;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import io.github.brennovilela.agendamento_api.profissional.Profissional;
import io.github.brennovilela.agendamento_api.servico.Servico;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agendamento {

    private static final Duration ANTECEDENCIA_MINIMA_CANCELAMENTO = Duration.ofHours(2);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Agendamento(Cliente cliente, Profissional profissional, Servico servico, LocalDateTime inicio) {
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.inicio = inicio;
        this.fim = inicio.plusMinutes(servico.getDuracaoMinutos());
        this.status = StatusAgendamento.AGENDADO;
    }

    public void cancelar(LocalDateTime agora) {
        if (status == StatusAgendamento.CANCELADO || status == StatusAgendamento.CONCLUIDO) {
            throw new IllegalStateException("Agendamento não pode ser cancelado no status " + status);
        }
        if (agora.plus(ANTECEDENCIA_MINIMA_CANCELAMENTO).isAfter(inicio)) {
            throw new IllegalStateException("Cancelamento exige pelo menos 2 horas de antecedência");
        }
        this.status = StatusAgendamento.CANCELADO;
    }

    public boolean conflitaCom(LocalDateTime outroInicio, LocalDateTime outroFim) {
        return inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
    }
}