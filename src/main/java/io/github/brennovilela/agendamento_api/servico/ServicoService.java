package io.github.brennovilela.agendamento_api.servico;

import io.github.brennovilela.agendamento_api.compartilhado.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public Servico buscar(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
    }

    @Transactional
    public Servico desativar(Long id) {
        Servico servico = buscar(id);
        servico.desativar();
        return servico;
    }
}