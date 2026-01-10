package com.example.sistemafinancas;

import com.example.sistemafinancas.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.io.FileInputStream;

@SpringBootApplication
public class SistemaFinancasApplication implements CommandLineRunner {

    @Autowired
    private TransacaoService transacaoService;

    // ESTE É O MÉTODO QUE ESTÁ FALTANDO NO SEU CÓDIGO:
    public static void main(String[] args) {
        SpringApplication.run(SistemaFinancasApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        File arquivo = new File("C:/Users/caiom/Downloads/EXTRATO_NOVEMBRO_NUBANK.ofx");
        if (arquivo.exists()) {
            transacaoService.importarOFX(new FileInputStream(arquivo));
            System.out.println("Processamento concluído!");
        } else {
            System.out.println("Arquivo não encontrado!");
        }
    }
}