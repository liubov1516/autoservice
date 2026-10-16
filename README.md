# Звіт з лабораторних робіт

## Лабораторна робота 1

| Завдання | Де виконано |
|---|---|
| Базовий CRUD-додаток | `controller/RepairOrderApiController.java`, `service/RepairOrderService.java`, `repository/RepairOrderRepository.java`, `mapper/RepairOrderMapper.java` |
| Схема БД керується міграціями | `src/main/resources/db/changelog/` |
| Базовий UI | `controller/OrderPageController.java`, `src/main/resources/templates/` |
| OIDC для UI-користувачів | `config/SecurityConfig.java` (uiSecurity) |
| JWT для прямих API-запитів | `config/SecurityConfig.java` (apiSecurity) |
| Обмеження на рівні SecurityFilterChain | `config/SecurityConfig.java` |
| Обмеження через `@PreAuthorize` | `service/RepairOrderService.java` (метод `delete`) |
| Готова колекція запитів | `postman/Autoservice.postman_collection.json` |

## Лабораторна робота 2

| Завдання | Де виконано |
|---|---|
| Тест зі звичайним входом (AC1) | `config/KeycloakRolesTest.java`, `service/RepairOrderServiceTest.java` |
| Тест на невалідний/граничний вхід (AC2) | `service/RepairOrderServiceTest.java` (`createNullDtoThrowsIllegalArgumentException`, `createNegativePriceThrowsIllegalArgumentException` та інші) |
| Залежності замоковано, без Spring-контексту (AC3) | `service/RepairOrderServiceTest.java` (`@Mock`/`@InjectMocks`) |
| Доказ мутаційного тестування (AC4) | `service/RepairOrderService.java` (метод `delete`) — перевіряється вручну перед захистом |
| Тест без БД/мережі/часу (AC5) | обидва тестові класи |
| Назва методу й структура Arrange-Act-Assert (AC6) | усі тестові класи |
| Перевірка через `verify()`/`ArgumentCaptor` (AC7) | `service/RepairOrderServiceTest.java` |
| AC1+AC2 для кожного публічного методу (AC8) | `service/RepairOrderServiceTest.java` |

## Лабораторна робота 3

| Завдання | Де виконано |
|---|---|
| Тип 1: Bean Validation Constraint (`@ValidPlateNumber`) | `validation/ValidPlateNumber.java`, `validation/PlateNumberValidator.java`, застосовано в `dto/RepairOrderDto.java` |
| Тип 2: Композитна анотація (`@PostMappingCreated`) | `web/PostMappingCreated.java`, застосовано в `controller/RepairOrderApiController.java` |
| Тип 3: MVC argument resolver (`@CurrentMechanic`) | `web/CurrentMechanic.java`, `web/CurrentMechanicArgumentResolver.java`, `web/WebConfig.java`, застосовано в `GET /api/orders/whoami` |
| AC1–AC3 (валідатор) | `validation/RepairOrderDtoValidationTest.java` |
| AC4–AC5 (композитна анотація) | `web/RepairOrderApiControllerWebMvcTest.java` |
| AC6–AC8 (argument resolver) | `web/CurrentMechanicArgumentResolverTest.java`, `web/RepairOrderApiControllerWebMvcTest.java` |
