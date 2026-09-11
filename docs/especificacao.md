# [P4-ETAPA-01] Especificação do Problema

---

## 3. Entradas

As entradas se dividem em três blocos.

### 3.1 Instante de referência

Um par data/hora que representa "o agora" para efeito das regras temporais. É fornecido explicitamente (e não obtido do relógio do sistema) para que a execução seja reproduzível e testável.

- Formato de data: `DD/MM/AAAA`
- Formato de hora: `HH:MM` (24 horas)

### 3.2 Cadastro base

- **Serviço** — código único, nome, duração em minutos.
- **Profissional** — código único, nome, lista de códigos de serviços que executa, jornada semanal.
- **Jornada semanal** — para cada dia da semana, zero ou mais janelas de trabalho `[hora inicial, hora final]`.
- **Cliente** — código único, nome.
- **Bloqueio** — código do profissional, data, `[hora inicial, hora final]`.
- **Agendamento pré-existente** — identificador, código do cliente, código do profissional, código do serviço, data, hora de início, situação (`ATIVO` ou `CANCELADO`).

Restrições sobre os dados de entrada (assume-se cadastro bem formado):

- A duração de um serviço é múltiplo de 15 minutos, entre 15 e 240 minutos.
- As horas inicial e final de janelas e bloqueios são múltiplos de 15 minutos, e a hora inicial é estritamente menor que a final.
- As janelas de um mesmo dia, para um mesmo profissional, não se sobrepõem.

### 3.3 Sequência de operações

Uma lista ordenada de comandos, executados em ordem. Cada comando produz uma saída antes que o próximo seja executado, e comandos aceitos alteram o estado da agenda para os comandos seguintes.

- **`CONSULTAR_DISPONIBILIDADE`** — data, código do serviço, código do profissional (opcional).
- **`AGENDAR`** — código do cliente, código do profissional, código do serviço, data, hora.
- **`CANCELAR`** — identificador do agendamento.
- **`REMARCAR`** — identificador do agendamento, nova data, nova hora.
- **`AGENDA_DIA`** — código do profissional, data.
- **`AGENDA_CLIENTE`** — código do cliente.

---

## 4. Saídas

Cada operação produz exatamente um resultado.

- **`CONSULTAR_DISPONIBILIDADE`** → lista de horários de início válidos, em ordem crescente, sem repetições. Quando nenhum profissional é informado, a lista é agrupada por profissional, com os profissionais em ordem crescente de código. A lista pode ser vazia — isso é um resultado válido, não um erro.

- **`AGENDAR`** → `ACEITO` acompanhado do identificador atribuído ao novo agendamento, ou `REJEITADO` acompanhado de um único código de motivo.

- **`CANCELAR`** → `ACEITO` acompanhado do identificador cancelado, ou `REJEITADO` com o código de motivo.

- **`REMARCAR`** → `ACEITO` com o identificador e o novo horário, ou `REJEITADO` com o código de motivo.

- **`AGENDA_DIA`** → lista dos agendamentos ativos do profissional na data, em ordem crescente de hora de início, contendo identificador, hora de início, hora de término, cliente e serviço.

- **`AGENDA_CLIENTE`** → lista dos agendamentos ativos do cliente, em ordem crescente de data e hora.

### 4.1 Códigos de motivo de rejeição

- **`ENTIDADE_INEXISTENTE`** — cliente, profissional, serviço ou agendamento referenciado não existe no cadastro.
- **`HORARIO_FORA_DA_GRADE`** — a hora de início não é múltiplo de 15 minutos, ou a data/hora é malformada.
- **`SERVICO_NAO_OFERECIDO`** — o profissional não executa o serviço solicitado.
- **`HORARIO_NO_PASSADO`** — a hora de início é anterior ao instante de referência.
- **`ANTECEDENCIA_MINIMA_NAO_ATENDIDA`** — falta menos que a antecedência mínima para o início.
- **`ALEM_DA_JANELA_DE_AGENDAMENTO`** — a data está além do horizonte máximo de agendamento.
- **`FORA_DA_JORNADA`** — o intervalo do atendimento não cabe inteiramente em uma janela de trabalho do profissional naquele dia.
- **`CONFLITO_BLOQUEIO`** — o intervalo se sobrepõe a um bloqueio do profissional.
- **`CONFLITO_PROFISSIONAL`** — o intervalo se sobrepõe a outro agendamento ativo do mesmo profissional.
- **`CONFLITO_CLIENTE`** — o intervalo se sobrepõe a outro agendamento ativo do mesmo cliente.
- **`CANCELAMENTO_FORA_DO_PRAZO`** — falta menos que o prazo mínimo de cancelamento para o início.
- **`AGENDAMENTO_JA_CANCELADO`** — o agendamento referenciado já se encontra cancelado.

---

## 5. Regras do problema

**R01 — Grade de horários.** Todo agendamento inicia em um horário múltiplo de 15 minutos (`:00`, `:15`, `:30`, `:45`).

**R02 — Ocupação.** Um agendamento com início `H` e serviço de duração `D` ocupa o intervalo `[H, H+D)`, com término **exclusivo**. Dois intervalos se sobrepõem quando `inicio₁ < fim₂` **e** `inicio₂ < fim₁`. Consequência: um atendimento que termina às 10:00 e outro que começa às 10:00 **não** conflitam.

**R03 — Habilitação.** O profissional só pode receber agendamentos de serviços que constam na sua lista de serviços.

**R04 — Jornada.** O intervalo `[H, H+D)` deve caber **inteiramente dentro de uma única janela** de trabalho do profissional no dia da semana correspondente à data. Um atendimento não pode atravessar o intervalo entre duas janelas (por exemplo, o horário de almoço), mesmo que a soma das janelas fosse suficiente.

**R05 — Conflito de profissional.** O intervalo não pode se sobrepor a nenhum agendamento **ativo** do mesmo profissional.

**R06 — Conflito de bloqueio.** O intervalo não pode se sobrepor a nenhum bloqueio do profissional naquela data.

**R07 — Conflito de cliente.** O intervalo não pode se sobrepor a nenhum agendamento **ativo** do mesmo cliente, ainda que com outro profissional.

**R08 — Antecedência mínima.** O início do atendimento deve estar a, no mínimo, **30 minutos** do instante de referência.

**R09 — Horizonte de agendamento.** A data do atendimento não pode ultrapassar **60 dias corridos** a partir da data do instante de referência.

**R10 — Cancelamento.** Um agendamento ativo só pode ser cancelado enquanto faltarem **2 horas ou mais** para o seu início. O cancelamento muda a situação para `CANCELADO` e libera imediatamente o intervalo, que volta a aparecer nas consultas de disponibilidade. Agendamentos já cancelados não podem ser cancelados novamente.

**R11 — Remarcação.** A remarcação é atômica: o novo horário é validado por todas as regras de agendamento **como se o agendamento original não existisse** (ele não conflita consigo mesmo). Se o novo horário for inválido, a operação é rejeitada e o agendamento original permanece intacto, no horário anterior. A remarcação não exige o prazo de cancelamento da R10, mas exige a antecedência mínima da R08 em relação ao novo horário. O identificador é preservado.

**R12 — Cálculo da disponibilidade.** Para uma data, um serviço de duração `D` e um profissional, o conjunto de horários disponíveis é obtido gerando todos os múltiplos de 15 minutos contidos nas janelas do dia e mantendo apenas aqueles que satisfazem simultaneamente R01 a R09. O critério R07 (conflito de cliente) não se aplica à consulta, pois nenhum cliente é informado.

**R13 — Precedência dos motivos.** Quando uma operação viola mais de uma regra, o motivo retornado é o **primeiro** na ordem abaixo:

1. `ENTIDADE_INEXISTENTE`
2. `HORARIO_FORA_DA_GRADE`
3. `AGENDAMENTO_JA_CANCELADO`
4. `SERVICO_NAO_OFERECIDO`
5. `HORARIO_NO_PASSADO`
6. `ANTECEDENCIA_MINIMA_NAO_ATENDIDA`
7. `ALEM_DA_JANELA_DE_AGENDAMENTO`
8. `FORA_DA_JORNADA`
9. `CONFLITO_BLOQUEIO`
10. `CONFLITO_PROFISSIONAL`
11. `CONFLITO_CLIENTE`
12. `CANCELAMENTO_FORA_DO_PRAZO`

**R14 — Identificadores.** Novos agendamentos recebem identificadores sequenciais no formato `AG-NNNN`, continuando a partir do maior número já presente no cadastro base. Identificadores de agendamentos rejeitados não são consumidos.

**R15 — Tempo.** Todas as datas e horas são locais, sem fuso horário e sem horário de verão. Um dia tem sempre 24 horas. Nenhum atendimento atravessa a meia-noite.

**R16 — Imutabilidade do cadastro.** As operações alteram apenas o conjunto de agendamentos. Serviços, profissionais, clientes, jornadas e bloqueios não são modificados durante a execução da sequência de comandos.

---

## 6. Casos de exemplo

Todos os exemplos usam o mesmo cadastro base e são executados **em sequência** (o efeito de um comando aceito vale para os seguintes).

### Cadastro base

**Instante de referência:** `05/10/2026 08:00` (segunda-feira)

**Serviços**

- `S1` — Corte, 30 minutos.
- `S2` — Coloração, 90 minutos.
- `S3` — Barba, 15 minutos.

**Profissionais**

- `P1` — Ana. Executa S1 e S2. Jornada: segunda a sexta, das 09:00 às 12:00 e das 13:00 às 18:00.
- `P2` — Bruno. Executa S1 e S3. Jornada: segunda, quarta e sexta, das 14:00 às 18:00.

**Clientes:** `C1` (Carla), `C2` (Diego)

**Bloqueios:** `P1`, em `05/10/2026`, das 10:00 às 11:00

**Agendamentos pré-existentes:** `AG-0001` — C2 / P1 / S1, em `05/10/2026` às `09:00` (ocupa 09:00–09:30), situação `ATIVO`

---

### Exemplo 1 — Consulta de disponibilidade

**Entrada:** `CONSULTAR_DISPONIBILIDADE 05/10/2026 S1 P1`

**Saída esperada:**
```
09:30, 11:00, 11:15, 11:30,
13:00, 13:15, 13:30, ... , 17:30
```
(na parte da tarde, todos os múltiplos de 15 minutos entre 13:00 e 17:30, inclusive — 19 horários)

**Justificativa:** 09:00 e 09:15 conflitam com AG-0001; 09:45, 10:00, 10:15, 10:30 e 10:45 conflitam com o bloqueio de 10:00–11:00; 09:30 é válido porque termina exatamente às 10:00 (término exclusivo, R02); 11:30 é o último início possível na janela da manhã, pois termina às 12:00.

---

### Exemplo 2 — Serviço que não cabe na janela

**Entrada:** `AGENDAR C1 P1 S2 05/10/2026 11:00`

**Saída esperada:** `REJEITADO — FORA_DA_JORNADA`

**Justificativa:** S2 dura 90 minutos; o intervalo seria 11:00–12:30, ultrapassando o fim da janela da manhã (12:00). Não é permitido atravessar o intervalo do almoço (R04).

---

### Exemplo 3 — Profissional não habilitado

**Entrada:** `AGENDAR C1 P2 S2 05/10/2026 15:00`

**Saída esperada:** `REJEITADO — SERVICO_NAO_OFERECIDO`

**Justificativa:** P2 executa apenas S1 e S3 (R03).

---

### Exemplo 4 — Agendamento válido

**Entrada:** `AGENDAR C1 P1 S1 05/10/2026 09:30`

**Saída esperada:** `ACEITO — AG-0002` (ocupa 09:30–10:00)

---

### Exemplo 5 — Conflito de cliente entre profissionais diferentes

**Entrada (dois comandos em sequência):**
```
AGENDAR C1 P1 S1 05/10/2026 15:00
AGENDAR C1 P2 S3 05/10/2026 15:15
```

**Saída esperada:**
```
ACEITO — AG-0003
REJEITADO — CONFLITO_CLIENTE
```

**Justificativa:** AG-0003 ocupa 15:00–15:30 para a cliente C1. O segundo pedido, ainda que com outro profissional e com horário livre na agenda de P2, colocaria C1 em dois lugares ao mesmo tempo (R07).

---

### Exemplo 6 — Horário fora da grade

**Entrada:** `AGENDAR C2 P1 S1 05/10/2026 09:20`

**Saída esperada:** `REJEITADO — HORARIO_FORA_DA_GRADE`

**Justificativa:** 09:20 não é múltiplo de 15 minutos (R01).

---

### Exemplo 7 — Cancelamento fora do prazo

**Entrada:** `CANCELAR AG-0001`

**Saída esperada:** `REJEITADO — CANCELAMENTO_FORA_DO_PRAZO`

**Justificativa:** AG-0001 começa às 09:00 e o instante de referência é 08:00 — falta 1 hora, menos que as 2 horas exigidas (R10).

---

### Exemplo 8 — Dia sem jornada

**Entrada:** `AGENDAR C2 P2 S1 06/10/2026 15:00`

**Saída esperada:** `REJEITADO — FORA_DA_JORNADA`

**Justificativa:** 06/10/2026 é uma terça-feira e P2 não possui janelas de trabalho nesse dia da semana.

---

### Exemplo 9 — Agenda do dia

**Entrada:** `AGENDA_DIA P1 05/10/2026`

**Saída esperada:**
```
AG-0001  09:00–09:30  C2  S1
AG-0002  09:30–10:00  C1  S1
AG-0003  15:00–15:30  C1  S1
```

---

## 7. Casos-limite

**CL1 — Adjacência exata.** Um atendimento de 09:30 às 10:00 e um bloqueio de 10:00 às 11:00 **não** conflitam. O mesmo vale para dois agendamentos consecutivos. Esse é o caso de fronteira mais importante da especificação: a comparação de sobreposição deve usar desigualdade estrita, e nunca `<=`, sob pena de o sistema perder horários legítimos.

**CL2 — Serviço mais longo que qualquer janela.** Se nenhuma janela do profissional na data comportar a duração do serviço, `CONSULTAR_DISPONIBILIDADE` retorna lista vazia e `AGENDAR` retorna `FORA_DA_JORNADA`. Lista vazia é resposta normal, não erro.

**CL3 — Antecedência mínima corta o início do dia.** Consultando a disponibilidade do próprio dia com instante de referência `05/10/2026 11:40`, o primeiro horário candidato aceitável é o primeiro múltiplo de 15 minutos igual ou posterior a 12:10 — ou seja, 12:15. Como a janela da manhã termina às 12:00, a manhã inteira desaparece do resultado e a lista começa em 13:00.

**CL4 — Cancelamento devolve o horário.** Após cancelar um agendamento, o intervalo correspondente volta a aparecer nas consultas de disponibilidade e pode ser reocupado por outro cliente. Agendamentos cancelados não participam de nenhuma verificação de conflito.

**CL5 — Remarcação inválida preserva o estado.** Se `REMARCAR AG-0002 05/10/2026 10:00` for rejeitada por conflito com o bloqueio, AG-0002 continua ativo às 09:30. Não pode existir um estado intermediário em que o agendamento tenha sido removido do horário antigo sem ter sido inserido no novo.

**CL6 — Janela do tamanho exato do serviço.** Uma janela de 14:00 às 14:15 com um serviço de 15 minutos oferece exatamente um horário: 14:00. Uma janela de 14:00 às 14:15 com um serviço de 30 minutos oferece nenhum.

**CL7 — Último dia do horizonte.** Com instante de referência em `05/10/2026`, um agendamento em `04/12/2026` (60º dia) é aceito e um em `05/12/2026` (61º dia) é rejeitado com `ALEM_DA_JANELA_DE_AGENDAMENTO`. A contagem é em dias corridos de calendário, ignorando as horas.

**CL8 — Cadastro sem profissionais habilitados.** `CONSULTAR_DISPONIBILIDADE` sem profissional informado, para um serviço que nenhum profissional executa, retorna resultado vazio — não `ENTIDADE_INEXISTENTE`, desde que o serviço exista no cadastro.