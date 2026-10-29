# Автосервіс — лабораторні №1-4

Швидкий запуск:
1. `docker compose up -d`   (Postgres + Keycloak; потрібно лише для лаби №1)
2. Відкрити проєкт (pom.xml) в IntelliJ IDEA і запустити `AutoserviceApplication`
3. UI:  http://localhost:8080  (admin/admin — роль ADMIN, user/user — роль USER)
4. API: імпортувати `postman/Autoservice.postman_collection.json` у Postman

Keycloak: http://localhost:8180 (admin/admin), realm `autoservice` імпортується автоматично.

## Лабораторна №2 — Unit-тести
Запуск усіх тестів (Docker і Keycloak для цього НЕ потрібні): `mvn test`
- `config/KeycloakRolesTest` — тест без моків.
- `service/RepairOrderServiceTest` — тест з моками (Mockito).

## Лабораторна №3 — Кастомні анотації
- `validation/ValidPlateNumber` + `PlateNumberValidator` — Bean Validation constraint.
- `web/PostMappingCreated` — композитна анотація (@PostMapping + @ResponseStatus(CREATED)).
- `web/CurrentMechanic` + `CurrentMechanicArgumentResolver` + `WebConfig` — MVC argument resolver.

## Лабораторна №4 — AOP
- `aop/TrackExecutionTime` + `ExecutionTimeAspect` (Around) — вимірює час виконання методу.
- `aop/AuditCreation` + `AuditCreationAspect` (AfterReturning) — журнал аудиту створення замовлень.
- `aop/PreventEditingCompletedOrder` + `LockCompletedOrderAspect` (Before) — блокує редагування завершених замовлень.
- `aop/SelfInvocationDemoTest` — демонстрація self-invocation: зовнішній виклик активує аспект, виклик через `this.` — ні.

## Лабораторна №5 — Асинхронна обробка
- `async/AsyncConfig` — явний `ThreadPoolTaskExecutor` (core=2, max=4, queue=10, префікс `autoservice-async-`) + `AsyncUncaughtExceptionHandler`.
- `async/OrderNotificationService` — три повільні операції (@Async, ~800/800/500 мс): лист клієнту, PDF-рахунок, виклик постачальника (fire-and-forget).
- `async/OrderNotificationOrchestrator` — запускає два незалежні виклики, об'єднує через `thenCombine`, обробляє збої через `exceptionally`.
- `controller/OrderNotificationController` — `POST /api/orders/{id}/notify`: обмежений тайм-аут очікування (`future.get(3, TimeUnit.SECONDS)`), явна обробка `TaskRejectedException` (503, а не 500).
- `async/ExecutorOverflowTest` — демонстрація переповнення виконавця (`TaskRejectedException`).
