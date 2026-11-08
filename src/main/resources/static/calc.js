// Matemática e regras de negócio puras (sem DOM), testáveis com `node --test`.

/** IR regressivo da renda fixa (Lei 11.033/2004), pelo prazo em dias corridos. */
export const irRate = (days) => (days <= 180 ? 0.225 : days <= 360 ? 0.2 : days <= 720 ? 0.175 : 0.15);

/** % a.m. -> % a.a. composta. */
export const annualize = (monthlyPct) => ((1 + monthlyPct / 100) ** 12 - 1) * 100;

/** Valor após `months` meses a uma taxa efetiva anual (% a.a.). */
export const grow = (principal, annualPct, months) => principal * (1 + annualPct / 100) ** (months / 12);

/** CDB/LCI: cada dia útil rende pctCdi% do CDI diário (base 252); 21 dias úteis por mês. */
export function growCdi(principal, cdiAnnualPct, pctCdi, months) {
  const daily = (1 + cdiAnnualPct / 100) ** (1 / 252) - 1;
  return principal * (1 + (daily * pctCdi) / 100) ** (months * 21);
}

/** Aplica o IR regressivo ao rendimento (mês = 30 dias). taxed=false para produtos isentos. */
export function settle(principal, grossFinal, months, taxed) {
  const gross = grossFinal - principal;
  const rate = taxed ? irRate(months * 30) : 0;
  const ir = gross * rate;
  return { principal, gross, rate, ir, net: gross - ir, final: principal + gross - ir };
}

/** Taxa mensal decimal: composta = (1+a)^(1/12)-1; simples = a/12. */
export function monthlyRate(annualPct, mode) {
  const a = annualPct / 100;
  return mode === 'simple' ? a / 12 : (1 + a) ** (1 / 12) - 1;
}

/** Saldo mês a mês (aportes no fim do mês), índices 0..months. */
export function balances({ capital, deposit, annualPct, months, mode }) {
  const i = monthlyRate(annualPct, mode);
  const out = [];
  let b = capital;
  for (let k = 0; k <= months; k++) {
    out.push(
      mode === 'simple'
        ? capital * (1 + i * k) + deposit * (k + (i * k * (k - 1)) / 2)
        : b,
    );
    b = b * (1 + i) + deposit;
  }
  return out;
}

export const invested = ({ capital, deposit }, k) => capital + deposit * k;
