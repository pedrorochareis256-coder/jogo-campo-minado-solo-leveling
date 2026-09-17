package util;

/**
 * Um registro de partida encerrada (vitória ou derrota), usado pelo
 * histórico de partidas (sugestão 31), pelo recorde de melhor tempo
 * (sugestão 30), pelo ranking (sugestão 42) e pela exportação em CSV
 * (sugestão 32).
 */
public class RegistroPartida {

    public final String dataHora;
    public final String dificuldade;
    public final int linhas;
    public final int colunas;
    public final int minas;
    public final boolean vitoria;
    public final int tempoSegundos;
    public final int jogadas;

    public RegistroPartida(String dataHora, String dificuldade, int linhas, int colunas, int minas,
                            boolean vitoria, int tempoSegundos, int jogadas) {
        this.dataHora = dataHora;
        this.dificuldade = dificuldade;
        this.linhas = linhas;
        this.colunas = colunas;
        this.minas = minas;
        this.vitoria = vitoria;
        this.tempoSegundos = tempoSegundos;
        this.jogadas = jogadas;
    }

    public String toLinhaCsv() {
        return String.join(",",
                dataHora, dificuldade, String.valueOf(linhas), String.valueOf(colunas),
                String.valueOf(minas), String.valueOf(vitoria), String.valueOf(tempoSegundos),
                String.valueOf(jogadas));
    }

    public static RegistroPartida deLinhaCsv(String linha) {
        String[] campos = linha.split(",", -1);
        return new RegistroPartida(
                campos[0], campos[1],
                Integer.parseInt(campos[2]), Integer.parseInt(campos[3]), Integer.parseInt(campos[4]),
                Boolean.parseBoolean(campos[5]), Integer.parseInt(campos[6]), Integer.parseInt(campos[7])
        );
    }

    public static String cabecalhoCsv() {
        return "dataHora,dificuldade,linhas,colunas,minas,vitoria,tempoSegundos,jogadas";
    }
}
