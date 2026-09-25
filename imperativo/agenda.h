#ifndef AGENDA_H
#define AGENDA_H
#define MAX_CODIGO 8
#define MAX_SERVICOS 10
#define MAX_PROFISSIONAIS 10
#define MAX_CLIENTES 20
#define MAX_SERVICOS_POR_PROFISSIONAL 5
#define MAX_JANELAS_POR_DIA 2
#define MAX_BLOQUEIOS 20
#define MAX_AGENDAMENTOS 100
#define MAX_HORARIOS 100

#define GRADE_MINUTOS 15
#define ANTECEDENCIA_MINIMA 30
#define PRAZO_CANCELAMENTO 120
#define HORIZONTE_DIAS 60
#define MINUTOS_POR_DIA 1440

typedef struct {
    int dia;
    int mes;
    int ano;
} Data;

typedef struct {
    int inicio;
    int fim;
} Janela;

typedef enum { 
    ATIVO,
    CANCELADO 
} Situacao;

typedef enum {
    SEM_MOTIVO,
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
} Motivo;

typedef struct {
    char codigo[MAX_CODIGO];
    int duracao;
} Servico;

typedef struct {
    char codigo[MAX_CODIGO];
    char servicos[MAX_SERVICOS_POR_PROFISSIONAL][MAX_CODIGO];
    int totalServicos;
    Janela jornada[7][MAX_JANELAS_POR_DIA];
    int totalJanelas[7];
} Profissional;

typedef struct {
    char profissional[MAX_CODIGO];
    Data data;
    int inicio;
    int fim;
} Bloqueio;

typedef struct {
    char id[MAX_CODIGO];
    char cliente[MAX_CODIGO];
    char profissional[MAX_CODIGO];
    char servico[MAX_CODIGO];
    Data data;
    int inicio;
    Situacao situacao;
} Agendamento;

typedef struct {
    Servico servicos[MAX_SERVICOS];
    int totalServicos;
    Profissional profissionais[MAX_PROFISSIONAIS];
    int totalProfissionais;
    char clientes[MAX_CLIENTES][MAX_CODIGO];
    int totalClientes;
    Bloqueio bloqueios[MAX_BLOQUEIOS];
    int totalBloqueios;
    Agendamento agendamentos[MAX_AGENDAMENTOS];
    int totalAgendamentos;
    int proximoNumero;
    Data hoje;
    int agora;
} Sistema;

typedef struct {
    int aceito;
    Motivo motivo;
    char id[MAX_CODIGO];
} Resultado;

void formatarHora(int minutos, char *destino);
int lerDataHora(const char *textoData, const char *textoHora, Data *data, int *minutos);

void carregarCadastro(Sistema *sistema);
int buscarServico(const Sistema *sistema, const char *codigo);
int buscarAgendamento(const Sistema *sistema, const char *id);

Resultado agendar(Sistema *sistema, const char *cliente, const char *profissional, const char *servico, Data data, int inicio);
Resultado cancelar(Sistema *sistema, const char *id);
Resultado remarcar(Sistema *sistema, const char *id, Data data, int inicio);
int consultarDisponibilidade(const Sistema *sistema, const char *profissional, const char *servico, Data data, int *horarios, Motivo *motivo);
int agendaDoDia(const Sistema *sistema, const char *profissional, Data data, int *indices, Motivo *motivo);
int agendaDoCliente(const Sistema *sistema, const char *cliente, int *indices, Motivo *motivo);
const char *textoMotivo(Motivo motivo);

int executarTestes(void);

#endif