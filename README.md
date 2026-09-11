# Projeto P4 — Um Problema, Quatro Paradigmas

**Disciplina:** Paradigmas de Programação — PUC-GO··
**Aluno:** Guilherme José Miranda Santos

Este repositório reúne as soluções de um mesmo problema — **agendamento de
atendimentos com múltiplos profissionais** — implementadas nos paradigmas
imperativo, orientado a objetos, funcional e lógico.

O objetivo não é traduzir um programa de uma linguagem para outra, e sim
reformular a solução segundo o modelo de programação de cada paradigma.

---

## O problema em uma frase

Dado um cadastro de serviços, profissionais e suas jornadas de trabalho, além
dos agendamentos e bloqueios existentes, determinar quais horários são válidos
para um novo atendimento e validar as operações de agendar, cancelar e remarcar
— sempre com justificativa da decisão.

Descrição completa: [`docs/problema.md`](docs/problema.md)
Especificação formal: [`docs/especificacao.md`](docs/especificacao.md)

---

## Etapas

- `[P4-ETAPA-01]` — Proposta do problema

---

## Estrutura do repositório

```
├── README.md
├── docs/
│   ├── problema.md         
│   ├── especificacao.md    
│   ├── decisoes.md
│   └── comparacao-final.md
├── testes/
│   └── casos.md
├── imperativo/
├── poo/
├── funcional/
├── logico/
└── integrado/
```

---

## Paradigmas e linguagens

- **Imperativo** — C
- **Orientado a objetos** — Java
- **Funcional** — a definir (Elixir ou Haskell)
- **Lógico** — Prolog (SWI-Prolog)