package io.github.brennovilela.agendamento_api.servico;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "servico")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo = true;

    public Servico(String nome, Integer duracaoMinutos, BigDecimal preco) {
        this.nome = nome;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
    }

    public void desativar() {
        this.ativo = false;
    }
}