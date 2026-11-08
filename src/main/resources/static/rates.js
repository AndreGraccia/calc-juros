// Conteúdo e regras de cada tipo de juros. `calc(valores, atual)` devolve as linhas do resultado;
// `atual` é a última leitura do BCB na unidade da série (% a.a., % a.m. ou % em 12 meses).
import { annualize, balances, grow, growCdi, invested, settle } from './calc.js';

const nf = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format;
const pct = (x) => `${nf.format(x)}%`;
const irLabel = (r) => (r ? pct(r * 100) : 'isento');

const valor = { id: 'valor', label: 'Valor a investir (R$)', value: 10000, min: 1, max: 1e9 };
const meses = { id: 'meses', label: 'Prazo (meses)', value: 12, min: 1, max: 600, step: 1 };

/** Linhas padrão de renda fixa: bruto, IR, líquido. */
const settleRows = (s) => [
  ['Valor investido', brl(s.principal)],
  ['Rendimento bruto', brl(s.gross)],
  [`Imposto de renda (${irLabel(s.rate)})`, `− ${brl(s.ir)}`],
  ['Rendimento líquido', brl(s.net)],
  ['Valor final líquido', brl(s.final), true],
];

const IR_RULE = 'IR regressivo sobre o rendimento: 22,5% até 180 dias, 20% de 181 a 360, 17,5% de 361 a 720 e 15% acima de 720 (mês contado como 30 dias).';

export const RATES = [
  {
    id: 'selic',
    label: 'Selic',
    title: 'Taxa Selic (meta)',
    what: 'É a taxa básica de juros da economia brasileira. O Copom, do Banco Central, define a meta a cada 45 dias, em média. Todas as outras taxas do país (empréstimos, financiamentos, cartão e renda fixa) giram em torno dela. Quando a Selic sobe, o crédito fica mais caro e a renda fixa rende mais; quando cai, acontece o contrário.',
    usage: [
      'Principal instrumento do Banco Central para controlar a inflação.',
      'O Tesouro Selic, título público, rende a Selic: é a aplicação de referência para a reserva de emergência.',
      'Serve de comparação para qualquer investimento: um produto só compensa o risco se rende acima dela.',
    ],
    inputs: [valor, meses],
    rules: [
      'Rendimento bruto pela Selic meta, capitalizada ao ano (aproximação do Tesouro Selic).',
      IR_RULE,
      'Não considera a taxa de custódia da B3 nem o IOF (só incide em resgates com menos de 30 dias).',
    ],
    calc: (v, atual) => ({ rows: settleRows(settle(v.valor, grow(v.valor, atual, v.meses), v.meses, true)) }),
  },
  {
    id: 'cdi',
    label: 'CDI',
    title: 'CDI (Certificado de Depósito Interbancário)',
    what: 'É a taxa dos empréstimos de um dia entre bancos e anda praticamente colada na Selic. Por isso virou a régua da renda fixa: em vez de prometer uma taxa fixa, muitos produtos pagam um percentual do CDI, como "110% do CDI".',
    usage: [
      'CDB, LCI, LCA e fundos DI informam o rendimento como percentual do CDI.',
      'Para comparar investimentos, olha-se quanto % do CDI cada um paga, já descontado o imposto.',
      'LCI e LCA são isentas de imposto de renda para pessoa física, por isso costumam pagar um percentual menor do CDI.',
    ],
    inputs: [
      valor, meses,
      { id: 'pct', label: '% do CDI que o produto paga', value: 100, min: 1, max: 300 },
      { id: 'tipo', label: 'Tipo de produto', value: 'cdb', options: [['cdb', 'CDB (com imposto de renda)'], ['lci', 'LCI / LCA (isento de IR)']] },
    ],
    rules: [
      'Cada dia útil rende o percentual informado do CDI diário (base 252 dias úteis; 21 dias úteis por mês).',
      'CDB: ' + IR_RULE,
      'LCI e LCA: isentas de imposto de renda para pessoa física.',
    ],
    calc: (v, atual) => ({ rows: settleRows(settle(v.valor, growCdi(v.valor, atual, v.pct, v.meses), v.meses, v.tipo === 'cdb')) }),
  },
  {
    id: 'ipca',
    label: 'IPCA',
    title: 'IPCA (inflação oficial)',
    what: 'É o índice do IBGE que mede a variação do custo de vida das famílias: quanto os preços subiram. O valor acumulado em 12 meses mostra quanto o seu dinheiro perdeu de poder de compra no último ano. Uma aplicação só enriquece de verdade se render acima do IPCA.',
    usage: [
      'Referência da meta de inflação do Banco Central.',
      'Corrige títulos como o Tesouro IPCA+, que pagam a inflação mais uma taxa fixa (o ganho "real").',
      'Usado em reajustes de contratos, salários e benefícios.',
    ],
    inputs: [valor, meses, { id: 'real', label: 'Taxa real do título (% a.a. acima do IPCA)', value: 6, min: 0, max: 30 }],
    rules: [
      'Tesouro IPCA+: rende (1 + IPCA) × (1 + taxa real) − 1 ao ano. A simulação supõe que o IPCA fique no valor acumulado em 12 meses mostrado.',
      IR_RULE,
      'Poder de compra: valor líquido final descontado da inflação do período.',
    ],
    calc: (v, atual) => {
      const nominal = ((1 + atual / 100) * (1 + v.real / 100) - 1) * 100;
      const s = settle(v.valor, grow(v.valor, nominal, v.meses), v.meses, true);
      const poder = s.final / (1 + atual / 100) ** (v.meses / 12);
      return { rows: [
        ['Taxa nominal ao ano', pct(nominal)],
        ...settleRows(s),
        ['Poder de compra (em valores de hoje)', brl(poder)],
        ['Ganho real, já descontada a inflação', brl(poder - v.valor), true],
      ] };
    },
  },
  {
    id: 'poupanca',
    label: 'Poupança',
    title: 'Rendimento da poupança',
    what: 'É a aplicação mais popular do país. Rende todo mês, no "aniversário" do depósito, e o saldo é isento de imposto de renda para pessoa física. A regra é fixa em lei: com a Selic acima de 8,5% ao ano, rende 0,5% ao mês mais a TR; com a Selic em 8,5% ou menos, rende 70% da Selic mais a TR.',
    usage: [
      'Reserva de emergência de quem quer simplicidade: sem taxa, sem imposto, liquidez imediata.',
      'Costuma render menos que CDB e Tesouro Selic quando a Selic está alta.',
      'Resgate antes do aniversário do depósito perde o rendimento daquele mês.',
    ],
    inputs: [valor, meses],
    rules: [
      'Usa o rendimento mensal publicado pelo Banco Central (já inclui a TR), repetido a cada mês.',
      'Isenta de imposto de renda e sem taxa de administração.',
      'Rendimento creditado só no aniversário mensal; o prazo conta meses completos.',
    ],
    calc: (v, atual) => ({ rows: [
      ['Rendimento mensal usado', `${nf.format(atual)}% a.m. (≈ ${pct(annualize(atual))} a.a.)`],
      ...settleRows(settle(v.valor, v.valor * (1 + atual / 100) ** v.meses, v.meses, false)),
    ] }),
  },
  {
    id: 'livre',
    label: 'Simples × compostos',
    title: 'Juros simples e juros compostos',
    what: 'Nos juros simples, o rendimento de cada período incide só sobre o valor inicial: o ganho cresce em linha reta. Nos juros compostos, os juros entram no saldo e passam a render também ("juros sobre juros"): o ganho cresce cada vez mais rápido. Quanto maior o prazo, maior a diferença.',
    usage: [
      'Juros compostos: poupança, CDB, Tesouro, financiamentos, cartão de crédito e cheque especial.',
      'Juros simples: multa e juros de mora em atrasos, desconto de duplicatas e alguns empréstimos de curto prazo.',
      'Para dívidas, os compostos explicam por que ela "vira bola de neve"; para investimentos, por que investir cedo compensa.',
    ],
    inputs: [
      { id: 'capital', label: 'Capital inicial (R$)', value: 10000, min: 0, max: 1e9 },
      { id: 'aporte', label: 'Aporte mensal (R$)', value: 500, min: 0, max: 1e9 },
      { id: 'taxa', label: 'Taxa (% ao ano)', value: 12, min: 0, max: 1000 },
      meses,
      { id: 'regime', label: 'Regime do resultado', value: 'compound', options: [['compound', 'Juros compostos'], ['simple', 'Juros simples']] },
    ],
    rules: [
      'Aportes feitos no fim de cada mês.',
      'Compostos: taxa mensal equivalente (1 + a)^(1/12) − 1. Simples: taxa anual dividida por 12.',
      'Sem imposto, IOF ou taxas: cálculo matemático puro.',
    ],
    calc: (v) => {
      const f = { capital: v.capital, deposit: v.aporte, annualPct: v.taxa, months: v.meses };
      const comp = balances({ ...f, mode: 'compound' });
      const simple = balances({ ...f, mode: 'simple' });
      const shown = (v.regime === 'simple' ? simple : comp)[v.meses];
      const put = invested(f, v.meses);
      return {
        rows: [
          ['Total investido', brl(put)],
          ['Juros ganhos', brl(shown - put)],
          ['Montante final', brl(shown), true],
          ['Compostos − simples', brl(comp[v.meses] - simple[v.meses])],
        ],
        curves: { labels: comp.map((_, k) => k), comp, simple, put: comp.map((_, k) => invested(f, k)) },
      };
    },
  },
];
