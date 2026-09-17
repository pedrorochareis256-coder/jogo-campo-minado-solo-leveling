package main;

import model.Tabuleiro;
import util.SolverAutomatico;

import java.util.List;
import java.util.Scanner;

/**
 * Loop principal do jogo, via console. Esta classe só conversa com
 * {@link Tabuleiro} — nunca acessa {@code Celula} diretamente, respeitando
 * o encapsulamento sugerido na arquitetura do enunciado.
 * <p>
 * Mantida como alternativa de execução sem interface gráfica; aceita
 * também o comando "s" para pedir que O Sistema (o solver automático)
 * deduza uma jogada segura sozinho.
 */
public class JogoCampoMinado {

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        System.out.println("=== CAMPO MINADO: SOLO LEVELING (modo console) ===");
        System.out.println("O Sistema registrou sua entrada no Portal.\n");

        int linhas = lerInteiro(teclado, "Número de linhas: ");
        int colunas = lerInteiro(teclado, "Número de colunas: ");
        int minas = lerInteiro(teclado, "Número de minas: ");

        Tabuleiro tabuleiro = new Tabuleiro(linhas, colunas, minas);
        SolverAutomatico sistema = new SolverAutomatico();

        while (!tabuleiro.isJogoEncerrado()) {
            tabuleiro.imprimir(false);
            System.out.println();
            System.out.println("Comandos: 'r linha coluna' revelar | 'm linha coluna' marcar | 's' O Sistema deduz por você");
            System.out.print("> ");

            String comando = teclado.next();

            if (comando.equalsIgnoreCase("s")) {
                aplicarPassoDoSistema(tabuleiro, sistema);
                continue;
            }

            int linha = teclado.nextInt();
            int coluna = teclado.nextInt();

            if (comando.equalsIgnoreCase("r")) {
                tabuleiro.revelar(linha, coluna);
            } else if (comando.equalsIgnoreCase("m")) {
                tabuleiro.alternarMarcacao(linha, coluna);
            } else {
                System.out.println("Comando inválido. Use 'r', 'm' ou 's'.");
            }
        }

        tabuleiro.imprimir(true);
        if (tabuleiro.isDerrota()) {
            System.out.println("\nVocê foi derrotado. O Portal se fecha.");
        } else {
            System.out.println("\nLEVEL UP! Missão concluída — todas as células seguras foram reveladas.");
        }

        teclado.close();
    }

    private static void aplicarPassoDoSistema(Tabuleiro tabuleiro, SolverAutomatico sistema) {
        SolverAutomatico.ResultadoAnalise resultado = sistema.analisar(tabuleiro);

        if (resultado.impasse) {
            System.out.println("O Sistema: nenhuma dedução certa disponível — é preciso arriscar.");
            return;
        }

        for (int[] posicao : resultado.celulasCertasDeMina) {
            if (!tabuleiro.getCelula(posicao[0], posicao[1]).isMarcada()) {
                tabuleiro.alternarMarcacao(posicao[0], posicao[1]);
            }
        }
        for (int[] posicao : resultado.celulasSegurasParaRevelar) {
            List<int[]> reveladas = tabuleiro.revelar(posicao[0], posicao[1]);
            if (!reveladas.isEmpty()) {
                System.out.println("O Sistema revelou (" + posicao[0] + "," + posicao[1] + ") com segurança.");
            }
        }
    }

    private static int lerInteiro(Scanner teclado, String mensagem) {
        System.out.print(mensagem);
        return teclado.nextInt();
    }
}
