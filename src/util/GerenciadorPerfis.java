package util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Permite que vários jogadores usem o mesmo programa, cada um com seu
 * próprio histórico, recordes e conquistas (sugestão 33). Cada perfil é
 * apenas um nome; os dados de fato ficam separados porque
 * {@link HistoricoPartidas} e {@link ConquistasManager} recebem o nome do
 * perfil e usam arquivos/chaves próprias para cada um.
 */
public class GerenciadorPerfis {

    private final Preferences prefs = Preferences.userNodeForPackage(GerenciadorPerfis.class);
    private static final String PADRAO = "Caçador";

    public List<String> listarPerfis() {
        Set<String> perfis = new LinkedHashSet<>();
        perfis.add(PADRAO);
        String salvos = prefs.get("perfis", "");
        if (!salvos.isBlank()) {
            for (String nome : salvos.split(";")) {
                if (!nome.isBlank()) {
                    perfis.add(nome);
                }
            }
        }
        return new ArrayList<>(perfis);
    }

    public void criarPerfil(String nome) {
        String nomeLimpo = nome.trim();
        if (nomeLimpo.isEmpty()) {
            return;
        }
        List<String> perfis = listarPerfis();
        if (!perfis.contains(nomeLimpo)) {
            perfis.add(nomeLimpo);
            prefs.put("perfis", String.join(";", perfis));
        }
    }
}
