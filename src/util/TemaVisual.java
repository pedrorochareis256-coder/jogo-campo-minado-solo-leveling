package util;

import java.awt.Color;

/**
 * Paleta de cores de um tema visual completo (fundo, cartões, células
 * ocultas/reveladas, textos, destaque). Substitui as constantes fixas que
 * existiam na View por um objeto trocável em tempo de execução — sugestão
 * 1 ("Sistema de temas visuais plugável").
 * <p>
 * O tema padrão do jogo é "Monarca das Sombras", com a estética roxo/azul
 * escuro e dourado inspirada em Solo Leveling (sem reproduzir nenhuma
 * arte ou logotipo da obra original — só a paleta e o clima).
 */
public class TemaVisual {

    public final String nome;
    public final Color fundo;
    public final Color fundoClaro;
    public final Color destaque;
    public final Color textoPrincipal;
    public final Color textoSecundario;
    public final Color card;
    public final Color cardHover;
    public final Color borda;
    public final Color celulaOculta;
    public final Color celulaOcultaHover;
    public final Color bordaOculta;
    public final Color celulaRevelada;
    public final Color bordaRevelada;
    public final Color textoSobreRevelada;
    public final Color minaFundo;
    public final Color mina;
    public final Color vitoria;
    public final Color bandeira;

    public TemaVisual(String nome, Color fundo, Color fundoClaro, Color destaque,
                       Color textoPrincipal, Color textoSecundario, Color card, Color cardHover,
                       Color borda, Color celulaOculta, Color celulaOcultaHover, Color bordaOculta,
                       Color celulaRevelada, Color bordaRevelada, Color textoSobreRevelada,
                       Color minaFundo, Color mina, Color vitoria, Color bandeira) {
        this.nome = nome;
        this.fundo = fundo;
        this.fundoClaro = fundoClaro;
        this.destaque = destaque;
        this.textoPrincipal = textoPrincipal;
        this.textoSecundario = textoSecundario;
        this.card = card;
        this.cardHover = cardHover;
        this.borda = borda;
        this.celulaOculta = celulaOculta;
        this.celulaOcultaHover = celulaOcultaHover;
        this.bordaOculta = bordaOculta;
        this.celulaRevelada = celulaRevelada;
        this.bordaRevelada = bordaRevelada;
        this.textoSobreRevelada = textoSobreRevelada;
        this.minaFundo = minaFundo;
        this.mina = mina;
        this.vitoria = vitoria;
        this.bandeira = bandeira;
    }

    /** "Monarca das Sombras" — tema padrão, roxo/azul escuro com dourado. */
    public static TemaVisual monarcaDasSombras() {
        return new TemaVisual(
                "Monarca das Sombras",
                new Color(15, 12, 26), new Color(28, 22, 46), new Color(147, 92, 255),
                new Color(232, 224, 250), new Color(150, 138, 180),
                new Color(35, 27, 58), new Color(52, 40, 84), new Color(80, 60, 130),
                new Color(45, 35, 74), new Color(63, 48, 102), new Color(110, 85, 170),
                new Color(224, 216, 245), new Color(170, 150, 210), new Color(30, 22, 48),
                new Color(60, 15, 25), new Color(230, 60, 90), new Color(90, 220, 150),
                new Color(230, 190, 60)
        );
    }

    /** Terminal retrô — verde fósforo sobre preto (sugestão 2). */
    public static TemaVisual terminalRetro() {
        return new TemaVisual(
                "Terminal do Sistema",
                new Color(6, 12, 6), new Color(12, 22, 12), new Color(60, 255, 90),
                new Color(80, 255, 110), new Color(40, 150, 60),
                new Color(10, 20, 10), new Color(18, 32, 18), new Color(40, 120, 50),
                new Color(10, 25, 10), new Color(20, 45, 20), new Color(50, 150, 60),
                new Color(20, 45, 20), new Color(60, 255, 90), new Color(80, 255, 110),
                new Color(40, 10, 10), new Color(255, 70, 70), new Color(60, 255, 90),
                new Color(60, 255, 90)
        );
    }

    /** Cyberpunk neon — roxo escuro, rosa e ciano (sugestão 3). */
    public static TemaVisual cyberpunkNeon() {
        return new TemaVisual(
                "Portal Cyberpunk",
                new Color(12, 8, 28), new Color(24, 14, 48), new Color(255, 45, 170),
                new Color(230, 230, 255), new Color(150, 140, 200),
                new Color(28, 16, 54), new Color(42, 24, 78), new Color(90, 40, 150),
                new Color(30, 18, 60), new Color(48, 26, 90), new Color(255, 45, 170),
                new Color(20, 220, 230), new Color(255, 45, 170), new Color(15, 10, 30),
                new Color(50, 10, 30), new Color(255, 60, 90), new Color(20, 220, 230),
                new Color(20, 220, 230)
        );
    }

    /** Halloween — laranja, roxo, marrom (sugestão 4). */
    public static TemaVisual halloween() {
        return new TemaVisual(
                "Portão da Noite das Bruxas",
                new Color(20, 12, 8), new Color(35, 20, 14), new Color(255, 140, 30),
                new Color(240, 210, 170), new Color(170, 130, 90),
                new Color(38, 22, 16), new Color(55, 32, 22), new Color(100, 60, 30),
                new Color(45, 22, 55), new Color(65, 34, 78), new Color(120, 60, 140),
                new Color(255, 210, 150), new Color(200, 120, 40), new Color(35, 18, 10),
                new Color(45, 15, 10), new Color(255, 100, 40), new Color(150, 210, 90),
                new Color(255, 140, 30)
        );
    }

    /** Oceano/tropical — azul profundo, coral e turquesa (sugestão 5). */
    public static TemaVisual oceanoTropical() {
        return new TemaVisual(
                "Abismo Azul",
                new Color(6, 22, 36), new Color(12, 38, 56), new Color(40, 200, 190),
                new Color(220, 245, 245), new Color(120, 180, 190),
                new Color(14, 42, 60), new Color(20, 58, 80), new Color(30, 90, 100),
                new Color(10, 45, 65), new Color(18, 65, 90), new Color(40, 200, 190),
                new Color(210, 245, 240), new Color(80, 190, 180), new Color(10, 35, 45),
                new Color(60, 20, 20), new Color(255, 110, 90), new Color(60, 220, 150),
                new Color(255, 160, 120)
        );
    }

    /** Alto contraste — pensado para acessibilidade: preto/branco/amarelo (sugestão 6). */
    public static TemaVisual altoContraste() {
        return new TemaVisual(
                "Visão do Monarca (Alto Contraste)",
                Color.BLACK, new Color(20, 20, 20), new Color(255, 220, 0),
                Color.WHITE, new Color(230, 230, 230),
                new Color(15, 15, 15), new Color(35, 35, 35), Color.WHITE,
                new Color(25, 25, 25), new Color(50, 50, 50), Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK,
                new Color(60, 0, 0), Color.RED, new Color(0, 220, 0),
                new Color(255, 220, 0)
        );
    }

    public static TemaVisual[] todos() {
        return new TemaVisual[]{
                monarcaDasSombras(), terminalRetro(), cyberpunkNeon(),
                halloween(), oceanoTropical(), altoContraste()
        };
    }

    public static TemaVisual porNome(String nome) {
        for (TemaVisual tema : todos()) {
            if (tema.nome.equals(nome)) {
                return tema;
            }
        }
        return monarcaDasSombras();
    }

    @Override
    public String toString() {
        return nome;
    }
}
