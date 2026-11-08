// Matemática pura (sem DOM), testável com `node --test`.
// Aportes no fim de cada mês. Taxa anual em %.

/** Taxa mensal decimal: composta = (1+a)^(1/12)-1; simples = a/12. */
export function monthlyRate(annualPct, mode) {
  const a = annualPct / 100;
  return mode === 'simple' ? a / 12 : (1 + a) ** (1 / 12) - 1;
}

/** Saldo mês a mês, índices 0..months. */
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
