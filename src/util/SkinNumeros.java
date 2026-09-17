package util;

/**
 * Skins alternativas para os números 1-8 exibidos nas células reveladas
 * (sugestão 9) e símbolos extras de apoio a jogadores daltônicos,
 * combináveis com qualquer skin (sugestão 25: usar forma além de cor).
 */
public enum SkinNumeros {

    CLASSICO("Números clássicos", new String[]{
            "", "1", "2", "3", "4", "5", "6", "7", "8"
    }),
    RANKS_DE_CACADOR("Ranks de Caçador", new String[]{
            "", "E", "D", "C", "B", "A", "S", "SS", "SSS"
    }),
    BESTAS_DE_SOMBRA("Bestas de Sombra", new String[]{
            "", "\uD83D\uDC7B", "\uD83E\uDD87", "\uD83D\uDC0D", "\uD83E\uDD82",
            "\uD83E\uDD8B\u200D", "\uD83D\uDC09", "\uD83D\uDC7E", "\uD83D\uDC80"
    });

    /** Um símbolo geométrico distinto por contagem, para apoiar daltônicos além da cor. */
    private static final String[] SIMBOLOS_FORMA = {
            "", "\u25B2", "\u25A0", "\u25CF", "\u25C6", "\u2605", "\u2726", "\u271A", "\u2716"
    };

    public final String nomeExibicao;
    private final String[] simbolos;

    SkinNumeros(String nomeExibicao, String[] simbolos) {
        this.nomeExibicao = nomeExibicao;
        this.simbolos = simbolos;
    }

    /**
     * Retorna o texto a exibir para uma célula com {@code minasVizinhas}
     * minas ao redor. Quando {@code comSimbolosDeForma} é verdadeiro,
     * anexa um símbolo geométrico fixo por número (independente da cor),
     * para apoio a jogadores daltônicos.
     */
    public String textoPara(int minasVizinhas, boolean comSimbolosDeForma) {
        if (minasVizinhas <= 0 || minasVizinhas >= simbolos.length) {
            return "";
        }
        String base = simbolos[minasVizinhas];
        if (!comSimbolosDeForma) {
            return base;
        }
        return SIMBOLOS_FORMA[minasVizinhas] + base;
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}
