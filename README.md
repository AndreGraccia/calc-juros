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
