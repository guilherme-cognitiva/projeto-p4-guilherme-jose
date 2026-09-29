package agenda;

public final class Resultado {

    private final Agendamento agendamento;
    private final Motivo motivo;

    private Resultado(Agendamento agendamento, Motivo motivo) {
        this.agendamento = agendamento;
        this.motivo = motivo;
    }

    public static Resultado aceito(Agendamento agendamento) {
        return new Resultado(agendamento, null);
    }

    public static Resultado recusado(Motivo motivo) {
        return new Resultado(null, motivo);
    }

    public boolean foiAceito() {
        return motivo == null;
    }

    public Agendamento agendamento() {
        return agendamento;
    }

    public Motivo motivo() {
        return motivo;
    }

    @Override
    public String toString() {
        return foiAceito()
            ? "ACEITO - " + agendamento.identificador()
            : "RECUSADO - " + motivo;
    }
}

enum Motivo {
    ENTIDADE_INEXISTENTE,
    HORARIO_FORA_DA_GRADE,
    AGENDAMENTO_JA_CANCELADO,
    SERVICO_NAO_OFERECIDO,
    HORARIO_NO_PASSADO,
    ANTECEDENCIA_MINIMA_NAO_ATENDIDA,
    ALEM_DA_JANELA_DE_AGENDAMENTO,
    FORA_DA_JORNADA,
    CONFLITO_BLOQUEIO,
    CONFLITO_PROFISSIONAL,
    CONFLITO_CLIENTE,
    CANCELAMENTO_FORA_DO_PRAZO
}
