# Budgeting AI

API inteligente de orçamento criada para o desafio Spring AI da DIO.

A aplicação permite registrar transações financeiras, consultar gastos por categoria e período e gerar um resumo do orçamento. No perfil `openai`, também recebe comandos de voz em português, transforma o áudio em texto, usa `ChatClient` com tool calling para executar os casos de uso reais e devolve uma resposta em áudio.

## O que foi implementado

Além do fluxo base de voz, esta versão evolui o projeto com:

- filtros combináveis por categoria, data inicial e data final;
- resumo com total, quantidade de transações e distribuição por categoria;
- categorias `FOOD`, `HEALTH`, `TRANSPORT`, `HOUSING`, `LEISURE` e `OTHER`;
- validação de descrição, valor, categoria e data;
- armazenamento monetário em centavos, evitando erros de ponto flutuante;
- perfil local com H2, que permite testar CRUD e consultas sem chave de IA;
- tratamento de erros com respostas JSON padronizadas.

## Tecnologias

- Java 17
- Spring Boot 3.5.13
- Spring AI 1.0.9
- Spring Web e Bean Validation
- Spring Data JPA
- H2 no desenvolvimento local
- OpenAI para transcrição, chat/tool calling e text-to-speech
- Gradle Wrapper 9.4.1

## Como executar

É necessário ter Java 17 instalado. O Gradle já está incluído no projeto pelo wrapper.

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

O perfil padrão é `local`. Os dados ficam em `./data/budgeting.mv.db`, que é ignorado pelo Git.

Para habilitar o fluxo de voz com OpenAI, defina a chave na sessão atual e ative o perfil:

```powershell
$env:OPENAI_API_KEY = "sua_chave_aqui"
.\gradlew.bat bootRun --args="--spring.profiles.active=openai"
```

A chave é lida apenas por variável de ambiente e não deve ser adicionada ao código.

## Endpoints REST

### Criar transação

```powershell
curl.exe -X POST http://localhost:8080/api/transactions `
  -H "Content-Type: application/json" `
  -d '{"description":"Mercado","amount":39.90,"category":"FOOD","occurredAt":"2026-09-10"}'
```

`occurredAt` é opcional; quando omitido, a data atual é usada. Valores são enviados em reais e persistidos em centavos.

### Consultar transações

Todos os filtros são opcionais:

```powershell
curl.exe "http://localhost:8080/api/transactions?category=FOOD&from=2026-09-01&to=2026-09-30"
```

### Consultar resumo

```powershell
curl.exe "http://localhost:8080/api/transactions/summary?from=2026-09-01&to=2026-09-30"
```

Exemplo de resposta:

```json
{
  "from": "2026-09-01",
  "to": "2026-09-30",
  "category": null,
  "transactionCount": 2,
  "totalAmount": 80.00,
  "totalsByCategory": {
    "FOOD": 50.00,
    "TRANSPORT": 30.00
  }
}
```

### Assistente de voz

Disponível no perfil `openai`:

```powershell
curl.exe -X POST http://localhost:8080/api/assistant/voice `
  -F "file=@.audiomeu-comando.m4a" `
  --output resposta.mp3
```

O comando deve ser em português e pode, por exemplo, dizer: “gastei 39 reais no mercado”. O modelo deve escolher uma ferramenta de negócio e a aplicação executa o caso de uso correspondente.

No perfil `local`, esse endpoint responde `503 Service Unavailable` explicando que a IA está desativada.

## Arquitetura

```text
HTTP Controller
      |
Application Use Cases
      |
Domain + TransactionRepository
      |
JPA/H2 Adapter
```

O adaptador de IA também chama os mesmos casos de uso da aplicação. Assim, uma transação criada por REST ou por tool calling passa pelas mesmas validações e regras de negócio.

## Testes

Os testes locais cobrem:

- regras de domínio e validações;
- uso da data atual quando a data não é informada;
- criação de transação via HTTP;
- filtro por categoria e período;
- resumo financeiro com distribuição por categoria;
- respostas `400` para dados inválidos;
- carregamento da aplicação sem `OPENAI_API_KEY`.
- teste end-to-end opcional de transcrição, tool calling e áudio com OpenAI.

```powershell
.\gradlew.bat test
```

O teste `OpenAiVoiceAssistantIT` é condicionado à variável `OPENAI_API_KEY` e é ignorado automaticamente sem ela. Quando habilitado, ele chama o provedor real e pode gerar custos.

## O que aprendi

O principal aprendizado foi separar o recurso de IA da regra de negócio. O modelo interpreta a intenção e escolhe uma ferramenta, mas quem valida e persiste a transação é o caso de uso da aplicação. Também pratiquei o armazenamento de dinheiro em centavos, filtros com Specification do Spring Data e configuração por profiles para separar execução local de integração externa.
