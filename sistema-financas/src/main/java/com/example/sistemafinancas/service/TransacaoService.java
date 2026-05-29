package com.example.sistemafinancas.service;

import com.example.sistemafinancas.model.Transacao;
import com.example.sistemafinancas.model.TipoTransacao;
import com.example.sistemafinancas.model.Usuario;
import com.example.sistemafinancas.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;

    // Injeção por construtor (melhor prática em vez de @Autowired no campo)
    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    /**
     * Importa transações de um arquivo OFX, associando-as ao usuário logado.
     * Usa o campo FITID do OFX para evitar importações duplicadas de forma confiável.
     *
     * @param inputStream Stream do arquivo .ofx enviado pelo usuário
     * @param usuario     O usuário dono das transações importadas
     * @return Quantidade de novas transações salvas
     */
    public int importarOFX(InputStream inputStream, Usuario usuario) {
        int novosSalvos = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String linha;
            Transacao transacaoAtual = null;

            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();

                if (linha.startsWith("<STMTTRN>")) {
                    transacaoAtual = new Transacao();

                } else if (linha.startsWith("<FITID>") && transacaoAtual != null) {
                    // FITID é o ID único da transação gerado pelo banco — uso para deduplicação
                    String fitId = extrairValor(linha, "FITID");
                    transacaoAtual.setFitId(fitId);

                } else if (linha.startsWith("<TRNAMT>") && transacaoAtual != null) {
                    String valorLimpo = extrairValor(linha, "TRNAMT").replace(",", ".");
                    BigDecimal valorBD = new BigDecimal(valorLimpo);
                    transacaoAtual.setValor(valorBD);
                    transacaoAtual.setTipo(valorBD.compareTo(BigDecimal.ZERO) > 0
                            ? TipoTransacao.ENTRADA
                            : TipoTransacao.SAIDA);

                } else if (linha.startsWith("<MEMO>") && transacaoAtual != null) {
                    transacaoAtual.setDescricao(extrairValor(linha, "MEMO"));

                } else if (linha.startsWith("<DTPOSTED>") && transacaoAtual != null) {
                    // O formato do OFX é: 20231115120000[-3:BRT] — pegamos apenas os 8 primeiros dígitos
                    String dataStr = extrairValor(linha, "DTPOSTED");
                    if (dataStr.length() >= 8) {
                        dataStr = dataStr.substring(0, 8);
                    }
                    transacaoAtual.setData(
                            LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("yyyyMMdd")));

                } else if (linha.startsWith("</STMTTRN>") && transacaoAtual != null) {
                    transacaoAtual.setUsuario(usuario);

                    // Verifica duplicidade usando fitId + usuário (robusto e confiável)
                    boolean jaExiste = transacaoAtual.getFitId() != null
                            && repository.existsByFitIdAndUsuario(
                                    transacaoAtual.getFitId(), usuario);

                    if (!jaExiste) {
                        repository.save(transacaoAtual);
                        novosSalvos++;
                        System.out.println("Salvo: " + transacaoAtual.getDescricao());
                    } else {
                        System.out.println("Ignorado (já existe): " + transacaoAtual.getDescricao());
                    }
                    transacaoAtual = null;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar arquivo: " + e.getMessage());
            throw new RuntimeException("Erro ao processar o arquivo OFX", e);
        }

        return novosSalvos;
    }

    /**
     * Extrai o valor de uma tag OFX de forma flexível.
     * Suporta tanto <TAG>valor</TAG> quanto <TAG>valor (sem closing tag).
     */
    private String extrairValor(String linha, String tag) {
        String abertura = "<" + tag + ">";
        String fechamento = "</" + tag + ">";
        String valor = linha.replace(abertura, "").trim();
        int fechamentoIdx = valor.indexOf(fechamento);
        if (fechamentoIdx >= 0) {
            valor = valor.substring(0, fechamentoIdx);
        }
        return valor.trim();
    }
}