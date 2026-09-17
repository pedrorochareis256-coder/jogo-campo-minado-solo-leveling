package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda o histórico de partidas de um perfil em um arquivo CSV simples
 * dentro da pasta do usuário. Cobre três sugestões relacionadas:
 * <ul>
 *   <li>31 — histórico de partidas (vitórias, derrotas ao longo do tempo);</li>
 *   <li>30 — melhor tempo registrado, separado por dificuldade;</li>
 *   <li>32 — exportar estatísticas para um arquivo CSV;</li>
 *   <li>42 — ranking dos melhores tempos, com algoritmo de ordenação
 *       implementado manualmente (inserção), sem usar
 *       {@code Collections.sort} para essa parte.</li>
 * </ul>
 */
public class HistoricoPartidas {

    private final Path arquivo;

    public HistoricoPartidas(String nomePerfil) {
        Path pasta = Paths.get(System.getProperty("user.home"), ".campo_minado_solo_leveling");
        try {
            Files.createDirectories(pasta);
        } catch (IOException ignorada) {
            // se não conseguir criar a pasta, as operações de arquivo abaixo falham
            // silenciosamente e o jogo simplesmente não persiste o histórico.
        }
        String nomeArquivo = "historico_" + nomePerfil.replaceAll("[^a-zA-Z0-9_-]", "_") + ".csv";
        this.arquivo = pasta.resolve(nomeArquivo);
    }

    public synchronized void registrar(RegistroPartida registro) {
        boolean existeArquivo = Files.exists(arquivo);
        try (BufferedWriter escritor = Files.newBufferedWriter(
                arquivo, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (!existeArquivo) {
                escritor.write(RegistroPartida.cabecalhoCsv());
                escritor.newLine();
            }
            escritor.write(registro.toLinhaCsv());
            escritor.newLine();
        } catch (IOException ignorada) {
            // histórico é um extra; uma falha de disco não deve derrubar o jogo.
        }
    }

    public synchronized List<RegistroPartida> listarTudo() {
        List<RegistroPartida> registros = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return registros;
        }
        try (BufferedReader leitor = Files.newBufferedReader(arquivo)) {
            String linha = leitor.readLine(); // cabeçalho
            while ((linha = leitor.readLine()) != null) {
                if (!linha.isBlank()) {
                    registros.add(RegistroPartida.deLinhaCsv(linha));
                }
            }
        } catch (IOException ignorada) {
            // se der erro de leitura, retorna o que já foi lido até então.
        }
        return registros;
    }

    /** Melhor tempo (em segundos) já registrado em uma vitória para a dificuldade informada. */
    public Optional<Integer> melhorTempo(String dificuldade) {
        int melhor = Integer.MAX_VALUE;
        boolean encontrado = false;
        for (RegistroPartida registro : listarTudo()) {
            if (registro.vitoria && registro.dificuldade.equals(dificuldade) && registro.tempoSegundos < melhor) {
                melhor = registro.tempoSegundos;
                encontrado = true;
            }
        }
        return encontrado ? Optional.of(melhor) : Optional.empty();
    }

    /**
     * Ranking das {@code topN} vitórias mais rápidas de uma dificuldade,
     * ordenadas manualmente por inserção (sugestão 42 pede um algoritmo
     * de ordenação implementado à mão, não {@code Collections.sort}).
     */
    public List<RegistroPartida> ranking(String dificuldade, int topN) {
        List<RegistroPartida> vitorias = new ArrayList<>();
        for (RegistroPartida registro : listarTudo()) {
            if (registro.vitoria && registro.dificuldade.equals(dificuldade)) {
                vitorias.add(registro);
            }
        }
        ordenarPorTempoInsercao(vitorias);
        return vitorias.subList(0, Math.min(topN, vitorias.size()));
    }

    /** Ordenação por inserção, crescente por tempoSegundos — implementada manualmente. */
    private void ordenarPorTempoInsercao(List<RegistroPartida> lista) {
        for (int i = 1; i < lista.size(); i++) {
            RegistroPartida atual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && lista.get(j).tempoSegundos > atual.tempoSegundos) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, atual);
        }
    }

    /** Exporta o histórico completo para um arquivo CSV escolhido pelo usuário. */
    public boolean exportarPara(File destino) {
        try {
            Files.copy(arquivo, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Path getArquivo() {
        return arquivo;
    }
}
