package io.github.brennovilela.agendamento_api.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
            select count(a) > 0 from Agendamento a
            where a.profissional.id = :profissionalId
              and a.status <> io.github.brennovilela.agendamento_api.agendamento.StatusAgendamento.CANCELADO
              and a.inicio < :fim
              and a.fim > :inicio
            """)
    boolean existeConflito(Long profissionalId, LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> findByProfissionalIdAndStatusNotAndInicioGreaterThanEqualAndInicioLessThan(
            Long profissionalId,
            StatusAgendamento status,
            LocalDateTime inicioDoDia,
            LocalDateTime inicioDoDiaSeguinte
    );
}