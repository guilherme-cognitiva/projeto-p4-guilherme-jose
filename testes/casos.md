# [P4-ETAPA-02] Contrato Semântico e Testes

## Casos normais

**Teste 1 — Consultar horários livres**

- **Identificador:** T01
- **Descrição:** Verifica se a tela mostra apenas os horários em que o atendimento realmente pode acontecer.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional.
  3. Escolher um serviço que ele executa.
  4. Escolher um dia em que ele trabalha.
- **Saída esperada:** A tela mostra os horários em que o serviço cabe inteiro na jornada do profissional, sem sobrepor agendamentos nem bloqueios.

**Teste 2 — Agendar em horário livre**

- **Identificador:** T02
- **Descrição:** Verifica o caminho principal do sistema: um agendamento sem nenhum impedimento.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço que ele executa e um dia em que ele trabalha.
  3. Escolher um dos horários livres mostrados.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O sistema confirma o agendamento e mostra o número dele.

**Teste 3 — Agendar serviço longo**

- **Identificador:** T03
- **Descrição:** Verifica se o sistema considera a duração do serviço, e não apenas o horário de início.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço de longa duração e um dia em que ele trabalha.
  3. Escolher um horário em que o serviço cabe inteiro na jornada.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O sistema confirma o agendamento. Ao consultar os horários livres de novo, nenhum horário dentro da duração do serviço aparece mais.

**Teste 4 — Cancelar agendamento**

- **Identificador:** T04
- **Descrição:** Verifica o cancelamento feito dentro do prazo.
- **Entrada:**
  1. Entrar na tela de agendamentos.
  2. Escolher um agendamento que começa daqui a mais de 2 horas.
  3. Escolher a opção de cancelar.
  4. Confirmar.
- **Saída esperada:** O sistema confirma o cancelamento, e o horário volta a aparecer como livre na tela de agendamento.

**Teste 5 — Remarcar agendamento**

- **Identificador:** T05
- **Descrição:** Verifica a troca de horário de um agendamento existente.
- **Entrada:**
  1. Entrar na tela de agendamentos.
  2. Escolher um agendamento.
  3. Escolher a opção de remarcar.
  4. Escolher um novo dia e um horário livre.
  5. Confirmar.
- **Saída esperada:** O sistema confirma a remarcação. O agendamento mantém o mesmo número e passa para o novo horário.

**Teste 6 — Consultar a agenda do dia**

- **Identificador:** T06
- **Descrição:** Verifica a visualização dos compromissos de um profissional.
- **Entrada:**
  1. Entrar na tela de agenda.
  2. Escolher um profissional.
  3. Escolher um dia.
- **Saída esperada:** A tela lista os agendamentos ativos do profissional naquele dia, em ordem de horário. Agendamentos cancelados não aparecem.

**Teste 7 — Profissional não executa o serviço**

- **Identificador:** T07
- **Descrição:** Verifica que cada profissional só recebe os serviços que sabe executar.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional.
  3. Escolher um serviço que esse profissional não executa.
- **Saída esperada:** O sistema recusa, informando que o profissional não executa esse serviço.

**Teste 8 — Serviço não cabe na jornada**

- **Identificador:** T08
- **Descrição:** Verifica que o atendimento precisa terminar dentro do horário de trabalho do profissional.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço e um dia em que ele trabalha.
  3. Informar um horário em que o serviço terminaria depois do fim do expediente.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O sistema recusa, informando que o horário está fora da jornada do profissional.

**Teste 9 — Horário bloqueado**

- **Identificador:** T09
- **Descrição:** Verifica que bloqueios pontuais, como reuniões ou folgas, impedem agendamentos.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço e um dia em que ele tem um bloqueio.
  3. Informar um horário dentro do bloqueio.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O sistema recusa, informando que o profissional está bloqueado nesse horário.

**Teste 10 — Cliente com dois atendimentos ao mesmo tempo**

- **Identificador:** T10
- **Descrição:** Verifica que um cliente não pode estar em dois atendimentos ao mesmo tempo, mesmo com profissionais diferentes.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço e um dia.
  3. Escolher um horário livre desse profissional.
  4. Escolher um cliente que já tem outro atendimento nesse mesmo horário, com outro profissional.
  5. Confirmar.
- **Saída esperada:** O sistema recusa, informando que o cliente já tem um atendimento nesse horário.

## Casos-limite

**Teste 11 — Começar quando outro compromisso termina**

- **Identificador:** T11
- **Descrição:** Verifica que compromissos encostados não são considerados conflito.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço e um dia.
  3. Escolher o horário que começa exatamente quando termina outro agendamento ou bloqueio do profissional.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O horário aparece como livre, e o sistema confirma o agendamento.

**Teste 12 — Terminar exatamente no fim do expediente**

- **Identificador:** T12
- **Descrição:** Verifica o limite final da jornada de trabalho.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço e um dia em que ele trabalha.
  3. Escolher o último horário em que o serviço termina exatamente no fim do expediente.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O horário aparece como livre, e o sistema confirma o agendamento.

**Teste 13 — Consulta em dia sem expediente**

- **Identificador:** T13
- **Descrição:** Verifica que a falta de horários é tratada como resultado normal, e não como erro.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional e um serviço.
  3. Escolher um dia em que o profissional não trabalha.
- **Saída esperada:** A tela informa que não há horários disponíveis, sem apresentar erro.

## Casos de entrada inválida

**Teste 14 — Horário fora da grade**

- **Identificador:** T14
- **Descrição:** Verifica que só são aceitos horários de 15 em 15 minutos (terminados em :00, :15, :30 ou :45).
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço e um dia.
  3. Informar um horário que não segue a grade de 15 minutos.
  4. Escolher o cliente.
  5. Confirmar.
- **Saída esperada:** O sistema recusa, informando que o horário é inválido.

**Teste 15 — Cliente inexistente**

- **Identificador:** T15
- **Descrição:** Verifica que o sistema não aceita operações com dados que não existem no cadastro.
- **Entrada:**
  1. Entrar na tela de agendamento.
  2. Escolher um profissional, um serviço, um dia e um horário livre.
  3. Informar um cliente que não está cadastrado.
  4. Confirmar.
- **Saída esperada:** O sistema recusa, informando que o cliente não existe.