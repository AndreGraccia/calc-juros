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

- Simula capital inicial + aporte mensal + taxa anual + prazo, em juros simples e compostos.
- Gráfico de linhas (compostos × simples × total investido) e de barras (quanto você teria em cada taxa do BCB).
- Clicar numa taxa do BCB a usa na simulação.
- Se o BCB não responder, mostra os últimos valores salvos no banco e avisa na tela.

## Backend

Pacote `io.github.andregraccia.calcjuros.indicator`, em camadas:

| Camada | Classe | Papel |
|---|---|---|
| controller | `IndicatorController` | `GET /api/indicators/rates` (e `/selic`) |
| service | `IndicatorService` | busca no BCB, grava, cai no banco se falhar, anualiza a poupança |
| integration | `BcbClient` | `RestClient` para `api.bcb.gov.br` (timeouts de 3 s/5 s) |
| repository | `IndicatorSnapshot(Repository)` | tabela `indicator_snapshot`, uma linha por indicador e data |
| common | `ApiExceptionHandler` | erros do BCB sem cache viram 502 em RFC 7807 |

Séries SGS usadas: Selic 432, CDI 4389, IPCA 12m 13522, poupança 195 (mensal, convertida para % a.a.).

### Fallback em SQL

Cada leitura bem-sucedida é gravada (sem duplicar a mesma data). Quando o BCB falha ou demora, a API devolve a
leitura mais recente salva, com `"stale": true`. Sem nada salvo, responde 502. O banco é um arquivo H2 em
`./data/` (ignorado pelo git).

### Exemplo de resposta

```json
[{"name":"Selic","rate":13.75,"unit":"% a.a.","date":"09/12/2026","stale":false}]
```

## Configuração

| Propriedade | Padrão |
|---|---|
| `bcb.base-url` | `https://api.bcb.gov.br` |
| `spring.datasource.url` | `jdbc:h2:file:./data/calcjuros` |

Para ver o fallback: rode uma vez com internet e depois suba com `--bcb.base-url=http://localhost:9`.

## Front

Em `src/main/resources/static/`: `index.html`, `style.css`, `app.js` (tela e gráficos) e `calc.js` (contas puras).
Visual claro/escuro, cores dos gráficos validadas para daltonismo e contraste, tabela alternativa aos gráficos.

## Limites conhecidos

- Aportes no fim de cada mês; sem imposto, IOF ou taxas. Simulação ilustrativa, não é recomendação de investimento.
- O histórico do banco cresce sem limpeza.
- Chart.js vem de CDN: sem internet, os gráficos não carregam.
# Calculadora de Juros e Indicadores Econômicos

Aplicação backend em Java para simular operações de juros e evoluir para consulta de indicadores econômicos oficiais do Banco Central do Brasil.

> **Status:** em desenvolvimento. As funcionalidades descritas como objetivos representam o roadmap e não necessariamente estão implementadas na versão atual.

## Objetivos

- Implementar cálculos de juros simples e compostos.
- Consultar séries temporais e indicadores publicados pelo Banco Central.
- Comparar taxas e períodos, destacando variações.
- Disponibilizar uma API HTTP com validação de entradas e respostas consistentes.
- Adicionar testes automatizados para regras financeiras e integração com fontes externas.

## Tecnologias

- Java 25
- Spring Boot
- Spring MVC
- Bean Validation
- Maven Wrapper

## Pré-requisitos

- JDK 25 instalado e configurado em `JAVA_HOME`.
- Git (opcional, para clonar o repositório).

Confira as versões instaladas:

```bash
java -version
```

## Como executar

Clone o repositório e entre na pasta:

```bash
git clone https://github.com/AndreGraccia/calc-juros.git
cd calc-juros
```

No Windows, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

## Executar os testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux ou macOS:

```bash
./mvnw test
```

## API REST

A aplicação disponibiliza os seguintes endpoints para simulação de juros:

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/calculations/simple` | Calcula juros simples. |
| `POST` | `/api/calculations/compound` | Calcula juros compostos. |

As rotas recebem os dados da simulação em JSON e retornam informações como valor principal, taxa, períodos, juros acumulados e montante final. As entradas são validadas e os erros seguem uma estrutura padronizada.

> Consulte os DTOs e os testes da aplicação para conferir o contrato JSON atualizado de cada endpoint.

## Roadmap

- [ ] Integrar séries temporais do Banco Central com tratamento de falhas externas.
- [ ] Documentar exemplos completos de requisição e resposta da API.
- [ ] Adicionar testes de integração para fontes externas.

## Princípios do projeto

- Usar `BigDecimal` para cálculos monetários, evitando erros de precisão de ponto flutuante.
- Separar regras de negócio, camada HTTP e integração externa.
- Validar entradas e retornar mensagens de erro claras.
- Não confundir dados oficiais consultados com estimativas ou exemplos.

## Fonte de dados

O projeto pretende utilizar dados públicos disponibilizados pelo Banco Central do Brasil. Quando a integração estiver implementada, a documentação deverá identificar as séries consultadas e explicar como os dados são interpretados.

## Contribuição

Mudanças devem ser feitas em branches separadas e acompanhadas de testes sempre que possível. Antes de abrir um pull request, execute `./mvnw test` (ou `mvnw.cmd test` no Windows).

## Aviso

Este projeto tem finalidade educacional e informativa. Simulações não constituem recomendação de investimento nem substituem uma avaliação financeira individual.
