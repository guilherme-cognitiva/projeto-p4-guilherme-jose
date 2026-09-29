package agenda;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Profissional {

    private final String codigo;
    private final String nome;
    private final Jornada jornada = new Jornada();
    private final Set<Servico> servicos = new LinkedHashSet<>();

    public Profissional(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    public void habilitar(Servico servico) {
        servicos.add(servico);
    }

    public void trabalhar(DayOfWeek dia, Intervalo janela) {
        jornada.adicionar(dia, janela);
    }

    public boolean executa(Servico servico) {
        return servicos.contains(servico);
    }

    public boolean atendeEm(LocalDate data, Intervalo intervalo) {
        return jornada.comporta(data.getDayOfWeek(), intervalo);
    }

    public List<Intervalo> janelasDe(LocalDate data) {
        return jornada.janelasDe(data.getDayOfWeek());
    }

    @Override
    public String toString() {
        return codigo;
    }
}

final class Jornada {

    private final Map<DayOfWeek, List<Intervalo>> janelasPorDia = new EnumMap<>(DayOfWeek.class);

    public void adicionar(DayOfWeek dia, Intervalo janela) {
        janelasPorDia.computeIfAbsent(dia, chave -> new ArrayList<>()).add(janela);
    }

    public List<Intervalo> janelasDe(DayOfWeek dia) {
        return Collections.unmodifiableList(janelasPorDia.getOrDefault(dia, List.of()));
    }

    public boolean comporta(DayOfWeek dia, Intervalo intervalo) {
        for (Intervalo janela : janelasDe(dia)) {
            if (intervalo.cabeEm(janela)) {
                return true;
            }
        }
        return false;
    }
}
