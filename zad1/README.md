# **System zarządzania pracownikami: CSV, API + Analizy + UI (Thymeleaf)**

### Główne adresy
- `/` – strona główna
- `/employees` – lista pracowników
- `/employees/add` – formularz dodawania pracownika
- `/employees/edit/{email}` – edycja pracownika
- `/employees/search` – wyszukiwarka po firmie
- `/employees/import` – import CSV/XML
- `/departments` – lista departamentów
- `/departments/add` – dodawanie departamentu
- `/departments/edit/{id}` – edycja departamentu
- `/departments/details/{id}` – szczegóły departamentu
- `/departments/documents/{id}` – dokumenty departamentu (upload / pobieranie)
- `/statistics` – dashboard statystyk
- `/statistics/company/{name}` – statystyki wybranej firmy

### Funkcje UI
- Wspólny layout (`layout.html`) z nawigacją i komunikatami flash (sukces / błąd).
- Walidacja formularzy, wyświetlanie błędów pod polami (Thymeleaf `th:errors`).
- Upload plików z formularza HTML (enctype multipart). Obsługa dokumentów pracowników (REST) i departamentów (MVC).
- Dynamiczne selecty dla enumów (Position, EmploymentStatus, DocumentType) oraz lista dostępnych departamentów przy dodawaniu/edycji pracownika.
- Statystyki: ogólne, per firma, rozkład stanowisk.

### Uruchomienie UI
```bash
./gradlew bootRun
```

## Lista dostępnych endpointów (skrót)
- Employees
    - GET `/api/employees`
    - GET `/api/employees?company={company}`
    - GET `/api/employees/{email}`
    - POST `/api/employees`
    - PUT `/api/employees/{email}`
    - PATCH `/api/employees/{email}/status`
    - DELETE `/api/employees/{email}`
    - GET `/api/employees/status/{status}`
- Statistics
    - GET `/api/statistics/salary/average`
    - GET `/api/statistics/salary/average?company={company}`
    - GET `/api/statistics/company/{companyName}`
    - GET `/api/statistics/positions`
    - GET `/api/statistics/status`
- Files
    - POST `/api/files/import/csv` 
    - POST `/api/files/import/xml` 
    - GET `/api/files/export/csv` 
    - GET `/api/files/reports/statistics/{companyName}` 
    - POST `/api/files/documents/{email}?type={DocumentType}` 
    - GET `/api/files/documents/{email}` 
    - GET `/api/files/documents/{email}/{documentId}`
    - DELETE `/api/files/documents/{email}/{documentId}` 
    - POST `/api/files/photos/{email}` 
    - GET `/api/files/photos/{email}` 

## Opis projektu
- Wymagania: 
    - JDK 17+
    - Dostęp do internetu (dla API)
    - Gradle Wrapper (`./gradlew`)

- Budowa i testy:
    - `./gradlew build` - buduje projekt
    -  `./gradlew clean` - czyści folder `/build` projekt
    - `./gradlew test` - uruchamia testy jednostkowe
    - `./gradlew check` - uruchamia wszystkie kontrole jakości kodu
    - `./gradlew jacocoTestReport` - generuje raport pokrycia testami
- Uruchomienie:
-   - `./gradlew Bootrun` - uruchamia aplikację

- Struktura projektu:
    - `src/main/java/com/example/zad1/model/...` - modele
    - `src/main/java/com/example/zad1/service/...` - serwisy
    - `src/main/java/com/example/zad1/exception/...` - wyjątki
    - `src/main/java/com/example/zad1/Zad1Application` - aplikacja
    - `src/test/java/com/example/zad1/...` - testy
    - `data/employees.csv` - przykładowy plik CSV

- Uwagi:
    - WSL potrzebuje uprawnień dla `gradlew`
    - Zadnie `run` nie jest dostępne
- Aby przejść do Spring Boot musiałem nie musiałem robić dużo z uwagi na to, że pierwotnie projekt był ustawiony aby używał Spring Boota. Wystarczyło więc:
    - Dodać plik AppConfig.java, który konfiguruje komponenty Springa
    - Stworzyć nową klasę główną EmployeeManagementApplication.java z adnotacją @SpringBootApplication
    - Zaktualizować pliki w /service aby używały adnotacji @Service i wstrzykiwania zależności przez konstruktor
    - Dodać employees-beans.xml do src/main/resources