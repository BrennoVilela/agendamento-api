package io.github.brennovilela.agendamento_api;

import org.springframework.boot.SpringApplication;

public class TestAgendamentoApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AgendamentoApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
