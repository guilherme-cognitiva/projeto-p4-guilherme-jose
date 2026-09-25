# [P4-ETAPA-03] Decisões de Implementação

## 1. Quais estados são mantidos

Todos os estados são mantidos: cadastros, agendamentos, contador, instante de referência. Qualquer função que receba `Sistema *` pode ver e alterar todos os campos.

## 2. Quais operações modificam estado

As operações que modificam os estados são:

agendar: que é uma função que adiciona um novo agendamento no vetor e aumenta 'totalAgendamentos', o campo que armazena a quantidade total de agendamentos.
cancelar: que é a função utilizada para cancelar um agendamento já marcado, mudando a situação dele para 'CANCELADO' dentro do vetor.
remarcar: que é uma função utilizada para remarcar um horário, ou seja, fazer uma edição em tal agendamento dentro do nosso vetor.

## 3. Onde aparecem efeitos colaterais

Os efeitos colaterais aparecem em três lugares:

Os feitos colatareais aparecem nas três operações que alteram o sistema que são 'agendar', 'cancelar' e 'remarcar'.
No 'imperativo.c', que é onde ficam os 'printf' e os 'fgets', ou seja, tudo que escreve na tela e lê do teclado.
Nas funções que preenchem um vetor de quem chamou, como a 'consultarDisponibilidade', que recebe o vetor 'horarios', preenche ele e devolve o total. É o jeito, em C, de uma função devolver uma lista.

## 4. Quais estruturas de controle foram escolhidas

for: como índice de busca e também para varredura, para percorrer todo o vetor.
while:utilizado somente no menu, que roda até que o usuário escolha a opção de sair.
Laços: na consulta de disponibilidade: o de fora percorre as janelas de trabalho do dia e o de dentro anda de 15 em 15 minutos dentro de cada janela.
switch: para converter o motivo em texto.
Insertion sort: para ordenar os agendamentos das listagens, do primeiro para o último horário.

## 5. Como os subprogramas foram organizados

Separei em 3 arquivos:

agenda.h: as estruturas, os structs, mais as constantes e as assinaturas das funções.
agenda.c: as funções da agenda: datas e horas, cadastro, as regras e as operações do sistema, que são agendar, cancelar, remarcar, buscar disponibilidade e listar a agenda.
main.c: os casos de teste e as telas do menu.

## 6. Por que a solução é predominantemente imperativa

- Separamos os dados e os comportamentos.
- O programa é uma sequência de mudanças de estado.
- Nada é encapsulado.
- Os fluxos estão explícitos.
- Estamos usando memória estática, com vetores de tamanhos fixos.