#include <stdio.h>
#include <string.h>
#include "agenda.h"

static Sistema sistema;

static Data dia(int d, int m, int a)
{
    Data data;
    data.dia = d;
    data.mes = m;
    data.ano = a;
    return data;
}

static int falhas = 0;

static void registrar(const char *identificador, const char *descricao, int passou)
{
    printf("[%s] %-42s %s\n", identificador, descricao, passou ? "PASSOU" : "FALHOU");
    if (!passou) {
        falhas = falhas + 1;
    }
}

static int tentarAgendar(const char *cliente, const char *profissional, const char *servico, Data data, int hora, int aceitoEsperado, Motivo motivoEsperado)
{
    Resultado resultado;
    carregarCadastro(&sistema);
    resultado = agendar(&sistema, cliente, profissional, servico, data, hora);
    if (resultado.aceito != aceitoEsperado) {
        return 0;
    }
    return aceitoEsperado || resultado.motivo == motivoEsperado;
}

static int casoConsultar(void)
{
    int esperados[23] = {570, 660, 675, 690, 780, 795, 810, 825, 840, 855, 870, 885, 900, 915, 930, 945, 960, 975, 990, 1005, 1020, 1035, 1050};
    int horarios[MAX_HORARIOS];
    Motivo motivo = SEM_MOTIVO;
    int total = 0;
    int i = 0;

    carregarCadastro(&sistema);
    total = consultarDisponibilidade(&sistema, "P1", "S1", dia(5, 10, 2026), horarios, &motivo);
    if (total != 23) {
        return 0;
    }
    for (i = 0; i < total; i++) {
        if (horarios[i] != esperados[i]) {
            return 0;
        }
    }
    return 1;
}

static int casoServicoLongo(void)
{
    Resultado resultado;
    int horarios[MAX_HORARIOS];
    Motivo motivo = SEM_MOTIVO;
    int total = 0;
    int i = 0;

    carregarCadastro(&sistema);
    resultado = agendar(&sistema, "C1", "P1", "S2", dia(5, 10, 2026), 13 * 60);
    if (!resultado.aceito) {
        return 0;
    }

    total = consultarDisponibilidade(&sistema, "P1", "S1", dia(5, 10, 2026), horarios, &motivo);
    for (i = 0; i < total; i++) {
        if (horarios[i] >= 13 * 60 && horarios[i] < 14 * 60 + 30) {
            return 0;
        }
    }
    return 1;
}

static int casoCancelar(void)
{
    Resultado agendado;
    Resultado cancelado;
    carregarCadastro(&sistema);
    agendado = agendar(&sistema, "C1", "P1", "S1", dia(5, 10, 2026), 14 * 60);
    cancelado = cancelar(&sistema, "AG-0002");
    return agendado.aceito && cancelado.aceito;
}

static int casoRemarcar(void)
{
    Resultado resultado;
    int indice = 0;
    carregarCadastro(&sistema);
    resultado = remarcar(&sistema, "AG-0001", dia(5, 10, 2026), 15 * 60);
    indice = buscarAgendamento(&sistema, "AG-0001");
    return resultado.aceito && sistema.agendamentos[indice].inicio == 15 * 60;
}

static int casoAgendaDoDia(void)
{
    int indices[MAX_AGENDAMENTOS];
    Motivo motivo = SEM_MOTIVO;
    int total = 0;
    carregarCadastro(&sistema);
    total = agendaDoDia(&sistema, "P1", dia(5, 10, 2026), indices, &motivo);
    if (total != 1) {
        return 0;
    }
    return strcmp(sistema.agendamentos[indices[0]].id, "AG-0001") == 0;
}

static int casoClienteOcupado(void)
{
    Resultado primeiro;
    Resultado segundo;
    carregarCadastro(&sistema);
    primeiro = agendar(&sistema, "C1", "P1", "S1", dia(5, 10, 2026), 15 * 60);
    segundo = agendar(&sistema, "C1", "P2", "S3", dia(5, 10, 2026), 15 * 60 + 15);
    return primeiro.aceito && !segundo.aceito && segundo.motivo == CONFLITO_CLIENTE;
}

static int casoDiaSemExpediente(void)
{
    int horarios[MAX_HORARIOS];
    Motivo motivo = SEM_MOTIVO;
    carregarCadastro(&sistema);
    return consultarDisponibilidade(&sistema, "P2", "S1", dia(6, 10, 2026), horarios, &motivo) == 0;
}

int executarTestes(void)
{
    Data hoje = dia(5, 10, 2026);
    falhas = 0;

    printf("=== CASOS DE TESTE DA ETAPA 02 ===\n\n-- Casos normais --\n");
    registrar("T01", "Consultar horarios livres", casoConsultar());
    registrar("T02", "Agendar em horario livre", tentarAgendar("C1", "P1", "S1", hoje, 14 * 60, 1, SEM_MOTIVO));
    registrar("T03", "Agendar servico longo", casoServicoLongo());
    registrar("T04", "Cancelar agendamento", casoCancelar());
    registrar("T05", "Remarcar agendamento", casoRemarcar());
    registrar("T06", "Consultar a agenda do dia", casoAgendaDoDia());
    registrar("T07", "Profissional nao executa o servico", tentarAgendar("C1", "P2", "S2", hoje, 15 * 60, 0, SERVICO_NAO_OFERECIDO));
    registrar("T08", "Servico nao cabe na jornada", tentarAgendar("C1", "P1", "S2", hoje, 11 * 60, 0, FORA_DA_JORNADA));
    registrar("T09", "Horario bloqueado", tentarAgendar("C1", "P1", "S1", hoje, 10 * 60 + 30, 0, CONFLITO_BLOQUEIO));
    registrar("T10", "Cliente com dois atendimentos", casoClienteOcupado());

    printf("\n-- Casos-limite --\n");
    registrar("T11", "Agendar quando o bloqueio termina", tentarAgendar("C1", "P1", "S1", hoje, 11 * 60, 1, SEM_MOTIVO));
    registrar("T12", "Atendimento termina no fim da jornada", tentarAgendar("C1", "P1", "S1", hoje, 11 * 60 + 30, 1, SEM_MOTIVO));
    registrar("T13", "Consulta em dia sem expediente", casoDiaSemExpediente());

    printf("\n-- Casos de entrada invalida --\n");
    registrar("T14", "Horario fora da grade", tentarAgendar("C2", "P1", "S1", hoje, 9 * 60 + 20, 0, HORARIO_FORA_DA_GRADE));
    registrar("T15", "Cliente inexistente", tentarAgendar("C9", "P1", "S1", hoje, 14 * 60, 0, ENTIDADE_INEXISTENTE));

    if (falhas == 0) {
        printf("\nRESULTADO: 15 de 15 casos passaram.\n");
    } else {
        printf("\nRESULTADO: %d caso(s) falharam.\n", falhas);
    }
    return falhas;
}


static void lerTexto(const char *rotulo, char *destino, int tamanho)
{
    int fim = 0;
    printf("%s", rotulo);
    if (fgets(destino, tamanho, stdin) == NULL) {
        strcpy(destino, "");
        return;
    }
    fim = (int) strlen(destino);
    while (fim > 0 && (destino[fim - 1] == '\n' || destino[fim - 1] == '\r')) {
        destino[fim - 1] = '\0';
        fim = fim - 1;
    }
}

static void mostrarResposta(Resultado resultado, const char *acao)
{
    if (resultado.aceito) {
        printf("\n>> ACEITO - agendamento %s %s\n", resultado.id, acao);
    } else {
        printf("\n>> RECUSADO - %s\n", textoMotivo(resultado.motivo));
    }
}

static void telaConsultarHorarios(void)
{
    char profissional[MAX_CODIGO];
    char servico[MAX_CODIGO];
    char textoData[20];
    char textoHora[8];
    Data data;
    int horarios[MAX_HORARIOS];
    Motivo motivo = SEM_MOTIVO;
    int total = 0;
    int i = 0;

    printf("\n--- CONSULTAR HORARIOS LIVRES ---\n");
    lerTexto("Profissional: ", profissional, MAX_CODIGO);
    lerTexto("Servico: ", servico, MAX_CODIGO);
    lerTexto("Dia (DD/MM/AAAA): ", textoData, 20);
    if (!lerDataHora(textoData, NULL, &data, NULL)) {
        printf("\n>> RECUSADO - HORARIO_FORA_DA_GRADE\n");
        return;
    }

    total = consultarDisponibilidade(&sistema, profissional, servico, data, horarios, &motivo);
    if (total == -1) {
        printf("\n>> RECUSADO - %s\n", textoMotivo(motivo));
        return;
    }
    if (total == 0) {
        printf("\nNenhum horario disponivel nesse dia.\n");
        return;
    }

    printf("\nHorarios livres (%d):\n", total);
    for (i = 0; i < total; i++) {
        formatarHora(horarios[i], textoHora);
        printf("  %s", textoHora);
        if ((i + 1) % 8 == 0) {
            printf("\n");
        }
    }
    if (total % 8 != 0) {
        printf("\n");
    }
}

static void telaAgendar(void)
{
    char cliente[MAX_CODIGO];
    char profissional[MAX_CODIGO];
    char servico[MAX_CODIGO];
    char textoData[20];
    char textoHora[10];
    Data data;
    int inicio = 0;

    printf("\n--- AGENDAR ATENDIMENTO ---\n");
    lerTexto("Profissional: ", profissional, MAX_CODIGO);
    lerTexto("Servico: ", servico, MAX_CODIGO);
    lerTexto("Dia (DD/MM/AAAA): ", textoData, 20);
    lerTexto("Horario (HH:MM): ", textoHora, 10);
    lerTexto("Cliente: ", cliente, MAX_CODIGO);

    if (!lerDataHora(textoData, textoHora, &data, &inicio)) {
        printf("\n>> RECUSADO - HORARIO_FORA_DA_GRADE\n");
        return;
    }
    mostrarResposta(agendar(&sistema, cliente, profissional, servico, data, inicio), "confirmado");
}

static void telaListar(int porCliente)
{
    char codigo[MAX_CODIGO];
    char textoData[20];
    char horaInicio[8];
    char horaFim[8];
    Data data;
    int indices[MAX_AGENDAMENTOS];
    Motivo motivo = SEM_MOTIVO;
    int total = 0;
    int i = 0;

    if (porCliente) {
        printf("\n--- AGENDAMENTOS DO CLIENTE ---\n");
        lerTexto("Cliente: ", codigo, MAX_CODIGO);
        total = agendaDoCliente(&sistema, codigo, indices, &motivo);
    } else {
        printf("\n--- AGENDA DO DIA ---\n");
        lerTexto("Profissional: ", codigo, MAX_CODIGO);
        lerTexto("Dia (DD/MM/AAAA): ", textoData, 20);
        if (!lerDataHora(textoData, NULL, &data, NULL)) {
            printf("\n>> RECUSADO - HORARIO_FORA_DA_GRADE\n");
            return;
        }
        total = agendaDoDia(&sistema, codigo, data, indices, &motivo);
    }

    if (total == -1) {
        printf("\n>> RECUSADO - %s\n", textoMotivo(motivo));
        return;
    }
    if (total == 0) {
        printf("\nNenhum agendamento encontrado.\n");
        return;
    }

    printf("\n");
    for (i = 0; i < total; i++) {
        const Agendamento *agendamento = &sistema.agendamentos[indices[i]];
        int indiceServico = buscarServico(&sistema, agendamento->servico);
        formatarHora(agendamento->inicio, horaInicio);
        formatarHora(agendamento->inicio + sistema.servicos[indiceServico].duracao, horaFim);
        printf("  %s  %02d/%02d/%04d  %s as %s  %s  %s  %s\n", agendamento->id, agendamento->data.dia, agendamento->data.mes, agendamento->data.ano, horaInicio, horaFim, agendamento->cliente, agendamento->profissional, agendamento->servico);
    }
}

static void telaCancelar(void)
{
    char id[MAX_CODIGO];
    printf("\n--- CANCELAR AGENDAMENTO ---\n");
    lerTexto("Agendamento (AG-0000): ", id, MAX_CODIGO);
    mostrarResposta(cancelar(&sistema, id), "cancelado");
}

static void telaRemarcar(void)
{
    char id[MAX_CODIGO];
    char textoData[20];
    char textoHora[10];
    Data data;
    int inicio = 0;

    printf("\n--- REMARCAR AGENDAMENTO ---\n");
    lerTexto("Agendamento (AG-0000): ", id, MAX_CODIGO);
    lerTexto("Novo dia (DD/MM/AAAA): ", textoData, 20);
    lerTexto("Novo horario (HH:MM): ", textoHora, 10);

    if (!lerDataHora(textoData, textoHora, &data, &inicio)) {
        printf("\n>> RECUSADO - HORARIO_FORA_DA_GRADE\n");
        return;
    }
    mostrarResposta(remarcar(&sistema, id, data, inicio), "remarcado");
}

int main(int argc, char *argv[])
{
    char opcao[10];
    char hora[8];
    int encerrar = 0;

    carregarCadastro(&sistema);

    if (argc > 1 && strcmp(argv[1], "--testes") == 0) {
        return executarTestes();
    }

    while (!encerrar) {
        formatarHora(sistema.agora, hora);
        printf("\n=== SISTEMA DE AGENDAMENTO (IMPERATIVO) ===\n");
        printf("Data e hora atuais: %02d/%02d/%04d %s\n\n", sistema.hoje.dia, sistema.hoje.mes, sistema.hoje.ano, hora);
        printf(" 1 - Consultar horarios livres\n");
        printf(" 2 - Agendar atendimento\n");
        printf(" 3 - Agenda do dia\n");
        printf(" 4 - Agendamentos de um cliente\n");
        printf(" 5 - Cancelar agendamento\n");
        printf(" 6 - Remarcar agendamento\n");
        printf(" 0 - Sair\n");
        lerTexto("\nOpcao: ", opcao, 10);

        if (strcmp(opcao, "1") == 0) {
            telaConsultarHorarios();
        } else if (strcmp(opcao, "2") == 0) {
            telaAgendar();
        } else if (strcmp(opcao, "3") == 0) {
            telaListar(0);
        } else if (strcmp(opcao, "4") == 0) {
            telaListar(1);
        } else if (strcmp(opcao, "5") == 0) {
            telaCancelar();
        } else if (strcmp(opcao, "6") == 0) {
            telaRemarcar();
        } else if (strcmp(opcao, "0") == 0) {
            encerrar = 1;
        } else {
            printf("\nOpcao invalida.\n");
        }
    }
    printf("\nEncerrado.\n");
    return 0;
}