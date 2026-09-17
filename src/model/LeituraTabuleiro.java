package model;

/**
 * Contrato somente-leitura do tabuleiro.
 * <p>
 * Este é o "canal" pelo qual a View enxerga o estado do Model. A View
 * recebe apenas uma {@code LeituraTabuleiro} (nunca um {@link Tabuleiro}
 * mutável diretamente), então ela consegue desenhar a tela mas nunca
 * consegue alterar o jogo por conta própria — quem manda no estado é
 * sempre o Controller, através do {@link Tabuleiro}.
 */
public interface LeituraTabuleiro {

    int getLinhas();

    int getColunas();

    boolean isRevelada(int linha, int coluna);

    boolean isMarcada(int linha, int coluna);

    boolean isInterrogada(int linha, int coluna);

    boolean isMinada(int linha, int coluna);

    boolean isExplodida(int linha, int coluna);

    int getMinasVizinhas(int linha, int coluna);

    boolean isJogoEncerrado();

    boolean isDerrota();

    /** Quantidade de vidas restantes no modo "3 vidas" (1 fora desse modo). */
    int getVidasRestantes();
}
