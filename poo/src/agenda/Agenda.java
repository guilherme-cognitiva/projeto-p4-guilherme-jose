package agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Agenda {

    private final Cadastro cadastro;
    private final Relogio relogio;
    private final Validador validador;
    private final List<Agendamento> agendamentos = new ArrayList<>();
    private final List<Bloqueio> bloqueios = new ArrayList<>();
    private int proximoNumero = 1;

    public Agenda(Cadastro cadastro, Relogio relogio, Validador validador) {
        this.cadastro = cadastro;
        this.relogio = relogio;
        this.validador = validador;
    }

    public Cadastro cadastro() {
        return cadastro;
    }

    public void bloquear(Bloqueio bloqueio) {
        bloqueios.add(bloqueio);
    }

    public List<Compromisso> compromissos() {
        List<Compromisso> todos = new ArrayList<>(agendamentos);
        todos.addAll(bloqueios);
        return todos;
    }

    public Resultado agendar(String codigoCliente, String codigoProfissional,
                             String codigoServico, LocalDate data, Horario inicio) {
        Optional<Cliente> cliente = cadastro.cliente(codigoCliente);
        Optional<Profissional> profissional = cadastro.profissional(codigoProfissional);
        Optional<Servico> servico = cadastro.servico(codigoServico);
        if (cliente.isEmpty() || profissional.isEmpty() || servico.isEmpty()) {
            return Resultado.recusado(Motivo.ENTIDADE_INEXISTENTE);
        }

        Pedido pedido = new Pedido(cliente.get(), profissional.get(), servico.get(),
            data, inicio, compromissos(), relogio, null);
        Optional<Motivo> recusa = validador.reprovar(pedido);
        if (recusa.isPresent()) {
            return Resultado.recusado(recusa.get());
        }

        Agendamento novo = new Agendamento(proximoIdentificador(), cliente.get(),
            profissional.get(), servico.get(), data, inicio);
        agendamentos.add(novo);
        return Resultado.aceito(novo);
    }

    public Resultado cancelar(String identificador) {
        Optional<Agendamento> encontrado = buscar(identificador);
        if (encontrado.isEmpty()) {
            return Resultado.recusado(Motivo.ENTIDADE_INEXISTENTE);
        }
        Agendamento agendamento = encontrado.get();
        if (!agendamento.vigente()) {
            return Resultado.recusado(Motivo.AGENDAMENTO_JA_CANCELADO);
        }
        long horas = ChronoUnit.HOURS.between(relogio.agora(), agendamento.comecaEm());
        if (horas < Regras.PRAZO_DE_CANCELAMENTO_EM_HORAS) {
            return Resultado.recusado(Motivo.CANCELAMENTO_FORA_DO_PRAZO);
        }
        agendamento.cancelar();
        return Resultado.aceito(agendamento);
    }

    public Resultado remarcar(String identificador, LocalDate novaData, Horario novoInicio) {
        Optional<Agendamento> encontrado = buscar(identificador);
        if (encontrado.isEmpty()) {
            return Resultado.recusado(Motivo.ENTIDADE_INEXISTENTE);
        }
        Agendamento agendamento = encontrado.get();
        if (!agendamento.vigente()) {
            return Resultado.recusado(Motivo.AGENDAMENTO_JA_CANCELADO);
        }

        Pedido pedido = new Pedido(agendamento.cliente(), agendamento.profissional(),
            agendamento.servico(), novaData, novoInicio, compromissos(), relogio, agendamento);
        Optional<Motivo> recusa = validador.reprovar(pedido);
        if (recusa.isPresent()) {
            return Resultado.recusado(recusa.get());
        }

        agendamento.remarcarPara(novaData, novoInicio);
        return Resultado.aceito(agendamento);
    }

    public Optional<List<Horario>> horariosLivres(String codigoProfissional,
                                                  String codigoServico, LocalDate data) {
        Optional<Profissional> profissional = cadastro.profissional(codigoProfissional);
        Optional<Servico> servico = cadastro.servico(codigoServico);
        if (profissional.isEmpty() || servico.isEmpty()) {
            return Optional.empty();
        }

        List<Horario> livres = new ArrayList<>();
        for (Intervalo janela : profissional.get().janelasDe(data)) {
            Horario candidato = janela.inicio();
            while (candidato.mais(servico.get().duracaoEmMinutos()).minutos() <= janela.fim().minutos()) {
                Pedido pedido = new Pedido(null, profissional.get(), servico.get(),
                    data, candidato, compromissos(), relogio, null);
                if (validador.reprovar(pedido).isEmpty()) {
                    livres.add(candidato);
                }
                candidato = candidato.mais(Regras.GRADE_EM_MINUTOS);
            }
        }
        return Optional.of(livres);
    }

    public Optional<List<Agendamento>> agendaDoDia(String codigoProfissional, LocalDate data) {
        Optional<Profissional> profissional = cadastro.profissional(codigoProfissional);
        if (profissional.isEmpty()) {
            return Optional.empty();
        }
        List<Agendamento> doDia = new ArrayList<>();
        for (Agendamento agendamento : agendamentos) {
            if (agendamento.vigente() && agendamento.ocupaAgendaDe(profissional.get())
                && agendamento.data().equals(data)) {
                doDia.add(agendamento);
            }
        }
        doDia.sort(Comparator.comparing(Agendamento::inicio));
        return Optional.of(doDia);
    }

    public Optional<List<Agendamento>> agendamentosDoCliente(String codigoCliente) {
        Optional<Cliente> cliente = cadastro.cliente(codigoCliente);
        if (cliente.isEmpty()) {
            return Optional.empty();
        }
        List<Agendamento> doCliente = new ArrayList<>();
        for (Agendamento agendamento : agendamentos) {
            if (agendamento.vigente() && agendamento.ocupaAgendaDe(cliente.get())) {
                doCliente.add(agendamento);
            }
        }
        doCliente.sort(Comparator.comparing(Agendamento::data).thenComparing(Agendamento::inicio));
        return Optional.of(doCliente);
    }

    public Optional<Agendamento> buscar(String identificador) {
        for (Agendamento agendamento : agendamentos) {
            if (agendamento.identificador().equals(identificador)) {
                return Optional.of(agendamento);
            }
        }
        return Optional.empty();
    }

    void incluirExistente(Agendamento agendamento) {
        agendamentos.add(agendamento);
        proximoNumero++;
    }

    private String proximoIdentificador() {
        return String.format("AG-%04d", proximoNumero++);
    }
}

final class Cadastro {

    private final Map<String, Servico> servicos = new LinkedHashMap<>();
    private final Map<String, Profissional> profissionais = new LinkedHashMap<>();
    private final Map<String, Cliente> clientes = new LinkedHashMap<>();

    public void incluir(Servico servico) {
        servicos.put(servico.codigo(), servico);
    }

    public void incluir(Profissional profissional) {
        profissionais.put(profissional.codigo(), profissional);
    }

    public void incluir(Cliente cliente) {
        clientes.put(cliente.codigo(), cliente);
    }

    public Optional<Servico> servico(String codigo) {
        return Optional.ofNullable(servicos.get(codigo));
    }

    public Optional<Profissional> profissional(String codigo) {
        return Optional.ofNullable(profissionais.get(codigo));
    }

    public Optional<Cliente> cliente(String codigo) {
        return Optional.ofNullable(clientes.get(codigo));
    }
}

interface Relogio {

    LocalDateTime agora();

    static Relogio fixoEm(LocalDateTime instante) {
        return () -> instante;
    }

    static Relogio doSistema() {
        return () -> LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    }
}
