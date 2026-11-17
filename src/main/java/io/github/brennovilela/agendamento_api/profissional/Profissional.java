package io.github.brennovilela.agendamento_api.profissional;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profissional")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false)
    private boolean ativo = true;

    public Profissional(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }
}