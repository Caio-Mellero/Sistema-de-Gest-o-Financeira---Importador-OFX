package com.example.sistemafinancas.model;
import jakarta.persistence.*; // Biblioteca que cuida do banco de dados
import lombok.Data;           // Faz o trabalho sujo de criar Getters/Setters
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity // Diz ao JPA: "Crie uma tabela chamada 'transacao' no Postgres"
@Data   // Diz ao Lombok: "Crie os métodos get e set automaticamente"

public class Transacao {

    @Id // Define que este campo é a Chave Primária (PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // O banco gera o ID (1, 2, 3...)
    private Long id;

    private String descricao;

    // BigDecimal é OBRIGATÓRIO em sistemas financeiros para evitar erro de centavos
    private BigDecimal valor;

    private LocalDate data;

    private String categoria;

    @Enumerated(EnumType.STRING) // Salva no banco o texto "ENTRADA" ou "SAIDA"
    private TipoTransacao tipo;
}