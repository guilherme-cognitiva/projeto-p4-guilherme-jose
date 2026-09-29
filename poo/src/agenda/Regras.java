package agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public final class Regras {

    public static final int GRADE_EM_MINUTOS = 15;
    public static final int ANTECEDENCIA_MINIMA_EM_MINUTOS = 30;
    public static final int PRAZO_DE_CANCELAMENTO_EM_HORAS = 2;
    public static final int HORIZONTE_EM_DIAS = 60;

    private Regras() {
    }

    public static List<Regra> todas() {
        return List.of(
            new HorarioNaGrade(),
            new ServicoOferecido(),
            new ForaDoPassado(),
            new AntecedenciaMinima(),
            new DentroDoHorizonte(),
            new DentroDaJornada(),
            new SemBloqueio(),
            new ProfissionalLivre(),
            new ClienteLivre()
        );
    }

    public static final class HorarioNaGrade implements Regra {
        @Override
        public boolean aprova(Pedido pedido) {
            return pedido.inicio().estaNaGrade(GRADE_EM_MINUTOS);
        }

        @Override
        public Motivo motivo() {
            return Motivo.HORARIO_FORA_DA_GRADE;
        }
    }

    public static final class ServicoOferecido implements Regra {
        @Override
        public boolean aprova(Pedido pedido) {
            return pedido.profissional().executa(pedido.servico());
        }

        @Override
        public Motivo motivo() {
            return Motivo.SERVICO_NAO_OFERECIDO;
        }
    }

    public static final class ForaDoPassado implements Regra {
        @Override
        public boolean aprova(Pedido pedido) {
            return !pedido.comecaEm().isBefore(pedido.relogio().agora());
        }

        @Override
        public Motivo motivo() {
            return Motivo.HORARIO_NO_PASSADO;
        }
    }

    public static final class AntecedenciaMinima implements Regra {
        @Override
        public boolean aprova(Pedido pedido) {
            long minutos = ChronoUnit.MINUTES.between(pedido.relogio().agora(), pedido.comecaEm());
            return minutos >= ANTECEDENCIA_MINIMA_EM_MINUTOS;
        }

        @Override
        public Motivo motivo() {
            return Motivo.ANTECEDENCIA_MINIMA_NAO_ATENDIDA;
        }
    }

    public static final class DentroDoHorizonte implements Regra {
        @Override
        public boolean aprova(Pedido pedido) {
            long dias = ChronoUnit.DAYS.between(pedido.relogio().agora().toLocalDate(), pedido.data());
            return dias <= HORIZONTE_EM_DIAS;
        }

        @Override
        public Motivo motivo() {
            return Motivo.ALEM_DA_JANELA_DE_AGENDAMENTO;
        }
    }

    public static final class DentroDaJornada implements Regra {
        @Override
        public boolean aprova(Pedido pedido) {
            return pedido.profissional().atendeEm(pedido.data(), pedido.intervalo());
        }

        @Override
        public Motivo motivo() {
            return Motivo.FORA_DA_JORNADA;
        }
    }

    public abstract static class Conflito implements Regra {

        @Override
        public final boolean aprova(Pedido pedido) {
            for (Compromisso compromisso : pedido.compromissos()) {
                if (pedido.deveIgnorar(compromisso) || !interessa(compromisso, pedido)) {
                    continue;
                }
                if (compromisso.colideCom(pedido.data(), pedido.intervalo())) {
                    return false;
                }
            }
            return true;
        }

        protected abstract boolean interessa(Compromisso compromisso, Pedido pedido);
    }

    public static final class SemBloqueio extends Conflito {
        @Override
        protected boolean interessa(Compromisso compromisso, Pedido pedido) {
            return compromisso instanceof Bloqueio
                && compromisso.ocupaAgendaDe(pedido.profissional());
        }

        @Override
        public Motivo motivo() {
            return Motivo.CONFLITO_BLOQUEIO;
        }
    }

    public static final class ProfissionalLivre extends Conflito {
        @Override
        protected boolean interessa(Compromisso compromisso, Pedido pedido) {
            return compromisso instanceof Agendamento
                && compromisso.ocupaAgendaDe(pedido.profissional());
        }

        @Override
        public Motivo motivo() {
            return Motivo.CONFLITO_PROFISSIONAL;
        }
    }

    public static final class ClienteLivre extends Conflito {
        @Override
        protected boolean interessa(Compromisso compromisso, Pedido pedido) {
            return pedido.cliente() != null
                && compromisso instanceof Agendamento
                && compromisso.ocupaAgendaDe(pedido.cliente());
        }

        @Override
        public Motivo motivo() {
            return Motivo.CONFLITO_CLIENTE;
        }
    }
}

interface Regra {

    boolean aprova(Pedido pedido);

    Motivo motivo();
}

final class Pedido {

    private final Cliente cliente;
    private final Profissional profissional;
    private final Servico servico;
    private final LocalDate data;
    private final Horario inicio;
    private final List<Compromisso> compromissos;
    private final Relogio relogio;
    private final Agendamento ignorado;

    public Pedido(Cliente cliente, Profissional profissional, Servico servico, LocalDate data,
                  Horario inicio, List<Compromisso> compromissos, Relogio relogio, Agendamento ignorado) {
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.data = data;
        this.inicio = inicio;
        this.compromissos = compromissos;
        this.relogio = relogio;
        this.ignorado = ignorado;
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

    public LocalDate data() {
        return data;
    }

    public Horario inicio() {
        return inicio;
    }

    public List<Compromisso> compromissos() {
        return compromissos;
    }

    public Relogio relogio() {
        return relogio;
    }

    public boolean deveIgnorar(Compromisso compromisso) {
        return ignorado != null && ignorado == compromisso;
    }

    public Intervalo intervalo() {
        return servico.intervaloIniciandoEm(inicio);
    }

    public LocalDateTime comecaEm() {
        return LocalDateTime.of(data, LocalTime.of(inicio.minutos() / 60, inicio.minutos() % 60));
    }
}

final class Validador {

    private final List<Regra> regras;

    public Validador(List<Regra> regras) {
        this.regras = List.copyOf(regras);
    }

    public static Validador padrao() {
        return new Validador(Regras.todas());
    }

    public Optional<Motivo> reprovar(Pedido pedido) {
        for (Regra regra : regras) {
            if (!regra.aprova(pedido)) {
                return Optional.of(regra.motivo());
            }
        }
        return Optional.empty();
    }
}
