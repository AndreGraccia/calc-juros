// Meta, teto ou referência oficial de cada juros, sempre com fonte.
// `ctx` = { meta, current }: meta de inflação lida do SGS (série 13521) e última leitura do indicador.
const nf = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const pct = (x) => `${nf.format(x)}%`;

const BCB_META = { label: 'Banco Central – Metas para a inflação', url: 'https://www.bcb.gov.br/controleinflacao/metainflacao' };
const BCB_SELIC = { label: 'Banco Central – Taxa Selic e Copom', url: 'https://www.bcb.gov.br/controleinflacao/taxaselic' };
const SGS = (n, nome) => ({ label: `Banco Central – SGS série ${n} (${nome})`, url: `https://api.bcb.gov.br/dados/serie/bcdata.sgs.${n}/dados/ultimos/1?formato=json` });

// Resolução CMN 5.141/2024: meta contínua de 3,00% com tolerância de ±1,5 p.p. a partir de 2025.
const TOLERANCE = 1.5;

export const TARGETS = {
  selic: () => ({
    headline: 'Não há teto nem meta para a Selic',
    text: 'A Selic é o instrumento do Banco Central, não o objetivo: o Copom sobe ou baixa a meta da Selic para levar a inflação (IPCA) à meta de inflação definida pelo Conselho Monetário Nacional. Por isso o "ideal" da Selic depende da inflação do momento.',
    sources: [BCB_SELIC, BCB_META, SGS(432, 'meta Selic')],
  }),
  cdi: () => ({
    headline: 'Não há teto nem meta oficial para o CDI',
    text: 'O CDI é uma taxa de mercado, apurada a partir das operações entre bancos, e acompanha de perto a meta da Selic. Quem influencia o CDI é o Copom, ao decidir a Selic.',
    sources: [SGS(4389, 'CDI anualizado, base 252'), BCB_SELIC],
  }),
  ipca: ({ meta, current }) => {
    const sources = [BCB_META, SGS(13521, 'meta para a inflação'), SGS(13522, 'IPCA acumulado em 12 meses')];
    if (meta == null) {
      return { headline: 'Meta de inflação definida pelo Conselho Monetário Nacional', text: 'O valor da meta está indisponível no momento.', sources };
    }
    const lo = meta - TOLERANCE;
    const hi = meta + TOLERANCE;
    const where = current == null ? '' : current > hi ? 'acima do limite superior' : current < lo ? 'abaixo do limite inferior' : 'dentro do intervalo de tolerância';
    return {
      headline: `Meta: ${pct(meta)} ao ano, com tolerância de ±1,5 ponto percentual (${pct(lo)} a ${pct(hi)})`,
      text: `${current == null ? '' : `O IPCA em 12 meses está em ${pct(current)}, ${where}. `}A meta vale de forma contínua (conferida todo mês sobre os 12 meses anteriores) e só é considerada descumprida quando a inflação fica fora do intervalo por seis meses seguidos. É definida pelo CMN e perseguida pelo Banco Central.`,
      sources,
    };
  },
  poupanca: () => ({
    headline: 'Regra definida em lei: depende da Selic',
    text: 'Com a meta da Selic acima de 8,5% ao ano, a poupança rende 0,5% ao mês mais a TR. Com a Selic em 8,5% ou menos, rende 70% da Selic mais a TR. Esse é o "teto" do que ela paga: nunca acompanha a Selic de verdade quando ela está alta.',
    sources: [
      { label: 'Lei 12.703/2012 (altera a Lei 8.177/1991)', url: 'https://www.planalto.gov.br/ccivil_03/_ato2011-2014/2012/lei/l12703.htm' },
      SGS(195, 'rendimento da poupança'),
    ],
  }),
};
