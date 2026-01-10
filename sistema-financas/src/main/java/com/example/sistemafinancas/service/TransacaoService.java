package com.example.sistemafinancas.service;

import com.example.sistemafinancas.model.Transacao;
import com.example.sistemafinancas.model.TipoTransacao;
import com.example.sistemafinancas.repository.TransacaoRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository repository;

    // Alterado para 'int' para retornar a quantidade de novos registros
    public int importarOFX(InputStream inputStream) {
        int novosSalvos = 0; // Contador de novos registros

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String linha;
            Transacao transacaoAtual = null;

            BigDecimal totalEntradas = BigDecimal.ZERO;
            BigDecimal totalSaidas = BigDecimal.ZERO;

            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();

                if (linha.startsWith("<STMTTRN>")) {
                    transacaoAtual = new Transacao();
                } else if (linha.startsWith("<TRNAMT>") && transacaoAtual != null) {
                    String valorLimpo = linha.replace("<TRNAMT>", "").replace("</TRNAMT>", "").trim();
                    BigDecimal valorBD = new BigDecimal(valorLimpo);
                    transacaoAtual.setValor(valorBD);

                    // Lógica do Totalizador
                    if (valorBD.compareTo(BigDecimal.ZERO) > 0) {
                        totalEntradas = totalEntradas.add(valorBD);
                    } else {
                        totalSaidas = totalSaidas.add(valorBD);
                    }

                    transacaoAtual.setTipo(valorBD.compareTo(BigDecimal.ZERO) > 0 ? TipoTransacao.ENTRADA : TipoTransacao.SAIDA);

                } else if (linha.startsWith("<MEMO>") && transacaoAtual != null) {
                    transacaoAtual.setDescricao(linha.replace("<MEMO>", "").replace("</MEMO>", "").trim());

                } else if (linha.startsWith("<DTPOSTED>") && transacaoAtual != null) {
                    String dataStr = linha.replace("<DTPOSTED>", "").substring(0, 8);
                    transacaoAtual.setData(LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("yyyyMMdd")));

                } else if (linha.startsWith("</STMTTRN>") && transacaoAtual != null) {
                    boolean jaExiste = repository.existsByDataAndValorAndDescricao(
                            transacaoAtual.getData(),
                            transacaoAtual.getValor(),
                            transacaoAtual.getDescricao()
                    );

                    if (!jaExiste) {
                        repository.save(transacaoAtual);
                        novosSalvos++; // Incrementa apenas se salvar no banco
                        System.out.println("Salvo: " + transacaoAtual.getDescricao());
                    } else {
                        System.out.println("Ignorado (já existe): " + transacaoAtual.getDescricao());
                    }

                    transacaoAtual = null;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar arquivo: " + e.getMessage());
            throw new RuntimeException("Erro ao processar o arquivo OFX", e); // Repassa o erro para o Controller tratar
        }

        return novosSalvos; // Retorna o total para o Controller
    }
}