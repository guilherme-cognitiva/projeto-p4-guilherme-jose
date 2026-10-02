# [P4-ETAPA-05] Comparação entre a implementação imperativa e a orientada a objetos

Comparação entre as duas implementações do mesmo sistema de agendamento: a versão em C (Etapa 03) e a versão em Java (Etapa 04). Todas as observações se referem ao código efetivamente produzido, que está em imperativo/ e em poo/.

Discuto abaixo os aspectos em que as duas versões realmente divergiram no nosso sistema, com evidência no código.

**Os números das duas versões**

- **C:** 1.007 linhas, 48 funções, 3 arquivos
- **Java:** 1.421 linhas, 21 classes e 145 métodos, 8 arquivos

As duas passam nos mesmos 15 casos de teste da Etapa 02, com os mesmos motivos de recusa e os mesmos 23 horários no caso T01.

## 1. Representação do estado

No imperativo, usamos uma struct Sistema onde a gente concentra tudo: serviços, profissionais, clientes, bloqueios e agendamentos. Declaramos ela uma única vez na main e passamos como ponteiro para a função que precisar.

No orientado a objetos, o estado foi dividido entre os objetos que precisam guardá-lo: a Agenda, o Profissional, a Jornada, o Agendamento. E parte virou cálculo: o agendamento não guarda a hora em que termina, ele pergunta ao serviço.

## 2. Reutilização

No imperativo, a gente reaproveita chamando a mesma função. Isso quebra quando o comportamento é só parecido: a verificação de conflito virou duas funções com o mesmo laço, uma para bloqueio e outra para agendamento.

No orientado a objetos, os dois passaram a implementar a interface Compromisso, porque são a mesma coisa vista de longe: algo que ocupa um intervalo. O laço ficou escrito uma vez só, na classe abstrata Conflito.

## 3. Facilidade de extensão

No imperativo, uma regra nova dá duas edições em lugares distantes: um valor no enum Motivo e um if no meio da função validar, os dois na posição certa da precedência.

No orientado a objetos, é uma classe nova e uma linha na lista do Regras.todas(). O Validador não muda, porque ele não conhece nenhuma regra específica.

## 4. Efeitos colaterais

No imperativo, aparecem em três lugares: nas três operações que alteram o estado, nos printf e fgets do main.c, e nas funções que preenchem um vetor de quem chamou.

No orientado a objetos, o terceiro some, porque um método devolve a lista inteira. Sobraram a alteração de estado, que são cinco linhas em dois arquivos, e a entrada e saída, que ficou só na Aplicacao.

## 5. Organização do código

No imperativo, o código é organizado por função. O agenda.c tem quatro blocos: datas e horas, cadastro, regras e operações.

No orientado a objetos, é organizado por entidade. Cada classe junta os dados e o que dá para fazer com eles: sobrepoe virou método do Intervalo, cabeNaJornada virou jornada.comporta.

# Respostas às perguntas

## 1. Qual problema ficou mais fácil de expressar de forma imperativa?

A varredura da grade de horários. O problema pede para começar no início da janela, andar de 15 em 15 minutos, testar cada candidato e guardar os que passam. Isso é um laço com acumulador, e no C a gente escreve exatamente isso:

c
for (hora = janela.inicio; hora + duracao <= janela.fim; hora += GRADE_MINUTOS) {
    if (validar(...) == SEM_MOTIVO) {
        horarios[total] = hora;
        total = total + 1;
    }
}


Em Java a gente precisou de um while, porque o Horario é imutável e não dá para somar 15 nele.

A ordem de precedência também ficou melhor no C. A sequência de if é a própria lista da regra R13, dá para ler de cima para baixo. Em Java essa ordem foi parar numa lista de construtores, então quem lê o Validador não enxerga a precedência.

## 2. Qual problema ficou mais fácil de expressar usando orientação a objetos?

A verificação de conflito. No C ficaram duas funções com o mesmo laço, uma para bloqueio e outra para agendamento. E a segunda recebe um parâmetro int porCliente que serve só para ela decidir lá dentro se compara pelo cliente ou pelo profissional, o que é claramente um remendo.

Em Java a gente viu que agendamento e bloqueio são a mesma coisa vista de longe: algo que ocupa um intervalo. Então os dois implementam a interface Compromisso, e o laço ficou escrito uma vez só.

A jornada também melhorou. No C ela é uma matriz jornada[7][2] dentro da struct do profissional, e quem quisesse saber se o atendimento cabia tinha que percorrer essa matriz de fora. Em Java a gente pergunta para a jornada e ela responde, e a matriz não sai de dentro dela.

## 3. Onde a orientação a objetos realmente trouxe vantagem?

Em três coisas.

A primeira é proteger o estado. No C, escrever sistema.agendamentos[i].inicio = 600 compila e passa por cima de todas as regras. Em Java isso não dá, porque o campo é privado e a Agenda nunca entrega a lista de dentro sem copiar.

A segunda é crescer sem mexer no que já existe. Regra nova é classe nova, e tipo novo de compromisso não obriga a tocar em nenhuma regra. No C as duas coisas pedem edição no meio de funções que já funcionam.

A terceira é testar. Como o Relogio é uma interface, a gente testa qualquer regra de tempo só passando outro relógio. No C isso exigiria mexer no estado no meio do teste.

## 4. Em quais situações os objetos acrescentaram complexidade desnecessária?

O Horario tem 80 linhas em Java, com construtor privado, equals, hashCode, compareTo e toString, para fazer o que no C era um int. Nesse sistema a gente nunca mistura horário com outro número, então boa parte disso não serve para nada.

O Pedido existe só para carregar os dados que as regras precisam, já que as regras são objetos separados e não enxergam o estado. No C a função validar recebia os parâmetros e pronto.

O Cadastro são três mapas e seis métodos para fazer o que no C eram três vetores e três buscas. Com dois profissionais e três serviços, isso não se paga.

E o caminho ficou mais longo: agendar passa por Agenda, Cadastro, Pedido, Validador e nove classes de regra. No C passa por duas funções. Para depurar, isso é só custo.

## 5. Que partes quase não mudaram entre as duas?

As regras do domínio continuam as mesmas 16 da Etapa 01, com os mesmos valores: grade de 15 minutos, 30 minutos de antecedência, 2 horas para cancelar e 60 dias de horizonte.

A conta de sobreposição é idêntica, com a mesma comparação estrita nas duas:

c
return inicio1 < fim2 && inicio2 < fim1;

java
return inicio.minutos() < outro.fim.minutos()
    && outro.inicio.minutos() < fim.minutos();


A ordem de precedência também é a mesma. Muda só o mecanismo: no C é a sequência de if, em Java é a ordem da lista.

Validar antes de escrever continua igual nas duas. Nenhuma operação toca no estado antes de a validação passar, e é por isso que a remarcação não precisa de desfazer.

E os 15 casos de teste rodam iguais e dão o mesmo resultado. Essa é a melhor prova de que o contrato da Etapa 02 fala de comportamento e não de implementação.

## 6. Que partes precisaram ser remodeladas?

No C a gente guardava hora como int e data como struct de três inteiros, e escreveu a aritmética de calendário na mão. Em Java usamos Horario e Intervalo imutáveis mais o LocalDate da biblioteca, então a conta de dias e o dia da semana sumiram do nosso código.

E o jeito de apontar para outra entidade. No C o agendamento guarda o texto "P1" e quem quiser saber quem é P1 precisa buscar no vetor. Em Java o agendamento aponta direto para o objeto Profissional, e a busca por código acontece uma vez só, na hora que a Agenda recebe o que foi digitado.