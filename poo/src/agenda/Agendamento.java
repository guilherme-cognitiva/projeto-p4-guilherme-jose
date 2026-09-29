package agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class Agendamento implements Compromisso {

    public enum Situacao { ATIVO, CANCELADO }

    private final String identificador;
    private final Cliente cliente;
    private final Profissional profissional;
    private final Servico servico;
    private LocalDate data;
    private Horario inicio;
    private Situacao situacao;

    public Agendamento(String identificador, Cliente cliente, Profissional profissional,
                       Servico servico, LocalDate data, Horario inicio) {
        this.identificador = identificador;
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.data = data;
        this.inicio = inicio;
        this.situacao = Situacao.ATIVO;
    }

    public String identificador() {
        return identificador;
    }

    public Cliente cliente() {
        return cliente;
    }

    public Profissional profissional() {
        return profissional;
    }

    public Servico servico() {
        return servico;
    }

    public Horario inicio() {
        return inicio;
    }

    public Situacao situacao() {
        return situacao;
    }

    @Override
    public LocalDate data() {
        return data;
    }

    @Override
    public Intervalo intervalo() {
        return servico.intervaloIniciandoEm(inicio);
    }

    @Override
    public boolean vigente() {
        return situacao == Situacao.ATIVO;
    }

    @Override
    public boolean ocupaAgendaDe(Profissional outro) {
        return profissional.equals(outro);
    }

    @Override
    public boolean ocupaAgendaDe(Cliente outro) {
        return cliente.equals(outro);
    }

    public LocalDateTime comecaEm() {
        return LocalDateTime.of(data, LocalTime.of(inicio.minutos() / 60, inicio.minutos() % 60));
    }

    public void cancelar() {
        if (situacao == Situacao.CANCELADO) {
            throw new IllegalStateException("Agendamento ja cancelado: " + identificador);
        }
        situacao = Situacao.CANCELADO;
    }

    public void remarcarPara(LocalDate novaData, Horario novoInicio) {
        if (situacao == Situacao.CANCELADO) {
            throw new IllegalStateException("Agendamento cancelado nao pode ser remarcado");
        }
        this.data = novaData;
        this.inicio = novoInicio;
    }

    @Override
    public String toString() {
        return identificador + "  " + data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
             + "  " + intervalo() + "  " + cliente + "  " + profissional + "  " + servico.codigo();
    }
}

interface Compromisso {

    LocalDate data();

    Intervalo intervalo();

    boolean vigente();

    boolean ocupaAgendaDe(Profissional profissional);

    boolean ocupaAgendaDe(Cliente cliente);

    default boolean colideCom(LocalDate data, Intervalo intervalo) {
        return vigente() && data().equals(data) && intervalo().sobrepoe(intervalo);
    }
}

final class Bloqueio implements Compromisso {

    private final Profissional profissional;
    private final LocalDate data;
    private final Intervalo intervalo;

    public Bloqueio(Profissional profissional, LocalDate data, Intervalo intervalo) {
        this.profissional = profissional;
        this.data = data;
        this.intervalo = intervalo;
    }

    @Override
    public LocalDate data() {
        return data;
    }

    @Override
    public Intervalo intervalo() {
        return intervalo;
    }

    @Override
    public boolean vigente() {
        return true;
    }

    @Override
    public boolean ocupaAgendaDe(Profissional outro) {
        return profissional.equals(outro);
    }

    @Override
    public boolean ocupaAgendaDe(Cliente cliente) {
        return false;
    }

    @Override
    public String toString() {
        return "Bloqueio de " + profissional + " em " + data + " " + intervalo;
    }
}
