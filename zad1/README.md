**System zarządzania pracownikami: CSV, API + Analizy**

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