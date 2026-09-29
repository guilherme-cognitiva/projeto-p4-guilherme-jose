# [P4-ETAPA-04] Reflexão — Como o modelo mudou do imperativo para o orientado a objetos

O problema é o mesmo da Etapa 01 e os testes são os mesmos da Etapa 02. O que mudou foi a forma de organizar a solução.

---

## 1. Representação do estado

**No imperativo:** o estado inteiro ficava numa struct `Sistema`, declarada uma vez no `main.c`. Todos os vetores, o contador de identificadores e o instante de referência viviam ali, e qualquer função que recebesse um `Sistema *` enxergava e podia alterar tudo. Uma função que não tivesse nada a ver com cancelamento conseguia escrever direto em `sistema.agendamentos[i].situacao`.

**No orientado a objetos:** o estado foi dividido entre os objetos que têm motivo para guardá-lo. A `Agenda` guarda os agendamentos e os bloqueios, o `Profissional` guarda a própria jornada, o `Agendamento` guarda a própria situação. Ninguém guarda o estado de outro.

Além disso, parte do que era estado virou cálculo. O `Agendamento` não guarda o horário de término: ele pergunta ao `Servico` qual intervalo aquele início ocupa. Dado que não é guardado não tem como ficar desatualizado.

Uma troca que vale registrar: no C, hora era um `int` solto e data era uma struct com três inteiros, e eu tive que escrever a aritmética de calendário na mão. Aqui, `Horario` e `Intervalo` viraram objetos de valor imutáveis, e a data passou a ser `LocalDate`, da biblioteca padrão. A conta de dias e o dia da semana deixaram de existir no meu código.

---

## 2. Responsabilidades

Essa é a mudança mais visível. No imperativo, o código era organizado **por função**: um bloco de regras, um bloco de operações, um bloco de datas. No orientado a objetos, passou a ser organizado **por entidade**: cada classe reúne o que sabe e o que faz.

Três exemplos de responsabilidade que mudou de lugar:

- **Saber se dois horários batem** era a função `sobrepoe`, que recebia quatro inteiros soltos. Agora é o próprio `Intervalo` que responde `sobrepoe(outro)`. Quem usa não precisa mais saber que um intervalo é feito de dois números.
- **Saber se o atendimento cabe no expediente** era a função `cabeNaJornada`, que recebia o profissional e vasculhava a matriz de janelas dele. Agora é a `Jornada` que responde `comporta(dia, intervalo)`, e a matriz nunca sai de dentro dela.
- **Saber quanto tempo um serviço ocupa** estava espalhado: quem precisasse do fim de um atendimento buscava o serviço e somava a duração. Agora o próprio `Servico` devolve `intervaloIniciandoEm(inicio)`.

---

## 3. Relacionamento entre os componentes

No C não havia relacionamento, havia índices. Um agendamento guardava o texto `"P1"`, e quem quisesse saber quem era P1 tinha que chamar `buscarProfissional` e percorrer o vetor. Aqui, um `Agendamento` aponta direto para o objeto `Profissional`.

Os tipos de relação ficaram explícitos:

- **Composição:** o `Profissional` cria e é dono da própria `Jornada`. A jornada não existe fora dele e não é compartilhada.
- **Agregação:** o `Profissional` conhece os `Servico` que executa, mas não é dono deles — os serviços existem no `Cadastro` por conta própria, e o mesmo serviço é referenciado por vários profissionais.
- **Dependência trocável:** a `Agenda` recebe um `Relogio` e um `Validador` no construtor, em vez de criá-los. É isso que permite rodar os testes com o instante fixo de 05/10/2026 08:00 sem depender do relógio da máquina.

---

## 4. Reutilização

A reutilização apareceu em três lugares que no imperativo eram código repetido ou quase repetido.

**A interface `Compromisso`.** No C havia duas funções de conflito quase idênticas: uma varria bloqueios, outra varria agendamentos. Aqui, `Agendamento` e `Bloqueio` implementam a mesma interface, porque os dois são "algo que ocupa um intervalo na agenda de alguém". A `Agenda` devolve uma lista única de compromissos e as regras tratam todos do mesmo jeito.

**A classe abstrata `Conflito`.** As três regras de conflito (bloqueio, profissional e cliente) têm o mesmo algoritmo: percorrer os compromissos e ver se algum cruza com o intervalo pedido. Esse algoritmo está escrito uma vez só, na classe abstrata, e cada subclasse só responde a uma pergunta: quais compromissos me interessam. É o único ponto do projeto em que usei herança, e usei porque havia comportamento comum de verdade a compartilhar — não para cumprir requisito.

**O `Horario` e o `Intervalo`.** A conversão de minutos para `"09:30"` e a regra de sobreposição existem em um lugar só e são usadas pelo sistema inteiro.

---

## 5. Encapsulamento

No imperativo não havia encapsulamento nenhum: todos os campos eram públicos por natureza, e o máximo de ocultação disponível era declarar uma função `static` para ela não sair do arquivo.

Aqui, três barreiras foram criadas:

- **Campos privados com acesso só por métodos.** A matriz de janelas do profissional, a lista de agendamentos da agenda e a situação de um agendamento não são alcançáveis de fora.
- **Listas devolvidas como cópia ou como lista não modificável.** `Jornada.janelasDe` devolve uma lista que não aceita alteração, e `Agenda.compromissos` monta uma lista nova a cada chamada. Sem isso, quem recebesse a lista poderia inserir um agendamento sem passar pelas regras, que é exatamente o furo que existia na versão em C.
- **Transições protegidas pelo próprio objeto.** `Agendamento.cancelar()` recusa cancelar algo já cancelado, e o construtor de `Intervalo` recusa um intervalo que termina antes de começar. O objeto não permite estado inválido, em vez de confiar em quem chama.

O ganho prático: na versão em C, um erro em qualquer parte do programa podia corromper a agenda. Aqui, para um agendamento existir, ele precisa ter passado pela `Agenda`, que passa pelo `Validador`.

---

## 6. Extensão do sistema

**Acrescentar uma regra nova.** No imperativo eram duas edições em pontos distantes: um valor no enum, na posição certa, e um `if` no meio da função `validar`, na mesma posição. Quem mexesse precisava entender a função inteira. Aqui, cria-se uma classe que implementa `Regra` e acrescenta-se ela na posição desejada da lista em `Regras.todas()`. O `Validador` não muda, porque ele não conhece nenhuma regra específica: só sabe que existem regras e que a ordem importa.

**Acrescentar um tipo novo de compromisso.** Se amanhã existisse "horário de almoço" ou "manutenção do equipamento", bastaria implementar `Compromisso`. As três regras de conflito continuariam funcionando sem uma linha alterada, porque elas conversam com a interface e não com as classes concretas.

**Trocar a origem do tempo.** `Relogio` é uma interface. Hoje o sistema usa um instante fixo; trocar para o relógio real é uma linha, e nada mais no sistema precisa saber.

**O que ficou mais difícil.** Vale registrar o outro lado. São 21 classes onde antes havia um punhado de funções, e para seguir um agendamento sendo criado é preciso passar por `Agenda`, `Pedido`, `Validador` e as classes de `Regra`. No C, dava para ler a função `agendar` de cima a baixo e entender tudo. A orientação a objetos espalhou a lógica em troca de isolar as mudanças: o preço é a leitura, o ganho é que mexer numa parte não quebra as outras.

As 21 classes estão distribuídas em 8 arquivos, agrupadas por conceito: tempo (`Horario` e `Intervalo`), oferta (`Servico` e `Cliente`), atendimento (`Profissional` e `Jornada`), compromissos (`Compromisso`, `Agendamento` e `Bloqueio`), resposta (`Resultado` e `Motivo`), regras (`Regra`, `Regras`, `Pedido` e `Validador`), agenda (`Agenda`, `Cadastro` e `Relogio`) e aplicação (`Aplicacao`, `CenarioBase` e `Testes`). Em Java, só a classe principal de cada arquivo é pública; as demais ficam visíveis apenas dentro do pacote, o que já indica quais são a porta de entrada do sistema.

---

## 7. Validação

Os 15 casos do contrato da Etapa 02 foram reimplementados nesta versão e produzem o mesmo resultado da versão imperativa:

```
cd poo
javac -d bin src/agenda/*.java
java -cp bin agenda.Aplicacao --testes
```

Resultado atual: **15 de 15 casos passam**, com os mesmos motivos de recusa e os mesmos 23 horários no caso T01.

Que as duas implementações passem exatamente nos mesmos testes é a prova de que o contrato da Etapa 02 cumpriu o papel dele: descrever o que o sistema faz, sem depender de como ele é feito.
