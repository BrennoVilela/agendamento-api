package io.github.brennovilela.agendamento_api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SegurancaIntegrationTest {

    @Autowired
    MockMvc mvc;


    private String registrarCliente() throws Exception {
        String email = "cliente-" + UUID.randomUUID() + "@email.com";
        String resposta = mvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Cliente Teste", "email": "%s", "senha": "senha12345"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private String loginAdmin() throws Exception {
        String resposta = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@agendamento.com", "senha": "admin12345"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private Long criar(String token, String url, String json) throws Exception {
        String resposta = mvc.perform(post(url)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private static Long clienteIdDoToken(String token) {
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(payload, "$.clienteId")).longValue();
    }

    @Test
    void catalogoEhPublico() throws Exception {
        mvc.perform(get("/servicos")).andExpect(status().isOk());
    }

    @Test
    void agendamentoExigeLogin() throws Exception {
        mvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoEhRecusado() throws Exception {
        mvc.perform(get("/agendamentos/1").header("Authorization", bearer("token-falso")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@agendamento.com", "senha": "senha-errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    void clienteNaoPodeCadastrarServico() throws Exception {
        mvc.perform(post("/servicos")
                        .header("Authorization", bearer(registrarCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Corte", "duracaoMinutos": 30, "preco": 50.00}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPodeCadastrarServico() throws Exception {
        mvc.perform(post("/servicos")
                        .header("Authorization", bearer(loginAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Corte", "duracaoMinutos": 30, "preco": 50.00}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void clienteNaoPodeAgendarEmNomeDeOutroCliente() throws Exception {
        mvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(registrarCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 999999, "profissionalId": 1, "servicoId": 1, "inicio": "2030-01-02T10:00:00"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoPodeVerListaDeClientes() throws Exception {
        mvc.perform(get("/clientes").header("Authorization", bearer(registrarCliente())))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelarAgendamentoInexistenteRetorna404ParaUsuarioLogado() throws Exception {
        mvc.perform(patch("/agendamentos/999999/cancelamento").header("Authorization", bearer(registrarCliente())))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteNaoPodeCancelarAgendamentoDeOutroCliente() throws Exception {
        // ADMIN monta o catálogo
        String admin = loginAdmin();
        Long servicoId = criar(admin, "/servicos", """
                {"nome": "Corte", "duracaoMinutos": 30, "preco": 50.00}
                """);
        Long profissionalId = criar(admin, "/profissionais", """
                {"nome": "João", "email": "prof-%s@barbearia.com"}
                """.formatted(UUID.randomUUID()));
        criar(admin, "/profissionais/" + profissionalId + "/horarios", """
                {"diaSemana": "WEDNESDAY", "horaInicio": "09:00", "horaFim": "18:00"}
                """);

        String clienteB = registrarCliente();
        Long agendamentoId = criar(clienteB, "/agendamentos", """
                {"clienteId": %d, "profissionalId": %d, "servicoId": %d, "inicio": "2030-01-02T10:00:00"}
                """.formatted(clienteIdDoToken(clienteB), profissionalId, servicoId));

        String clienteA = registrarCliente();
        mvc.perform(patch("/agendamentos/" + agendamentoId + "/cancelamento").header("Authorization", bearer(clienteA)))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/agendamentos/" + agendamentoId + "/cancelamento").header("Authorization", bearer(clienteB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}