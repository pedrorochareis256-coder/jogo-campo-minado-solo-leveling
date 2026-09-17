package main;

import controller.CampoMinadoController;
import view.CampoMinadoView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do jogo com interface gráfica.
 * <p>
 * Esta classe não tem lógica nenhuma — só monta a arquitetura MVC:
 * cria a View e o Controller e deixa o Controller assumir a partir daí.
 * O Model ({@code Tabuleiro}) só é criado quando o jogador escolhe uma
 * dificuldade, dentro do Controller.
 */
public class JogoCampoMinadoGUI {

    public static void main(String[] args) {
        try {
            // Usar o Look and Feel do SISTEMA (ex.: Aqua no macOS) faz o
            // próprio SO pintar os JButtons, ignorando as cores que
            // definimos via setBackground/setForeground — é por isso que
            // as células apareciam com fundo branco e texto cinza.
            // O Metal (cross-platform) sempre respeita as cores que a
            // gente define, então o visual fica igual em qualquer SO.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Se por algum motivo não estiver disponível, segue com o padrão.
        }

        SwingUtilities.invokeLater(() -> {
            CampoMinadoView view = new CampoMinadoView();
            CampoMinadoController controller = new CampoMinadoController(view);
            controller.iniciar();
        });
    }
}
