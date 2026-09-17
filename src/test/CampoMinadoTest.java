package test;

import model.Celula;
import model.Tabuleiro;
import org.junit.jupiter.api.Test;
import util.SolverAutomatico;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários do MODEL e do solver.
 * <p>
 * Todos os testes usam o construtor de {@link Tabuleiro} que recebe as
 * posições das minas explicitamente (ou uma semente fixa), para que o
 * resultado seja determinístico — sem depender do sorteio aleatório.
 */
public class CampoMinadoTest {

    // ================================================================
    // Regras clássicas
    // ================================================================

    @Test
    void testContagemDeMinasVizinhas() {
        int[][] minas = {{1, 1}};
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(1, tabuleiro.getCelula(0, 0).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(0, 1).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(2, 2).getMinasVizinhas());
        assertEquals(0, tabuleiro.getCelula(1, 1).getMinasVizinhas());
    }

    @Test
    void testContagemDeMinasVizinhasComDuasMinasAdjacentes() {
        int[][] minas = {{0, 0}, {0, 1}};
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        // (1,0) toca as duas minas: (0,0) em cima e (0,1) na diagonal.
        assertEquals(2, tabuleiro.getCelula(1, 0).getMinasVizinhas());
        // ATENÇÃO: o teste original do projeto afirmava 2 aqui, mas (0,2) só
        // é vizinha de (0,1) — a mina em (0,0) está a duas casas de distância.
        // O valor correto é 1.
        assertEquals(1, tabuleiro.getCelula(0, 2).getMinasVizinhas());
        // (2,2) não toca nenhuma das duas minas da linha 0.
        assertEquals(0, tabuleiro.getCelula(2, 2).getMinasVizinhas());
    }

    @Test
    void testCascataRevelaTodasAsCelulasSemMinasProximas() {
        Tabuleiro tabuleiro = new Tabuleiro(4, 4, new int[][]{});
        tabuleiro.revelar(0, 0);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertTrue(tabuleiro.getCelula(i, j).isRevelada(),
                        "Célula (" + i + "," + j + ") deveria ter sido revelada pela cascata");
            }
        }
    }

    @Test
    void testCascataNaoRevelaMinaNemPassaDelaAdiante() {
        Tabuleiro tabuleiro = new Tabuleiro(5, 5, new int[][]{{2, 2}});
        tabuleiro.revelar(0, 0);

        assertFalse(tabuleiro.getCelula(2, 2).isRevelada(),
                "A célula minada nunca deve ser revelada pela cascata");
        assertTrue(tabuleiro.getCelula(1, 2).isRevelada());
    }

    @Test
    void testRevelarCelulaMinadaEncerraOJogoComDerrota() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{1, 1}});
        tabuleiro.revelar(1, 1);

        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertTrue(tabuleiro.getCelula(1, 1).isRevelada());
        assertTrue(tabuleiro.getCelula(1, 1).isExplodida(),
                "A mina clicada deve ficar marcada como a que explodiu");
    }

    @Test
    void testCelulaMarcadaNaoPodeSerRevelada() {
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, new int[][]{});
        tabuleiro.alternarMarcacao(0, 0);
        tabuleiro.revelar(0, 0);

        assertFalse(tabuleiro.getCelula(0, 0).isRevelada(),
                "Uma célula marcada com bandeira não deve ser revelada");
    }

    @Test
    void testVerificarVitoriaQuandoTodasAsCelulasSegurasForamReveladas() {
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, new int[][]{{0, 0}});
        assertFalse(tabuleiro.verificarVitoria());

        tabuleiro.revelar(0, 1);
        tabuleiro.revelar(1, 0);
        tabuleiro.revelar(1, 1);

        assertTrue(tabuleiro.verificarVitoria());
        assertTrue(tabuleiro.isJogoEncerrado());
        assertFalse(tabuleiro.isDerrota());
    }

    @Test
    void testVerificarVitoriaEhFalsaEnquantoHouverCelulaSeguraNaoRevelada() {
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, new int[][]{{0, 0}});
        tabuleiro.revelar(0, 1);
        assertFalse(tabuleiro.verificarVitoria());
    }

    // ================================================================
    // Marcação em três estados (sugestão 20)
    // ================================================================

    @Test
    void testCicloDeMarcacaoNenhumaBandeiraInterrogacao() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{0, 0}});
        Celula celula = tabuleiro.getCelula(1, 1);

        assertEquals(Celula.EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());

        tabuleiro.alternarMarcacao(1, 1);
        assertTrue(celula.isMarcada());
        assertFalse(celula.isInterrogada());

        tabuleiro.alternarMarcacao(1, 1);
        assertFalse(celula.isMarcada());
        assertTrue(celula.isInterrogada());

        tabuleiro.alternarMarcacao(1, 1);
        assertEquals(Celula.EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());
    }

    @Test
    void testCelulaInterrogadaPodeSerRevelada() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{2, 2}});
        tabuleiro.alternarMarcacao(0, 0); // bandeira
        tabuleiro.alternarMarcacao(0, 0); // interrogação
        tabuleiro.revelar(0, 0);

        assertTrue(tabuleiro.getCelula(0, 0).isRevelada(),
                "A interrogação não bloqueia a revelação, diferente da bandeira");
    }

    @Test
    void testCelulaReveladaNaoAceitaMarcacao() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{2, 2}});
        tabuleiro.revelar(0, 0);
        tabuleiro.alternarMarcacao(0, 0);

        assertFalse(tabuleiro.getCelula(0, 0).isMarcada());
        assertFalse(tabuleiro.getCelula(0, 0).isInterrogada());
    }

    // ================================================================
    // Modo sem cascata (sugestão 12)
    // ================================================================

    @Test
    void testModoSemCascataRevelaApenasACelulaClicada() {
        Tabuleiro tabuleiro = new Tabuleiro(4, 4, new int[][]{});
        tabuleiro.revelar(0, 0, false);

        int reveladas = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tabuleiro.getCelula(i, j).isRevelada()) {
                    reveladas++;
                }
            }
        }
        assertEquals(1, reveladas, "Sem cascata, apenas a célula clicada é revelada");
    }

    // ================================================================
    // Modo 3 vidas (sugestão 17)
    // ================================================================

    @Test
    void testModoTresVidasNaoEncerraNoPrimeiroAcertoDeMina() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{0, 0}, {0, 1}, {0, 2}});
        tabuleiro.setVidasMaximas(3);

        tabuleiro.revelar(0, 0);
        assertFalse(tabuleiro.isJogoEncerrado());
        assertEquals(2, tabuleiro.getVidasRestantes());

        tabuleiro.revelar(0, 1);
        assertFalse(tabuleiro.isJogoEncerrado());
        assertEquals(1, tabuleiro.getVidasRestantes());

        tabuleiro.revelar(0, 2);
        assertTrue(tabuleiro.isJogoEncerrado(), "A terceira mina esgota as vidas e encerra o jogo");
        assertTrue(tabuleiro.isDerrota());
        assertEquals(0, tabuleiro.getVidasRestantes());
    }

    @Test
    void testSemModoDeVidasUmaMinaJaEncerra() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{0, 0}});
        assertEquals(1, tabuleiro.getVidasRestantes());

        tabuleiro.revelar(0, 0);
        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
    }

    // ================================================================
    // Tabuleiro toroidal (sugestão 18)
    // ================================================================

    @Test
    void testTabuleiroToroidalContaVizinhasAtravessandoAsBordas() {
        // Tabuleiro 3x3 toroidal com uma única mina no canto (0,0).
        // No modo normal, (2,2) não seria vizinha de (0,0). No toroidal, é.
        Tabuleiro toroidal = new Tabuleiro(3, 3, 1, semearComMinaNoCanto(), true);
        // Como o construtor com seed sorteia, usamos um caminho determinístico:
        // verificamos apenas a propriedade estrutural do modo.
        assertTrue(toroidal.isToroidal());

        int somaVizinhas = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                somaVizinhas += toroidal.getMinasVizinhas(i, j);
            }
        }
        // Num toroidal 3x3, toda célula é vizinha de todas as outras 8,
        // então uma única mina é contada por todas as 8 células restantes.
        assertEquals(8, somaVizinhas,
                "No toroidal 3x3, a única mina deve ser vizinha de todas as outras células");
    }

    private long semearComMinaNoCanto() {
        // Qualquer semente serve: o teste acima verifica uma propriedade
        // que vale para qualquer posição da mina num toroidal 3x3.
        return 12345L;
    }

    @Test
    void testTabuleiroNaoToroidalNaoConectaBordas() {
        Tabuleiro normal = new Tabuleiro(3, 3, new int[][]{{0, 0}});
        assertFalse(normal.isToroidal());
        assertEquals(0, normal.getMinasVizinhas(2, 2),
                "Sem toroidal, o canto oposto não enxerga a mina");
    }

    // ================================================================
    // Semente fixa / Portal Diário (sugestão 49)
    // ================================================================

    @Test
    void testMesmaSementeGeraOMesmoTabuleiro() {
        Tabuleiro primeiro = new Tabuleiro(9, 9, 10, 2026L);
        Tabuleiro segundo = new Tabuleiro(9, 9, 10, 2026L);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertEquals(primeiro.isMinada(i, j), segundo.isMinada(i, j),
                        "A mesma semente deve produzir exatamente o mesmo campo de minas");
            }
        }
    }

    @Test
    void testSementesDiferentesGeramTabuleirosDiferentes() {
        Tabuleiro primeiro = new Tabuleiro(16, 16, 40, 1L);
        Tabuleiro segundo = new Tabuleiro(16, 16, 40, 2L);

        boolean encontrouDiferenca = false;
        for (int i = 0; i < 16 && !encontrouDiferenca; i++) {
            for (int j = 0; j < 16; j++) {
                if (primeiro.isMinada(i, j) != segundo.isMinada(i, j)) {
                    encontrouDiferenca = true;
                    break;
                }
            }
        }
        assertTrue(encontrouDiferenca);
    }

    // ================================================================
    // Modo Relâmpago (sugestão 16)
    // ================================================================

    @Test
    void testModoRelampagoNuncaRevelaUmaMina() {
        Tabuleiro tabuleiro = new Tabuleiro(12, 12, 20, 777L);
        tabuleiro.revelarAleatoriasSeguras(8, new Random(777L));

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                if (tabuleiro.isMinada(i, j)) {
                    assertFalse(tabuleiro.isRevelada(i, j),
                            "A revelação relâmpago só pode abrir células seguras");
                }
            }
        }
    }

    @Test
    void testModoRelampagoRevelaAlgumaCoisa() {
        Tabuleiro tabuleiro = new Tabuleiro(12, 12, 20, 555L);
        assertFalse(tabuleiro.revelarAleatoriasSeguras(5, new Random(555L)).isEmpty());
    }

    // ================================================================
    // Solver automático — "O Sistema" (sugestão 38, item desafiador)
    // ================================================================

    @Test
    void testSolverDeduzCelulasSegurasQuandoAsBandeirasFecham() {
        // Mina única em (0,0) de um 3x3. Revelando (1,1), ela mostra "1".
        // Marcando (0,0) com bandeira, todo o resto ao redor de (1,1) é seguro.
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][]{{0, 0}});
        tabuleiro.revelar(1, 1, false);
        tabuleiro.alternarMarcacao(0, 0);

        SolverAutomatico sistema = new SolverAutomatico();
        SolverAutomatico.ResultadoAnalise resultado = sistema.analisar(tabuleiro);

        assertFalse(resultado.impasse);
        assertFalse(resultado.celulasSegurasParaRevelar.isEmpty(),
                "Com a bandeira fechando a conta, o Sistema deve achar células seguras");
    }

    @Test
    void testSolverDeduzMinaCertaQuandoOsOcultosFechamONumero() {
        // Mina em (0,0). Revelando (0,1) — que mostra "1" — e sendo (0,0)
        // a única célula oculta relevante ao redor dela, o Sistema conclui mina.
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, new int[][]{{0, 0}});
        tabuleiro.revelar(0, 1, false);
        tabuleiro.revelar(1, 0, false);
        tabuleiro.revelar(1, 1, false);

        SolverAutomatico sistema = new SolverAutomatico();
        SolverAutomatico.ResultadoAnalise resultado = sistema.analisar(tabuleiro);

        assertFalse(resultado.celulasCertasDeMina.isEmpty(),
                "O Sistema deve concluir que a única célula oculta restante é mina");
        int[] deduzida = resultado.celulasCertasDeMina.get(0);
        assertEquals(0, deduzida[0]);
        assertEquals(0, deduzida[1]);
    }

    @Test
    void testSolverNuncaMarcaComoSeguraUmaCelulaQueEhMina() {
        // Propriedade central de correção: em vários tabuleiros aleatórios,
        // nenhuma célula apontada como "segura" pelo Sistema pode ser mina.
        SolverAutomatico sistema = new SolverAutomatico();

        for (long semente = 0; semente < 40; semente++) {
            Tabuleiro tabuleiro = new Tabuleiro(9, 9, 10, semente);
            tabuleiro.revelarAleatoriasSeguras(3, new Random(semente));

            SolverAutomatico.ResultadoAnalise resultado = sistema.analisar(tabuleiro);
            for (int[] posicao : resultado.celulasSegurasParaRevelar) {
                assertFalse(tabuleiro.isMinada(posicao[0], posicao[1]),
                        "O Sistema apontou como segura uma célula minada (semente " + semente + ")");
            }
            for (int[] posicao : resultado.celulasCertasDeMina) {
                assertTrue(tabuleiro.isMinada(posicao[0], posicao[1]),
                        "O Sistema apontou como mina uma célula segura (semente " + semente + ")");
            }
        }
    }

    @Test
    void testSolverAdmiteImpasseEmTabuleiroIntocado() {
        Tabuleiro tabuleiro = new Tabuleiro(9, 9, 10, 99L);
        SolverAutomatico sistema = new SolverAutomatico();
        SolverAutomatico.ResultadoAnalise resultado = sistema.analisar(tabuleiro);

        assertTrue(resultado.impasse,
                "Sem nenhuma célula revelada, não há dedução possível — o Sistema deve admitir impasse");
    }

    @Test
    void testPalpiteDoSolverSempreCaiEmCelulaOcultaENaoMarcada() {
        Tabuleiro tabuleiro = new Tabuleiro(9, 9, 10, 31L);
        tabuleiro.revelarAleatoriasSeguras(4, new Random(31L));
        tabuleiro.alternarMarcacao(0, 0);

        SolverAutomatico sistema = new SolverAutomatico();
        int[] palpite = sistema.escolherPalpiteAoAcaso(tabuleiro);

        assertNotNull(palpite);
        assertFalse(tabuleiro.isRevelada(palpite[0], palpite[1]));
        assertFalse(tabuleiro.isMarcada(palpite[0], palpite[1]));
    }

    // ================================================================
    // Validação de parâmetros
    // ================================================================

    @Test
    void testTabuleiroRejeitaDimensoesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(0, 5, 1));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(5, 0, 1));
    }

    @Test
    void testTabuleiroRejeitaQuantidadeDeMinasInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(3, 3, 9));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(3, 3, -1));
    }
}
