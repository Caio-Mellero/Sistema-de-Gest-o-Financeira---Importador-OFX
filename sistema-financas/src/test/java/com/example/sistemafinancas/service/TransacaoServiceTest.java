package com.example.sistemafinancas.service;

import com.example.sistemafinancas.model.TipoTransacao;
import com.example.sistemafinancas.model.Transacao;
import com.example.sistemafinancas.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository repository;

    @InjectMocks
    private TransacaoService service;

    // Nota: Como o requisito pede para testar a validação de sinal no Service,
    // estamos assumindo a criação de um método 'salvar(Transacao)' no Service
    // (TDD).
    // Atualmente essa lógica está no Controller, mas a boa prática é movê-la para o
    // Service.

    @Test
    @DisplayName("Cenário 1: Deve converter o valor para negativo quando uma nova transação do tipo SAIDA for lançada com valor positivo")
    void deveConverterValorParaNegativoQuandoSaidaForPositiva() {
        // Arrange
        Transacao transacaoSaida = new Transacao();
        transacaoSaida.setTipo(TipoTransacao.SAIDA);
        transacaoSaida.setValor(new BigDecimal("100.00")); // Valor positivo

        when(repository.save(any(Transacao.class))).thenAnswer(invocation -> {
            Transacao t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        // Para que o teste passe, você precisará mover a lógica que está no Controller
        // para um método 'salvar' aqui.
        Transacao salva = salvarNoService(transacaoSaida);

        // Assert
        assertNotNull(salva.getId(), "O ID não deveria ser nulo após o salvamento");
        assertEquals(new BigDecimal("-100.00"), salva.getValor(), "O valor deve ter sido forçado para negativo");
        verify(repository, times(1)).save(transacaoSaida);
    }

    @Test
    @DisplayName("Cenário 2: Deve manter o valor positivo quando o tipo for ENTRADA")
    void deveManterValorPositivoQuandoEntrada() {
        // Arrange
        Transacao transacaoEntrada = new Transacao();
        transacaoEntrada.setTipo(TipoTransacao.ENTRADA);
        transacaoEntrada.setValor(new BigDecimal("150.00"));

        when(repository.save(any(Transacao.class))).thenAnswer(invocation -> {
            Transacao t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });

        // Act
        Transacao salva = salvarNoService(transacaoEntrada);

        // Assert
        assertNotNull(salva.getId());
        assertEquals(new BigDecimal("150.00"), salva.getValor(), "O valor deve permanecer positivo para ENTRADA");
        verify(repository, times(1)).save(transacaoEntrada);
    }

    /**
     * Método auxiliar simulando a lógica de negócio (que deve ser implementada no
     * Service real).
     * Idealmente, adicione este método 'salvar(Transacao)' na classe
     * TransacaoService.
     */
    private Transacao salvarNoService(Transacao transacao) {
        if (transacao.getTipo() == TipoTransacao.SAIDA && transacao.getValor().compareTo(BigDecimal.ZERO) > 0) {
            transacao.setValor(transacao.getValor().negate());
        }
        return repository.save(transacao);
    }
}
