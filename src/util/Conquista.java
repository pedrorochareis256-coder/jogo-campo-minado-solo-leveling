package util;

/** Uma conquista/badge desbloqueável (sugestão 50), temática de Solo Leveling. */
public enum Conquista {

    PRIMEIRO_PORTAL("Primeiro Portal", "Venceu sua primeira missão."),
    SEM_BANDEIRA("Instinto de Caçador", "Venceu uma partida sem usar nenhuma bandeira."),
    RELAMPAGO_30S("Passo Relâmpago", "Venceu uma partida em menos de 30 segundos."),
    SOBREVIVENTE("Regeneração de Sombra", "Sobreviveu a um acerto de mina no modo 3 Vidas e ainda venceu."),
    RANK_S("Caçador Rank S", "Venceu uma missão na dificuldade Avançado (Rank S)."),
    DEZ_VITORIAS("Monarca em Ascensão", "Venceu 10 missões."),
    PORTAL_DIARIO("Portal Diário Fechado", "Completou o desafio diário do dia.");

    public final String titulo;
    public final String descricao;

    Conquista(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }
}
