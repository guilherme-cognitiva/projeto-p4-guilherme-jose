## [P4-ETAPA-01]

### D01 — Escolha do problema

**Decisão:** agendamento de atendimentos com múltiplos profissionais.

**Justificativa:** o problema precisava permitir modelagens genuinamente
distintas nos quatro paradigmas, e não apenas quatro traduções do mesmo
algoritmo. O agendamento atende a isso porque seu núcleo é verificação de
restrições sobre intervalos: o paradigma lógico resolve por retrocesso sobre um
espaço finito de candidatos, o funcional por composição de predicados puros, o
orientado a objetos por entidades com invariantes, e o imperativo por varredura
sobre estado mutável. Além disso, o custo de implementação é baixo em todas as
quatro linguagens, já que não há otimização combinatória nem estruturas de dados
complexas envolvidas.

**Alternativas descartadas:**

- *Motor de cálculo de taxas e split de transações financeiras* — descartado por
  ser avaliação determinística de função, sem espaço de busca. O paradigma
  lógico não teria nada a acrescentar e as quatro implementações ficariam
  parecidas demais, enfraquecendo a comparação.
- *Máquina de estados de funil de atendimento* — boa aderência aos paradigmas,
  mas com risco de a solução ficar rasa demais (quatro variações de uma tabela
  de transições) para um projeto de semestre inteiro.

### D02 — Ausência de otimização no escopo

**Decisão:** o sistema lista as opções válidas, mas não escolhe o melhor horário
nem aloca profissionais automaticamente.

**Justificativa:** incluir alocação automática tornaria o problema
significativamente mais fácil no paradigma lógico e significativamente mais
trabalhoso no imperativo, desequilibrando a comparação entre os paradigmas, que
é o objetivo central do projeto.

### D03 — Determinismo como requisito explícito

**Decisão:** três mecanismos garantem que qualquer implementação correta produza
saída idêntica para entrada idêntica — o instante de referência entra como dado
de entrada em vez de vir do relógio do sistema; o término dos intervalos é
exclusivo; e há uma ordem de precedência definida entre os motivos de rejeição.

**Justificativa:** sem esses três pontos, duas implementações igualmente
corretas poderiam divergir — o que inviabilizaria usar o mesmo conjunto de casos
de teste nas quatro versões, e portanto inviabilizaria a comparação entre elas.

### D04 — Linguagens previstas por paradigma

**Decisão:** C (imperativo), Java (orientado a objetos), Elixir (funcional) e
Prolog/SWI-Prolog (lógico).

**Justificativa:** C e Java são linguagens já conhecidas, o que reduz o risco de
a curva de aprendizado da linguagem atrapalhar o estudo do paradigma. Elixir foi
preferido a Haskell por ser imutável por construção e não possuir laços — o que
força o uso de recursão e de operações sobre listas — com uma curva de entrada
menor. Prolog não tem substituto razoável para o paradigma lógico.

**Situação:** provisória. Pode ser revista nas etapas de implementação.