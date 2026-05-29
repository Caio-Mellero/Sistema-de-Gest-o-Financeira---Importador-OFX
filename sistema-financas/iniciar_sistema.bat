@echo off
title Servidor - Sistema de Financas
echo Iniciando o servidor Java...
echo POR FAVOR, AGUARDE O CARREGAMENTO COMPLETO.
echo.

:: Inicia o Spring Boot usando o Maven (compila e roda o codigo mais recente)
start "LOGS-SPRING" cmd /c ".\mvnw.cmd spring-boot:run"

:: Delay de 8 segundos (tempo médio para o Spring Boot subir com H2)
echo Aguardando 8 segundos para o sistema subir...
timeout /t 8 /nobreak > nul

echo Abrindo o navegador...
start http://localhost:8080

echo Tudo pronto! Pode usar o sistema agora.
exit