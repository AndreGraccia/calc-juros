import { balances, invested } from './calc.js';

const $ = (id) => document.getElementById(id);
const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const pct = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
const css = (n) => getComputedStyle(document.documentElement).getPropertyValue(n).trim();
const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
const axisMoney = (v) => brl.format(v).replace(/,00$/, '');

let rates = []; // {name, rate, date}
let lineChart;
let barChart;

function readForm() {
  const v = (id) => Number($(id).value);
  const f = { capital: v('capital'), deposit: v('deposit'), annualPct: v('rate'), months: Math.round(v('months')) };
  const errors = {
    capital: f.capital >= 0 ? '' : 'Informe um valor maior ou igual a zero.',
    deposit: f.deposit >= 0 ? '' : 'Informe um valor maior ou igual a zero.',
    rate: f.annualPct >= 0 && f.annualPct <= 1000 ? '' : 'Informe uma taxa entre 0 e 1000.',
    months: f.months >= 1 && f.months <= 600 ? '' : 'Informe de 1 a 600 meses.',
  };
  for (const [id, msg] of Object.entries(errors)) {
    $(id).setAttribute('aria-invalid', msg ? 'true' : 'false');
    $(`${id}-err`).textContent = msg;
  }
  return Object.values(errors).some(Boolean) ? null : f;
}

function render() {
  const f = readForm();
  if (!f) return;
  const c = { a: css('--data-1'), b: css('--data-2'), grid: css('--line'), ink: css('--muted'), neutral: css('--neutral') };
  const simple = balances({ ...f, mode: 'simple' });
  const comp = balances({ ...f, mode: 'compound' });
  const put = invested(f, f.months);
  const shown = document.querySelector('input[name=mode]:checked').value === 'simple' ? simple : comp;
  const final = shown[f.months];

  $('k-final').textContent = brl.format(final);
  $('k-invested').textContent = brl.format(put);
  $('k-interest').textContent = brl.format(final - put);
  $('k-diff').textContent = brl.format(comp[f.months] - simple[f.months]);

  const labels = comp.map((_, k) => k);
  const base = {
    responsive: true, maintainAspectRatio: false, animation: reduceMotion ? false : { duration: 250 },
  };
  const tip = { callbacks: { label: (t) => `${t.dataset.label}: ${brl.format(t.parsed.y ?? t.parsed.x)}` } };

  lineChart?.destroy();
  lineChart = new Chart($('line'), {
    type: 'line',
    data: { labels, datasets: [
      { label: 'Juros compostos', data: comp, borderColor: c.a, borderWidth: 2, pointRadius: 0, pointHoverRadius: 5 },
      { label: 'Juros simples', data: simple, borderColor: c.b, borderWidth: 2, pointRadius: 0, pointHoverRadius: 5 },
      { label: 'Total investido', data: labels.map((k) => invested(f, k)), borderColor: c.neutral, borderWidth: 1.5, borderDash: [5, 4], pointRadius: 0, pointHoverRadius: 4 },
    ] },
    options: { ...base, interaction: { mode: 'index', intersect: false },
      scales: {
        x: { title: { display: true, text: 'Meses', color: c.ink }, ticks: { color: c.ink, maxTicksLimit: 8 }, grid: { display: false } },
        y: { ticks: { color: c.ink, callback: axisMoney }, grid: { color: c.grid } },
      },
      plugins: { tooltip: tip, legend: { labels: { color: c.ink, boxWidth: 12, boxHeight: 2 } } } },
  });

  // Mesmo capital, aporte e prazo, capitalizados à taxa de cada indicador.
  const rows = [{ name: 'Sua taxa', rate: f.annualPct, own: true }, ...rates]
    .map((r) => ({ ...r, final: balances({ ...f, annualPct: r.rate, mode: 'compound' })[f.months] }))
    .sort((x, y) => y.final - x.final);
  barChart?.destroy();
  barChart = new Chart($('bars'), {
    type: 'bar',
    data: { labels: rows.map((r) => `${r.name} (${pct.format(r.rate)}%)`), datasets: [
      { label: 'Montante final', data: rows.map((r) => r.final), backgroundColor: rows.map((r) => (r.own ? c.a : c.b)), borderRadius: 4, maxBarThickness: 28 },
    ] },
    options: { ...base, indexAxis: 'y',
      scales: { x: { ticks: { color: c.ink, callback: axisMoney }, grid: { color: c.grid } }, y: { ticks: { color: c.ink }, grid: { display: false } } },
      plugins: { tooltip: tip, legend: { display: false } } },
  });

  $('table-body').replaceChildren(...rows.map((r) => {
    const tr = document.createElement('tr');
    for (const t of [r.name, pct.format(r.rate), brl.format(r.final)]) {
      const td = document.createElement('td');
      td.textContent = t;
      tr.append(td);
    }
    return tr;
  }));
  $('line').setAttribute('aria-label', `Evolução do saldo em ${f.months} meses: compostos ${brl.format(comp[f.months])}, simples ${brl.format(simple[f.months])}.`);
}

async function loadRates() {
  const box = $('rates');
  try {
    const res = await fetch('/api/indicators/rates');
    if (!res.ok) throw new Error(res.status);
    const data = await res.json();
    rates = data.map((r) => ({ name: r.name, rate: Number(r.rate), date: r.date }));
    const stale = $('stale');
    stale.hidden = !data.some((r) => r.stale);
    stale.textContent = 'O Banco Central não respondeu agora. Mostrando os últimos valores salvos; as datas ao lado de cada taxa indicam a leitura usada.';
    box.replaceChildren(...rates.map((r) => {
      const b = document.createElement('button');
      b.type = 'button';
      b.className = 'chip';
      const n = document.createElement('span');
      n.textContent = r.name;
      const v = document.createElement('strong');
      v.textContent = `${pct.format(r.rate)}% a.a.`;
      const d = document.createElement('small');
      d.textContent = r.date;
      b.append(n, v, d);
      b.addEventListener('click', () => { $('rate').value = r.rate; render(); });
      return b;
    }));
  } catch {
    box.textContent = 'Não foi possível carregar as taxas do Banco Central. A calculadora segue funcionando com a taxa que você digitar.';
    box.classList.add('warn');
  }
  render();
}

for (const el of document.querySelectorAll('#calc input')) el.addEventListener('input', render);
$('calc').addEventListener('submit', (e) => e.preventDefault());
matchMedia('(prefers-color-scheme: dark)').addEventListener('change', render);
render();
loadRates();
