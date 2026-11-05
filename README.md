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
