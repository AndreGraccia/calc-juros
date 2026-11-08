import test from 'node:test';
import assert from 'node:assert/strict';
import { annualize, balances, grow, growCdi, invested, irRate, settle } from '../../main/resources/static/calc.js';
import { RATES } from '../../main/resources/static/rates.js';

const near = (a, b, eps = 1e-6) => assert.ok(Math.abs(a - b) < eps, `${a} != ${b}`);

test('IR regressivo: 22,5% / 20% / 17,5% / 15% nas fronteiras de prazo', () => {
  assert.equal(irRate(180), 0.225);
  assert.equal(irRate(181), 0.2);
  assert.equal(irRate(360), 0.2);
  assert.equal(irRate(361), 0.175);
  assert.equal(irRate(720), 0.175);
  assert.equal(irRate(721), 0.15);
});

test('settle: 12 meses a 10% a.a. em R$ 1000 paga 20% de IR sobre R$ 100', () => {
  const s = settle(1000, grow(1000, 10, 12), 12, true);
  near(s.gross, 100);
  near(s.ir, 20);
  near(s.final, 1080);
});

test('settle isento (poupança, LCI) não desconta IR', () => {
  const s = settle(1000, 1100, 6, false);
  assert.equal(s.ir, 0);
  assert.equal(s.final, 1100);
});

test('CDI: 100% do CDI por 252 dias úteis (12 meses) rende o CDI anual', () => {
  near(growCdi(1000, 13.65, 100, 12), 1000 * 1.1365, 1e-6);
});

test('poupança: 0,5% a.m. equivale a 6,1678% a.a.', () => {
  near(annualize(0.5), 6.167781, 1e-5);
});

test('calc das abas: IPCA+ separa ganho real da inflação e CDB paga mais IR que LCI', () => {
  const byId = Object.fromEntries(RATES.map((r) => [r.id, r]));
  const ipca = byId.ipca.calc({ valor: 1000, meses: 12, real: 6 }, 5).rows;
  const real = ipca.find(([l]) => l.startsWith('Ganho real'))[1];
  assert.ok(real.includes('R$'));
  const cdb = byId.cdi.calc({ valor: 1000, meses: 12, pct: 100, tipo: 'cdb' }, 13).rows;
  const lci = byId.cdi.calc({ valor: 1000, meses: 12, pct: 100, tipo: 'lci' }, 13).rows;
  const final = (rows) => rows.find(([l]) => l === 'Valor final líquido')[1];
  assert.notEqual(final(cdb), final(lci));
});

test('simples × compostos: coincidem no mês 0 e compostos vencem depois; aportes entram no investido', () => {
  const f = { capital: 1000, deposit: 100, annualPct: 12, months: 24 };
  assert.equal(balances({ ...f, mode: 'simple' })[0], 1000);
  assert.ok(balances({ ...f, mode: 'compound' })[24] > balances({ ...f, mode: 'simple' })[24]);
  assert.equal(invested(f, 24), 3400);
});
