package com.example.finsight.repository;

import com.example.finsight.model.Transacao;
import com.example.finsight.model.Usuario;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

       // Busca todas as transações de um usuário específico com ordenação
       List<Transacao> findByUsuario(Usuario usuario, Sort sort);

       // Busca todas as transações de um usuário (sem ordenação, para o dashboard)
       List<Transacao> findByUsuario(Usuario usuario);

       // Verifica duplicidade: mesmo fitId + mesmo usuário = mesma transação bancária
       boolean existsByFitIdAndUsuario(String fitId, Usuario usuario);

       // Soma total por mês e tipo para o histórico do dashboard
       @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
                     "WHERE t.usuario = :usuario AND t.tipo = :tipo " +
                     "AND FUNCTION('YEAR', t.data) = :ano AND FUNCTION('MONTH', t.data) = :mes")
       BigDecimal somarPorUsuarioTipoEMes(@Param("usuario") Usuario usuario,
                     @Param("tipo") com.example.finsight.model.TipoTransacao tipo,
                     @Param("ano") int ano,
                     @Param("mes") int mes);
}
