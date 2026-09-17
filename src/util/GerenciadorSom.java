package util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Toca efeitos sonoros e uma música ambiente simples, tudo sintetizado em
 * memória (nenhum arquivo de áudio externo é necessário). Cobre as
 * sugestões 35 (efeitos sonoros) e 36 (música ambiente com volume).
 * <p>
 * Qualquer falha de áudio (ex.: ambiente sem placa de som) é ignorada
 * silenciosamente — som é um extra, nunca deve travar o jogo.
 */
public class GerenciadorSom {

    private static final float TAXA_AMOSTRAGEM = 44100f;

    private boolean efeitosLigados = true;
    private boolean musicaLigada = false;
    private double volumeMusica = 0.25;

    private Thread threadMusica;
    private volatile boolean musicaTocando = false;

    public void setEfeitosLigados(boolean ligado) {
        this.efeitosLigados = ligado;
    }

    public boolean isEfeitosLigados() {
        return efeitosLigados;
    }

    public void setVolumeMusica(double volume) {
        this.volumeMusica = Math.max(0, Math.min(1, volume));
    }

    public double getVolumeMusica() {
        return volumeMusica;
    }

    public boolean isMusicaLigada() {
        return musicaLigada;
    }

    public void setMusicaLigada(boolean ligada) {
        this.musicaLigada = ligada;
        if (ligada) {
            iniciarMusica();
        } else {
            pararMusica();
        }
    }

    // ---- efeitos curtos ----

    public void tocarClique() {
        tocarSeLigado(() -> tocarTom(880, 40, 0.15));
    }

    public void tocarBandeira() {
        tocarSeLigado(() -> tocarTom(520, 60, 0.18));
    }

    public void tocarExplosao() {
        tocarSeLigado(() -> {
            tocarTom(160, 220, 0.35);
            tocarTom(90, 260, 0.30);
        });
    }

    public void tocarVitoria() {
        tocarSeLigado(() -> {
            int[] notas = {523, 659, 784, 1046};
            for (int nota : notas) {
                tocarTom(nota, 110, 0.22);
            }
        });
    }

    public void tocarDica() {
        tocarSeLigado(() -> {
            tocarTom(700, 70, 0.15);
            tocarTom(1000, 90, 0.15);
        });
    }

    private void tocarSeLigado(Runnable acao) {
        if (!efeitosLigados) {
            return;
        }
        new Thread(() -> {
            try {
                acao.run();
            } catch (Exception ignorada) {
                // som é auxiliar; qualquer erro de áudio é ignorado.
            }
        }).start();
    }

    private void tocarTom(double frequenciaHz, int duracaoMs, double amplitude) {
        try {
            AudioFormat formato = new AudioFormat(TAXA_AMOSTRAGEM, 8, 1, true, true);
            try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                linha.open(formato);
                linha.start();
                int numAmostras = (int) (TAXA_AMOSTRAGEM * duracaoMs / 1000.0);
                byte[] buffer = new byte[numAmostras];
                for (int i = 0; i < numAmostras; i++) {
                    double angulo = 2.0 * Math.PI * i * frequenciaHz / TAXA_AMOSTRAGEM;
                    buffer[i] = (byte) (Math.sin(angulo) * amplitude * 100);
                }
                linha.write(buffer, 0, buffer.length);
                linha.drain();
            }
        } catch (Exception ignorada) {
            // ambiente sem suporte a áudio: ignora silenciosamente.
        }
    }

    // ---- música ambiente em loop (drone suave em segundo plano) ----

    private void iniciarMusica() {
        if (musicaTocando) {
            return;
        }
        musicaTocando = true;
        threadMusica = new Thread(() -> {
            try {
                AudioFormat formato = new AudioFormat(TAXA_AMOSTRAGEM, 8, 1, true, true);
                try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                    linha.open(formato);
                    linha.start();
                    double[] acordeHz = {110, 130.8, 164.8};
                    int duracaoMs = 4000;
                    while (musicaTocando) {
                        int numAmostras = (int) (TAXA_AMOSTRAGEM * duracaoMs / 1000.0);
                        byte[] buffer = new byte[numAmostras];
                        for (int i = 0; i < numAmostras; i++) {
                            double soma = 0;
                            for (double freq : acordeHz) {
                                soma += Math.sin(2.0 * Math.PI * i * freq / TAXA_AMOSTRAGEM);
                            }
                            buffer[i] = (byte) (soma / acordeHz.length * volumeMusica * 40);
                        }
                        linha.write(buffer, 0, buffer.length);
                    }
                }
            } catch (Exception ignorada) {
                // sem suporte a áudio: encerra a thread silenciosamente.
            }
        }, "musica-ambiente");
        threadMusica.setDaemon(true);
        threadMusica.start();
    }

    private void pararMusica() {
        musicaTocando = false;
    }
}
