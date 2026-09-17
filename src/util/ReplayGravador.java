package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Grava a sequência de jogadas (revelar/marcar) de uma partida, na ordem
 * e com o intervalo de tempo entre elas, para permitir reproduzi-la
 * depois jogada a jogada (sugestão 41 — Modo replay).
 */
public class ReplayGravador {

    /** Um passo gravado do replay. */
    public static class Jogada {
        public final boolean revelar; // true = revelar, false = marcar/alternar bandeira
        public final int linha;
        public final int coluna;
        public final long instanteMs;

        public Jogada(boolean revelar, int linha, int coluna, long instanteMs) {
            this.revelar = revelar;
            this.linha = linha;
            this.coluna = coluna;
            this.instanteMs = instanteMs;
        }
    }

    private final List<Jogada> jogadas = new ArrayList<>();
    private long inicio;
    private long seedUsada;
    private ConfigPartida configUsada;

    public void iniciarGravacao(ConfigPartida config, long seed) {
        jogadas.clear();
        inicio = System.currentTimeMillis();
        this.seedUsada = seed;
        this.configUsada = config.copiar();
    }

    public void registrarRevelar(int linha, int coluna) {
        jogadas.add(new Jogada(true, linha, coluna, System.currentTimeMillis() - inicio));
    }

    public void registrarMarcar(int linha, int coluna) {
        jogadas.add(new Jogada(false, linha, coluna, System.currentTimeMillis() - inicio));
    }

    public List<Jogada> getJogadas() {
        return jogadas;
    }

    public long getSeedUsada() {
        return seedUsada;
    }

    public ConfigPartida getConfigUsada() {
        return configUsada;
    }

    public boolean temReplayDisponivel() {
        return configUsada != null && !jogadas.isEmpty();
    }
}
