# [P4-ETAPA-01] Proposta do Problema

## 1. Descrição do problema

Estabelecimentos que prestam serviços por hora marcada — clínicas, estúdios, barbearias, complexos de lazer com atrações agendáveis, prestadores autônomos — precisam controlar quais horários de cada profissional estão livres e quais já foram comprometidos.

Na prática, esse controle costuma ser feito manualmente (caderno, planilha ou conversa de WhatsApp). O atendente precisa, de cabeça, cruzar quatro informações antes de confirmar um horário: a jornada de trabalho do profissional naquele dia, os compromissos já marcados, os bloqueios pontuais (almoço estendido, reunião, folga) e a duração do serviço solicitado. O resultado é lento e sujeito a erro — em especial ao *overbooking*, quando dois clientes são marcados em horários que se sobrepõem.

O problema a ser resolvido é, portanto: **dado um cadastro de serviços, profissionais e suas jornadas, e um conjunto de agendamentos e bloqueios já existentes, determinar quais horários de início são válidos para um novo atendimento, e validar ou rejeitar — com justificativa — as operações de agendamento, cancelamento e remarcação.**

Em essência, trata-se de um problema de **verificação de restrições sobre intervalos de tempo**. Não há otimização envolvida: o sistema não escolhe o melhor horário nem o melhor profissional, apenas responde o que é e o que não é permitido, e por quê.

---

## 2. Objetivo

O sistema deverá ser capaz de:

1. Receber um cadastro base contendo serviços, profissionais, clientes, jornadas de trabalho, bloqueios e agendamentos pré-existentes.
2. **Consultar a disponibilidade**: listar todos os horários de início válidos para um serviço, em uma data, para um profissional específico ou para todos os profissionais habilitados.
3. **Registrar um agendamento**, aceitando-o e atribuindo-lhe um identificador, ou rejeitando-o com um motivo determinado.
4. **Cancelar um agendamento** existente, respeitando o prazo de cancelamento.
5. **Remarcar um agendamento** para outra data/hora, de forma atômica.
6. **Listar a agenda** de um profissional em uma data.
7. **Listar os agendamentos** de um cliente.

Todas as respostas devem ser determinísticas: a mesma entrada produz sempre exatamente a mesma saída.

---

## 8. Restrições (fora do escopo)

O projeto **não** contempla:

- Interface gráfica ou web nas implementações de cada paradigma. A interação se dá por entrada e saída em texto.
- Persistência obrigatória em banco de dados. O cadastro base é carregado a cada execução.
- Autenticação, autorização, perfis de usuário ou controle de acesso.
- Pagamentos, cobrança, precificação ou emissão de comprovantes.
- Envio de notificações, lembretes, e-mails ou mensagens.
- Fusos horários, horário de verão e localização de formatos de data.
- Feriados automáticos: toda indisponibilidade excepcional é modelada como bloqueio explícito.
- Agendamentos recorrentes, pacotes de sessões ou séries.
- Fila de espera, lista de encaixe e overbooking controlado.
- Escolha automática do "melhor" profissional ou do "melhor" horário. O sistema apenas lista as opções válidas; a decisão é de quem consulta.
- Concorrência, acesso simultâneo por múltiplos operadores e controle transacional distribuído. A sequência de comandos é executada de forma estritamente serial.
- Histórico de auditoria, versionamento ou reversão de operações.

---

## 9. Principais conceitos do domínio

- **Serviço** — tipo de atendimento oferecido, caracterizado por uma duração fixa.
- **Profissional** — quem executa atendimentos; possui um conjunto de serviços que sabe realizar e uma jornada de trabalho.
- **Cliente** — quem recebe o atendimento.
- **Jornada de trabalho** — conjunto de janelas semanais recorrentes em que o profissional atende.
- **Janela de trabalho** — intervalo contínuo de disponibilidade regular dentro de um dia.
- **Bloqueio** — indisponibilidade pontual e não recorrente, associada a uma data específica.
- **Agendamento** — compromisso que associa cliente, profissional, serviço e um instante de início, ocupando um intervalo de tempo.
- **Situação do agendamento** — condição que determina se o agendamento ocupa ou não o horário (`ATIVO` ou `CANCELADO`).
- **Intervalo de tempo** — par início/fim, com fim exclusivo; unidade sobre a qual toda a lógica de conflito opera.
- **Sobreposição (conflito)** — relação entre dois intervalos que compartilham ao menos um instante.
- **Grade de horários** — discretização do tempo em passos de 15 minutos, que define os inícios candidatos.
- **Disponibilidade** — conjunto de inícios candidatos que satisfazem todas as restrições.
- **Instante de referência** — o "agora" declarado, base das regras de antecedência, passado e horizonte.
- **Motivo de rejeição** — justificativa determinística para a recusa de uma operação.

---

## 10. Adequação aos quatro paradigmas

O problema foi escolhido por ter três características que o tornam confortável em qualquer um dos paradigmas: o estado é pequeno e bem delimitado, as regras são independentes entre si, e o cálculo central (disponibilidade) é uma filtragem sobre um conjunto finito e enumerável de candidatos.

**Imperativo.** O cálculo da disponibilidade é naturalmente expresso como uma varredura: percorrer as janelas do dia, avançar de 15 em 15 minutos, testar cada candidato contra as listas de agendamentos e bloqueios e acumular os aprovados. A agenda é um estado mutável que os comandos alteram em sequência, o que corresponde diretamente à noção imperativa de programa como sucessão de modificações de estado. Estruturas simples — vetores e registros — bastam.

**Orientado a objetos.** O domínio já vem com entidades bem definidas, cada uma com dados e comportamento próprios: um Agendamento sabe calcular seu próprio intervalo e responder se colide com outro; uma Jornada sabe dizer se comporta um intervalo; uma Agenda encapsula a coleção e protege o invariante de não haver sobreposições. As doze regras de validação são candidatas naturais a uma interface comum de validador, com a lista ordenada de validadores implementando a precedência de motivos por polimorfismo — extensível sem alterar o código que a percorre.

**Funcional.** A disponibilidade é literalmente uma composição de transformações sobre listas: gerar os candidatos, filtrar por uma sequência de predicados e ordenar. Cada regra é uma função pura de um candidato para um valor booleano, e a precedência de motivos é uma redução sobre uma lista de pares (predicado, motivo). A agenda pode ser tratada como valor imutável, com cada comando sendo uma função que recebe o estado e devolve um novo estado junto com o resultado — o que também torna a atomicidade da remarcação trivial, já que basta descartar o novo estado em caso de rejeição.

**Lógico.** É onde o problema mais se destaca, porque o núcleo é exatamente satisfação de restrições. O cadastro base vira um conjunto de fatos (`servico/3`, `profissional/2`, `executa/2`, `janela/4`, `bloqueio/4`, `agendamento/6`) e as regras viram cláusulas: `sobrepoe/4`, `cabe_na_jornada/4`, `livre/4`, `disponivel/4`. A consulta de disponibilidade não precisa de nenhum algoritmo de busca escrito à mão — o próprio mecanismo de unificação e retrocesso enumera as soluções que satisfazem as restrições. A negação por falha exprime diretamente "não existe conflito", e a precedência de motivos é modelada como uma sequência ordenada de cláusulas de rejeição.

---

## 11. Linguagens inicialmente consideradas

**Imperativo — C** (alternativa: Python em estilo procedural)
C obriga a tratar explicitamente estruturas, memória e laços, deixando evidente
o custo do controle manual de estado — que é justamente o que o paradigma tem a
ensinar.

**Orientado a objetos — Java** (alternativas: TypeScript, C#)
Java tem tipagem estática forte, interfaces e herança bem explícitas, o que
favorece a modelagem de validadores polimórficos e a discussão de encapsulamento
e invariantes.

**Funcional — Elixir** (alternativas: Haskell, F#)
Elixir é imutável por construção e não possui laços, obrigando o uso de recursão
e de operações sobre listas — exatamente o que o cálculo de disponibilidade
exige. Haskell seria mais rigoroso quanto à pureza, por forçar a explicitação de
efeitos no sistema de tipos, e permanece como alternativa.

**Lógico — Prolog (SWI-Prolog)** (alternativa: Prolog com CLP(FD))
Prolog é a escolha canônica e a que melhor expressa o problema: fatos para o
cadastro, regras para as restrições, e retrocesso para enumerar os horários
válidos. A biblioteca CLP(FD) permitiria tratar os horários como variáveis com
domínio finito, aproximando a solução da programação por restrições.

**Observação:** a escolha final de cada linguagem pode ser revista nas etapas
seguintes. Nesta etapa, o compromisso é apenas com o problema, não com a
implementação.

**Observação:** a escolha final de cada linguagem pode ser revista nas etapas seguintes. Nesta etapa, o compromisso é apenas com o problema, não com a implementação.