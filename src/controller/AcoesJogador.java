package controller;

import util.ConfigPartida;

/**
 * Contrato de ações que a View dispara em resposta à interação do
 * jogador. Quem implementa esta interface é sempre o Controller — a View
 * nunca decide o que essas ações significam, apenas avisa que elas
 * aconteceram.
 */
public interface AcoesJogador {

    /** Disparado quando o jogador escolhe uma dificuldade/modo na tela inicial. */
    void aoEscolherDificuldade(ConfigPartida config);

    /** Disparado no clique esquerdo (ou Espaço, via teclado) sobre uma célula: revelar. */
    void aoRevelarCelula(int linha, int coluna);

    /** Disparado no clique direito (ou F, via teclado) sobre uma célula: marcar/desmarcar. */
    void aoMarcarCelula(int linha, int coluna);

    /** Disparado quando o jogador pede para voltar à tela inicial. */
    void aoPedirNovoJogo();

    /** Botão de dica: revela uma célula segura aleatória (sugestão 23). */
    void aoPedirDica();

    /** "O Sistema" analisa e aplica um único passo de dedução lógica (sugestão 38, item 🔴). */
    void aoPedirPassoDoSistema();

    /** "O Sistema" resolve a partida inteira, passo a passo, de forma animada (sugestão 38, item 🔴). */
    void aoPedirSistemaResolverTudo();

    /** Exporta o histórico de partidas do perfil ativo para um arquivo CSV (sugestão 32). */
    void aoExportarHistoricoCsv();

    /** Gera o texto de "compartilhar resultado" da última partida (sugestão 44). */
    String gerarTextoCompartilhamento();

    /** Reproduz a última partida jogada, jogada a jogada (sugestão 41). */
    void aoReproduzirUltimoReplay();

    /** Troca o perfil de jogador ativo (sugestão 33). */
    void aoTrocarPerfil(String nomePerfil);

    /** Cria um novo perfil de jogador (sugestão 33). */
    void aoCriarPerfil(String nomePerfil);

    /** Variante de {@link #aoRevelarCelula} para o Modo Dois Jogadores (sugestão 43): {@code jogador} é 1 ou 2. */
    void aoRevelarCelulaJogador(int jogador, int linha, int coluna);

    /** Variante de {@link #aoMarcarCelula} para o Modo Dois Jogadores (sugestão 43). */
    void aoMarcarCelulaJogador(int jogador, int linha, int coluna);
}
