import { RATES } from './rates.js';
import { TARGETS } from './targets.js';

const $ = (id) => document.getElementById(id);
const nf = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const css = (n) => getComputedStyle(document.documentElement).getPropertyValue(n).trim();
const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
const brDate = (iso) => iso.split('-').reverse().join('/');
const axisMoney = (v) => brl.format(v).replace(/,00$/, '');

const cache = new Map(); // id -> histórico do backend
let rate = RATES[0];
let hist = null; // histórico do juros selecionado (null no "livre" ou se falhou)
let chart;
let token = 0; // ignora respostas de seleções antigas

const picker = $('picker');
for (const r of RATES) {
  const b = document.createElement('button');
  b.type = 'button';
  b.className = 'pill';
  b.dataset.id = r.id;
  b.textContent = r.label;
  b.addEventListener('click', () => select(r.id));
  picker.append(b);
}

async function load(id) {
  if (!cache.has(id)) {
    const res = await fetch(`/api/indicators/${id}/history`);
    if (!res.ok) throw new Error(res.status);
    cache.set(id, await res.json());
  }
  return cache.get(id);
}

const last = () => hist.points[hist.points.length - 1];

function buildFields() {
  const box = $('fields');
  box.replaceChildren(...rate.inputs.map((def) => {
    const wrap = document.createElement('div');
    wrap.className = 'field';
    const label = document.createElement('label');
    label.htmlFor = `f-${def.id}`;
    label.textContent = def.label;
    let el;
    if (def.options) {
      el = document.createElement('select');
      for (const [v, text] of def.options) el.add(new Option(text, v));
    } else {
      el = document.createElement('input');
      Object.assign(el, { type: 'number', min: def.min, max: def.max, step: def.step ?? 'any', inputMode: 'decimal' });
    }
    el.id = `f-${def.id}`;
    el.value = def.value;
    el.setAttribute('aria-describedby', `e-${def.id}`);
    el.addEventListener('input', compute);
    const err = document.createElement('div');
    err.className = 'err';
    err.id = `e-${def.id}`;
    err.setAttribute('role', 'alert');
    wrap.append(label, el, err);
    return wrap;
  }));
}

function readValues() {
  const values = {};
  let ok = true;
  for (const def of rate.inputs) {
    const el = $(`f-${def.id}`);
    let msg = '';
    if (def.options) {
      values[def.id] = el.value;
    } else {
      const n = el.value === '' ? NaN : Number(el.value);
      values[def.id] = n;
      if (!(n >= def.min && n <= def.max)) msg = `Informe um valor entre ${nf.format(def.min)} e ${nf.format(def.max)}.`;
      if (!msg && def.step === 1 && !Number.isInteger(n)) msg = 'Informe um número inteiro.';
    }
    el.setAttribute('aria-invalid', msg ? 'true' : 'false');
    $(`e-${def.id}`).textContent = msg;
    ok &&= !msg;
  }
  return ok ? values : null;
}

function renderRows(rows) {
  $('results').replaceChildren(...rows.map(([label, value, strong]) => {
    const row = document.createElement('div');
    row.className = strong ? 'row strong' : 'row';
    const dt = document.createElement('dt');
    dt.textContent = label;
    const dd = document.createElement('dd');
    dd.textContent = value;
    row.append(dt, dd);
    return row;
  }));
}

function compute() {
  const values = readValues();
  if (!values) return;
  if (rate.id !== 'livre' && !hist) {
    renderRows([['Taxa indisponível', 'Sem dados do Banco Central']]);
    return;
  }
  const { rows, curves } = rate.calc(values, hist ? Number(last().value) : 0);
  renderRows(rows);
  if (curves) drawFree(curves);
}

function chartColors() {
  return { a: css('--data-1'), b: css('--data-2'), grid: css('--line'), ink: css('--muted'), neutral: css('--neutral') };
}

function baseOptions(c) {
  return {
    responsive: true, maintainAspectRatio: false, animation: reduceMotion ? false : { duration: 250 },
    interaction: { mode: 'index', intersect: false },
    scales: { x: { ticks: { color: c.ink, maxTicksLimit: 8 }, grid: { display: false } }, y: { ticks: { color: c.ink }, grid: { color: c.grid } } },
  };
}

function drawHistory() {
  const c = chartColors();
  const o = baseOptions(c);
  chart?.destroy();
  chart = new Chart($('chart'), {
    type: 'line',
    data: { labels: hist.points.map((p) => brDate(p.date)), datasets: [
      { label: hist.name, data: hist.points.map((p) => Number(p.value)), borderColor: c.a, borderWidth: 2, pointRadius: 0, pointHoverRadius: 5, stepped: rate.id === 'selic' },
    ] },
    options: { ...o,
      scales: { ...o.scales, y: { ...o.scales.y, ticks: { color: c.ink, callback: (v) => `${nf.format(v)}%` } } },
      plugins: { legend: { display: false }, tooltip: { callbacks: { label: (t) => `${nf.format(t.parsed.y)} ${hist.unit}` } } } },
  });
  const vals = hist.points.map((p) => Number(p.value));
  $('chart').setAttribute('aria-label', `${hist.name}: de ${brDate(hist.points[0].date)} a ${brDate(last().date)}, mínimo ${nf.format(Math.min(...vals))}, máximo ${nf.format(Math.max(...vals))}, último ${nf.format(vals.at(-1))} ${hist.unit}.`);
}

function drawFree(cv) {
  const c = chartColors();
  const o = baseOptions(c);
  chart?.destroy();
  chart = new Chart($('chart'), {
    type: 'line',
    data: { labels: cv.labels, datasets: [
      { label: 'Juros compostos', data: cv.comp, borderColor: c.a, borderWidth: 2, pointRadius: 0, pointHoverRadius: 5 },
      { label: 'Juros simples', data: cv.simple, borderColor: c.b, borderWidth: 2, pointRadius: 0, pointHoverRadius: 5 },
      { label: 'Total investido', data: cv.put, borderColor: c.neutral, borderWidth: 1.5, borderDash: [5, 4], pointRadius: 0, pointHoverRadius: 4 },
    ] },
    options: { ...o,
      scales: { x: { ...o.scales.x, title: { display: true, text: 'Meses', color: c.ink } }, y: { ...o.scales.y, ticks: { color: c.ink, callback: axisMoney } } },
      plugins: { legend: { labels: { color: c.ink, boxWidth: 12, boxHeight: 2 } }, tooltip: { callbacks: { label: (t) => `${t.dataset.label}: ${brl.format(t.parsed.y)}` } } } },
  });
  $('chart').setAttribute('aria-label', `Evolução do saldo: compostos ${brl.format(cv.comp.at(-1))}, simples ${brl.format(cv.simple.at(-1))}, investido ${brl.format(cv.put.at(-1))}.`);
}

function showTarget(meta) {
  const make = TARGETS[rate.id];
  $('target').hidden = !make;
  if (!make) return;
  const t = make({ meta, current: hist ? Number(last().value) : null });
  $('t-head').textContent = t.headline;
  $('t-text').textContent = t.text;
  $('t-src').replaceChildren(...t.sources.flatMap((s, i) => {
    const a = Object.assign(document.createElement('a'), { href: s.url, textContent: s.label, target: '_blank', rel: 'noopener' });
    return i ? [document.createTextNode(' · '), a] : [a];
  }));
}

function fill(id, items) {
  $(id).replaceChildren(...items.map((t) => Object.assign(document.createElement('li'), { textContent: t })));
}

function showHeader() {
  const err = $('chart-err');
  err.hidden = true;
  $('stale').hidden = true;
  if (rate.id === 'livre') {
    $('r-now').textContent = 'Você escolhe a taxa';
    $('r-date').textContent = 'Compare os dois regimes com os seus números.';
    $('source-now').textContent = 'Nesta opção o cálculo usa só os valores que você digitar; as taxas oficiais estão nas outras abas.';
    return;
  }
  if (!hist) {
    $('r-now').textContent = 'Indisponível';
    $('r-date').textContent = '';
    err.hidden = false;
    err.textContent = 'Não foi possível carregar este indicador do Banco Central e não há dados salvos.';
    $('source-now').textContent = 'Nenhuma leitura disponível agora para esta série.';
    return;
  }
  const l = last();
  const v = Number(l.value);
  $('r-now').textContent = `${nf.format(v)} ${hist.unit}`;
  $('r-date').textContent = `última leitura em ${brDate(l.date)}${rate.id === 'poupanca' ? ' · rendimento mensal' : ''}`;
  $('stale').hidden = !hist.stale;
  $('source-now').textContent = `Exibindo: série SGS ${hist.series} (${hist.name}), leitura de ${brDate(l.date)}${hist.stale ? ', valor salvo no banco porque o Banco Central não respondeu' : ''}.`;
}

async function select(id) {
  rate = RATES.find((r) => r.id === id) ?? RATES[0];
  history.replaceState(null, '', `#${rate.id}`);
  for (const b of picker.children) b.setAttribute('aria-pressed', String(b.dataset.id === rate.id));
  $('r-title').textContent = rate.title;
  $('r-what').textContent = rate.what;
  fill('r-usage', rate.usage);
  fill('rules', rate.rules);
  buildFields();
  hist = null;
  const mine = ++token;
  if (rate.id !== 'livre') {
    $('r-now').textContent = 'Carregando…';
    $('r-date').textContent = '';
    try { hist = await load(rate.id); } catch { /* sem dados: showHeader mostra o aviso */ }
    if (mine !== token) return;
  }
  let meta = null; // meta de inflação oficial (SGS 13521), só para o IPCA
  if (rate.id === 'ipca') {
    try { const m = await load('meta-ipca'); meta = Number(m.points[m.points.length - 1].value); } catch { /* texto cai no aviso de indisponível */ }
    if (mine !== token) return;
  }
  showTarget(meta);
  showHeader();
  if (hist) drawHistory();
  else if (rate.id !== 'livre') { chart?.destroy(); chart = null; }
  compute();
}

$('calc').addEventListener('submit', (e) => e.preventDefault());
matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
  if (hist) drawHistory();
  else compute();
});
select(location.hash.slice(1));
