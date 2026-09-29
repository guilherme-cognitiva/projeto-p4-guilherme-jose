package agenda;

public final class Horario implements Comparable<Horario> {

    private final int minutos;

    private Horario(int minutos) {
        if (minutos < 0 || minutos >= 1440) {
            throw new IllegalArgumentException("Horario fora do dia: " + minutos);
        }
        this.minutos = minutos;
    }

    public static Horario de(int hora, int minuto) {
        return new Horario(hora * 60 + minuto);
    }

    public static Horario deMinutos(int minutos) {
        return new Horario(minutos);
    }

    public static Horario analisar(String texto) {
        String[] partes = texto.trim().split(":");
        if (partes.length != 2) {
            return null;
        }
        try {
            int hora = Integer.parseInt(partes[0]);
            int minuto = Integer.parseInt(partes[1]);
            if (hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
                return null;
            }
            return Horario.de(hora, minuto);
        } catch (NumberFormatException erro) {
            return null;
        }
    }

    public int minutos() {
        return minutos;
    }

    public Horario mais(int minutosAdiante) {
        return new Horario(minutos + minutosAdiante);
    }

    public boolean estaNaGrade(int passo) {
        return minutos % passo == 0;
    }

    public boolean antesDe(Horario outro) {
        return minutos < outro.minutos;
    }

    @Override
    public int compareTo(Horario outro) {
        return Integer.compare(minutos, outro.minutos);
    }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Horario && ((Horario) objeto).minutos == minutos;
    }

    @Override
    public int hashCode() {
        return minutos;
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", minutos / 60, minutos % 60);
    }
}

final class Intervalo {

    private final Horario inicio;
    private final Horario fim;

    public Intervalo(Horario inicio, Horario fim) {
        if (!inicio.antesDe(fim)) {
            throw new IllegalArgumentException("Intervalo invalido: " + inicio + "-" + fim);
        }
        this.inicio = inicio;
        this.fim = fim;
    }

    public Horario inicio() {
        return inicio;
    }

    public Horario fim() {
        return fim;
    }

    public boolean sobrepoe(Intervalo outro) {
        return inicio.minutos() < outro.fim.minutos()
            && outro.inicio.minutos() < fim.minutos();
    }

    public boolean cabeEm(Intervalo janela) {
        return inicio.minutos() >= janela.inicio.minutos()
            && fim.minutos() <= janela.fim.minutos();
    }

    @Override
    public String toString() {
        return inicio + "-" + fim;
    }
}
