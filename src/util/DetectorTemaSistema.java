package util;

/**
 * Detecta, de forma "melhor esforço", se o sistema operacional está
 * configurado em modo claro ou escuro (sugestão 7). Como o Swing puro não
 * tem uma API multiplataforma para isso, cada SO é consultado com o
 * comando apropriado; se a detecção falhar por qualquer motivo (SO não
 * reconhecido, comando ausente, permissão negada), o método assume modo
 * escuro — que é, de qualquer forma, o clima padrão do jogo.
 */
public final class DetectorTemaSistema {

    private DetectorTemaSistema() {
    }

    public static boolean sistemaEstaEmModoEscuro() {
        String so = System.getProperty("os.name", "").toLowerCase();
        try {
            if (so.contains("win")) {
                return detectarWindows();
            } else if (so.contains("mac")) {
                return detectarMac();
            } else {
                return detectarLinux();
            }
        } catch (Exception ignorada) {
            return true;
        }
    }

    private static boolean detectarWindows() throws Exception {
        Process processo = new ProcessBuilder(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"
        ).start();
        String saida = lerSaida(processo);
        processo.waitFor();
        // valor 0x0 = modo escuro; 0x1 = modo claro.
        return saida.contains("0x0");
    }

    private static boolean detectarMac() throws Exception {
        Process processo = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start();
        String saida = lerSaida(processo);
        processo.waitFor();
        return saida.toLowerCase().contains("dark");
    }

    private static boolean detectarLinux() throws Exception {
        Process processo = new ProcessBuilder(
                "gsettings", "get", "org.gnome.desktop.interface", "color-scheme"
        ).start();
        String saida = lerSaida(processo);
        processo.waitFor();
        if (saida.toLowerCase().contains("dark")) {
            return true;
        }
        if (saida.toLowerCase().contains("default") || saida.toLowerCase().contains("light")) {
            return false;
        }
        return true;
    }

    private static String lerSaida(Process processo) throws Exception {
        try (java.io.InputStream in = processo.getInputStream()) {
            return new String(in.readAllBytes());
        }
    }
}
