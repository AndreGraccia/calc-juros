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

## Roadmap

- [ ] Definir e documentar os contratos dos endpoints REST.
- [ ] Implementar e testar juros simples e compostos.
- [ ] Validar taxas, valores monetários e períodos de entrada.
- [ ] Integrar séries temporais do Banco Central com tratamento de falhas externas.
- [ ] Adicionar testes de unidade e de integração.
- [ ] Documentar exemplos de requisição e resposta da API.
- [ ] Adicionar pipeline de CI para executar os testes em pull requests.

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


## API de cálculos de juros

A API disponibiliza endpoints para simular juros simples e compostos. Inicie a aplicação com `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

### Juros simples

`POST /api/calculations/simple`

### Juros compostos

`POST /api/calculations/compound`

Ambos recebem JSON com capital inicial (`principal`), taxa percentual por período (`ratePercent`) e quantidade de períodos (`periods`). Por exemplo, `ratePercent: 2` significa 2% por período.

Exemplo de requisição:

```json
{
  "principal": 1000.00,
  "ratePercent": 2,
  "periods": 3
}
```

Exemplo de resposta para juros simples:

```json
{
  "type": "SIMPLE",
  "principal": 1000.00,
  "ratePercent": 2,
  "periods": 3,
  "interestAmount": 60.00,
  "finalAmount": 1060.00
}
```

A resposta de juros compostos usa `"type": "COMPOUND"` e aplica capitalização por período. Os valores monetários são calculados com `BigDecimal` e arredondados para duas casas decimais.

Entradas ausentes, negativas ou fora dos limites documentados retornam HTTP 400 com um objeto `ApiError`, incluindo timestamp, status, mensagem e caminho da requisição. O número de períodos deve estar entre 0 e 10000; o capital inicial aceita até 15 dígitos inteiros e 2 decimais, e a taxa até 6 dígitos inteiros e 4 decimais.
