package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa o tabuleiro do Campo Minado: uma matriz bidimensional de
 * {@link Celula}. É a única classe que conhece a grade inteira, que sabe
 * posicionar minas e calcular vizinhança.
 * <p>
 * Parte do MODEL na arquitetura MVC. Implementa {@link LeituraTabuleiro}
 * para que a View possa consultar o estado do jogo sem depender da API
 * completa (mutável) desta classe.
 * <p>
 * Além da regra clássica, esta classe suporta algumas variantes usadas
 * pelos "modos de missão" do jogo (tema Solo Leveling): tabuleiro
 * toroidal (as bordas se conectam, sugestão 18), várias "vidas" antes de
 * ser derrotado (sugestão 17), revelação sem efeito cascata (sugestão
 * 12) e revelação relâmpago de células seguras antes da primeira jogada
 * (sugestão 16). Também aceita uma semente (seed) fixa para permitir o
 * "Portal Diário" com o mesmo tabuleiro para todo mundo (sugestão 49).
 */
public class Tabuleiro implements LeituraTabuleiro {

    private final int linhas;
    private final int colunas;
    private final int numMinas;
    private final Celula[][] grade;
    private final boolean toroidal;

    private boolean jogoEncerrado;
    private boolean derrota;

    private int vidasMaximas = 1;
    private int vidasRestantes = 1;

    /**
     * Cria um tabuleiro novo com minas posicionadas aleatoriamente.
     */
    public Tabuleiro(int linhas, int colunas, int numMinas) {
        this(linhas, colunas, numMinas, new Random().nextLong(), false);
    }

    /** Cria um tabuleiro com semente fixa (usado pelo Portal Diário). */
    public Tabuleiro(int linhas, int colunas, int numMinas, long seed) {
        this(linhas, colunas, numMinas, seed, false);
    }

    /** Cria um tabuleiro com semente fixa e, opcionalmente, bordas toroidais. */
    public Tabuleiro(int linhas, int colunas, int numMinas, long seed, boolean toroidal) {
        if (linhas <= 0 || colunas <= 0) {
            throw new IllegalArgumentException("Linhas e colunas devem ser maiores que zero.");
        }
        if (numMinas < 0 || numMinas >= linhas * colunas) {
            throw new IllegalArgumentException("Número de minas inválido para esse tabuleiro.");
        }

        this.linhas = linhas;
        this.colunas = colunas;
        this.numMinas = numMinas;
        this.toroidal = toroidal;
        this.grade = new Celula[linhas][colunas];
        inicializarGrade();
        posicionarMinasAleatoriamente(new Random(seed));
        calcularMinasVizinhasDeTodasAsCelulas();
    }

    /**
     * Construtor auxiliar que recebe as posições das minas explicitamente,
     * em vez de sortear. Pensado para ser usado em testes unitários, onde
     * é preciso saber exatamente onde as minas estão para verificar o
     * comportamento da cascata e da contagem de vizinhas.
     */
    public Tabuleiro(int linhas, int colunas, int[][] posicoesMinas) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.numMinas = posicoesMinas.length;
        this.toroidal = false;
        this.grade = new Celula[linhas][colunas];
        inicializarGrade();
        for (int[] posicao : posicoesMinas) {
            grade[posicao[0]][posicao[1]].setMinada(true);
        }
        calcularMinasVizinhasDeTodasAsCelulas();
    }

    private void inicializarGrade() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                grade[i][j] = new Celula();
            }
        }
    }

    private void posicionarMinasAleatoriamente(Random sorteio) {
        int minasColocadas = 0;
        while (minasColocadas < numMinas) {
            int linha = sorteio.nextInt(linhas);
            int coluna = sorteio.nextInt(colunas);
            if (!grade[linha][coluna].isMinada()) {
                grade[linha][coluna].setMinada(true);
                minasColocadas++;
            }
        }
    }

    private void calcularMinasVizinhasDeTodasAsCelulas() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                grade[i][j].setMinasVizinhas(contarMinasVizinhas(i, j));
            }
        }
    }

    private int contarMinasVizinhas(int linha, int coluna) {
        int total = 0;
        for (int[] vizinho : vizinhosDe(linha, coluna)) {
            if (grade[vizinho[0]][vizinho[1]].isMinada()) {
                total++;
            }
        }
        return total;
    }

    /**
     * Retorna as coordenadas vizinhas válidas de uma célula. Em modo
     * toroidal (sugestão 18), a borda "dá a volta" — a coluna 0 é vizinha
     * da última coluna, e a linha 0 é vizinha da última linha.
     */
    private List<int[]> vizinhosDe(int linha, int coluna) {
        List<int[]> vizinhos = new ArrayList<>();
        for (int deltaLinha = -1; deltaLinha <= 1; deltaLinha++) {
            for (int deltaColuna = -1; deltaColuna <= 1; deltaColuna++) {
                if (deltaLinha == 0 && deltaColuna == 0) {
                    continue;
                }
                int vizinhoLinha = linha + deltaLinha;
                int vizinhoColuna = coluna + deltaColuna;
                if (toroidal) {
                    vizinhoLinha = ((vizinhoLinha % linhas) + linhas) % linhas;
                    vizinhoColuna = ((vizinhoColuna % colunas) + colunas) % colunas;
                    vizinhos.add(new int[]{vizinhoLinha, vizinhoColuna});
                } else if (dentroDosLimites(vizinhoLinha, vizinhoColuna)) {
                    vizinhos.add(new int[]{vizinhoLinha, vizinhoColuna});
                }
            }
        }
        return vizinhos;
    }

    private boolean dentroDosLimites(int linha, int coluna) {
        return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
    }

    /** Define quantas vezes o jogador pode acertar uma mina antes de perder (sugestão 17: "3 vidas"). */
    public void setVidasMaximas(int vidas) {
        this.vidasMaximas = Math.max(1, vidas);
        this.vidasRestantes = this.vidasMaximas;
    }

    @Override
    public int getVidasRestantes() {
        return vidasRestantes;
    }

    /**
     * Revela a célula indicada. Se a célula não tiver minas vizinhas, o
     * efeito cascata revela automaticamente as células ao redor (e assim
     * sucessivamente), sem nunca revelar uma célula minada por engano.
     *
     * @return a lista das células que foram reveladas nesta jogada, na
     *         ordem em que foram reveladas.
     */
    public List<int[]> revelar(int linha, int coluna) {
        return revelar(linha, coluna, true);
    }

    /**
     * Revela a célula indicada. Quando {@code comCascata} é falso (modo
     * "sem cascata", sugestão 12), revela apenas a célula clicada mesmo
     * que ela não tenha minas vizinhas.
     */
    public List<int[]> revelar(int linha, int coluna, boolean comCascata) {
        List<int[]> ordemRevelacao = new ArrayList<>();

        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return ordemRevelacao;
        }

        Celula celulaInicial = grade[linha][coluna];
        if (celulaInicial.isRevelada() || celulaInicial.isMarcada()) {
            return ordemRevelacao;
        }

        if (celulaInicial.isMinada()) {
            celulaInicial.revelar();
            celulaInicial.marcarComoExplodida();
            ordemRevelacao.add(new int[]{linha, coluna});
            registrarAcertoDeMina();
            return ordemRevelacao;
        }

        if (!comCascata) {
            celulaInicial.revelar();
            ordemRevelacao.add(new int[]{linha, coluna});
            if (verificarVitoria()) {
                jogoEncerrado = true;
            }
            return ordemRevelacao;
        }

        List<int[]> pendentes = new ArrayList<>();
        pendentes.add(new int[]{linha, coluna});

        while (!pendentes.isEmpty()) {
            int[] posicaoAtual = pendentes.remove(pendentes.size() - 1);
            int linhaAtual = posicaoAtual[0];
            int colunaAtual = posicaoAtual[1];
            Celula atual = grade[linhaAtual][colunaAtual];

            if (atual.isRevelada() || atual.isMarcada() || atual.isMinada()) {
                continue;
            }

            atual.revelar();
            ordemRevelacao.add(new int[]{linhaAtual, colunaAtual});

            if (atual.getMinasVizinhas() == 0) {
                for (int[] vizinho : vizinhosDe(linhaAtual, colunaAtual)) {
                    Celula vizinha = grade[vizinho[0]][vizinho[1]];
                    if (!vizinha.isRevelada() && !vizinha.isMarcada() && !vizinha.isMinada()) {
                        pendentes.add(vizinho);
                    }
                }
            }
        }

        if (verificarVitoria()) {
            jogoEncerrado = true;
        }

        return ordemRevelacao;
    }

    /**
     * Trata o acerto de uma mina considerando o modo de vidas (sugestão
     * 17). Só encerra o jogo em derrota quando as vidas se esgotam.
     */
    private void registrarAcertoDeMina() {
        vidasRestantes = Math.max(0, vidasRestantes - 1);
        if (vidasRestantes <= 0) {
            jogoEncerrado = true;
            derrota = true;
        }
    }

    /**
     * Revela aleatoriamente algumas células seguras antes do jogo
     * "começar de verdade" — o Modo Relâmpago (sugestão 16), tematizado
     * como o Sistema concedendo uma visão rápida do campo antes da missão.
     */
    public List<int[]> revelarAleatoriasSeguras(int quantidade, Random aleatorio) {
        List<int[]> candidatas = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (!celula.isMinada() && !celula.isRevelada() && celula.getMinasVizinhas() == 0) {
                    candidatas.add(new int[]{i, j});
                }
            }
        }
        // Se não houver células "zero" suficientes, aceita qualquer célula segura.
        if (candidatas.size() < quantidade) {
            for (int i = 0; i < linhas; i++) {
                for (int j = 0; j < colunas; j++) {
                    Celula celula = grade[i][j];
                    if (!celula.isMinada() && !celula.isRevelada()) {
                        int[] pos = new int[]{i, j};
                        boolean jaListada = candidatas.stream().anyMatch(p -> p[0] == pos[0] && p[1] == pos[1]);
                        if (!jaListada) {
                            candidatas.add(pos);
                        }
                    }
                }
            }
        }
        java.util.Collections.shuffle(candidatas, aleatorio);

        List<int[]> reveladas = new ArrayList<>();
        int total = Math.min(quantidade, candidatas.size());
        for (int i = 0; i < total; i++) {
            int[] pos = candidatas.get(i);
            reveladas.addAll(revelar(pos[0], pos[1], true));
        }
        return reveladas;
    }

    /**
     * Marca ou desmarca uma célula com bandeira, sem revelá-la.
     */
    public void alternarMarcacao(int linha, int coluna) {
        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return;
        }
        grade[linha][coluna].alternarMarcacao();
    }

    /**
     * O jogo é vencido quando todas as células que não são minas já
     * foram reveladas.
     */
    public boolean verificarVitoria() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (!celula.isMinada() && !celula.isRevelada()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isDerrota() {
        return derrota;
    }

    @Override
    public boolean isJogoEncerrado() {
        return jogoEncerrado;
    }

    @Override
    public int getLinhas() {
        return linhas;
    }

    @Override
    public int getColunas() {
        return colunas;
    }

    public int getNumMinas() {
        return numMinas;
    }

    public boolean isToroidal() {
        return toroidal;
    }

    // ----- implementação de LeituraTabuleiro (usada pela View) -----

    @Override
    public boolean isRevelada(int linha, int coluna) {
        return grade[linha][coluna].isRevelada();
    }

    @Override
    public boolean isMarcada(int linha, int coluna) {
        return grade[linha][coluna].isMarcada();
    }

    @Override
    public boolean isInterrogada(int linha, int coluna) {
        return grade[linha][coluna].isInterrogada();
    }

    @Override
    public boolean isMinada(int linha, int coluna) {
        return grade[linha][coluna].isMinada();
    }

    @Override
    public boolean isExplodida(int linha, int coluna) {
        return grade[linha][coluna].isExplodida();
    }

    @Override
    public int getMinasVizinhas(int linha, int coluna) {
        return grade[linha][coluna].getMinasVizinhas();
    }

    /**
     * Retorna a célula em uma posição específica. Mantido para uso interno
     * do próprio Model e para os testes unitários — a View nunca deve
     * chamar este método diretamente; ela usa {@link LeituraTabuleiro}.
     */
    public Celula getCelula(int linha, int coluna) {
        return grade[linha][coluna];
    }

    /**
     * Imprime o tabuleiro no console. Quando revelarTudo é true (por
     * exemplo, ao final de uma derrota), mostra também as minas.
     */
    public void imprimir(boolean revelarTudo) {
        StringBuilder cabecalho = new StringBuilder("   ");
        for (int j = 0; j < colunas; j++) {
            cabecalho.append(String.format("%2d", j));
        }
        System.out.println(cabecalho);

        for (int i = 0; i < linhas; i++) {
            StringBuilder linhaTexto = new StringBuilder(String.format("%2d ", i));
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (revelarTudo && celula.isMinada()) {
                    linhaTexto.append(" *");
                } else {
                    linhaTexto.append(" ").append(celula);
                }
            }
            System.out.println(linhaTexto);
        }
    }
}
