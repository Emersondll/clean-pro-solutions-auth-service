# Clean Pro Solutions - Auth Service 🔐

## 🎯 Papel no Ecossistema
O **Auth Service** é o guardião da segurança da plataforma. Ele é responsável por:
- Autenticação de usuários (Clientes e Profissionais).
- Geração e validação de tokens JWT.
- Gerenciamento de permissões e segurança de acesso.
- Comunicação com o `user-service` para verificação de credenciais.

Este serviço é fundamental para garantir que apenas usuários autorizados acessem os recursos sensíveis do ecossistema.

## 🚀 Tecnologias
- **Java 21** & **Spring Boot 3.3.4**
- **Spring Security** & **JWT**
- **MongoDB** (Persistência de credenciais e tokens)
- **RabbitMQ** (Eventos de login/logout e auditoria)
- **Netflix Eureka** (Service Discovery)

## 🛠️ Como Executar

### 1. Execução Isolada (Individual)
Para rodar apenas este serviço e suas dependências mínimas (Mongo, RabbitMQ, Eureka):
```bash
docker-compose up -d --build
```
O serviço estará disponível em `http://localhost:8081`.

### 2. Execução Integrada
Este serviço é orquestrado pelo projeto principal [Clean Pro Platform](../README.md). Para rodar no contexto completo, utilize o docker-compose da raiz.

## 🧪 Qualidade
- **Cobertura de Testes**: Mínimo de 80% (JaCoCo).
- **Build**: `mvn clean verify` valida a cobertura e os testes unitários.

---
© 2026 Clean Pro Solutions - Desenvolvido por Emerson Lima.
