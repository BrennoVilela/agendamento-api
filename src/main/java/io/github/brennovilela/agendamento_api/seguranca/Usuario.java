package io.github.brennovilela.agendamento_api.seguranca;

import io.github.brennovilela.agendamento_api.cliente.Cliente;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private Usuario(String email, String senhaCriptografada, Perfil perfil, Cliente cliente) {
        this.email = email;
        this.senha = senhaCriptografada;
        this.perfil = perfil;
        this.cliente = cliente;
    }

    public static Usuario admin(String email, String senhaCriptografada) {
        return new Usuario(email, senhaCriptografada, Perfil.ADMIN, null);
    }

    public static Usuario cliente(Cliente cliente, String senhaCriptografada) {
        return new Usuario(cliente.getEmail(), senhaCriptografada, Perfil.CLIENTE, cliente);
    }
}