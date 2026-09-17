package view;

import controller.AcoesJogador;
import model.LeituraTabuleiro;
import util.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * VIEW da arquitetura MVC: cuida só de desenhar a tela e capturar
 * interações do usuário. Nunca decide o que um clique "significa" em
 * termos de regra de jogo — ela apenas repassa o clique para quem
 * implementa {@link AcoesJogador} (o Controller) e espera ser chamada de
 * volta para atualizar o que aparece na tela.
 * <p>
 * Reskin temático "Solo Leveling" (Monarca das Sombras): paleta padrão
 * roxo/azul escuro com dourado, dificuldades renomeadas para Ranks de
 * Caçador, e a "voz do Sistema" usada nas mensagens de status. Nenhuma
 * arte, logotipo ou texto original da obra é reproduzido — apenas a
 * estética e os termos genéricos do gênero (rank, portal, caçador,
 * monarca, sistema).
 */
public class CampoMinadoView extends JFrame {

    private static final Font FONTE_CELULA_BASE = new Font("Segoe UI Emoji", Font.BOLD, 20);
    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_NUMERO = new Font("Consolas", Font.BOLD, 18);
    private static final Font FONTE_PEQUENA = new Font("Segoe UI", Font.PLAIN, 12);

    private static final String EMOJI_CAVEIRA = "\uD83D\uDC80";
    private static final String EMOJI_COROA = "\uD83D\uDC51";
    private static final String EMOJI_PORTAL = "\uD83C\uDF00";
    private static final String EMOJI_EXPLOSAO = "\uD83D\uDCA5";
    private static final String EMOJI_RELOGIO = "\u23F1";
    private static final String EMOJI_JOGADA = "\uD83D\uDC46";
    private static final String EMOJI_CORACAO = "\u2764";
    private static final String EMOJI_LAMPADA = "\uD83D\uDCA1";
    private static final String EMOJI_SISTEMA = "\uD83E\uDDE0";

    private static final Color[] CORES_NUMEROS = {
            null,
            new Color(90, 160, 255), new Color(90, 210, 130), new Color(230, 90, 90),
            new Color(60, 90, 220), new Color(170, 50, 50), new Color(40, 190, 200),
            new Color(230, 230, 230), new Color(160, 160, 170)
    };

    private AcoesJogador ouvinte;

    private JButton[][] botoes;
    private JButton[][] botoesP2;
    private JLabel labelStatus;

    private JLabel lblTempo;
    private JLabel lblMinasRestantes;
    private JLabel lblCelulasReveladas;
    private JLabel lblJogadas;
    private JLabel lblVidas;
    private JLabel lblDicas;
    private JLabel lblRecorde;
    private JLabel lblTurno;
    private JProgressBar barraProgresso;
    private JPanel painelTabuleiroWrapper;
    private JScrollPane scrollTabuleiro;

    // ---- controles da tela inicial (lidos no momento de iniciar a partida) ----
    private JComboBox<String> comboPerfil;
    private JComboBox<TemaVisual> comboTema;
    private JComboBox<SkinBandeira> comboSkinBandeira;
    private JComboBox<SkinNumeros> comboSkinNumeros;
    private JComboBox<String> comboTempo;
    private JCheckBox chkDaltonismo;
    private JCheckBox chkDeteccaoAutoTema;
    private JCheckBox chkSomEfeitos;
    private JCheckBox chkMusica;
    private JSlider sliderVolume;
    private JSlider sliderZoom;
    private JCheckBox chkSemCascata;
    private JCheckBox chkMinasVisiveis;
    private JCheckBox chkRelampago;
    private JCheckBox chkTresVidas;
    private JCheckBox chkToroidal;
    private JCheckBox chkCooperativo;
    private JCheckBox chkDoisJogadores;

    private TemaVisual tema = TemaVisual.monarcaDasSombras();
    private SkinBandeira skinBandeira = SkinBandeira.ADAGA_SOMBRA;
    private SkinNumeros skinNumeros = SkinNumeros.CLASSICO;
    private boolean simbolosDaltonismo = false;
    private int tamanhoCelula = 36;

    private int cursorLinha = 0;
    private int cursorColuna = 0;
    private int linhasAtuais;
    private int colunasAtuais;

    private final GerenciadorSom somPreview = new GerenciadorSom();
    private final ConfiguracoesJogo configuracoes = new ConfiguracoesJogo();

    public CampoMinadoView() {
        super(EMOJI_COROA + " Campo Minado: Solo Leveling — O Sistema");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(560, 420));
    }

    public void setOuvinte(AcoesJogador ouvinte) {
        this.ouvinte = ouvinte;
    }

    // ================================================================
    // TEMA / SKINS / APARÊNCIA (sugestões 1-9, 25)
    // ================================================================

    public void aplicarTema(TemaVisual novoTema) {
        this.tema = novoTema;
        getContentPane().setBackground(tema.fundo);
        repaint();
    }

    public void aplicarSkinBandeira(SkinBandeira skin) {
        this.skinBandeira = skin;
    }

    public void aplicarSkinNumeros(SkinNumeros skin) {
        this.skinNumeros = skin;
    }

    public void setSimbolosDaltonismo(boolean ativo) {
        this.simbolosDaltonismo = ativo;
    }

    public void setTamanhoCelula(int tamanho) {
        this.tamanhoCelula = Math.max(20, Math.min(64, tamanho));
        aplicarZoomAosBotoes();
    }

    private void aplicarZoomAosBotoes() {
        aplicarZoomEm(botoes);
        aplicarZoomEm(botoesP2);
        if (painelTabuleiroWrapper != null) {
            painelTabuleiroWrapper.revalidate();
        }
    }

    private void aplicarZoomEm(JButton[][] grade) {
        if (grade == null) {
            return;
        }
        Font fonteCelula = FONTE_CELULA_BASE.deriveFont((float) Math.max(12, tamanhoCelula / 1.8));
        for (JButton[] linha : grade) {
            for (JButton botao : linha) {
                if (botao != null) {
                    botao.setPreferredSize(new Dimension(tamanhoCelula, tamanhoCelula));
                    botao.setFont(fonteCelula);
                }
            }
        }
    }

    // ================================================================
    // TELA INICIAL
    // ================================================================

    public void mostrarTelaInicial(List<String> perfis, String perfilAtivo) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(tema.fundo);

        JPanel raiz = new JPanel();
        raiz.setLayout(new BoxLayout(raiz, BoxLayout.Y_AXIS));
        raiz.setBackground(tema.fundo);
        raiz.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel titulo = new JLabel(EMOJI_COROA + " CAMPO MINADO: SOLO LEVELING", SwingConstants.CENTER);
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(tema.destaque);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        raiz.add(titulo);

        JLabel subtitulo = new JLabel("\"Você foi escolhido... para se tornar um Caçador.\" — O Sistema", SwingConstants.CENTER);
        subtitulo.setFont(FONTE_NORMAL);
        subtitulo.setForeground(tema.textoSecundario);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(6, 0, 18, 0));
        raiz.add(subtitulo);

        raiz.add(criarPainelPerfil(perfis, perfilAtivo));
        raiz.add(Box.createVerticalStrut(14));

        JPanel painelCards = new JPanel(new GridLayout(1, 3, 15, 0));
        painelCards.setBackground(tema.fundo);
        painelCards.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelCards.add(criarCardDificuldade("Rank E", "9 × 9", "10 minas", 9, 9, 10));
        painelCards.add(criarCardDificuldade("Rank C", "16 × 16", "40 minas", 16, 16, 40));
        painelCards.add(criarCardDificuldade("Rank S", "16 × 30", "99 minas", 16, 30, 99));
        raiz.add(painelCards);
        raiz.add(Box.createVerticalStrut(10));

        JPanel painelBotoesExtra = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        painelBotoesExtra.setBackground(tema.fundo);
        painelBotoesExtra.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelBotoesExtra.add(criarBotaoSecundario("Dificuldade customizada", e -> abrirDialogoDificuldadeCustomizada()));
        painelBotoesExtra.add(criarBotaoSecundario(EMOJI_PORTAL + " Portal Diário", e -> iniciarPortalDiario()));
        raiz.add(painelBotoesExtra);
        raiz.add(Box.createVerticalStrut(10));

        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(tema.card);
        abas.setForeground(tema.textoPrincipal);
        abas.add("Modos de missão", criarPainelModos());
        abas.add("Aparência", criarPainelAparencia());
        abas.add("Áudio", criarPainelAudio());
        abas.add("Extras", criarPainelExtras());
        abas.setMaximumSize(new Dimension(640, 210));
        abas.setPreferredSize(new Dimension(620, 200));
        abas.setAlignmentX(Component.CENTER_ALIGNMENT);
        raiz.add(abas);

        JScrollPane scroll = new JScrollPane(raiz);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(tema.fundo);
        scroll.getViewport().setBackground(tema.fundo);
        add(scroll, BorderLayout.CENTER);

        setSize(760, 720);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel criarPainelPerfil(List<String> perfis, String perfilAtivo) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        painel.setBackground(tema.fundo);
        painel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel("Caçador:");
        lbl.setFont(FONTE_PEQUENA);
        lbl.setForeground(tema.textoSecundario);
        painel.add(lbl);

        comboPerfil = new JComboBox<>(perfis.toArray(new String[0]));
        comboPerfil.setSelectedItem(perfilAtivo);
        comboPerfil.addActionListener(e -> {
            String selecionado = (String) comboPerfil.getSelectedItem();
            if (selecionado != null && ouvinte != null) {
                ouvinte.aoTrocarPerfil(selecionado);
            }
        });
        painel.add(comboPerfil);

        JButton btnNovoPerfil = criarBotaoSecundario("+ Novo perfil", e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome do novo Caçador:");
            if (nome != null && !nome.isBlank() && ouvinte != null) {
                ouvinte.aoCriarPerfil(nome.trim());
            }
        });
        painel.add(btnNovoPerfil);
        return painel;
    }

    private JPanel criarPainelModos() {
        JPanel painel = new JPanel(new GridLayout(0, 2, 8, 4));
        painel.setBackground(tema.card);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chkSemCascata = criarCheckBox("Sem cascata (revela só a célula clicada)");
        chkMinasVisiveis = criarCheckBox("Minas visíveis (modo treino)");
        chkRelampago = criarCheckBox("Modo Relâmpago (revela células iniciais)");
        chkTresVidas = criarCheckBox("Modo 3 Vidas (tolera até 3 minas)");
        chkToroidal = criarCheckBox("Tabuleiro toroidal (bordas conectadas)");
        chkCooperativo = criarCheckBox("Cooperativo local (2 jogadores revezam)");
        chkDoisJogadores = criarCheckBox("Dois jogadores competindo (tabuleiros separados)");

        painel.add(chkSemCascata);
        painel.add(chkMinasVisiveis);
        painel.add(chkRelampago);
        painel.add(chkTresVidas);
        painel.add(chkToroidal);
        painel.add(chkCooperativo);
        painel.add(chkDoisJogadores);
        return painel;
    }

    private JCheckBox criarCheckBox(String texto) {
        JCheckBox chk = new JCheckBox(texto);
        chk.setFont(FONTE_PEQUENA);
        chk.setForeground(tema.textoPrincipal);
        chk.setBackground(tema.card);
        return chk;
    }

    private JPanel criarPainelAparencia() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(tema.card);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        comboTema = new JComboBox<>(TemaVisual.todos());
        comboTema.setSelectedItem(TemaVisual.porNome(configuracoes.getTemaVisualNome()));
        comboTema.addActionListener(e -> {
            TemaVisual escolhido = (TemaVisual) comboTema.getSelectedItem();
            if (escolhido != null) {
                configuracoes.setTemaVisualNome(escolhido.nome);
                aplicarTema(escolhido);
                mostrarTelaInicial(listaAtualDoComboPerfil(), (String) comboPerfil.getSelectedItem());
            }
        });
        painel.add(criarLinha("Tema visual:", comboTema));

        chkDeteccaoAutoTema = criarCheckBox("Detectar claro/escuro do sistema automaticamente");
        chkDeteccaoAutoTema.setSelected(configuracoes.isDeteccaoAutomaticaDeTema());
        chkDeteccaoAutoTema.addActionListener(e ->
                configuracoes.setDeteccaoAutomaticaDeTema(chkDeteccaoAutoTema.isSelected()));
        painel.add(chkDeteccaoAutoTema);

        comboSkinBandeira = new JComboBox<>(SkinBandeira.values());
        comboSkinBandeira.setSelectedItem(skinBandeira);
        comboSkinBandeira.addActionListener(e -> {
            skinBandeira = (SkinBandeira) comboSkinBandeira.getSelectedItem();
            configuracoes.setSkinBandeira(skinBandeira.name());
        });
        painel.add(criarLinha("Skin de bandeira:", comboSkinBandeira));

        comboSkinNumeros = new JComboBox<>(SkinNumeros.values());
        comboSkinNumeros.setSelectedItem(skinNumeros);
        comboSkinNumeros.addActionListener(e -> {
            skinNumeros = (SkinNumeros) comboSkinNumeros.getSelectedItem();
            configuracoes.setSkinNumeros(skinNumeros.name());
        });
        painel.add(criarLinha("Skin dos números:", comboSkinNumeros));

        chkDaltonismo = criarCheckBox("Símbolos extras para apoio a daltônicos");
        chkDaltonismo.setSelected(simbolosDaltonismo);
        chkDaltonismo.addActionListener(e -> {
            simbolosDaltonismo = chkDaltonismo.isSelected();
            configuracoes.setSimbolosDaltonismo(simbolosDaltonismo);
        });
        painel.add(chkDaltonismo);

        sliderZoom = new JSlider(20, 64, tamanhoCelula);
        sliderZoom.setBackground(tema.card);
        sliderZoom.addChangeListener(e -> {
            setTamanhoCelula(sliderZoom.getValue());
            configuracoes.setTamanhoCelula(sliderZoom.getValue());
        });
        painel.add(criarLinha("Zoom das células:", sliderZoom));

        return painel;
    }

    private JPanel criarPainelAudio() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(tema.card);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chkSomEfeitos = criarCheckBox("Efeitos sonoros");
        chkSomEfeitos.setSelected(configuracoes.isSomEfeitosLigado());
        chkSomEfeitos.addActionListener(e -> {
            configuracoes.setSomEfeitosLigado(chkSomEfeitos.isSelected());
            somPreview.setEfeitosLigados(chkSomEfeitos.isSelected());
            somPreview.tocarClique();
        });
        painel.add(chkSomEfeitos);

        chkMusica = criarCheckBox("Música ambiente");
        chkMusica.setSelected(configuracoes.isMusicaLigada());
        chkMusica.addActionListener(e -> configuracoes.setMusicaLigada(chkMusica.isSelected()));
        painel.add(chkMusica);

        sliderVolume = new JSlider(0, 100, (int) (configuracoes.getVolumeMusica() * 100));
        sliderVolume.setBackground(tema.card);
        sliderVolume.addChangeListener(e -> configuracoes.setVolumeMusica(sliderVolume.getValue() / 100.0));
        painel.add(criarLinha("Volume da música:", sliderVolume));

        JLabel aviso = new JLabel("<html>As mudanças de som têm efeito completo ao reiniciar a partida.</html>");
        aviso.setFont(FONTE_PEQUENA);
        aviso.setForeground(tema.textoSecundario);
        painel.add(aviso);

        return painel;
    }

    private JPanel criarPainelExtras() {
        JPanel painel = new JPanel(new GridLayout(0, 2, 8, 6));
        painel.setBackground(tema.card);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        comboTempo = new JComboBox<>(new String[]{"Sem limite", "1 minuto", "2 minutos", "3 minutos", "5 minutos"});
        painel.add(criarLinhaCompacta("Tempo rápido:", comboTempo));

        painel.add(criarBotaoSecundario("Ver tutorial", e -> mostrarTutorial()));
        painel.add(criarBotaoSecundario("Histórico e ranking", e -> mostrarHistoricoERanking()));
        painel.add(criarBotaoSecundario("Exportar histórico (CSV)", e -> {
            if (ouvinte != null) ouvinte.aoExportarHistoricoCsv();
        }));
        painel.add(criarBotaoSecundario("Reproduzir último replay", e -> {
            if (ouvinte != null) ouvinte.aoReproduzirUltimoReplay();
        }));
        painel.add(criarBotaoSecundario("Ver conquistas", e -> mostrarConquistas()));
        painel.add(criarBotaoSecundario("Tela cheia (F11)", e -> alternarTelaCheia()));

        return painel;
    }

    private List<String> listaAtualDoComboPerfil() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        if (comboPerfil != null) {
            for (int i = 0; i < comboPerfil.getItemCount(); i++) {
                lista.add(comboPerfil.getItemAt(i));
            }
        } else {
            lista.add(configuracoes.getPerfilAtivo());
        }
        return lista;
    }

    private JPanel criarLinha(String rotulo, JComponent controle) {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(tema.card);
        painel.setMaximumSize(new Dimension(560, 34));
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(FONTE_PEQUENA);
        lbl.setForeground(tema.textoSecundario);
        painel.add(lbl, BorderLayout.WEST);
        painel.add(controle, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarLinhaCompacta(String rotulo, JComponent controle) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        painel.setBackground(tema.card);
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(FONTE_PEQUENA);
        lbl.setForeground(tema.textoSecundario);
        painel.add(lbl);
        painel.add(controle);
        return painel;
    }

    private JButton criarBotaoSecundario(String texto, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_PEQUENA);
        botao.setForeground(tema.textoPrincipal);
        botao.setBackground(tema.fundoClaro);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.borda),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao);
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(tema.cardHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(tema.fundoClaro);
            }
        });
        return botao;
    }

    private void abrirDialogoDificuldadeCustomizada() {
        try {
            String linhasStr = JOptionPane.showInputDialog(this, "Linhas (5-40):", "16");
            if (linhasStr == null) return;
            String colunasStr = JOptionPane.showInputDialog(this, "Colunas (5-40):", "16");
            if (colunasStr == null) return;
            String minasStr = JOptionPane.showInputDialog(this, "Quantidade de minas:", "30");
            if (minasStr == null) return;

            int linhas = Math.max(5, Math.min(40, Integer.parseInt(linhasStr.trim())));
            int colunas = Math.max(5, Math.min(40, Integer.parseInt(colunasStr.trim())));
            int maxMinas = linhas * colunas - 1;
            int minas = Math.max(1, Math.min(maxMinas, Integer.parseInt(minasStr.trim())));

            ConfigPartida config = montarConfigComModos("Customizada", linhas, colunas, minas);
            if (ouvinte != null) {
                ouvinte.aoEscolherDificuldade(config);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite apenas números válidos.", "Entrada inválida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void iniciarPortalDiario() {
        ConfigPartida config = montarConfigComModos("Rank C", 16, 16, 40);
        config.diario = true;
        config.relampago = false;
        config.doisJogadores = false;
        config.cooperativo = false;
        if (ouvinte != null) {
            ouvinte.aoEscolherDificuldade(config);
        }
    }

    private ConfigPartida montarConfigComModos(String nome, int linhas, int colunas, int minas) {
        ConfigPartida config = ConfigPartida.padrao(nome, linhas, colunas, minas);
        config.tempoLimiteSegundos = getTempoLimiteSegundosSelecionado();
        config.semCascata = chkSemCascata != null && chkSemCascata.isSelected();
        config.minasVisiveis = chkMinasVisiveis != null && chkMinasVisiveis.isSelected();
        config.relampago = chkRelampago != null && chkRelampago.isSelected();
        config.vidas = (chkTresVidas != null && chkTresVidas.isSelected()) ? 3 : 1;
        config.toroidal = chkToroidal != null && chkToroidal.isSelected();
        config.cooperativo = chkCooperativo != null && chkCooperativo.isSelected();
        config.doisJogadores = chkDoisJogadores != null && chkDoisJogadores.isSelected();
        return config;
    }

    private int getTempoLimiteSegundosSelecionado() {
        if (comboTempo == null) {
            return 0;
        }
        String selecionado = (String) comboTempo.getSelectedItem();
        if (selecionado == null || selecionado.startsWith("Sem")) return 0;
        if (selecionado.contains("1 minuto")) return 60;
        if (selecionado.contains("2 minutos")) return 120;
        if (selecionado.contains("3 minutos")) return 180;
        if (selecionado.contains("5 minutos")) return 300;
        return 0;
    }

    private void mostrarTutorial() {
        String texto = "1. Escolha um Rank de dificuldade (ou personalize o seu).\n"
                + "2. Clique com o botão esquerdo (ou Espaço) para revelar uma célula.\n"
                + "3. Clique com o botão direito (ou F) para marcar: nenhuma -> bandeira -> interrogação.\n"
                + "4. Revele todas as células sem minas para vencer a missão.\n"
                + "5. Use as setas do teclado para mover o cursor pelo tabuleiro.\n"
                + "6. O botão de Dica revela uma célula seguramente livre de minas.\n"
                + "7. O Sistema pode assumir a missão sozinho: um passo por vez, ou até o fim.\n"
                + "8. Modos especiais (menu 'Modos de missão') mudam as regras: sem cascata, "
                + "3 vidas, tabuleiro toroidal, cooperativo, dois jogadores e mais.\n";
        JTextArea area = new JTextArea(texto);
        area.setFont(FONTE_NORMAL);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(tema.fundoClaro);
        area.setForeground(tema.textoPrincipal);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Como jogar", JOptionPane.PLAIN_MESSAGE);
    }

    private void mostrarHistoricoERanking() {
        String perfil = comboPerfil != null ? (String) comboPerfil.getSelectedItem() : configuracoes.getPerfilAtivo();
        HistoricoPartidas historicoConsulta = new HistoricoPartidas(perfil);
        List<RegistroPartida> registros = historicoConsulta.listarTudo();

        StringBuilder texto = new StringBuilder();
        texto.append("Histórico de ").append(perfil).append(" (últimas partidas):\n\n");
        int inicio = Math.max(0, registros.size() - 20);
        for (int i = registros.size() - 1; i >= inicio; i--) {
            RegistroPartida r = registros.get(i);
            texto.append(r.vitoria ? "✔ " : "✘ ").append(r.dificuldade)
                    .append(" — ").append(r.tempoSegundos).append("s — ")
                    .append(r.jogadas).append(" jogadas\n");
        }
        texto.append("\nRanking (Rank E, mais rápidos):\n");
        for (RegistroPartida r : historicoConsulta.ranking("Rank E", 5)) {
            texto.append(r.tempoSegundos).append("s\n");
        }

        JTextArea area = new JTextArea(texto.toString());
        area.setEditable(false);
        area.setFont(FONTE_NORMAL);
        area.setBackground(tema.fundoClaro);
        area.setForeground(tema.textoPrincipal);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 380));
        JOptionPane.showMessageDialog(this, scroll, "Histórico e Ranking", JOptionPane.PLAIN_MESSAGE);
    }

    private void mostrarConquistas() {
        String perfil = comboPerfil != null ? (String) comboPerfil.getSelectedItem() : configuracoes.getPerfilAtivo();
        ConquistasManager manager = new ConquistasManager(perfil);
        Set<Conquista> desbloqueadas = manager.listarDesbloqueadas();

        StringBuilder texto = new StringBuilder();
        for (Conquista c : Conquista.values()) {
            texto.append(desbloqueadas.contains(c) ? "\uD83C\uDFC5 " : "\uD83D\uDD12 ")
                    .append(c.titulo).append(" — ").append(c.descricao).append("\n");
        }
        JTextArea area = new JTextArea(texto.toString());
        area.setEditable(false);
        area.setFont(FONTE_NORMAL);
        area.setBackground(tema.fundoClaro);
        area.setForeground(tema.textoPrincipal);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Conquistas de " + perfil, JOptionPane.PLAIN_MESSAGE);
    }

    private void alternarTelaCheia() {
        GraphicsDevice dispositivo = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (dispositivo.getFullScreenWindow() == this) {
            dispositivo.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setVisible(true);
        } else if (dispositivo.isFullScreenSupported()) {
            dispose();
            setUndecorated(true);
            dispositivo.setFullScreenWindow(this);
            setVisible(true);
        }
    }

    private JPanel criarCardDificuldade(String titulo, String dimensao, String minasTexto,
                                         int linhas, int colunas, int minas) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(tema.card);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.borda, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONTE_SUBTITULO);
        lblTitulo.setForeground(tema.destaque);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitulo);

        JLabel lblDim = new JLabel(dimensao);
        lblDim.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDim.setForeground(tema.textoPrincipal);
        lblDim.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDim.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        card.add(lblDim);

        JLabel lblMinas = new JLabel(EMOJI_CAVEIRA + " " + minasTexto);
        lblMinas.setFont(FONTE_NORMAL);
        lblMinas.setForeground(tema.textoSecundario);
        lblMinas.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblMinas);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(tema.cardHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(tema.card);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (ouvinte != null) {
                    ouvinte.aoEscolherDificuldade(montarConfigComModos(titulo, linhas, colunas, minas));
                }
            }
        });

        return card;
    }

    // ================================================================
    // TELA DE JOGO (single player / cooperativo)
    // ================================================================

    public void iniciarTelaDeJogo(int linhas, int colunas, int totalMinas, int totalCelulas,
                                   int tempoLimiteSegundos, int vidas) {
        this.linhasAtuais = linhas;
        this.colunasAtuais = colunas;
        getContentPane().removeAll();
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(tema.fundo);

        add(criarPainelSuperior(), BorderLayout.NORTH);

        JPanel painelPrincipal = new JPanel(new BorderLayout(15, 0));
        painelPrincipal.setBackground(tema.fundo);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        botoes = new JButton[linhas][colunas];
        painelTabuleiroWrapper = criarPainelTabuleiro(linhas, colunas, botoes, 1);
        scrollTabuleiro = new JScrollPane(painelTabuleiroWrapper);
        scrollTabuleiro.setBorder(null);
        scrollTabuleiro.getViewport().setBackground(tema.fundo);
        painelPrincipal.add(scrollTabuleiro, BorderLayout.CENTER);

        JScrollPane scrollStatus = new JScrollPane(criarPainelEstatisticas(totalMinas, totalCelulas, vidas));
        scrollStatus.setBorder(null);
        scrollStatus.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollStatus.getViewport().setBackground(tema.fundoClaro);
        scrollStatus.setPreferredSize(new Dimension(190 + Math.min(100, totalMinas * 2), 0));
        painelPrincipal.add(scrollStatus, BorderLayout.EAST);

        add(painelPrincipal, BorderLayout.CENTER);
        instalarNavegacaoPorTeclado();

        cursorLinha = 0;
        cursorColuna = 0;

        // Dimensiona a janela para caber o tabuleiro inteiro + o painel de
        // status, respeitando o tamanho útil da tela do usuário. O
        // tabuleiro fica dentro de um JScrollPane, então tabuleiros muito
        // grandes (Rank S com zoom alto) continuam acessíveis por rolagem.
        Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int larguraTabuleiro = colunas * (tamanhoCelula + 2) + 20;
        int alturaTabuleiro = linhas * (tamanhoCelula + 2) + 20;
        int larguraPainelStatus = 190 + Math.min(100, totalMinas * 2);
        int largura = Math.min(areaUtil.width - 40, larguraTabuleiro + larguraPainelStatus + 70);
        int altura = Math.min(areaUtil.height - 40, alturaTabuleiro + 200);
        setSize(Math.max(820, largura), Math.max(640, altura));
        setLocationRelativeTo(null);
        revalidate();
        repaint();
        painelTabuleiroWrapper.requestFocusInWindow();
    }

    private JPanel criarPainelSuperior() {
        // Duas faixas empilhadas: os botões de ação em cima e a "voz do
        // Sistema" (status) logo abaixo, ocupando a largura inteira. Antes
        // o status dividia a mesma linha dos botões e ficava truncado.
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(tema.fundo);
        painel.setBorder(BorderFactory.createEmptyBorder(12, 15, 8, 15));

        JPanel faixaBotoes = new JPanel(new BorderLayout());
        faixaBotoes.setBackground(tema.fundo);

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        esquerda.setBackground(tema.fundo);
        esquerda.add(criarBotaoSecundario("\u2190 Novo Jogo", e -> {
            if (ouvinte != null) ouvinte.aoPedirNovoJogo();
        }));
        esquerda.add(criarBotaoSecundario(EMOJI_LAMPADA + " Dica", e -> {
            if (ouvinte != null) ouvinte.aoPedirDica();
        }));
        esquerda.add(criarBotaoSecundario(EMOJI_SISTEMA + " Sistema: 1 passo", e -> {
            if (ouvinte != null) ouvinte.aoPedirPassoDoSistema();
        }));
        esquerda.add(criarBotaoSecundario(EMOJI_SISTEMA + " Sistema: resolver tudo", e -> {
            if (ouvinte != null) ouvinte.aoPedirSistemaResolverTudo();
        }));

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        direita.setBackground(tema.fundo);
        direita.add(criarBotaoSecundario("Compartilhar", e -> compartilharResultado()));

        labelStatus = new JLabel("O Sistema aguarda sua próxima jogada.", SwingConstants.CENTER);
        labelStatus.setFont(FONTE_SUBTITULO);
        labelStatus.setForeground(tema.textoSecundario);

        faixaBotoes.add(esquerda, BorderLayout.WEST);
        faixaBotoes.add(direita, BorderLayout.EAST);

        painel.add(faixaBotoes, BorderLayout.NORTH);
        painel.add(labelStatus, BorderLayout.CENTER);
        return painel;
    }

    private void compartilharResultado() {
        if (ouvinte == null) return;
        String texto = ouvinte.gerarTextoCompartilhamento();
        JTextField campo = new JTextField(texto);
        campo.setEditable(false);
        JOptionPane.showMessageDialog(this, campo, "Compartilhar resultado", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel criarPainelEstatisticas(int totalMinas, int totalCelulas, int vidas) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(tema.fundoClaro);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.borda, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        int largura = 190 + Math.min(100, totalMinas * 2);
        painel.setPreferredSize(new Dimension(largura, 0));

        JLabel lblTitulo = new JLabel("Status da Missão");
        lblTitulo.setFont(FONTE_SUBTITULO);
        lblTitulo.setForeground(tema.destaque);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(16));

        lblTempo = adicionarItem(painel, EMOJI_RELOGIO + " Tempo", "00:00");
        lblMinasRestantes = adicionarItem(painel, EMOJI_CAVEIRA + " Minas", String.valueOf(totalMinas));
        lblCelulasReveladas = adicionarItem(painel, "\uD83D\uDDFA Reveladas", "0 / " + totalCelulas);
        lblJogadas = adicionarItem(painel, EMOJI_JOGADA + " Jogadas", "0");
        lblVidas = adicionarItem(painel, EMOJI_CORACAO + " Vidas", String.valueOf(vidas));
        lblDicas = adicionarItem(painel, EMOJI_LAMPADA + " Dicas", "3");
        lblRecorde = adicionarItem(painel, "\uD83C\uDFC6 Recorde", "--");
        lblTurno = adicionarItem(painel, "Turno", "-");
        lblTurno.getParent().setVisible(false);

        painel.add(Box.createVerticalStrut(14));
        JLabel lblProgTitulo = new JLabel("Progresso");
        lblProgTitulo.setFont(FONTE_NORMAL);
        lblProgTitulo.setForeground(tema.textoSecundario);
        lblProgTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblProgTitulo);

        barraProgresso = new JProgressBar(0, Math.max(totalCelulas, 1));
        barraProgresso.setStringPainted(true);
        barraProgresso.setForeground(tema.destaque);
        barraProgresso.setBackground(tema.fundo);
        barraProgresso.setBorder(BorderFactory.createLineBorder(tema.borda));
        barraProgresso.setPreferredSize(new Dimension(150, 20));
        barraProgresso.setMaximumSize(new Dimension(150, 20));
        barraProgresso.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(barraProgresso);
        painel.add(Box.createVerticalStrut(10));
        painel.add(Box.createVerticalGlue());

        JLabel lblDica = new JLabel("<html><center>Esquerdo/Espaço: revelar<br>Direito/F: marcar<br>Setas: mover cursor</center></html>");
        lblDica.setFont(FONTE_PEQUENA);
        lblDica.setForeground(tema.textoSecundario);
        lblDica.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblDica);

        return painel;
    }

    private JLabel adicionarItem(JPanel painelPai, String titulo, String valorInicial) {
        JPanel painelItem = new JPanel();
        painelItem.setLayout(new BoxLayout(painelItem, BoxLayout.Y_AXIS));
        painelItem.setBackground(tema.fundoClaro);
        painelItem.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelItem.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONTE_PEQUENA);
        lblTitulo.setForeground(tema.textoSecundario);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(FONTE_NUMERO);
        lblValor.setForeground(tema.textoPrincipal);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelItem.add(lblTitulo);
        painelItem.add(lblValor);
        painelPai.add(painelItem);
        return lblValor;
    }

    private JPanel criarPainelTabuleiro(int linhas, int colunas, JButton[][] destino, int jogador) {
        JPanel grade = new JPanel(new GridLayout(linhas, colunas, 2, 2));
        grade.setBackground(tema.fundo);
        grade.setFocusable(true);

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                JButton botao = criarBotaoCelula(i, j, jogador);
                destino[i][j] = botao;
                grade.add(botao);
            }
        }
        aplicarZoomEm(destino);
        return grade;
    }

    private JButton criarBotaoCelula(int linha, int coluna, int jogador) {
        JButton botao = new JButton();
        botao.setPreferredSize(new Dimension(tamanhoCelula, tamanhoCelula));
        botao.setFont(FONTE_CELULA_BASE);
        botao.setFocusPainted(false);
        botao.setBackground(tema.celulaOculta);
        botao.setForeground(tema.textoPrincipal);
        botao.setMargin(new Insets(0, 0, 0, 0));
        botao.setBorder(criarBordaOculta());
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.putClientProperty("revelada", Boolean.FALSE);

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (Boolean.FALSE.equals(botao.getClientProperty("revelada"))) {
                    botao.setBackground(tema.celulaOcultaHover);
                    botao.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(tema.destaque, 2),
                            BorderFactory.createEmptyBorder(1, 1, 1, 1)));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (Boolean.FALSE.equals(botao.getClientProperty("revelada"))) {
                    botao.setBackground(tema.celulaOculta);
                    botao.setBorder(criarBordaOculta());
                }
            }

            @Override
            public void mouseReleased(MouseEvent evento) {
                if (ouvinte == null) return;
                boolean botaoDireito = SwingUtilities.isRightMouseButton(evento) || evento.getButton() == MouseEvent.BUTTON3;
                cursorLinha = linha;
                cursorColuna = coluna;
                if (jogador == 1) {
                    if (botaoDireito) ouvinte.aoMarcarCelula(linha, coluna);
                    else if (SwingUtilities.isLeftMouseButton(evento)) ouvinte.aoRevelarCelula(linha, coluna);
                } else {
                    if (botaoDireito) ouvinte.aoMarcarCelulaJogador(2, linha, coluna);
                    else if (SwingUtilities.isLeftMouseButton(evento)) ouvinte.aoRevelarCelulaJogador(2, linha, coluna);
                }
            }
        });
        return botao;
    }

    private Border criarBordaOculta() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.bordaOculta, 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    // ---- Navegação por teclado (sugestão 26) ----

    private void instalarNavegacaoPorTeclado() {
        painelTabuleiroWrapper.setFocusable(true);
        for (KeyListener antigo : painelTabuleiroWrapper.getKeyListeners()) {
            painelTabuleiroWrapper.removeKeyListener(antigo);
        }
        painelTabuleiroWrapper.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (botoes == null) return;
                int codigo = e.getKeyCode();
                if (codigo == KeyEvent.VK_UP) moverCursor(-1, 0);
                else if (codigo == KeyEvent.VK_DOWN) moverCursor(1, 0);
                else if (codigo == KeyEvent.VK_LEFT) moverCursor(0, -1);
                else if (codigo == KeyEvent.VK_RIGHT) moverCursor(0, 1);
                else if (codigo == KeyEvent.VK_SPACE && ouvinte != null) ouvinte.aoRevelarCelula(cursorLinha, cursorColuna);
                else if (codigo == KeyEvent.VK_F && ouvinte != null) ouvinte.aoMarcarCelula(cursorLinha, cursorColuna);
                else if (codigo == KeyEvent.VK_F11) alternarTelaCheia();
                else if (e.isControlDown() && codigo == KeyEvent.VK_PLUS) setTamanhoCelula(tamanhoCelula + 4);
                else if (e.isControlDown() && codigo == KeyEvent.VK_MINUS) setTamanhoCelula(tamanhoCelula - 4);
            }
        });
    }

    private void moverCursor(int deltaLinha, int deltaColuna) {
        int novaLinha = Math.max(0, Math.min(linhasAtuais - 1, cursorLinha + deltaLinha));
        int novaColuna = Math.max(0, Math.min(colunasAtuais - 1, cursorColuna + deltaColuna));
        JButton anterior = botoes[cursorLinha][cursorColuna];
        anterior.setBorder(Boolean.TRUE.equals(anterior.getClientProperty("revelada"))
                ? BorderFactory.createLineBorder(tema.bordaRevelada, 1) : criarBordaOculta());

        cursorLinha = novaLinha;
        cursorColuna = novaColuna;
        JButton atual = botoes[cursorLinha][cursorColuna];
        atual.setBorder(BorderFactory.createLineBorder(tema.destaque, 3));
    }

    // ================================================================
    // TELA DE DOIS JOGADORES (sugestão 43)
    // ================================================================

    public void iniciarTelaDoisJogadores(int linhas, int colunas, int totalMinas, int totalCelulas, int tempoLimiteSegundos) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(tema.fundo);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(tema.fundo);
        topo.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        topo.add(criarBotaoSecundario("\u2190 Novo Jogo", e -> {
            if (ouvinte != null) ouvinte.aoPedirNovoJogo();
        }), BorderLayout.WEST);
        labelStatus = new JLabel("Caçador 1  vs  Caçador 2 — corrida contra o tempo!", SwingConstants.CENTER);
        labelStatus.setFont(FONTE_SUBTITULO);
        labelStatus.setForeground(tema.textoSecundario);
        topo.add(labelStatus, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new GridLayout(1, 2, 20, 0));
        corpo.setBackground(tema.fundo);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        botoes = new JButton[linhas][colunas];
        botoesP2 = new JButton[linhas][colunas];
        corpo.add(criarPainelJogador("Caçador 1", criarPainelTabuleiro(linhas, colunas, botoes, 1)));
        corpo.add(criarPainelJogador("Caçador 2", criarPainelTabuleiro(linhas, colunas, botoesP2, 2)));

        add(corpo, BorderLayout.CENTER);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel criarPainelJogador(String titulo, JPanel tabuleiro) {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(tema.fundo);
        JLabel lbl = new JLabel(titulo, SwingConstants.CENTER);
        lbl.setFont(FONTE_SUBTITULO);
        lbl.setForeground(tema.destaque);
        painel.add(lbl, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabuleiro);
        scroll.getViewport().setBackground(tema.fundo);
        scroll.setBorder(null);
        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    public void atualizarCelulaJogador(int jogador, int linha, int coluna, LeituraTabuleiro leitura) {
        JButton[][] grade = jogador == 1 ? botoes : botoesP2;
        pintarCelula(grade[linha][coluna], linha, coluna, leitura);
    }

    public void atualizarEstatisticasDoisJogadores(int reveladas1, int total1, int reveladas2, int total2) {
        if (labelStatus != null) {
            labelStatus.setText(String.format("Caçador 1: %d/%d   |   Caçador 2: %d/%d", reveladas1, total1, reveladas2, total2));
        }
    }

    public void mostrarResultadoDoisJogadores(int jogadorQueTerminou, boolean venceu) {
        String vencedor = venceu ? "Caçador " + jogadorQueTerminou : "Caçador " + (jogadorQueTerminou == 1 ? 2 : 1);
        labelStatus.setText(EMOJI_COROA + " " + vencedor + " venceu a corrida!");
        labelStatus.setForeground(tema.vitoria);
    }

    // ================================================================
    // TELA DE REPLAY (sugestão 41)
    // ================================================================

    public void iniciarTelaDeReplay(int linhas, int colunas, int totalMinas, int totalCelulas) {
        iniciarTelaDeJogo(linhas, colunas, totalMinas, totalCelulas, 0, 1);
        labelStatus.setText(EMOJI_SISTEMA + " Reprisando missão gravada...");
    }

    public void atualizarCelulaReplay(int linha, int coluna, LeituraTabuleiro leitura) {
        atualizarCelula(linha, coluna, leitura);
    }

    // ================================================================
    // ATUALIZAÇÕES CHAMADAS PELO CONTROLLER
    // ================================================================

    public void atualizarCelula(int linha, int coluna, LeituraTabuleiro leitura) {
        pintarCelula(botoes[linha][coluna], linha, coluna, leitura);
    }

    /** Tamanho do ícone vetorial desenhado dentro da célula, proporcional ao zoom atual. */
    private int tamanhoIconeAtual() {
        return Math.max(14, (int) (tamanhoCelula * 0.62));
    }

    private void pintarCelula(JButton botao, int linha, int coluna, LeituraTabuleiro leitura) {
        botao.putClientProperty("revelada", leitura.isRevelada(linha, coluna));

        if (leitura.isMarcada(linha, coluna)) {
            botao.setText("");
            botao.setIcon(IconesVetoriais.paraSkinBandeira(skinBandeira, tema.bandeira, tamanhoIconeAtual()));
            botao.setBackground(tema.celulaOculta);
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(tema.bandeira, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            return;
        }

        if (leitura.isInterrogada(linha, coluna)) {
            botao.setIcon(null);
            botao.setText("?");
            botao.setForeground(tema.destaque);
            botao.setBackground(tema.celulaOculta);
            botao.setBorder(criarBordaOculta());
            return;
        }

        if (!leitura.isRevelada(linha, coluna)) {
            botao.setIcon(null);
            botao.setText("");
            botao.setBackground(tema.celulaOculta);
            botao.setBorder(criarBordaOculta());
            return;
        }

        if (leitura.isMinada(linha, coluna)) {
            botao.setText("");
            botao.setIcon(IconesVetoriais.caveira(tema.mina, tema.minaFundo, tamanhoIconeAtual()));
            botao.setBackground(tema.minaFundo);
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(tema.mina, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        } else {
            botao.setIcon(null);
            botao.setBackground(tema.celulaRevelada);
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(tema.bordaRevelada, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            int vizinhas = leitura.getMinasVizinhas(linha, coluna);
            if (vizinhas == 0) {
                botao.setText("");
                botao.setForeground(tema.textoSobreRevelada);
            } else {
                botao.setText(skinNumeros.textoPara(vizinhas, simbolosDaltonismo));
                botao.setForeground(CORES_NUMEROS[vizinhas]);
            }
        }
    }

    /** Marca discretamente, sem revelar, onde estão as minas — Modo Treino (sugestão 13). */
    public void mostrarMinasComoDebug(LeituraTabuleiro leitura) {
        for (int i = 0; i < leitura.getLinhas(); i++) {
            for (int j = 0; j < leitura.getColunas(); j++) {
                if (leitura.isMinada(i, j) && !leitura.isRevelada(i, j)) {
                    JButton botao = botoes[i][j];
                    botao.setIcon(null);
                    botao.setText("\u2022");
                    botao.setForeground(tema.mina);
                }
            }
        }
    }

    public void atualizarTempo(String texto) {
        if (lblTempo != null) lblTempo.setText(texto);
    }

    public void atualizarEstatisticas(int minasRestantes, int celulasReveladas, int totalCelulas, int jogadas, int vidasRestantes) {
        if (lblMinasRestantes != null) lblMinasRestantes.setText(String.valueOf(minasRestantes));
        if (lblCelulasReveladas != null) lblCelulasReveladas.setText(celulasReveladas + " / " + totalCelulas);
        if (lblJogadas != null) lblJogadas.setText(String.valueOf(jogadas));
        if (lblVidas != null) lblVidas.setText(String.valueOf(vidasRestantes));

        int progresso = totalCelulas > 0 ? (int) ((celulasReveladas * 100.0) / totalCelulas) : 0;
        if (barraProgresso != null) {
            barraProgresso.setValue(celulasReveladas);
            barraProgresso.setString(progresso + "%");
            barraProgresso.setForeground(progresso < 30 ? new Color(220, 80, 80)
                    : progresso < 70 ? new Color(220, 180, 60) : tema.vitoria);
        }
    }

    public void setDicasRestantes(int quantidade) {
        if (lblDicas != null) lblDicas.setText(String.valueOf(quantidade));
    }

    public void atualizarTurno(int jogadorDaVez) {
        if (lblTurno != null) {
            lblTurno.getParent().setVisible(true);
            lblTurno.setText("Caçador " + jogadorDaVez);
        }
    }

    public void avisarSeBandeirasExcedem(int marcadas, int totalMinas) {
        if (marcadas > totalMinas && labelStatus != null) {
            labelStatus.setText("\u26A0 Bandeiras além do número de ameaças conhecidas!");
            labelStatus.setForeground(new Color(230, 160, 40));
        }
    }

    public void mostrarAvisoVidaPerdida(int vidasRestantes) {
        if (labelStatus != null) {
            labelStatus.setText(EMOJI_CORACAO + " Uma mina te atingiu! Vidas restantes: " + vidasRestantes);
            labelStatus.setForeground(tema.mina);
        }
    }

    public void mostrarMensagemSistema(String mensagem) {
        if (labelStatus != null) {
            labelStatus.setText(EMOJI_SISTEMA + " " + mensagem);
            labelStatus.setForeground(tema.destaque);
        }
    }

    public void atualizarRecorde(int segundos) {
        if (lblRecorde != null) {
            lblRecorde.setText(String.format("%02d:%02d", segundos / 60, segundos % 60));
        }
    }

    public void mostrarConquistasDesbloqueadas(List<String> titulos) {
        JOptionPane.showMessageDialog(this,
                "\uD83C\uDFC5 Nova(s) conquista(s):\n" + String.join("\n", titulos),
                "Conquista desbloqueada", JOptionPane.INFORMATION_MESSAGE);
    }

    public File pedirLocalParaSalvarCsv(String perfil) {
        JFileChooser seletor = new JFileChooser();
        seletor.setSelectedFile(new File("historico_" + perfil + ".csv"));
        int resultado = seletor.showSaveDialog(this);
        return resultado == JFileChooser.APPROVE_OPTION ? seletor.getSelectedFile() : null;
    }

    public void mostrarResultadoExportacao(boolean sucesso, File destino) {
        JOptionPane.showMessageDialog(this,
                sucesso ? "Histórico exportado para:\n" + destino.getAbsolutePath() : "Não foi possível exportar o histórico.",
                "Exportar CSV", sucesso ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    public String getTempoExibidoAtual() {
        return lblTempo != null ? lblTempo.getText() : "00:00";
    }

    public void mostrarDerrota() {
        labelStatus.setText(EMOJI_EXPLOSAO + " Você foi derrotado. O Portal se fecha...");
        labelStatus.setForeground(tema.mina);
    }

    public void mostrarVitoria() {
        labelStatus.setText(EMOJI_COROA + " LEVEL UP! Missão concluída.");
        labelStatus.setForeground(tema.vitoria);
    }

    public void piscarFundoDeExplosao(boolean explodindo) {
        getContentPane().setBackground(explodindo ? tema.minaFundo : tema.fundo);
    }

    public void marcarMinaExplodida(int linha, int coluna) {
        JButton botao = botoes[linha][coluna];
        botao.setText("");
        botao.setIcon(IconesVetoriais.caveira(tema.mina, tema.minaFundo, tamanhoIconeAtual()));
        botao.setBackground(tema.minaFundo);
        botao.setBorder(BorderFactory.createLineBorder(tema.mina, 1));
    }

    public void destacarCelulaVencedora(int linha, int coluna) {
        botoes[linha][coluna].setBackground(tema.vitoria.darker());
    }

    /** Efeito simples de confete/partículas na vitória (sugestão 11), sem depender de bibliotecas externas. */
    public void dispararConfeteDeVitoria() {
        JWindow overlay = new JWindow(this);
        overlay.setBackground(new Color(0, 0, 0, 0));
        overlay.setBounds(getBounds());
        java.util.List<Point> particulas = new java.util.ArrayList<>();
        java.util.List<Color> cores = new java.util.ArrayList<>();
        java.util.Random aleatorio = new java.util.Random();
        for (int i = 0; i < 80; i++) {
            particulas.add(new Point(aleatorio.nextInt(Math.max(getWidth(), 400)), -aleatorio.nextInt(200)));
            cores.add(new Color(aleatorio.nextInt(256), aleatorio.nextInt(256), aleatorio.nextInt(256)));
        }
        JPanel tela = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int i = 0; i < particulas.size(); i++) {
                    g.setColor(cores.get(i));
                    Point p = particulas.get(i);
                    g.fillRect(p.x, p.y, 6, 6);
                }
            }
        };
        tela.setOpaque(false);
        overlay.setContentPane(tela);
        overlay.setVisible(true);

        Timer animacao = new Timer(30, null);
        int[] passos = {0};
        animacao.addActionListener(e -> {
            for (Point p : particulas) {
                p.y += 8;
            }
            tela.repaint();
            passos[0]++;
            if (passos[0] > 40) {
                animacao.stop();
                overlay.dispose();
            }
        });
        animacao.start();
    }
}
