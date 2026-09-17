package util;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.*;

/**
 * Ícones desenhados diretamente por código (Java2D), no lugar dos emojis
 * usados antes para mina e bandeira. Resolve o problema de depender da
 * fonte de emoji do sistema operacional (o comentário original do projeto
 * já alertava sobre isso: "FONTE_CELULA = Segoe UI Emoji") e dá um
 * acabamento mais próprio ao tema Solo Leveling — uma caveira/runa de
 * perigo para a mina, e adaga/estrela/coração/alfinete/bandeira para as
 * skins de marcação.
 * <p>
 * Cada método devolve um {@link Icon} que desenha sua forma centralizada
 * num quadrado de {@code tamanho} pixels, sempre com anti-serrilhado
 * ligado. Nenhum arquivo externo é carregado — tudo é vetor (linhas,
 * curvas e polígonos) calculado na hora.
 */
public final class IconesVetoriais {

    private IconesVetoriais() {
    }

    private interface Desenhador {
        void desenhar(Graphics2D g2, int tamanho);
    }

    private static Icon criar(int tamanho, Desenhador desenhador) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                desenhador.desenhar(g2, tamanho);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return tamanho;
            }

            @Override
            public int getIconHeight() {
                return tamanho;
            }
        };
    }

    /** Ícone de perigo (mina): uma caveira estilizada. */
    public static Icon caveira(Color corPrincipal, Color corSombra, int tamanho) {
        return criar(tamanho, (g2, t) -> {
            double s = t;
            g2.setColor(corPrincipal);

            // Crânio (topo arredondado).
            Ellipse2D cranio = new Ellipse2D.Double(s * 0.18, s * 0.10, s * 0.64, s * 0.58);
            g2.fill(cranio);

            // Maxilar (base mais estreita, levemente sobreposta ao crânio).
            RoundRectangle2D maxilar = new RoundRectangle2D.Double(s * 0.28, s * 0.46, s * 0.44, s * 0.28, s * 0.14, s * 0.14);
            g2.fill(maxilar);

            // Órbitas oculares (vazadas, na cor de sombra).
            g2.setColor(corSombra);
            g2.fill(new Ellipse2D.Double(s * 0.30, s * 0.28, s * 0.16, s * 0.20));
            g2.fill(new Ellipse2D.Double(s * 0.54, s * 0.28, s * 0.16, s * 0.20));

            // Nariz (pequeno triângulo invertido).
            Path2D nariz = new Path2D.Double();
            nariz.moveTo(s * 0.50, s * 0.44);
            nariz.lineTo(s * 0.44, s * 0.54);
            nariz.lineTo(s * 0.56, s * 0.54);
            nariz.closePath();
            g2.fill(nariz);

            // Dentes (linhas verticais curtas na maxilar).
            g2.setStroke(new BasicStroke((float) (s * 0.035)));
            for (int i = 0; i < 3; i++) {
                double x = s * 0.38 + i * s * 0.13;
                g2.draw(new Line2D.Double(x, s * 0.60, x, s * 0.70));
            }
        });
    }

    /** Adaga das Sombras: lâmina + guarda + cabo, "fincada" — skin de bandeira padrão do tema. */
    public static Icon adaga(Color cor, int tamanho) {
        return criar(tamanho, (g2, t) -> {
            double s = t;
            g2.setColor(cor);

            // Lâmina: losango alongado apontando para cima (mais larga que antes,
            // para continuar legível em células pequenas).
            Path2D lamina = new Path2D.Double();
            lamina.moveTo(s * 0.50, s * 0.04);
            lamina.lineTo(s * 0.68, s * 0.54);
            lamina.lineTo(s * 0.50, s * 0.64);
            lamina.lineTo(s * 0.32, s * 0.54);
            lamina.closePath();
            g2.fill(lamina);

            // Guarda (linha horizontal grossa).
            g2.setStroke(new BasicStroke((float) (s * 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(s * 0.22, s * 0.62, s * 0.78, s * 0.62));

            // Cabo.
            g2.fill(new RoundRectangle2D.Double(s * 0.42, s * 0.62, s * 0.16, s * 0.24, s * 0.06, s * 0.06));

            // Pomo.
            g2.fill(new Ellipse2D.Double(s * 0.38, s * 0.84, s * 0.24, s * 0.14));
        });
    }

    /** Estrela de 5 pontas. */
    public static Icon estrela(Color cor, int tamanho) {
        return criar(tamanho, (g2, t) -> {
            double s = t;
            double cx = s * 0.5, cy = s * 0.5;
            double raioExterno = s * 0.46, raioInterno = s * 0.19;
            Path2D estrela = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double angulo = Math.PI / 2 + i * Math.PI / 5;
                double raio = (i % 2 == 0) ? raioExterno : raioInterno;
                double px = cx - raio * Math.cos(angulo);
                double py = cy - raio * Math.sin(angulo);
                if (i == 0) estrela.moveTo(px, py);
                else estrela.lineTo(px, py);
            }
            estrela.closePath();
            g2.setColor(cor);
            g2.fill(estrela);
        });
    }

    /** Coração desenhado com duas curvas de Bézier. */
    public static Icon coracao(Color cor, int tamanho) {
        return criar(tamanho, (g2, t) -> {
            double s = t;
            Path2D coracao = new Path2D.Double();
            coracao.moveTo(s * 0.5, s * 0.82);
            coracao.curveTo(s * 0.05, s * 0.52, s * 0.14, s * 0.10, s * 0.5, s * 0.30);
            coracao.curveTo(s * 0.86, s * 0.10, s * 0.95, s * 0.52, s * 0.5, s * 0.82);
            coracao.closePath();
            g2.setColor(cor);
            g2.fill(coracao);
        });
    }

    /** Alfinete de mapa (cabeça redonda + ponta). */
    public static Icon alfinete(Color cor, int tamanho) {
        return criar(tamanho, (g2, t) -> {
            double s = t;
            g2.setColor(cor);

            Path2D corpo = new Path2D.Double();
            corpo.moveTo(s * 0.5, s * 0.92);
            corpo.curveTo(s * 0.5, s * 0.70, s * 0.20, s * 0.62, s * 0.20, s * 0.36);
            corpo.curveTo(s * 0.20, s * 0.16, s * 0.80, s * 0.16, s * 0.80, s * 0.36);
            corpo.curveTo(s * 0.80, s * 0.62, s * 0.5, s * 0.70, s * 0.5, s * 0.92);
            corpo.closePath();
            g2.fill(corpo);

            g2.setColor(new Color(255, 255, 255, 160));
            g2.fill(new Ellipse2D.Double(s * 0.36, s * 0.24, s * 0.28, s * 0.28));
        });
    }

    /** Bandeira clássica de triângulo em um mastro — reconhecível do Campo Minado tradicional. */
    public static Icon bandeiraClassica(Color cor, int tamanho) {
        return criar(tamanho, (g2, t) -> {
            double s = t;
            g2.setColor(new Color(120, 90, 40));
            g2.fill(new RoundRectangle2D.Double(s * 0.46, s * 0.14, s * 0.08, s * 0.74, s * 0.04, s * 0.04));

            Path2D triangulo = new Path2D.Double();
            triangulo.moveTo(s * 0.54, s * 0.16);
            triangulo.lineTo(s * 0.86, s * 0.30);
            triangulo.lineTo(s * 0.54, s * 0.46);
            triangulo.closePath();
            g2.setColor(cor);
            g2.fill(triangulo);

            g2.setColor(new Color(60, 45, 20));
            g2.fill(new Ellipse2D.Double(s * 0.36, s * 0.84, s * 0.28, s * 0.09));
        });
    }

    /** Roteia a skin de bandeira escolhida para o ícone correspondente. */
    public static Icon paraSkinBandeira(SkinBandeira skin, Color cor, int tamanho) {
        switch (skin) {
            case ESTRELA:
                return estrela(cor, tamanho);
            case CORACAO:
                return coracao(cor, tamanho);
            case ALFINETE:
                return alfinete(cor, tamanho);
            case BANDEIRA_CLASSICA:
                return bandeiraClassica(cor, tamanho);
            case ADAGA_SOMBRA:
            default:
                return adaga(cor, tamanho);
        }
    }
}
