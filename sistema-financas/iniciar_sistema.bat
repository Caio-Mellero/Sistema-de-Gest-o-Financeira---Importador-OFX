@echo off
title Servidor - Sistema de Financas
echo Iniciando o servidor Java...
echo POR FAVOR, AGUARDE O CARREGAMENTO COMPLETO.
echo.

:: Inicia o Java em uma nova janela para permitir que o script continue para o delay
start "LOGS-SPRING" java -jar target/sistema-financas-0.0.1-SNAPSHOT.jar

:: Delay de 8 segundos (tempo médio para o Spring Boot subir com H2)
echo Aguardando 8 segundos para o sistema subir...
timeout /t 8 /nobreak > nul

echo Abrindo o navegador...
start http://localhost:8080

echo Tudo pronto! Pode usar o sistema agora.
exit