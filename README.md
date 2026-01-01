Sistema de Gestão Financeira - Importador OFX
Este é um projeto Full Stack desenvolvido em Java com Spring Boot, focado na automação da gestão financeira pessoal através da importação de arquivos de extrato bancário (OFX).

Funcionalidades

Upload de Arquivos: Interface web para subir arquivos .ofx exportados diretamente do aplicativo do banco (ex: Nubank).

Processamento Inteligente: Leitura manual de tags OFX utilizando BufferedReader para extração de datas, valores e descrições.

Prevenção de Duplicidade: Lógica customizada que verifica no banco de dados se a transação já foi importada antes de salvá-la, evitando poluição de dados.

Dashboard Financeiro: Exibição de cards com o Saldo Total, Entradas e Saídas calculados em tempo real.

Classificação de Gastos: Sistema de categorização de transações com categorias pré-definidas (Alimentação, Lazer, etc.) via Modais dinâmicos.

Tecnologias Utilizadas

Backend: Java 21, Spring Boot 4.x, Spring Data JPA.

Banco de Dados: PostgreSQL.

Frontend: Thymeleaf, HTML5, Bootstrap 5.3 (CSS & JS).

Gestão de Dependências: Maven.
