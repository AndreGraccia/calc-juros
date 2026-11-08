# Calc Juros

Calculadora de juros simples e compostos com aportes mensais, gráficos comparativos e taxas reais da
[API do Banco Central (SGS)](https://dadosabertos.bcb.gov.br/): Selic, CDI, IPCA 12 meses e poupança.

Java 25 · Spring Boot 4.1 · H2 (SQL) · front em HTML/CSS/JS puro + Chart.js

## Sobre o projeto

Consulta dados econômicos oficiais do Banco Central e os usa para apresentar comparações entre taxas, além de
simulações financeiras de juros simples e compostos com taxas informadas pelo usuário ou obtidas do BCB.

### Objetivos

- Consumir dados oficiais do Banco Central e consultar taxas e indicadores econômicos.
- Comparar indicadores e exibir variações (histórico por período: próximo passo).
- Implementar cálculos de juros simples e compostos e simulações com taxas oficiais.
- Apresentar os dados de forma visual por meio de gráficos.

## Como rodar

```bash
./mvnw spring-boot:run
```

Abra <http://localhost:8080>. Testes: `./mvnw test` (Java) e `node --test src/test/js/calc.test.mjs` (contas do front).

## O que faz

Fluxo da tela: **escolher o juros** (Selic, CDI, IPCA, poupança ou simples × compostos) → **gráfico** do indicador
→ **o que significa** e **como é usado** → **meta, teto ou referência oficial** (com fonte) → **calculadora** com as
regras de negócio daquele produto.

- Selic (Tesouro Selic), CDI (CDB com IR ou LCI/LCA isenta, % do CDI), IPCA (Tesouro IPCA+, com poder de compra),
  poupança (isenta) e juros simples × compostos com aportes mensais.
- IR regressivo da renda fixa: 22,5% (até 180 dias), 20% (181–360), 17,5% (361–720), 15% (acima de 720).
- IPCA mostra a meta de inflação oficial (3,00% ±1,5 p.p., Resolução CMN 5.141/2024) e se a inflação atual está dentro.
- Se o BCB não responder, mostra o último histórico salvo no banco e avisa na tela.
- O rodapé traz sempre a fonte dos dados (séries SGS e leis usadas).

## Backend

Pacote `io.github.andregraccia.calcjuros.indicator`, em camadas:

| Camada | Classe | Papel |
|---|---|---|
| controller | `IndicatorController` | `GET /api/indicators/{id}/history` (`selic`, `cdi`, `ipca`, `poupanca`, `meta-ipca`) e `/selic` |
| service | `IndicatorService` | busca o histórico no BCB, grava, cai no banco se falhar |
| integration | `BcbClient` | `RestClient` para `api.bcb.gov.br` (timeouts de 3 s/5 s) |
| repository | `IndicatorPoint(Repository)` | tabela `indicator_point`, uma linha por indicador e data |
| common | `ApiExceptionHandler` | erros do BCB sem cache viram 502 em RFC 7807 |

Séries SGS usadas: Selic 432, CDI 4389, IPCA 12m 13522, meta de inflação 13521, poupança 195 (% a.m.).
O SGS limita `ultimos/N` a poucos valores, então o histórico é pedido por `dataInicial`.

### Fallback em SQL

Cada histórico buscado com sucesso é gravado (sem duplicar datas). Quando o BCB falha ou demora, a API devolve o
histórico já salvo, com `"stale": true`. Sem nada salvo, responde 502. O banco é um arquivo H2 em
`./data/` (ignorado pelo git).

### Exemplo de resposta

```json
{"id":"selic","name":"Selic (meta)","unit":"% a.a.","series":432,"stale":false,
 "points":[{"date":"2026-12-09","value":13.75}]}
```

## Configuração

| Propriedade | Padrão |
|---|---|
| `bcb.base-url` | `https://api.bcb.gov.br` |
| `spring.datasource.url` | `jdbc:h2:file:./data/calcjuros` |

Para ver o fallback: rode uma vez com internet e depois suba com `--bcb.base-url=http://localhost:9`.

## Front

Em `src/main/resources/static/`: `index.html`, `style.css`, `app.js` (tela e gráficos), `rates.js` (texto e regras de cada juros),
`targets.js` (meta/teto oficial com fonte) e `calc.js` (contas puras).
Visual claro/escuro, cores dos gráficos validadas para daltonismo e contraste.

## Limites conhecidos

- Aportes no fim de cada mês; sem imposto, IOF ou taxas. Simulação ilustrativa, não é recomendação de investimento.
- O histórico do banco cresce sem limpeza.
- Chart.js vem de CDN: sem internet, os gráficos não carregam.
