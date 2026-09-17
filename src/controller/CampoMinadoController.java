package controller;

import model.Celula;
import model.Tabuleiro;
import util.*;
import view.CampoMinadoView;

import javax.swing.Timer;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLLER da arquitetura MVC: é o único ponto que conhece tanto o
 * {@link Tabuleiro} (Model) quanto a {@link CampoMinadoView} (View).
 * Recebe notificações de clique da View através de {@link AcoesJogador},
 * aplica a jogada no Model e manda a View se redesenhar. A View nunca
 * toca no Model diretamente, e o Model nunca conhece a View.
 * <p>
 * Além da regra clássica, este Controller também orquestra os serviços de
 * apoio do pacote {@code util}: tema visual, som, configurações
 * persistentes, histórico/recordes, conquistas, perfis, replay e o
 * solver automático ("O Sistema").
 */
public class CampoMinadoController implements AcoesJogador {

    private static final int DICAS_POR_PARTIDA = 3;

    private final CampoMinadoView view;

    private final ConfiguracoesJogo configuracoes = new ConfiguracoesJogo();
    private final GerenciadorSom som = new GerenciadorSom();
    private final GerenciadorPerfis gerenciadorPerfis = new GerenciadorPerfis();
    private final SolverAutomatico solver = new SolverAutomatico();
    private final ReplayGravador replay = new ReplayGravador();

    private String perfilAtivo;
    private HistoricoPartidas historico;
    private ConquistasManager conquistas;

    private Tabuleiro tabuleiro;
    private ConfigPartida configAtual;
    private int totalMinas;
    private int totalCelulas;
    private int celulasReveladas;
    private int jogadas;
    private boolean jogoIniciado;
    private long tempoInicio;
    private int limiteSegundos;
    private Timer timerJogo;
    private Timer timerSistema;

    private boolean usouBandeiraNestaPartida;
    private boolean usouVidaExtraNestaPartida;
    private int dicasRestantes;
    private int jogadorDaVez = 1;

    // ---- estado do Modo Dois Jogadores (sugestão 43) ----
    private Tabuleiro tabuleiroP2;
    private int celulasReveladasP2;
    private int totalCelulasP2;

    public CampoMinadoController(CampoMinadoView view) {
        this.view = view;
        this.view.setOuvinte(this);
    }

    public void iniciar() {
        perfilAtivo = configuracoes.getPerfilAtivo();
        historico = new HistoricoPartidas(perfilAtivo);
        conquistas = new ConquistasManager(perfilAtivo);

        som.setEfeitosLigados(configuracoes.isSomEfeitosLigado());
        som.setVolumeMusica(configuracoes.getVolumeMusica());
        som.setMusicaLigada(configuracoes.isMusicaLigada());

        TemaVisual tema = resolverTemaInicial();
        view.aplicarTema(tema);
        view.aplicarSkinBandeira(SkinBandeira.valueOf(configuracoes.getSkinBandeira()));
        view.aplicarSkinNumeros(SkinNumeros.valueOf(configuracoes.getSkinNumeros()));
        view.setSimbolosDaltonismo(configuracoes.isSimbolosDaltonismo());
        view.setTamanhoCelula(configuracoes.getTamanhoCelula());

        view.mostrarTelaInicial(gerenciadorPerfis.listarPerfis(), perfilAtivo);
        view.setVisible(true);
    }

    private TemaVisual resolverTemaInicial() {
        if (configuracoes.isDeteccaoAutomaticaDeTema()) {
            boolean escuro = DetectorTemaSistema.sistemaEstaEmModoEscuro();
            return escuro ? TemaVisual.monarcaDasSombras() : TemaVisual.altoContraste();
        }
        return TemaVisual.porNome(configuracoes.getTemaVisualNome());
    }

    // ================================================================
    // AcoesJogador — chamado pela View
    // ================================================================

    @Override
    public void aoEscolherDificuldade(ConfigPartida config) {
        this.configAtual = config.copiar();
        pararTimers();

        long seed = config.diario ? sementeDoDiaAtual() : System.nanoTime();
        this.tabuleiro = new Tabuleiro(config.linhas, config.colunas, config.minas, seed, config.toroidal);
        this.tabuleiro.setVidasMaximas(config.vidas);

        this.totalMinas = config.minas;
        this.totalCelulas = config.linhas * config.colunas - config.minas;
        this.celulasReveladas = 0;
        this.jogadas = 0;
        this.jogoIniciado = false;
        this.limiteSegundos = config.tempoLimiteSegundos;
        this.usouBandeiraNestaPartida = false;
        this.usouVidaExtraNestaPartida = false;
        this.dicasRestantes = DICAS_POR_PARTIDA;
        this.jogadorDaVez = 1;

        replay.iniciarGravacao(config, seed);

        if (config.doisJogadores) {
            this.tabuleiroP2 = new Tabuleiro(config.linhas, config.colunas, config.minas, seed, config.toroidal);
            this.tabuleiroP2.setVidasMaximas(config.vidas);
            this.totalCelulasP2 = totalCelulas;
            this.celulasReveladasP2 = 0;
            view.iniciarTelaDoisJogadores(config.linhas, config.colunas, totalMinas, totalCelulas, limiteSegundos);
        } else {
            view.iniciarTelaDeJogo(config.linhas, config.colunas, totalMinas, totalCelulas, limiteSegundos, config.vidas);
            if (config.cooperativo) {
                view.atualizarTurno(jogadorDaVez);
            }
        }

        view.atualizarEstatisticas(totalMinas, 0, totalCelulas, 0, tabuleiro.getVidasRestantes());
        view.setDicasRestantes(dicasRestantes);

        if (config.minasVisiveis) {
            view.mostrarMinasComoDebug(tabuleiro);
        }

        if (config.relampago) {
            List<int[]> reveladas = tabuleiro.revelarAleatoriasSeguras(
                    Math.max(1, (config.linhas * config.colunas) / 12), new java.util.Random(seed));
            for (int[] pos : reveladas) {
                view.atualizarCelula(pos[0], pos[1], tabuleiro);
            }
            celulasReveladas = contarCelulasReveladas();
            view.atualizarEstatisticas(totalMinas, celulasReveladas, totalCelulas, jogadas, tabuleiro.getVidasRestantes());
        }
    }

    /** Semente igual para todo mundo no mesmo dia — Portal Diário (sugestão 49). */
    private long sementeDoDiaAtual() {
        String hoje = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return hoje.hashCode();
    }

    @Override
    public void aoPedirNovoJogo() {
        pararTimers();
        view.mostrarTelaInicial(gerenciadorPerfis.listarPerfis(), perfilAtivo);
    }

    @Override
    public void aoMarcarCelula(int linha, int coluna) {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        tabuleiro.alternarMarcacao(linha, coluna);
        if (tabuleiro.getCelula(linha, coluna).isMarcada()) {
            usouBandeiraNestaPartida = true;
            som.tocarBandeira();
        }
        replay.registrarMarcar(linha, coluna);
        view.atualizarCelula(linha, coluna, tabuleiro);
        atualizarEstatisticasNaView();
        view.avisarSeBandeirasExcedem(contarMarcadas(), totalMinas);
    }

    @Override
    public void aoRevelarCelula(int linha, int coluna) {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        if (tabuleiro.getCelula(linha, coluna).isMarcada()) {
            return;
        }

        if (!jogoIniciado) {
            jogoIniciado = true;
            tempoInicio = System.currentTimeMillis();
            iniciarTimer();
        }

        if (limiteSegundos > 0 && obterSegundosPassados() >= limiteSegundos) {
            encerrarPorTempo();
            return;
        }

        jogadas++;
        replay.registrarRevelar(linha, coluna);
        int vidasAntes = tabuleiro.getVidasRestantes();
        List<int[]> reveladas = tabuleiro.revelar(linha, coluna, !configAtual.semCascata);
        if (tabuleiro.getVidasRestantes() < vidasAntes && !tabuleiro.isJogoEncerrado()) {
            usouVidaExtraNestaPartida = true;
            view.mostrarAvisoVidaPerdida(tabuleiro.getVidasRestantes());
        }
        celulasReveladas = contarCelulasReveladas();

        int atraso = reveladas.size() > 80 ? 3 : (reveladas.size() > 25 ? 8 : 18);
        som.tocarClique();
        animarRevelacao(reveladas, 0, atraso);

        if (configAtual.cooperativo && !tabuleiro.isJogoEncerrado()) {
            jogadorDaVez = jogadorDaVez == 1 ? 2 : 1;
            view.atualizarTurno(jogadorDaVez);
        }
    }

    @Override
    public void aoRevelarCelulaJogador(int jogador, int linha, int coluna) {
        Tabuleiro alvo = jogador == 1 ? tabuleiro : tabuleiroP2;
        if (alvo == null || alvo.isJogoEncerrado() || alvo.getCelula(linha, coluna).isMarcada()) {
            return;
        }
        if (!jogoIniciado) {
            jogoIniciado = true;
            tempoInicio = System.currentTimeMillis();
            iniciarTimer();
        }
        List<int[]> reveladas = alvo.revelar(linha, coluna, true);
        for (int[] pos : reveladas) {
            view.atualizarCelulaJogador(jogador, pos[0], pos[1], alvo);
        }
        if (jogador == 1) {
            celulasReveladas = contarCelulasReveladasDe(tabuleiro);
        } else {
            celulasReveladasP2 = contarCelulasReveladasDe(tabuleiroP2);
        }
        view.atualizarEstatisticasDoisJogadores(celulasReveladas, totalCelulas, celulasReveladasP2, totalCelulasP2);

        if (alvo.isJogoEncerrado()) {
            pararTimers();
            boolean venceu = !alvo.isDerrota();
            view.mostrarResultadoDoisJogadores(jogador, venceu);
        }
    }

    @Override
    public void aoMarcarCelulaJogador(int jogador, int linha, int coluna) {
        Tabuleiro alvo = jogador == 1 ? tabuleiro : tabuleiroP2;
        if (alvo == null || alvo.isJogoEncerrado()) {
            return;
        }
        alvo.alternarMarcacao(linha, coluna);
        view.atualizarCelulaJogador(jogador, linha, coluna, alvo);
    }

    @Override
    public void aoPedirDica() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado() || dicasRestantes <= 0) {
            return;
        }
        int[] segura = encontrarCelulaSeguraAleatoria();
        if (segura == null) {
            return;
        }
        dicasRestantes--;
        view.setDicasRestantes(dicasRestantes);
        som.tocarDica();
        aoRevelarCelula(segura[0], segura[1]);
    }

    private int[] encontrarCelulaSeguraAleatoria() {
        List<int[]> candidatas = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (!tabuleiro.isMinada(i, j) && !tabuleiro.isRevelada(i, j) && !tabuleiro.isMarcada(i, j)) {
                    candidatas.add(new int[]{i, j});
                }
            }
        }
        if (candidatas.isEmpty()) {
            return null;
        }
        return candidatas.get((int) (Math.random() * candidatas.size()));
    }

    // ================================================================
    // "O Sistema" — solver automático (sugestão 38, item 🔴)
    // ================================================================

    @Override
    public void aoPedirPassoDoSistema() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        aplicarUmPassoDoSistema();
    }

    @Override
    public void aoPedirSistemaResolverTudo() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        if (timerSistema != null) {
            timerSistema.stop();
        }
        timerSistema = new Timer(260, null);
        timerSistema.addActionListener(e -> {
            if (tabuleiro.isJogoEncerrado()) {
                timerSistema.stop();
                return;
            }
            boolean progrediu = aplicarUmPassoDoSistema();
            if (!progrediu) {
                timerSistema.stop();
            }
        });
        view.mostrarMensagemSistema("O Sistema: analisando ameaças...");
        timerSistema.start();
    }

    /** @return true se alguma ação foi tomada (dedução ou palpite); false se não havia mais nada a fazer. */
    private boolean aplicarUmPassoDoSistema() {
        SolverAutomatico.ResultadoAnalise resultado = solver.analisar(tabuleiro);
        boolean agiu = false;

        for (int[] pos : resultado.celulasCertasDeMina) {
            if (tabuleiro.getCelula(pos[0], pos[1]).getEstadoMarcacao() == Celula.EstadoMarcacao.NENHUMA) {
                tabuleiro.alternarMarcacao(pos[0], pos[1]);
                view.atualizarCelula(pos[0], pos[1], tabuleiro);
                agiu = true;
            }
        }
        for (int[] pos : resultado.celulasSegurasParaRevelar) {
            if (!tabuleiro.getCelula(pos[0], pos[1]).isRevelada()) {
                List<int[]> reveladas = tabuleiro.revelar(pos[0], pos[1], !configAtual.semCascata);
                for (int[] r : reveladas) {
                    view.atualizarCelula(r[0], r[1], tabuleiro);
                }
                agiu = true;
            }
        }

        if (!agiu && !resultado.impasse) {
            // segurança: se por algum motivo marcou "impasse" errado, evita loop infinito.
            agiu = true;
        }

        if (resultado.impasse) {
            view.mostrarMensagemSistema("O Sistema: nenhuma dedução certa — arriscando um palpite.");
            int[] palpite = solver.escolherPalpiteAoAcaso(tabuleiro);
            if (palpite != null) {
                List<int[]> reveladas = tabuleiro.revelar(palpite[0], palpite[1], !configAtual.semCascata);
                for (int[] r : reveladas) {
                    view.atualizarCelula(r[0], r[1], tabuleiro);
                }
                agiu = true;
            }
        } else {
            view.mostrarMensagemSistema("O Sistema: ameaças identificadas e neutralizadas.");
        }

        if (!jogoIniciado && agiu) {
            jogoIniciado = true;
            tempoInicio = System.currentTimeMillis();
            iniciarTimer();
        }

        celulasReveladas = contarCelulasReveladas();
        atualizarEstatisticasNaView();

        if (tabuleiro.isJogoEncerrado()) {
            finalizarJogada();
        }
        return agiu;
    }

    // ================================================================
    // Histórico, exportação, compartilhamento e replay
    // ================================================================

    @Override
    public void aoExportarHistoricoCsv() {
        File destino = view.pedirLocalParaSalvarCsv(perfilAtivo);
        if (destino != null) {
            boolean sucesso = historico.exportarPara(destino);
            view.mostrarResultadoExportacao(sucesso, destino);
        }
    }

    @Override
    public String gerarTextoCompartilhamento() {
        if (configAtual == null) {
            return "Ainda não completei nenhuma missão no Campo Minado: Solo Leveling.";
        }
        String tempo = view.getTempoExibidoAtual();
        return String.format(
                "\u2694\uFE0F Concluí a missão \"%s\" (%dx%d, %d minas) em %s no Campo Minado: Solo Leveling!",
                configAtual.nomeDificuldade, configAtual.linhas, configAtual.colunas, configAtual.minas, tempo);
    }

    @Override
    public void aoReproduzirUltimoReplay() {
        if (!replay.temReplayDisponivel()) {
            view.mostrarMensagemSistema("Nenhuma missão gravada ainda para reprisar.");
            return;
        }
        ConfigPartida config = replay.getConfigUsada();
        Tabuleiro tabuleiroReplay = new Tabuleiro(config.linhas, config.colunas, config.minas,
                replay.getSeedUsada(), config.toroidal);
        tabuleiroReplay.setVidasMaximas(config.vidas);

        view.iniciarTelaDeReplay(config.linhas, config.colunas, totalMinas, totalCelulas);
        reproduzirPasso(tabuleiroReplay, replay.getJogadas(), 0);
    }

    private void reproduzirPasso(Tabuleiro tabuleiroReplay, List<ReplayGravador.Jogada> jogadasGravadas, int indice) {
        if (indice >= jogadasGravadas.size()) {
            return;
        }
        ReplayGravador.Jogada jogada = jogadasGravadas.get(indice);
        List<int[]> afetadas;
        if (jogada.revelar) {
            afetadas = tabuleiroReplay.revelar(jogada.linha, jogada.coluna, true);
        } else {
            tabuleiroReplay.alternarMarcacao(jogada.linha, jogada.coluna);
            afetadas = List.of(new int[]{jogada.linha, jogada.coluna});
        }
        for (int[] pos : afetadas) {
            view.atualizarCelulaReplay(pos[0], pos[1], tabuleiroReplay);
        }
        Timer proximo = new Timer(180, e -> reproduzirPasso(tabuleiroReplay, jogadasGravadas, indice + 1));
        proximo.setRepeats(false);
        proximo.start();
    }

    // ================================================================
    // Perfis
    // ================================================================

    @Override
    public void aoTrocarPerfil(String nomePerfil) {
        this.perfilAtivo = nomePerfil;
        this.historico = new HistoricoPartidas(nomePerfil);
        this.conquistas = new ConquistasManager(nomePerfil);
        configuracoes.setPerfilAtivo(nomePerfil);
    }

    @Override
    public void aoCriarPerfil(String nomePerfil) {
        gerenciadorPerfis.criarPerfil(nomePerfil);
        aoTrocarPerfil(nomePerfil);
        view.mostrarTelaInicial(gerenciadorPerfis.listarPerfis(), perfilAtivo);
    }

    // ================================================================
    // Contagens e sincronização com a View
    // ================================================================

    private int contarCelulasReveladas() {
        return contarCelulasReveladasDe(tabuleiro);
    }

    private int contarCelulasReveladasDe(Tabuleiro alvo) {
        int count = 0;
        for (int i = 0; i < alvo.getLinhas(); i++) {
            for (int j = 0; j < alvo.getColunas(); j++) {
                if (alvo.isRevelada(i, j) && !alvo.isMinada(i, j)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int contarMarcadas() {
        int count = 0;
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isMarcada(i, j)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void atualizarEstatisticasNaView() {
        int restantes = totalMinas - contarMarcadas();
        view.atualizarEstatisticas(restantes, celulasReveladas, totalCelulas, jogadas, tabuleiro.getVidasRestantes());
    }

    // ================================================================
    // Timer do cronômetro
    // ================================================================

    private void iniciarTimer() {
        timerJogo = new Timer(1000, e -> atualizarTempo());
        timerJogo.start();
    }

    private void pararTimer() {
        if (timerJogo != null) {
            timerJogo.stop();
        }
    }

    private void pararTimers() {
        pararTimer();
        if (timerSistema != null) {
            timerSistema.stop();
        }
    }

    private long obterSegundosPassados() {
        return (System.currentTimeMillis() - tempoInicio) / 1000;
    }

    private void atualizarTempo() {
        long segundosPassados = obterSegundosPassados();
        if (limiteSegundos > 0) {
            long restantes = Math.max(0, limiteSegundos - segundosPassados);
            view.atualizarTempo(String.format("-%02d:%02d", restantes / 60, restantes % 60));
            if (restantes <= 0) {
                encerrarPorTempo();
                return;
            }
        } else {
            view.atualizarTempo(String.format("%02d:%02d", segundosPassados / 60, segundosPassados % 60));
        }
    }

    private void encerrarPorTempo() {
        pararTimers();
        view.mostrarDerrota();
        som.tocarExplosao();
        registrarPartidaEncerrada(false);
    }

    // ================================================================
    // Animações (o Controller decide o ritmo; a View só desenha um passo)
    // ================================================================

    private void animarRevelacao(List<int[]> celulas, int indice, int atraso) {
        if (indice >= celulas.size()) {
            finalizarJogada();
            return;
        }
        int[] posicao = celulas.get(indice);
        view.atualizarCelula(posicao[0], posicao[1], tabuleiro);

        Timer timer = new Timer(atraso, e -> animarRevelacao(celulas, indice + 1, atraso));
        timer.setRepeats(false);
        timer.start();
    }

    private void finalizarJogada() {
        atualizarEstatisticasNaView();

        if (!tabuleiro.isJogoEncerrado()) {
            return;
        }

        pararTimer();

        if (tabuleiro.isDerrota()) {
            view.mostrarDerrota();
            som.tocarExplosao();
            animarExplosao();
            registrarPartidaEncerrada(false);
        } else {
            view.mostrarVitoria();
            som.tocarVitoria();
            animarVitoria();
            registrarPartidaEncerrada(true);
        }
    }

    private void registrarPartidaEncerrada(boolean venceu) {
        if (configAtual == null) {
            return;
        }
        long segundos = jogoIniciado ? obterSegundosPassados() : 0;
        RegistroPartida registro = new RegistroPartida(
                java.time.LocalDateTime.now().toString(), configAtual.nomeDificuldade,
                configAtual.linhas, configAtual.colunas, configAtual.minas,
                venceu, (int) segundos, jogadas);
        historico.registrar(registro);

        if (!venceu) {
            return;
        }

        Integer melhorAnterior = historico.melhorTempo(configAtual.nomeDificuldade).orElse(null);
        view.atualizarRecorde(melhorAnterior == null || segundos <= melhorAnterior ? (int) segundos : melhorAnterior);

        List<String> desbloqueadasAgora = new ArrayList<>();
        if (conquistas.desbloquear(Conquista.PRIMEIRO_PORTAL)) {
            desbloqueadasAgora.add(Conquista.PRIMEIRO_PORTAL.titulo);
        }
        if (!usouBandeiraNestaPartida && conquistas.desbloquear(Conquista.SEM_BANDEIRA)) {
            desbloqueadasAgora.add(Conquista.SEM_BANDEIRA.titulo);
        }
        if (segundos < 30 && conquistas.desbloquear(Conquista.RELAMPAGO_30S)) {
            desbloqueadasAgora.add(Conquista.RELAMPAGO_30S.titulo);
        }
        if (usouVidaExtraNestaPartida && conquistas.desbloquear(Conquista.SOBREVIVENTE)) {
            desbloqueadasAgora.add(Conquista.SOBREVIVENTE.titulo);
        }
        if (configAtual.nomeDificuldade.toLowerCase().contains("s") && configAtual.linhas >= 16 && configAtual.colunas >= 24
                && conquistas.desbloquear(Conquista.RANK_S)) {
            desbloqueadasAgora.add(Conquista.RANK_S.titulo);
        }
        if (configAtual.diario && conquistas.desbloquear(Conquista.PORTAL_DIARIO)) {
            desbloqueadasAgora.add(Conquista.PORTAL_DIARIO.titulo);
        }
        long totalVitorias = historico.listarTudo().stream().filter(r -> r.vitoria).count();
        if (totalVitorias >= 10 && conquistas.desbloquear(Conquista.DEZ_VITORIAS)) {
            desbloqueadasAgora.add(Conquista.DEZ_VITORIAS.titulo);
        }

        if (!desbloqueadasAgora.isEmpty()) {
            view.mostrarConquistasDesbloqueadas(desbloqueadasAgora);
        }
    }

    private void animarExplosao() {
        Timer piscar = new Timer(100, null);
        int[] contador = {0};
        piscar.addActionListener(e -> {
            contador[0]++;
            view.piscarFundoDeExplosao(contador[0] % 2 == 1);
            if (contador[0] >= 6) {
                piscar.stop();
                view.piscarFundoDeExplosao(false);
                revelarMinasComAnimacao();
            }
        });
        piscar.start();
    }

    private void revelarMinasComAnimacao() {
        List<int[]> minasNaoReveladas = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isMinada(i, j) && !tabuleiro.isRevelada(i, j)) {
                    minasNaoReveladas.add(new int[]{i, j});
                }
            }
        }
        revelarMinasPasso(minasNaoReveladas, 0);
    }

    private void revelarMinasPasso(List<int[]> minas, int indice) {
        if (indice >= minas.size()) {
            return;
        }
        int[] posicao = minas.get(indice);
        view.marcarMinaExplodida(posicao[0], posicao[1]);

        Timer timer = new Timer(80, e -> revelarMinasPasso(minas, indice + 1));
        timer.setRepeats(false);
        timer.start();
    }

    private void animarVitoria() {
        List<int[]> celulasSeguras = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isRevelada(i, j) && !tabuleiro.isMinada(i, j)) {
                    celulasSeguras.add(new int[]{i, j});
                }
            }
        }
        vitoriaPasso(celulasSeguras, 0);
        view.dispararConfeteDeVitoria();
    }

    private void vitoriaPasso(List<int[]> celulas, int indice) {
        if (indice >= celulas.size()) {
            return;
        }
        int[] atual = celulas.get(indice);
        view.destacarCelulaVencedora(atual[0], atual[1]);

        Timer timer = new Timer(8, e -> vitoriaPasso(celulas, indice + 1));
        timer.setRepeats(false);
        timer.start();
    }
}
