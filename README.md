# Projeto Dízimo WG (Aplicativo de Doações)

## 📖 Sobre o Projeto

O Dízimo WG é um aplicativo full-stack (Android/Kotlin + Node.js) para gerenciamento de doações (dízimo). A aplicação permite que os usuários façam doações pontuais de forma segura, utilizando PIX ou Cartão de Crédito, com uma integração completa com a API V1 do Mercado Pago.

Este projeto inclui um fluxo de autenticação, histórico de doações e um sistema robusto de pagamento com cartão, incluindo a capacidade de salvar cartões no cofre do Mercado Pago (via WebView V1) e reutilizá-los para pagamentos futuros (com solicitação de CVV).

---

## ✨ Funcionalidades Principais

* **Autenticação de Usuário:** Login seguro (JWT).
* **Fluxo de Doação:**
    * Seleção de valor (presets ou customizado).
    * Escolha da forma de pagamento (PIX ou Cartão).
* **Pagamento com PIX:**
    * Geração de QR Code (Copia e Cola) via API do Mercado Pago.
    * Polling de status do pagamento.
* **Pagamento com Cartão de Crédito:**
    * **Adicionar Cartão:** Formulário seguro em `WebView` (isolado) usando o **SDK V1 do Mercado Pago** (`mercadopago.js`).
    * **Listar Cartões:** Exibe os cartões salvos do usuário.
    * **Pagar com Cartão Salvo:** Fluxo de segurança (PCI-compliant) que solicita o CVV e gera um token temporário no backend antes de processar o pagamento.
* **Histórico:** Tela de histórico de doações.

---

## 🛠️ Tecnologias Utilizadas

Este projeto é dividido em duas partes principais:

### 📱 Frontend (Android)

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Arquitetura:** MVVM (ViewModel, UiState, Repository)
* **Navegação:** [Jetpack Navigation Component](https://developer.android.com/guide/navigation)
* **Comunicação (Rede):** [Retrofit 2](https://square.github.io/retrofit/) & OkHttp (com `AuthInterceptor` para JWT)
* **WebView:** Para o formulário seguro (PCI) de adição de cartão do Mercado Pago V1.

### ⚙️ Backend (Node.js)

* **Linguagem:** [TypeScript](https://www.typescriptlang.org/)
* **Framework:** [Express.js](https://expressjs.com/pt-br/)
* **Banco de Dados (ORM):** [Prisma](https://www.prisma.io/) (conectado ao seu banco de dados)
* **Autenticação:** JWT (Tokens Bearer)
* **Pagamentos:** [API do Mercado Pago (V1)](https://www.mercadopago.com.br/developers/pt)
    * Criação de Clientes (`/v1/customers`)
    * Criação de Cartões (`/v1/customers/.../cards`)
    * Criação de Tokens com CVV (`/v1/card_tokens`)
    * Processamento de Pagamentos (`/v1/payments`)
    * Criação de Ordens PIX (`/v1/orders`)

---
