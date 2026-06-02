package com.example.finsight.repository;

import com.example.finsight.model.TipoTransacao;
import com.example.finsight.model.Transacao;
import com.example.finsight.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TransacaoRepositoryTest {

    @Autowired
    private TransacaoRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Cenário 1: Deve salvar uma transação com sucesso no banco H2")
    void deveSalvarTransacaoComSucesso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Caio Teste");
        usuario.setEmail("caio@teste.com");
        usuario.setSenha("123456");
        entityManager.persist(usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Compra no mercado");
        transacao.setValor(new BigDecimal("-150.50"));
        transacao.setTipo(TipoTransacao.SAIDA);
        transacao.setData(LocalDate.now());
        transacao.setUsuario(usuario);

        // Act
        Transacao salva = repository.save(transacao);

        // Assert
        assertNotNull(salva.getId(), "O ID não deveria ser nulo após persistência");
        assertEquals("Compra no mercado", salva.getDescricao());
        assertEquals(TipoTransacao.SAIDA, salva.getTipo());
    }

    @Test
    @DisplayName("Cenário 2: Deve deletar uma transação pelo ID corretamente")
    void deveDeletarTransacaoPeloId() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Caio Delete");
        usuario.setEmail("caiodel@teste.com");
        usuario.setSenha("123");
        entityManager.persist(usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Gasto indevido");
        transacao.setValor(new BigDecimal("-50.00"));
        transacao.setTipo(TipoTransacao.SAIDA);
        transacao.setData(LocalDate.now());
        transacao.setUsuario(usuario);

        Transacao transacaoSalva = entityManager.persistAndFlush(transacao);
        Long idParaDeletar = transacaoSalva.getId();

        // Verifica se realmente está lá antes de deletar
        assertTrue(repository.findById(idParaDeletar).isPresent());

        // Act
        repository.deleteById(idParaDeletar);

        // Assert
        Optional<Transacao> buscaPosDelete = repository.findById(idParaDeletar);
        assertFalse(buscaPosDelete.isPresent(), "A transação deveria ter sido removida do banco");
    }
}
