package util;

import model.LeituraTabuleiro;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Item 🔴 (dificuldade desafiadora) escolhido da lista: sugestão 38 —
 * "Solver automático — algoritmo que resolve o tabuleiro sozinho".
 * <p>
 * Tematizado como <b>O Sistema</b> assumindo o controle da missão: ele
 * "enxerga" exatamente o que um jogador humano enxergaria (apenas células
 * já reveladas, seus números e as bandeiras já colocadas) e aplica dedução
 * lógica local — nunca olha para onde as minas realmente estão. Por isso,
 * o Sistema é <i>justo</i>: só afirma "seguro" ou "mina certa" quando a
 * lógica garante isso, e admite quando fica em impasse (nesse caso, uma
 * jogada aleatória entre as candidatas restantes é necessária, como um
 * jogador humano precisaria fazer).
 * <p>
 * Fica no pacote {@code util} (e não em {@code model}) porque é um
 * serviço de apoio ao Controller, não uma regra de domínio do tabuleiro —
 * ele consome o Model apenas através da interface somente-leitura
 * {@link LeituraTabuleiro}, exatamente como a View faz.
 */
public class SolverAutomatico {

    /** Resultado de uma rodada de análise do Sistema. */
    public static class ResultadoAnalise {
        public final List<int[]> celulasSegurasParaRevelar = new ArrayList<>();
        public final List<int[]> celulasCertasDeMina = new ArrayList<>();
        public boolean impasse;
    }

    /**
     * Analisa o estado atual do tabuleiro e retorna todas as deduções
     * certas que consegue fazer nesta rodada (pode ser mais de uma célula
     * de cada vez). Se não encontrar nenhuma dedução certa, marca
     * {@code impasse = true}.
     */
    public ResultadoAnalise analisar(LeituraTabuleiro leitura) {
        ResultadoAnalise resultado = new ResultadoAnalise();
        Set<Long> jaSeguras = new LinkedHashSet<>();
        Set<Long> jaMinas = new LinkedHashSet<>();

        int linhas = leitura.getLinhas();
        int colunas = leitura.getColunas();

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (!leitura.isRevelada(i, j) || leitura.isMinada(i, j)) {
                    continue;
                }
                int numero = leitura.getMinasVizinhas(i, j);
                if (numero == 0) {
                    continue;
                }

                List<int[]> vizinhosOcultos = new ArrayList<>();
                int bandeirasVizinhas = 0;
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0) {
                            continue;
                        }
                        int vi = i + di;
                        int vj = j + dj;
                        if (vi < 0 || vi >= linhas || vj < 0 || vj >= colunas) {
                            continue;
                        }
                        if (leitura.isMarcada(vi, vj)) {
                            bandeirasVizinhas++;
                        } else if (!leitura.isRevelada(vi, vj)) {
                            vizinhosOcultos.add(new int[]{vi, vj});
                        }
                    }
                }

                if (vizinhosOcultos.isEmpty()) {
                    continue;
                }

                // Regra 1: se as bandeiras ao redor já somam o número da célula,
                // todo o resto oculto ao redor é seguro.
                if (bandeirasVizinhas == numero) {
                    for (int[] vizinho : vizinhosOcultos) {
                        long chave = chaveDe(vizinho[0], vizinho[1], colunas);
                        if (jaSeguras.add(chave)) {
                            resultado.celulasSegurasParaRevelar.add(vizinho);
                        }
                    }
                }
                // Regra 2: se bandeiras + ocultos ao redor bate exatamente com o
                // número, todos os ocultos restantes são minas com certeza.
                else if (bandeirasVizinhas + vizinhosOcultos.size() == numero) {
                    for (int[] vizinho : vizinhosOcultos) {
                        long chave = chaveDe(vizinho[0], vizinho[1], colunas);
                        if (jaMinas.add(chave)) {
                            resultado.celulasCertasDeMina.add(vizinho);
                        }
                    }
                }
            }
        }

        resultado.impasse = resultado.celulasSegurasParaRevelar.isEmpty()
                && resultado.celulasCertasDeMina.isEmpty();
        return resultado;
    }

    /**
     * Quando o Sistema chega a um impasse lógico, escolhe uma célula
     * oculta e não marcada ao acaso entre as restantes — a mesma situação
     * de risco que um jogador humano enfrentaria.
     */
    public int[] escolherPalpiteAoAcaso(LeituraTabuleiro leitura) {
        List<int[]> candidatas = new ArrayList<>();
        for (int i = 0; i < leitura.getLinhas(); i++) {
            for (int j = 0; j < leitura.getColunas(); j++) {
                if (!leitura.isRevelada(i, j) && !leitura.isMarcada(i, j)) {
                    candidatas.add(new int[]{i, j});
                }
            }
        }
        if (candidatas.isEmpty()) {
            return null;
        }
        return candidatas.get((int) (Math.random() * candidatas.size()));
    }

    private long chaveDe(int linha, int coluna, int colunas) {
        return (long) linha * colunas + coluna;
    }
}
