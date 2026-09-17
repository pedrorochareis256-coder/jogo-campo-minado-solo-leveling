# Campo Minado: Solo Leveling

Versão em Java do Campo Minado com interface gráfica Swing e arquitetura MVC, com reskin temático inspirado na estética de *Solo Leveling* (Monarca das Sombras): paleta roxo/azul escuro com dourado, dificuldades como Ranks de Caçador (E / C / S), e a "voz do Sistema" narrando o status da missão.

> **Sobre o tema:** o jogo usa apenas a *estética* e os termos genéricos do gênero (rank, portal, caçador, monarca, sistema, sombras). Nenhuma arte, logotipo, personagem ou texto original da obra é reproduzido — todos os visuais são gerados por código (cores, bordas, emojis Unicode padrão, efeitos Java2D).

## Estrutura do projeto

- `src/main` — classes principais de execução.
  - `JogoCampoMinadoGUI.java` — entrada do jogo em modo gráfico.
  - `JogoCampoMinado.java` — alternativa de execução em console (aceita o comando `s` para o Sistema deduzir uma jogada).
- `src/controller` — controlador MVC.
  - `CampoMinadoController.java` — lógica de jogo, modos, som, histórico, conquistas, perfis, replay e orquestração do solver.
  - `AcoesJogador.java` — interface de ações disparadas pela View.
- `src/view` — camada de interface gráfica.
  - `CampoMinadoView.java` — telas de menu, jogo, dois jogadores e replay; temas, skins e efeitos visuais.
- `src/model` — modelo de domínio do jogo.
  - `Tabuleiro.java` — lógica do tabuleiro, minas, revelação, vitória, vidas, toroidal e semente fixa.
  - `Celula.java` — estado de cada célula (incluindo o ciclo bandeira → interrogação).
  - `LeituraTabuleiro.java` — interface somente-leitura do estado do tabuleiro.
- `src/util` — serviços de apoio (não são regra de domínio).
  - `TemaVisual.java`, `SkinBandeira.java`, `SkinNumeros.java`, `DetectorTemaSistema.java` — aparência.
  - `GerenciadorSom.java` — efeitos e música sintetizados em memória.
  - `ConfiguracoesJogo.java`, `GerenciadorPerfis.java` — persistência de preferências e perfis.
  - `HistoricoPartidas.java`, `RegistroPartida.java`, `Conquista.java`, `ConquistasManager.java` — histórico, ranking, CSV e conquistas.
  - `SolverAutomatico.java` — **"O Sistema"**, o solver automático.
  - `ConfigPartida.java`, `ReplayGravador.java` — configuração de partida e gravação de replay.
- `src/test` — testes unitários.
  - `CampoMinadoTest.java` — 30+ testes JUnit 5 cobrindo regras clássicas, cada variante de regra e a correção do solver.

## Funcionalidades implementadas

### Aparência e acessibilidade
- Sistema de temas visuais plugável, com 6 temas prontos (Monarca das Sombras, Terminal do Sistema, Portal Cyberpunk, Portão da Noite das Bruxas, Abismo Azul, Visão do Monarca/alto contraste).
- Detecção automática do modo claro/escuro do sistema operacional.
- Skins de bandeira (Adaga das Sombras, estrela, coração, alfinete, bandeira clássica).
- Skins dos números: clássico, Ranks de Caçador (E→SSS) e Bestas de Sombra.
- Símbolos geométricos extras para apoio a jogadores daltônicos (forma além da cor).
- Zoom das células ajustável e modo tela cheia (F11).
- Navegação completa por teclado: setas movem o cursor, Espaço revela, F marca.
- Realce de hover, animação de cascata, piscar de explosão e confete na vitória.

### Regras e modos de missão
- Marcação em três estados: nenhuma → bandeira → interrogação.
- Modo sem cascata (revela só a célula clicada).
- Modo treino com minas visíveis.
- Modo Relâmpago (o Sistema abre algumas células seguras antes da missão começar).
- Modo 3 Vidas (tolera até três minas antes da derrota).
- Tabuleiro toroidal (as bordas se conectam).
- Cooperativo local (dois jogadores revezam turnos no mesmo tabuleiro).
- Dois jogadores competindo em tabuleiros idênticos, lado a lado.
- Dificuldade customizada (linhas, colunas e minas à escolha).
- Portal Diário: tabuleiro gerado por semente do dia, igual para todo mundo.
- Tempo limite selecionável (até 5 minutos).
- Aviso quando o número de bandeiras ultrapassa o de minas.

### Progressão e dados
- Perfis de jogador, cada um com histórico, recordes e conquistas próprios.
- Histórico de partidas persistido em CSV na pasta do usuário.
- Recorde de melhor tempo por dificuldade.
- Ranking dos tempos mais rápidos (ordenação por inserção implementada manualmente).
- Exportação do histórico para um arquivo CSV à escolha.
- 7 conquistas desbloqueáveis (Primeiro Portal, Instinto de Caçador, Passo Relâmpago, Regeneração de Sombra, Caçador Rank S, Monarca em Ascensão, Portal Diário Fechado).
- Texto de compartilhamento do resultado da partida.
- Modo replay: reproduz a última partida jogada, jogada a jogada.
- Botão de dica (3 por partida) que revela uma célula seguramente livre de minas.

### Áudio
- Efeitos sonoros para clique, bandeira, dica, explosão e vitória — todos sintetizados em memória, sem arquivos externos.
- Música ambiente em loop com controle de volume.

### "O Sistema" — solver automático
O item de dificuldade desafiadora escolhido. O Sistema enxerga **apenas o que um jogador humano enxergaria** (células reveladas, seus números e as bandeiras já colocadas) — nunca consulta onde as minas realmente estão. Ele aplica duas regras de dedução local:

1. Se as bandeiras ao redor de uma célula numerada já somam o número dela, todo o resto oculto ao redor é seguro.
2. Se bandeiras + células ocultas ao redor batem exatamente com o número, todas as ocultas restantes são minas.

Quando nenhuma dedução é possível, ele admite o impasse e arrisca um palpite — a mesma situação que um humano enfrentaria. Dois modos na interface: **1 passo** (aplica uma rodada de dedução) e **resolver tudo** (roda em loop animado até terminar ou empacar).

## Compilação

A partir da pasta do projeto:

```powershell
javac -d out src\main\*.java src\controller\*.java src\view\*.java src\model\*.java src\util\*.java
```

No Linux/macOS:

```bash
javac -d out src/main/*.java src/controller/*.java src/view/*.java src/model/*.java src/util/*.java
```

## Execução

Interface gráfica:

```powershell
java -cp out main.JogoCampoMinadoGUI
```

Versão em console:

```powershell
java -cp out main.JogoCampoMinado
```

## Testes

Os testes usam JUnit 5 (Jupiter). Com o JUnit configurado na sua IDE, execute `src/test/CampoMinadoTest.java` normalmente. Pela linha de comando, com o `junit-platform-console-standalone.jar`:

```bash
javac -cp out:junit-platform-console-standalone.jar -d out src/test/*.java
java -jar junit-platform-console-standalone.jar --class-path out --select-class test.CampoMinadoTest
```

## Observações

- A interface gráfica usa Swing e respeita cores personalizadas graças ao LookAndFeel cross-platform (Metal). Usar o LookAndFeel do sistema faria o SO repintar os botões e ignorar as cores do tema.
- Preferências, perfis e conquistas são salvos via `java.util.prefs`; o histórico de partidas fica em `~/.campo_minado_solo_leveling/`.
- O áudio é opcional: em ambientes sem placa de som, qualquer falha é ignorada silenciosamente e o jogo continua normalmente.
- Correção herdada: o teste original afirmava que a célula (0,2) tinha 2 minas vizinhas com minas em (0,0) e (0,1). Como (0,0) está a duas casas de distância, o valor correto é 1 — a asserção foi corrigida.
