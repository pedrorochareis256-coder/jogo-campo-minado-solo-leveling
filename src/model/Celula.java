package model;

/**
 * Representa uma única célula do tabuleiro de Campo Minado.
 * <p>
 * Parte do MODEL (na arquitetura MVC): não conhece Swing, não conhece a
 * View nem o Controller. Todo o estado (minada, revelada, marcação,
 * minasVizinhas) é privado; nenhuma classe externa altera esses valores
 * diretamente, apenas através dos métodos públicos abaixo.
 */
public class Celula {

    /**
     * Os três estados possíveis de marcação de uma célula ainda oculta,
     * em ciclo: NENHUMA -&gt; BANDEIRA -&gt; INTERROGACAO -&gt; NENHUMA.
     * (Sugestão 20 da lista de implementações.)
     */
    public enum EstadoMarcacao {
        NENHUMA, BANDEIRA, INTERROGACAO
    }

    private boolean minada;
    private boolean revelada;
    private EstadoMarcacao marcacao;
    private int minasVizinhas;
    private boolean explodida;

    public Celula() {
        this.minada = false;
        this.revelada = false;
        this.marcacao = EstadoMarcacao.NENHUMA;
        this.minasVizinhas = 0;
        this.explodida = false;
    }

    // ----- minada -----

    public boolean isMinada() {
        return minada;
    }

    public void setMinada(boolean minada) {
        this.minada = minada;
    }

    // ----- revelada -----

    public boolean isRevelada() {
        return revelada;
    }

    /**
     * Revela a célula. Uma célula com bandeira não pode ser revelada por
     * engano; é preciso desmarcá-la primeiro. Uma célula com interrogação
     * pode ser revelada normalmente (comportamento clássico).
     */
    public void revelar() {
        if (marcacao != EstadoMarcacao.BANDEIRA) {
            this.revelada = true;
        }
    }

    /** Marca esta célula como a mina que efetivamente explodiu (para destaque visual). */
    public void marcarComoExplodida() {
        this.explodida = true;
    }

    public boolean isExplodida() {
        return explodida;
    }

    // ----- marcação (bandeira / interrogação) -----

    public boolean isMarcada() {
        return marcacao == EstadoMarcacao.BANDEIRA;
    }

    public boolean isInterrogada() {
        return marcacao == EstadoMarcacao.INTERROGACAO;
    }

    public EstadoMarcacao getEstadoMarcacao() {
        return marcacao;
    }

    /**
     * Avança o ciclo de marcação: sem marcação -&gt; bandeira -&gt;
     * interrogação -&gt; sem marcação. Só é possível marcar uma célula
     * que ainda não foi revelada.
     */
    public void alternarMarcacao() {
        if (revelada) {
            return;
        }
        switch (marcacao) {
            case NENHUMA:
                marcacao = EstadoMarcacao.BANDEIRA;
                break;
            case BANDEIRA:
                marcacao = EstadoMarcacao.INTERROGACAO;
                break;
            default:
                marcacao = EstadoMarcacao.NENHUMA;
                break;
        }
    }

    // ----- minasVizinhas -----

    public int getMinasVizinhas() {
        return minasVizinhas;
    }

    public void setMinasVizinhas(int minasVizinhas) {
        this.minasVizinhas = minasVizinhas;
    }

    /**
     * Representação usada para exibir a célula no console.
     */
    @Override
    public String toString() {
        if (marcacao == EstadoMarcacao.BANDEIRA) {
            return "F";
        }
        if (marcacao == EstadoMarcacao.INTERROGACAO) {
            return "?";
        }
        if (!revelada) {
            return ".";
        }
        if (minada) {
            return "*";
        }
        if (minasVizinhas == 0) {
            return " ";
        }
        return String.valueOf(minasVizinhas);
    }
}
