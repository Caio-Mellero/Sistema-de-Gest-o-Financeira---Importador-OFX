package com.example.sistemafinancas.model;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID único do banco (da tag <FITID> do arquivo OFX) para evitar duplicidade real
    private String fitId;

    private String descricao;

    // BigDecimal é OBRIGATÓRIO em sistemas financeiros para evitar erro de centavos
    private BigDecimal valor;

    private LocalDate data;

    private String categoria;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    // Cada transação pertence a um único usuário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}