#include <stdio.h>
#include <string.h>
#include "agenda.h"


static int diasNoMes(int mes, int ano)
{
    int dias[12] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    int bissexto = (ano % 400 == 0) || (ano % 4 == 0 && ano % 100 != 0);
    if (mes == 2 && bissexto) {
        return 29;
    }
    return dias[mes - 1];
}

static int dataValida(Data data)
{
    if (data.ano < 1900 || data.ano > 2200 || data.mes < 1 || data.mes > 12) {
        return 0;
    }
    return data.dia >= 1 && data.dia <= diasNoMes(data.mes, data.ano);
}

static int diasAbsolutos(Data data)
{
    int ano = data.ano;
    int mes = data.mes;
    if (mes <= 2) {
        ano = ano - 1;
        mes = mes + 12;
    }
    return 365 * ano + ano / 4 - ano / 100 + ano / 400 + (153 * (mes - 3) + 2) / 5 + data.dia;
}

static int diaDaSemana(Data data)
{
    return (diasAbsolutos(data) + 1) % 7;
}

static long minutosAbsolutos(Data data, int minutos)
{
    return (long) diasAbsolutos(data) * MINUTOS_POR_DIA + minutos;
}

void formatarHora(int minutos, char *destino)
{
    sprintf(destino, "%02d:%02d", minutos / 60, minutos % 60);
}

int lerDataHora(const char *textoData, const char *textoHora, Data *data, int *minutos)
{
    int horas = 0;
    int mins = 0;
    if (sscanf(textoData, "%d/%d/%d", &data->dia, &data->mes, &data->ano) != 3) {
        return 0;
    }
    if (textoHora == NULL) {
        return 1;
    }
    if (sscanf(textoHora, "%d:%d", &horas, &mins) != 2) {
        return 0;
    }
    if (horas < 0 || horas > 23 || mins < 0 || mins > 59) {
        return 0;
    }
    *minutos = horas * 60 + mins;
    return 1;
}

static void adicionarJanela(Profissional *profissional, int dia, int inicio, int fim)
{
    int quantidade = profissional->totalJanelas[dia];
    profissional->jornada[dia][quantidade].inicio = inicio;
    profissional->jornada[dia][quantidade].fim = fim;
    profissional->totalJanelas[dia] = quantidade + 1;
}

void carregarCadastro(Sistema *sistema)
{
    Data hoje;
    Profissional *p1 = NULL;
    Profissional *p2 = NULL;
    Agendamento *inicial = NULL;
    int dia = 0;

    hoje.dia = 5;
    hoje.mes = 10;
    hoje.ano = 2026;
    sistema->hoje = hoje;
    sistema->agora = 8 * 60;
    sistema->proximoNumero = 2;

    strcpy(sistema->servicos[0].codigo, "S1");
    sistema->servicos[0].duracao = 30;
    strcpy(sistema->servicos[1].codigo, "S2");
    sistema->servicos[1].duracao = 90;
    strcpy(sistema->servicos[2].codigo, "S3");
    sistema->servicos[2].duracao = 15;
    sistema->totalServicos = 3;

    strcpy(sistema->clientes[0], "C1");
    strcpy(sistema->clientes[1], "C2");
    sistema->totalClientes = 2;

    p1 = &sistema->profissionais[0];
    strcpy(p1->codigo, "P1");
    strcpy(p1->servicos[0], "S1");
    strcpy(p1->servicos[1], "S2");
    p1->totalServicos = 2;
    for (dia = 0; dia < 7; dia++) {
        p1->totalJanelas[dia] = 0;
    }
    for (dia = 0; dia <= 4; dia++) {
        adicionarJanela(p1, dia, 9 * 60, 12 * 60);
        adicionarJanela(p1, dia, 13 * 60, 18 * 60);
    }

    p2 = &sistema->profissionais[1];
    strcpy(p2->codigo, "P2");
    strcpy(p2->servicos[0], "S1");
    strcpy(p2->servicos[1], "S3");
    p2->totalServicos = 2;
    for (dia = 0; dia < 7; dia++) {
        p2->totalJanelas[dia] = 0;
    }
    adicionarJanela(p2, 0, 14 * 60, 18 * 60);
    adicionarJanela(p2, 2, 14 * 60, 18 * 60);
    adicionarJanela(p2, 4, 14 * 60, 18 * 60);
    sistema->totalProfissionais = 2;

    strcpy(sistema->bloqueios[0].profissional, "P1");
    sistema->bloqueios[0].data = hoje;
    sistema->bloqueios[0].inicio = 10 * 60;
    sistema->bloqueios[0].fim = 11 * 60;
    sistema->totalBloqueios = 1;

    inicial = &sistema->agendamentos[0];
    strcpy(inicial->id, "AG-0001");
    strcpy(inicial->cliente, "C2");
    strcpy(inicial->profissional, "P1");
    strcpy(inicial->servico, "S1");
    inicial->data = hoje;
    inicial->inicio = 9 * 60;
    inicial->situacao = ATIVO;
    sistema->totalAgendamentos = 1;
}

int buscarServico(const Sistema *sistema, const char *codigo)
{
    int i = 0;
    for (i = 0; i < sistema->totalServicos; i++) {
        if (strcmp(sistema->servicos[i].codigo, codigo) == 0) {
            return i;
        }
    }
    return -1;
}

static int buscarProfissional(const Sistema *sistema, const char *codigo)
{
    int i = 0;
    for (i = 0; i < sistema->totalProfissionais; i++) {
        if (strcmp(sistema->profissionais[i].codigo, codigo) == 0) {
            return i;
        }
    }
    return -1;
}

static int buscarCliente(const Sistema *sistema, const char *codigo)
{
    int i = 0;
    for (i = 0; i < sistema->totalClientes; i++) {
        if (strcmp(sistema->clientes[i], codigo) == 0) {
            return i;
        }
    }
    return -1;
}

int buscarAgendamento(const Sistema *sistema, const char *id)
{
    int i = 0;
    for (i = 0; i < sistema->totalAgendamentos; i++) {
        if (strcmp(sistema->agendamentos[i].id, id) == 0) {
            return i;
        }
    }
    return -1;
}

static int sobrepoe(int inicio1, int fim1, int inicio2, int fim2)
{
    return inicio1 < fim2 && inicio2 < fim1;
}

static int executaServico(const Profissional *profissional, const char *servico)
{
    int i = 0;
    for (i = 0; i < profissional->totalServicos; i++) {
        if (strcmp(profissional->servicos[i], servico) == 0) {
            return 1;
        }
    }
    return 0;
}

static int cabeNaJornada(const Profissional *profissional, int diaSemana, int inicio, int fim)
{
    int i = 0;
    for (i = 0; i < profissional->totalJanelas[diaSemana]; i++) {
        if (inicio >= profissional->jornada[diaSemana][i].inicio && fim <= profissional->jornada[diaSemana][i].fim) {
            return 1;
        }
    }
    return 0;
}

static int conflitoComBloqueio(const Sistema *sistema, const char *profissional, Data data, int inicio, int fim)
{
    int i = 0;
    for (i = 0; i < sistema->totalBloqueios; i++) {
        const Bloqueio *bloqueio = &sistema->bloqueios[i];
        if (strcmp(bloqueio->profissional, profissional) != 0) {
            continue;
        }
        if (diasAbsolutos(bloqueio->data) != diasAbsolutos(data)) {
            continue;
        }
        if (sobrepoe(inicio, fim, bloqueio->inicio, bloqueio->fim)) {
            return 1;
        }
    }
    return 0;
}

static int fimDoAgendamento(const Sistema *sistema, const Agendamento *agendamento)
{
    int indice = buscarServico(sistema, agendamento->servico);
    return agendamento->inicio + sistema->servicos[indice].duracao;
}

static int conflitoComAgendamento(const Sistema *sistema, int porCliente, const char *codigo, Data data, int inicio, int fim, int ignorar)
{
    int i = 0;
    for (i = 0; i < sistema->totalAgendamentos; i++) {
        const Agendamento *agendamento = &sistema->agendamentos[i];
        const char *comparar = porCliente ? agendamento->cliente : agendamento->profissional;
        if (i == ignorar || agendamento->situacao != ATIVO) {
            continue;
        }
        if (strcmp(comparar, codigo) != 0) {
            continue;
        }
        if (diasAbsolutos(agendamento->data) != diasAbsolutos(data)) {
            continue;
        }
        if (sobrepoe(inicio, fim, agendamento->inicio, fimDoAgendamento(sistema, agendamento))) {
            return 1;
        }
    }
    return 0;
}

static Motivo validar(const Sistema *sistema, const char *cliente, const char *profissional, const char *servico, Data data, int inicio, int ignorar)
{
    int indiceProfissional = buscarProfissional(sistema, profissional);
    int indiceServico = buscarServico(sistema, servico);
    long instanteAlvo = 0;
    long instanteAtual = 0;
    int fim = 0;

    if (indiceProfissional == -1 || indiceServico == -1) {
        return ENTIDADE_INEXISTENTE;
    }
    if (cliente != NULL && buscarCliente(sistema, cliente) == -1) {
        return ENTIDADE_INEXISTENTE;
    }
    if (!dataValida(data) || inicio < 0 || inicio >= MINUTOS_POR_DIA) {
        return HORARIO_FORA_DA_GRADE;
    }
    if (inicio % GRADE_MINUTOS != 0) {
        return HORARIO_FORA_DA_GRADE;
    }
    if (!executaServico(&sistema->profissionais[indiceProfissional], servico)) {
        return SERVICO_NAO_OFERECIDO;
    }

    instanteAlvo = minutosAbsolutos(data, inicio);
    instanteAtual = minutosAbsolutos(sistema->hoje, sistema->agora);
    if (instanteAlvo < instanteAtual) {
        return HORARIO_NO_PASSADO;
    }
    if (instanteAlvo - instanteAtual < ANTECEDENCIA_MINIMA) {
        return ANTECEDENCIA_MINIMA_NAO_ATENDIDA;
    }
    if (diasAbsolutos(data) - diasAbsolutos(sistema->hoje) > HORIZONTE_DIAS) {
        return ALEM_DA_JANELA_DE_AGENDAMENTO;
    }

    fim = inicio + sistema->servicos[indiceServico].duracao;
    if (!cabeNaJornada(&sistema->profissionais[indiceProfissional], diaDaSemana(data), inicio, fim)) {
        return FORA_DA_JORNADA;
    }
    if (conflitoComBloqueio(sistema, profissional, data, inicio, fim)) {
        return CONFLITO_BLOQUEIO;
    }
    if (conflitoComAgendamento(sistema, 0, profissional, data, inicio, fim, ignorar)) {
        return CONFLITO_PROFISSIONAL;
    }
    if (cliente != NULL && conflitoComAgendamento(sistema, 1, cliente, data, inicio, fim, ignorar)) {
        return CONFLITO_CLIENTE;
    }
    return SEM_MOTIVO;
}

static Resultado responder(int aceito, Motivo motivo, const char *id)
{
    Resultado resultado;
    resultado.aceito = aceito;
    resultado.motivo = motivo;
    strcpy(resultado.id, id);
    return resultado;
}

Resultado agendar(Sistema *sistema, const char *cliente, const char *profissional, const char *servico, Data data, int inicio)
{
    Motivo motivo = validar(sistema, cliente, profissional, servico, data, inicio, -1);
    Agendamento *novo = NULL;
    char id[MAX_CODIGO];

    if (motivo != SEM_MOTIVO) {
        return responder(0, motivo, "");
    }

    sprintf(id, "AG-%04d", sistema->proximoNumero);
    sistema->proximoNumero = sistema->proximoNumero + 1;
    novo = &sistema->agendamentos[sistema->totalAgendamentos];
    strcpy(novo->id, id);
    strcpy(novo->cliente, cliente);
    strcpy(novo->profissional, profissional);
    strcpy(novo->servico, servico);
    novo->data = data;
    novo->inicio = inicio;
    novo->situacao = ATIVO;
    sistema->totalAgendamentos = sistema->totalAgendamentos + 1;

    return responder(1, SEM_MOTIVO, id);
}

Resultado cancelar(Sistema *sistema, const char *id)
{
    int indice = buscarAgendamento(sistema, id);
    Agendamento *agendamento = NULL;
    long falta = 0;

    if (indice == -1) {
        return responder(0, ENTIDADE_INEXISTENTE, "");
    }
    agendamento = &sistema->agendamentos[indice];
    if (agendamento->situacao == CANCELADO) {
        return responder(0, AGENDAMENTO_JA_CANCELADO, "");
    }

    falta = minutosAbsolutos(agendamento->data, agendamento->inicio) - minutosAbsolutos(sistema->hoje, sistema->agora);
    if (falta < PRAZO_CANCELAMENTO) {
        return responder(0, CANCELAMENTO_FORA_DO_PRAZO, "");
    }

    agendamento->situacao = CANCELADO;
    return responder(1, SEM_MOTIVO, id);
}

Resultado remarcar(Sistema *sistema, const char *id, Data data, int inicio)
{
    int indice = buscarAgendamento(sistema, id);
    Agendamento *agendamento = NULL;
    Motivo motivo = SEM_MOTIVO;

    if (indice == -1) {
        return responder(0, ENTIDADE_INEXISTENTE, "");
    }
    agendamento = &sistema->agendamentos[indice];
    if (agendamento->situacao == CANCELADO) {
        return responder(0, AGENDAMENTO_JA_CANCELADO, "");
    }

    motivo = validar(sistema, agendamento->cliente, agendamento->profissional, agendamento->servico, data, inicio, indice);
    if (motivo != SEM_MOTIVO) {
        return responder(0, motivo, "");
    }

    agendamento->data = data;
    agendamento->inicio = inicio;
    return responder(1, SEM_MOTIVO, id);
}

int consultarDisponibilidade(const Sistema *sistema, const char *profissional, const char *servico, Data data, int *horarios, Motivo *motivo)
{
    int indiceProfissional = buscarProfissional(sistema, profissional);
    int indiceServico = buscarServico(sistema, servico);
    const Profissional *dadosProfissional = NULL;
    int duracao = 0;
    int diaSemana = 0;
    int janela = 0;
    int hora = 0;
    int total = 0;

    *motivo = SEM_MOTIVO;
    if (indiceProfissional == -1 || indiceServico == -1) {
        *motivo = ENTIDADE_INEXISTENTE;
        return -1;
    }
    if (!dataValida(data)) {
        *motivo = HORARIO_FORA_DA_GRADE;
        return -1;
    }

    dadosProfissional = &sistema->profissionais[indiceProfissional];
    duracao = sistema->servicos[indiceServico].duracao;
    diaSemana = diaDaSemana(data);

    for (janela = 0; janela < dadosProfissional->totalJanelas[diaSemana]; janela++) {
        for (hora = dadosProfissional->jornada[diaSemana][janela].inicio; hora + duracao <= dadosProfissional->jornada[diaSemana][janela].fim; hora += GRADE_MINUTOS) {
            if (validar(sistema, NULL, profissional, servico, data, hora, -1) == SEM_MOTIVO && total < MAX_HORARIOS) {
                horarios[total] = hora;
                total = total + 1;
            }
        }
    }
    return total;
}

static void ordenarPorInstante(const Sistema *sistema, int *indices, int total)
{
    int i = 0;
    int j = 0;
    int auxiliar = 0;
    for (i = 1; i < total; i++) {
        for (j = i; j > 0; j--) {
            long anterior = minutosAbsolutos(sistema->agendamentos[indices[j - 1]].data, sistema->agendamentos[indices[j - 1]].inicio);
            long atual = minutosAbsolutos(sistema->agendamentos[indices[j]].data, sistema->agendamentos[indices[j]].inicio);
            if (anterior <= atual) {
                break;
            }
            auxiliar = indices[j - 1];
            indices[j - 1] = indices[j];
            indices[j] = auxiliar;
        }
    }
}

int agendaDoDia(const Sistema *sistema, const char *profissional, Data data, int *indices, Motivo *motivo)
{
    int i = 0;
    int total = 0;

    *motivo = SEM_MOTIVO;
    if (buscarProfissional(sistema, profissional) == -1) {
        *motivo = ENTIDADE_INEXISTENTE;
        return -1;
    }
    for (i = 0; i < sistema->totalAgendamentos; i++) {
        const Agendamento *agendamento = &sistema->agendamentos[i];
        if (agendamento->situacao == ATIVO && strcmp(agendamento->profissional, profissional) == 0 && diasAbsolutos(agendamento->data) == diasAbsolutos(data)) {
            indices[total] = i;
            total = total + 1;
        }
    }
    ordenarPorInstante(sistema, indices, total);
    return total;
}

int agendaDoCliente(const Sistema *sistema, const char *cliente, int *indices, Motivo *motivo)
{
    int i = 0;
    int total = 0;

    *motivo = SEM_MOTIVO;
    if (buscarCliente(sistema, cliente) == -1) {
        *motivo = ENTIDADE_INEXISTENTE;
        return -1;
    }
    for (i = 0; i < sistema->totalAgendamentos; i++) {
        const Agendamento *agendamento = &sistema->agendamentos[i];
        if (agendamento->situacao == ATIVO && strcmp(agendamento->cliente, cliente) == 0) {
            indices[total] = i;
            total = total + 1;
        }
    }
    ordenarPorInstante(sistema, indices, total);
    return total;
}

const char *textoMotivo(Motivo motivo)
{
    switch (motivo) {
        case ENTIDADE_INEXISTENTE: return "ENTIDADE_INEXISTENTE";
        case HORARIO_FORA_DA_GRADE: return "HORARIO_FORA_DA_GRADE";
        case AGENDAMENTO_JA_CANCELADO: return "AGENDAMENTO_JA_CANCELADO";
        case SERVICO_NAO_OFERECIDO: return "SERVICO_NAO_OFERECIDO";
        case HORARIO_NO_PASSADO: return "HORARIO_NO_PASSADO";
        case ANTECEDENCIA_MINIMA_NAO_ATENDIDA: return "ANTECEDENCIA_MINIMA_NAO_ATENDIDA";
        case ALEM_DA_JANELA_DE_AGENDAMENTO: return "ALEM_DA_JANELA_DE_AGENDAMENTO";
        case FORA_DA_JORNADA: return "FORA_DA_JORNADA";
        case CONFLITO_BLOQUEIO: return "CONFLITO_BLOQUEIO";
        case CONFLITO_PROFISSIONAL: return "CONFLITO_PROFISSIONAL";
        case CONFLITO_CLIENTE: return "CONFLITO_CLIENTE";
        case CANCELAMENTO_FORA_DO_PRAZO: return "CANCELAMENTO_FORA_DO_PRAZO";
        default: return "SEM_MOTIVO";
    }
}