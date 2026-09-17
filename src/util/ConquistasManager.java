package util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Guarda quais {@link Conquista} já foram desbloqueadas por cada perfil de
 * jogador (sugestão 50), persistidas via {@link Preferences}.
 */
public class ConquistasManager {

    private final Preferences prefs = Preferences.userNodeForPackage(ConquistasManager.class);
    private final String nomePerfil;

    public ConquistasManager(String nomePerfil) {
        this.nomePerfil = nomePerfil;
    }

    private String chave() {
        return "conquistas_" + nomePerfil;
    }

    public Set<Conquista> listarDesbloqueadas() {
        Set<Conquista> resultado = new LinkedHashSet<>();
        String salvo = prefs.get(chave(), "");
        if (salvo.isBlank()) {
            return resultado;
        }
        for (String id : salvo.split(",")) {
            try {
                resultado.add(Conquista.valueOf(id));
            } catch (IllegalArgumentException ignorada) {
                // conquista desconhecida/antiga: ignora.
            }
        }
        return resultado;
    }

    /** Desbloqueia a conquista, se ainda não estiver desbloqueada. @return true se acabou de ser desbloqueada agora. */
    public boolean desbloquear(Conquista conquista) {
        Set<Conquista> atuais = listarDesbloqueadas();
        if (atuais.contains(conquista)) {
            return false;
        }
        atuais.add(conquista);
        StringBuilder construtor = new StringBuilder();
        for (Conquista c : atuais) {
            if (construtor.length() > 0) {
                construtor.append(",");
            }
            construtor.append(c.name());
        }
        prefs.put(chave(), construtor.toString());
        return true;
    }
}
