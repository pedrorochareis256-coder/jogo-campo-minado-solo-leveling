package util;

/**
 * Ícones alternativos para a marcação de bandeira, no lugar do triângulo
 * padrão (sugestão 8). O tema do jogo usa "Adaga das Sombras" como
 * padrão, remetendo às adagas do protagonista de Solo Leveling, sem
 * reproduzir nenhuma arte original.
 */
public enum SkinBandeira {

    ADAGA_SOMBRA("Adaga das Sombras", "\uD83D\uDDE1"),
    ESTRELA("Estrela", "\u2B50"),
    CORACAO("Coração", "\u2764"),
    ALFINETE("Alfinete", "\uD83D\uDCCC"),
    BANDEIRA_CLASSICA("Bandeira clássica", "\uD83D\uDEA9");

    public final String nomeExibicao;
    public final String emoji;

    SkinBandeira(String nomeExibicao, String emoji) {
        this.nomeExibicao = nomeExibicao;
        this.emoji = emoji;
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}
