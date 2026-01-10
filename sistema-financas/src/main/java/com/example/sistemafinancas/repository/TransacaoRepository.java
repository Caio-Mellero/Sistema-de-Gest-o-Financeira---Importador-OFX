package com.example.sistemafinancas.repository;

import com.example.sistemafinancas.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
    public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
        // Busca uma transação que tenha a mesma data, valor e descrição ao mesmo tempo
        @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.tipo = :tipo")
        BigDecimal somarPorTipo(@Param("tipo") String tipo);
        boolean existsByDataAndValorAndDescricao(LocalDate data, BigDecimal valor, String descricao);
    }
