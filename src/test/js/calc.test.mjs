import test from 'node:test';
import assert from 'node:assert/strict';
import { balances, invested } from '../../main/resources/static/calc.js';

const base = { capital: 1000, deposit: 0, annualPct: 12, months: 12 };

test('composto: 12 meses a 12% a.a. rende exatamente 12%', () => {
  assert.ok(Math.abs(balances({ ...base, mode: 'compound' })[12] - 1120) < 1e-9);
});
test('simples: 12 meses a 12% a.a. rende 12% sobre o capital', () => {
  assert.ok(Math.abs(balances({ ...base, mode: 'simple' })[12] - 1120) < 1e-9);
});
test('composto supera simples após 1 ano e aportes entram no total investido', () => {
  const f = { ...base, deposit: 100, months: 24 };
  assert.ok(balances({ ...f, mode: 'compound' })[24] > balances({ ...f, mode: 'simple' })[24]);
  assert.equal(invested(f, 24), 3400);
});
test('simples e composto coincidem no mês 0', () => {
  assert.equal(balances({ ...base, mode: 'simple' })[0], 1000);
});
