package util;

/**
 * Agrupa tudo que define "como" uma partida vai ser jogada: dimensões,
 * tempo limite e todas as variantes de regra pedidas na lista de
 * implementações (modo sem cascata, minas visíveis, relâmpago, 3 vidas,
 * toroidal, diário). Existe para o Controller não precisar de uma dezena
 * de parâmetros soltos em cada chamada.
 */
public class ConfigPartida {

    public String nomeDificuldade = "Rank E";
    public int linhas = 9;
    public int colunas = 9;
    public int minas = 10;
    public int tempoLimiteSegundos = 0;

    public boolean semCascata = false;
    public boolean minasVisiveis = false;
    public boolean relampago = false;
    public int vidas = 1;
    public boolean toroidal = false;
    public boolean diario = false;
    public long seed = 0L;
    public boolean cooperativo = false;
    public boolean doisJogadores = false;

    public static ConfigPartida padrao(String nome, int linhas, int colunas, int minas) {
        ConfigPartida config = new ConfigPartida();
        config.nomeDificuldade = nome;
        config.linhas = linhas;
        config.colunas = colunas;
        config.minas = minas;
        return config;
    }

    public ConfigPartida copiar() {
        ConfigPartida copia = new ConfigPartida();
        copia.nomeDificuldade = nomeDificuldade;
        copia.linhas = linhas;
        copia.colunas = colunas;
        copia.minas = minas;
        copia.tempoLimiteSegundos = tempoLimiteSegundos;
        copia.semCascata = semCascata;
        copia.minasVisiveis = minasVisiveis;
        copia.relampago = relampago;
        copia.vidas = vidas;
        copia.toroidal = toroidal;
        copia.diario = diario;
        copia.seed = seed;
        copia.cooperativo = cooperativo;
        copia.doisJogadores = doisJogadores;
        return copia;
    }
}
