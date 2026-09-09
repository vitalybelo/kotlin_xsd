# 🚀 Kotlin XSD Integration & Routing Service

Сервис для интеграции и маршрутизации событий пользователей между Identity Provider (Keycloak) и внешними системами (CRM, Billing) через брокер сообщений Apache ActiveMQ Artemis.

В основе механизма лежит **жесткая XSD-валидация** XML-событий с автоматической изоляцией невалидных сообщений в Dead Letter Queue (DLQ).

---

## 🛠 Технологический стек

* **Язык:** Kotlin
* **Фреймворк:** Spring Boot (Web, JMS, Configuration Properties)
* **Брокер сообщений:** Apache ActiveMQ Artemis
* **Identity Provider:** Keycloak (Admin REST Client)
* **Парсинг и валидация:** Jackson XML, W3C XML Schema (XSD)

---

## 🏗 Архитектура потока данных

1. **REST API (`/keycloak/user`)** принимает JSON с данными пользователя.
2. Сервис проверяет наличие пользователя в **Keycloak**. Создает нового или обновляет существующего, включая маппинг кастомных атрибутов (например, `phone`, `required_2FA`).
3. Генерируется **XML-событие** (`UserEvent`) на основе состояния пользователя и типа операции (`CREATED`, `UPDATED`).
4. **XSD-валидатор** (`XsdValidator`) проверяет XML на соответствие строгой схеме `user-event.xsd`.
5. **Маршрутизация (ArtemisXsdSender):**
    * **Успех:** Сообщение летит в `queue.crm.out` и `queue.billing.out`.
    * **Ошибка валидации:** Перехватывается исключение, и XML отправляется в отстойник `queue.dlq.out`.

---

## 📁 Структура конфигурации

* **`application.yml`** — Настройки портов, кредов Keycloak, адресов брокера и маппинг JMS-очередей.
* **`user-event.xsd`** — Схема данных. Содержит `xs:enumeration` для жесткой привязки типов событий и методов 2FA, а также `minOccurs="0"` для необязательных полей.
* **`broker.xml`** — Конфигурация Artemis. Жестко заданные `anycast` очереди, `diverts` для перенаправления и политика отправки недоставленных сообщений в `queue.dlq.in`.

---

## 🚀 Быстрый старт (Docker Compose)

### 1. Подготовка
Убедись, что рядом с `docker-compose.yml` лежит файл настроек брокера `broker.xml`. В нем прописаны все необходимые адреса и очереди.

### 2. Запуск инфраструктуры
```bash
docker-compose up -d --build
```
Это поднимет:
* **ActiveMQ Artemis** на портах `61616` (брокер) и `8161` (веб-консоль).
* **Keycloak** на порту `8443` (замаплен на внутренний 8080).
* **Kotlin XSD App** на порту `8080`.

### 3. Настройка Keycloak
Для корректной работы сервиса в Keycloak необходимо создать:
* Realm: `SpringBootKeycloak`
* Client: `login-admin` (с включенной Service Accounts Roles и кредами из `application.yml`).

---

## 📡 REST API Эндпоинты

### 1. Создание / Обновление пользователя
`POST /api/xsd/keycloak/user`

**Payload:**
```json
{
  "username": "vitos_test",
  "firstName": "Vitaly",
  "lastName": "Test",
  "email": "vitos@local.dev",
  "phone": "+79991234567",
  "required_2FA": "TELEGRAM"
}
```
*Примечание: Если атрибуты отсутствуют, они не будут включены в итоговый XML благодаря `@JsonInclude(JsonInclude.Include.NON_EMPTY)`.*

### 2. Поиск пользователя
`GET /api/xsd/keycloak/user?username=vitos_test`

Возвращает `UserRepresentation` напрямую из Keycloak.

---

## 🧪 Сценарии тестирования

### ✅ Happy Path (Валидные данные)
Отправка пользователя с поддерживаемым методом двухфакторки (например, `TELEGRAM`).
* **Логи:** `Message validated by XSD successfully sent to CRM/Billing for user = vitos_test`
* **Artemis:** Сообщения появляются в очередях `queue.crm.out` и `queue.billing.out`.

### 🚨 DLQ Path (Невалидные данные XSD)
Отправка пользователя с кривым методом 2FA (например, `VIBER`), которого нет в `user-event.xsd`.
* **Логи Spring:** Ошибка `XSD validation failed. Message routed to DLQ`.
* **Логи JMS Listener:** `<---- Received INVALID message in DLQ: <XML...>`
* **Artemis:** Сообщение перенаправляется в `queue.dlq.out`, минуя CRM и Billing.

---
*© Belotserkovskii Vitalii*