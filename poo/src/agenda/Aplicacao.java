package agenda;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public final class Aplicacao {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Relogio relogio = Relogio.doSistema();
    private final Agenda agenda = CenarioBase.montar(relogio);
    private final Scanner teclado = new Scanner(System.in);

    public static void main(String[] argumentos) {
        if (argumentos.length > 0 && argumentos[0].equals("--testes")) {
            System.exit(Testes.executar());
        }
        new Aplicacao().rodar();
    }

    private void rodar() {
        boolean encerrar = false;
        while (!encerrar) {
            mostrarMenu();
            switch (ler("\nOpcao: ")) {
                case "1" -> consultarHorarios();
                case "2" -> agendar();
                case "3" -> listarAgendaDoDia();
                case "4" -> listarAgendamentosDoCliente();
                case "5" -> cancelar();
                case "6" -> remarcar();
                case "0" -> encerrar = true;
                default -> System.out.println("\nOpcao invalida.");
            }
        }
        System.out.println("\nEncerrado.");
    }

    private void mostrarMenu() {
        System.out.println("\n=== SISTEMA DE AGENDAMENTO (ORIENTADO A OBJETOS) ===");
        System.out.println("Data e hora atuais: " + relogio.agora().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n");
        System.out.println(" 1 - Consultar horarios livres");
        System.out.println(" 2 - Agendar atendimento");
        System.out.println(" 3 - Agenda do dia");
        System.out.println(" 4 - Agendamentos de um cliente");
        System.out.println(" 5 - Cancelar agendamento");
        System.out.println(" 6 - Remarcar agendamento");
        System.out.println(" 0 - Sair");
    }

    private void consultarHorarios() {
        System.out.println("\n--- CONSULTAR HORARIOS LIVRES ---");
        String profissional = ler("Profissional: ");
        String servico = ler("Servico: ");
        LocalDate data = lerData("Dia (DD/MM/AAAA): ");
        if (data == null) {
            return;
        }
        Optional<List<Horario>> livres = agenda.horariosLivres(profissional, servico, data);
        if (livres.isEmpty()) {
            System.out.println("\n>> RECUSADO - " + Motivo.ENTIDADE_INEXISTENTE);
            return;
        }
        if (livres.get().isEmpty()) {
            System.out.println("\nNenhum horario disponivel nesse dia.");
            return;
        }
        System.out.println("\nHorarios livres (" + livres.get().size() + "):");
        int coluna = 0;
        for (Horario horario : livres.get()) {
            System.out.print("  " + horario);
            coluna++;
            if (coluna % 8 == 0) {
                System.out.println();
            }
        }
        if (coluna % 8 != 0) {
            System.out.println();
        }
    }

    private void agendar() {
        System.out.println("\n--- AGENDAR ATENDIMENTO ---");
        String profissional = ler("Profissional: ");
        String servico = ler("Servico: ");
        LocalDate data = lerData("Dia (DD/MM/AAAA): ");
        if (data == null) {
            return;
        }
        Horario inicio = lerHorario("Horario (HH:MM): ");
        if (inicio == null) {
            return;
        }
        String cliente = ler("Cliente: ");
        System.out.println("\n>> " + agenda.agendar(cliente, profissional, servico, data, inicio));
    }

    private void listarAgendaDoDia() {
        System.out.println("\n--- AGENDA DO DIA ---");
        String profissional = ler("Profissional: ");
        LocalDate data = lerData("Dia (DD/MM/AAAA): ");
        if (data == null) {
            return;
        }
        mostrar(agenda.agendaDoDia(profissional, data));
    }

    private void listarAgendamentosDoCliente() {
        System.out.println("\n--- AGENDAMENTOS DO CLIENTE ---");
        mostrar(agenda.agendamentosDoCliente(ler("Cliente: ")));
    }

    private void mostrar(Optional<List<Agendamento>> lista) {
        if (lista.isEmpty()) {
            System.out.println("\n>> RECUSADO - " + Motivo.ENTIDADE_INEXISTENTE);
            return;
        }
        if (lista.get().isEmpty()) {
            System.out.println("\nNenhum agendamento encontrado.");
            return;
        }
        System.out.println();
        for (Agendamento agendamento : lista.get()) {
            System.out.println("  " + agendamento);
        }
    }

    private void cancelar() {
        System.out.println("\n--- CANCELAR AGENDAMENTO ---");
        System.out.println("\n>> " + agenda.cancelar(ler("Agendamento (AG-0000): ")));
    }

    private void remarcar() {
        System.out.println("\n--- REMARCAR AGENDAMENTO ---");
        String identificador = ler("Agendamento (AG-0000): ");
        LocalDate data = lerData("Novo dia (DD/MM/AAAA): ");
        if (data == null) {
            return;
        }
        Horario inicio = lerHorario("Novo horario (HH:MM): ");
        if (inicio == null) {
            return;
        }
        System.out.println("\n>> " + agenda.remarcar(identificador, data, inicio));
    }

    private String ler(String rotulo) {
        System.out.print(rotulo);
        return teclado.hasNextLine() ? teclado.nextLine().trim() : "";
    }

    private LocalDate lerData(String rotulo) {
        try {
            return LocalDate.parse(ler(rotulo), FORMATO);
        } catch (Exception erro) {
            System.out.println("\n>> RECUSADO - " + Motivo.HORARIO_FORA_DA_GRADE);
            return null;
        }
    }

    private Horario lerHorario(String rotulo) {
        Horario horario = Horario.analisar(ler(rotulo));
        if (horario == null) {
            System.out.println("\n>> RECUSADO - " + Motivo.HORARIO_FORA_DA_GRADE);
        }
        return horario;
    }
}

final class CenarioBase {

    public static final LocalDate HOJE = LocalDate.of(2026, 10, 5);
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 5, 8, 0);

    private CenarioBase() {
    }

    public static Agenda montar() {
        return montar(Relogio.fixoEm(AGORA));
    }

    public static Agenda montar(Relogio relogio) {
        LocalDate hoje = relogio.agora().toLocalDate();
        Cadastro cadastro = new Cadastro();

        Servico corte = new Servico("S1", "Corte", 30);
        Servico coloracao = new Servico("S2", "Coloracao", 90);
        Servico barba = new Servico("S3", "Barba", 15);
        cadastro.incluir(corte);
        cadastro.incluir(coloracao);
        cadastro.incluir(barba);

        Cliente c1 = new Cliente("C1", "Cliente 1");
        Cliente c2 = new Cliente("C2", "Cliente 2");
        cadastro.incluir(c1);
        cadastro.incluir(c2);

        Profissional p1 = new Profissional("P1", "Profissional 1");
        p1.habilitar(corte);
        p1.habilitar(coloracao);
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            p1.trabalhar(dia, new Intervalo(Horario.de(9, 0), Horario.de(12, 0)));
            p1.trabalhar(dia, new Intervalo(Horario.de(13, 0), Horario.de(18, 0)));
        }
        cadastro.incluir(p1);

        Profissional p2 = new Profissional("P2", "Profissional 2");
        p2.habilitar(corte);
        p2.habilitar(barba);
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}) {
            p2.trabalhar(dia, new Intervalo(Horario.de(14, 0), Horario.de(18, 0)));
        }
        cadastro.incluir(p2);

        Agenda agenda = new Agenda(cadastro, relogio, Validador.padrao());
        agenda.bloquear(new Bloqueio(p1, hoje, new Intervalo(Horario.de(10, 0), Horario.de(11, 0))));
        agenda.incluirExistente(new Agendamento("AG-0001", c2, p1, corte, hoje, Horario.de(9, 0)));
        return agenda;
    }
}

final class Testes {

    private static int falhas = 0;

    private Testes() {
    }

    public static int executar() {
        falhas = 0;
        System.out.println("=== CASOS DE TESTE DA ETAPA 02 ===\n");
        System.out.println("-- Casos normais --");
        registrar("T01", "Consultar horarios livres", consultarHorarios());
        registrar("T02", "Agendar em horario livre",
            tentarAgendar("C1", "P1", "S1", CenarioBase.HOJE, Horario.de(14, 0), true, null));
        registrar("T03", "Agendar servico longo", agendarServicoLongo());
        registrar("T04", "Cancelar agendamento", cancelarAgendamento());
        registrar("T05", "Remarcar agendamento", remarcarAgendamento());
        registrar("T06", "Consultar a agenda do dia", consultarAgendaDoDia());
        registrar("T07", "Profissional nao executa o servico",
            tentarAgendar("C1", "P2", "S2", CenarioBase.HOJE, Horario.de(15, 0), false, Motivo.SERVICO_NAO_OFERECIDO));
        registrar("T08", "Servico nao cabe na jornada",
            tentarAgendar("C1", "P1", "S2", CenarioBase.HOJE, Horario.de(11, 0), false, Motivo.FORA_DA_JORNADA));
        registrar("T09", "Horario bloqueado",
            tentarAgendar("C1", "P1", "S1", CenarioBase.HOJE, Horario.de(10, 30), false, Motivo.CONFLITO_BLOQUEIO));
        registrar("T10", "Cliente com dois atendimentos", clienteOcupado());

        System.out.println("\n-- Casos-limite --");
        registrar("T11", "Agendar quando o bloqueio termina",
            tentarAgendar("C1", "P1", "S1", CenarioBase.HOJE, Horario.de(11, 0), true, null));
        registrar("T12", "Atendimento termina no fim da jornada",
            tentarAgendar("C1", "P1", "S1", CenarioBase.HOJE, Horario.de(11, 30), true, null));
        registrar("T13", "Consulta em dia sem expediente", diaSemExpediente());

        System.out.println("\n-- Casos de entrada invalida --");
        registrar("T14", "Horario fora da grade",
            tentarAgendar("C2", "P1", "S1", CenarioBase.HOJE, Horario.de(9, 20), false, Motivo.HORARIO_FORA_DA_GRADE));
        registrar("T15", "Cliente inexistente",
            tentarAgendar("C9", "P1", "S1", CenarioBase.HOJE, Horario.de(14, 0), false, Motivo.ENTIDADE_INEXISTENTE));

        if (falhas == 0) {
            System.out.println("\nRESULTADO: 15 de 15 casos passaram.");
        } else {
            System.out.println("\nRESULTADO: " + falhas + " caso(s) falharam.");
        }
        return falhas;
    }

    private static void registrar(String identificador, String descricao, boolean passou) {
        System.out.printf("[%s] %-42s %s%n", identificador, descricao, passou ? "PASSOU" : "FALHOU");
        if (!passou) {
            falhas++;
        }
    }

    private static boolean tentarAgendar(String cliente, String profissional, String servico,
                                         LocalDate data, Horario inicio,
                                         boolean aceitoEsperado, Motivo motivoEsperado) {
        Resultado resultado = CenarioBase.montar().agendar(cliente, profissional, servico, data, inicio);
        if (resultado.foiAceito() != aceitoEsperado) {
            return false;
        }
        return aceitoEsperado || resultado.motivo() == motivoEsperado;
    }

    private static boolean consultarHorarios() {
        int[] esperados = {570, 660, 675, 690, 780, 795, 810, 825, 840, 855, 870, 885,
                           900, 915, 930, 945, 960, 975, 990, 1005, 1020, 1035, 1050};
        Optional<List<Horario>> livres = CenarioBase.montar().horariosLivres("P1", "S1", CenarioBase.HOJE);
        if (livres.isEmpty() || livres.get().size() != esperados.length) {
            return false;
        }
        for (int i = 0; i < esperados.length; i++) {
            if (livres.get().get(i).minutos() != esperados[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean agendarServicoLongo() {
        Agenda agenda = CenarioBase.montar();
        if (!agenda.agendar("C1", "P1", "S2", CenarioBase.HOJE, Horario.de(13, 0)).foiAceito()) {
            return false;
        }
        for (Horario horario : agenda.horariosLivres("P1", "S1", CenarioBase.HOJE).orElseThrow()) {
            if (horario.minutos() >= 13 * 60 && horario.minutos() < 14 * 60 + 30) {
                return false;
            }
        }
        return true;
    }

    private static boolean cancelarAgendamento() {
        Agenda agenda = CenarioBase.montar();
        boolean agendou = agenda.agendar("C1", "P1", "S1", CenarioBase.HOJE, Horario.de(14, 0)).foiAceito();
        boolean cancelou = agenda.cancelar("AG-0002").foiAceito();
        return agendou && cancelou;
    }

    private static boolean remarcarAgendamento() {
        Agenda agenda = CenarioBase.montar();
        Resultado resultado = agenda.remarcar("AG-0001", CenarioBase.HOJE, Horario.de(15, 0));
        return resultado.foiAceito()
            && agenda.buscar("AG-0001").orElseThrow().inicio().equals(Horario.de(15, 0));
    }

    private static boolean consultarAgendaDoDia() {
        List<Agendamento> doDia = CenarioBase.montar()
            .agendaDoDia("P1", CenarioBase.HOJE).orElseThrow();
        return doDia.size() == 1 && doDia.get(0).identificador().equals("AG-0001");
    }

    private static boolean clienteOcupado() {
        Agenda agenda = CenarioBase.montar();
        Resultado primeiro = agenda.agendar("C1", "P1", "S1", CenarioBase.HOJE, Horario.de(15, 0));
        Resultado segundo = agenda.agendar("C1", "P2", "S3", CenarioBase.HOJE, Horario.de(15, 15));
        return primeiro.foiAceito() && !segundo.foiAceito()
            && segundo.motivo() == Motivo.CONFLITO_CLIENTE;
    }

    private static boolean diaSemExpediente() {
        Optional<List<Horario>> livres = CenarioBase.montar()
            .horariosLivres("P2", "S1", LocalDate.of(2026, 10, 6));
        return livres.isPresent() && livres.get().isEmpty();
    }
}
