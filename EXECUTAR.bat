@echo off
title Campo Minado: Solo Leveling
java -jar "%~dp0CampoMinadoSoloLeveling.jar"
if errorlevel 1 (
  echo.
  echo Nao foi possivel iniciar o jogo.
  echo Verifique se o Java esta instalado: abra o Prompt e digite  java -version
  echo Se nao estiver, baixe em: https://adoptium.net
  echo.
  pause
)
