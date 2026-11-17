package io.github.brennovilela.agendamento_api.seguranca;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    public record RegistroRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @Size(max = 20) String telefone,
            @NotBlank @Size(min = 8, max = 72) String senha
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String senha
    ) {
    }

    public record TokenResponse(String token) {
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(@RequestBody @Valid RegistroRequest request) {
        String token = authService.registrar(request.nome(), request.email(), request.telefone(), request.senha());
        return new TokenResponse(token);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return new TokenResponse(authService.login(request.email(), request.senha()));
    }
}