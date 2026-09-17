package util;

import java.util.prefs.Preferences;

/**
 * Configurações persistentes entre execuções do jogo (sugestão 34):
 * tema visual escolhido, skins de bandeira/números, som, música,
 * densidade de minas padrão da dificuldade customizada, tamanho de
 * célula (zoom) e o último perfil de jogador usado. Usa
 * {@link Preferences}, que já grava em disco/registro sem exigir que o
 * jogo gerencie caminhos de arquivo manualmente.
 */
public class ConfiguracoesJogo {

    private final Preferences prefs = Preferences.userNodeForPackage(ConfiguracoesJogo.class);

    public String getTemaVisualNome() {
        return prefs.get("temaVisual", TemaVisual.monarcaDasSombras().nome);
    }

    public void setTemaVisualNome(String nome) {
        prefs.put("temaVisual", nome);
    }

    public String getSkinBandeira() {
        return prefs.get("skinBandeira", SkinBandeira.ADAGA_SOMBRA.name());
    }

    public void setSkinBandeira(String nome) {
        prefs.put("skinBandeira", nome);
    }

    public String getSkinNumeros() {
        return prefs.get("skinNumeros", SkinNumeros.CLASSICO.name());
    }

    public void setSkinNumeros(String nome) {
        prefs.put("skinNumeros", nome);
    }

    public boolean isSimbolosDaltonismo() {
        return prefs.getBoolean("simbolosDaltonismo", false);
    }

    public void setSimbolosDaltonismo(boolean ativo) {
        prefs.putBoolean("simbolosDaltonismo", ativo);
    }

    public boolean isSomEfeitosLigado() {
        return prefs.getBoolean("somEfeitos", true);
    }

    public void setSomEfeitosLigado(boolean ligado) {
        prefs.putBoolean("somEfeitos", ligado);
    }

    public boolean isMusicaLigada() {
        return prefs.getBoolean("musicaLigada", false);
    }

    public void setMusicaLigada(boolean ligada) {
        prefs.putBoolean("musicaLigada", ligada);
    }

    public double getVolumeMusica() {
        return prefs.getDouble("volumeMusica", 0.25);
    }

    public void setVolumeMusica(double volume) {
        prefs.putDouble("volumeMusica", volume);
    }

    public int getTamanhoCelula() {
        return prefs.getInt("tamanhoCelula", 36);
    }

    public void setTamanhoCelula(int tamanho) {
        prefs.putInt("tamanhoCelula", Math.max(20, Math.min(64, tamanho)));
    }

    public String getPerfilAtivo() {
        return prefs.get("perfilAtivo", "Caçador");
    }

    public void setPerfilAtivo(String nomePerfil) {
        prefs.put("perfilAtivo", nomePerfil);
    }

    public boolean isDeteccaoAutomaticaDeTema() {
        return prefs.getBoolean("deteccaoAutomaticaTema", false);
    }

    public void setDeteccaoAutomaticaDeTema(boolean ativo) {
        prefs.putBoolean("deteccaoAutomaticaTema", ativo);
    }
}
