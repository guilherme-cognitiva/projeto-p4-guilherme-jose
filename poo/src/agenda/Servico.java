package agenda;

public final class Servico {

    private final String codigo;
    private final String nome;
    private final int duracaoEmMinutos;

    public Servico(String codigo, String nome, int duracaoEmMinutos) {
        this.codigo = codigo;
        this.nome = nome;
        this.duracaoEmMinutos = duracaoEmMinutos;
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    public int duracaoEmMinutos() {
        return duracaoEmMinutos;
    }

    public Intervalo intervaloIniciandoEm(Horario inicio) {
        return new Intervalo(inicio, inicio.mais(duracaoEmMinutos));
    }

    @Override
    public String toString() {
        return codigo + " (" + nome + ", " + duracaoEmMinutos + " min)";
    }
}

final class Cliente {

    private final String codigo;
    private final String nome;

    public Cliente(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    @Override
    public String toString() {
        return codigo;
    }
}
