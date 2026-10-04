# Расход топлива (Fuel Expense)

Android-приложение для учёта заправок и расчёта расхода топлива.
Язык интерфейса — русский. Написан на Kotlin, использует Material Design,
ViewPager2 с двумя вкладками и SQLite для хранения данных.

## Возможности

**Вкладка «Заправка»** — форма ввода записи:
- дата заправки (выбор из календаря);
- пробег по спидометру (км);
- вид топлива (АИ-92, АИ-95, АИ-98, Дизель, Газ);
- количество заправленного топлива (л);
- цена за литр (₽);
- общая стоимость заправки (₽).

Под формой — история всех заправок с возможностью удаления.

**Вкладка «Расчёт»** — автоматические вычисления по парам заправок
(метод «полного бака»):
- расход на 1 км (л/км);
- расход на 100 км (л/100км);
- стоимость 1 км (₽/км);
- стоимость 100 км (₽/100км);
- сводная статистика: пройдено км, всего заправок, всего топлива,
  общие затраты, средняя цена топлива.

## Как расход считается

Расход считается между двумя соседними заправками: топливо, залитое на
текущей заправке, делится на пройденное с предыдущей заправки расстояние
(разница пробегов). Для корректности лучше заправляться «до полного бака».
Для расчёта требуется не менее двух записей с возрастающим пробегом.

## Сборка

Требуется [Android Studio](https://developer.android.com/studio) (Hedgehog 2023.1.1
или новее) с JDK 17 и Android SDK 34.

1. Откройте папку `FuelExpense` как проект в Android Studio
   (File → Open → выберите `FuelExpense`).
2. Дождитесь синхронизации Gradle (первые зависимости скачаются из сети).
3. Run ▶ — приложение установится на подключённое устройство или эмулятор.

### Технологический стек и версии

| Компонент | Версия |
|---|---|
| Kotlin | 1.9.22 |
| Android Gradle Plugin | 8.2.2 |
| Gradle | 8.2 (wrapper) |
| compileSdk / targetSdk | 34 |
| minSdk | 23 |
| Material Components | 1.11.0 |
| ViewPager2 | 1.0.0 |

## Структура проекта

```
FuelExpense/
├── build.gradle.kts              # корневой build-скрипт
├── settings.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts          # модуль приложения
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/fuelexpense/
        │   ├── MainActivity.kt       # вкладки + ViewPager2
        │   ├── PagerAdapter.kt
        │   ├── InputFragment.kt      # вкладка ввода и история
        │   ├── CalculationFragment.kt# вкладка расчёта
        │   ├── FuelDatabase.kt       # SQLite-хранилище
        │   ├── FuelCalculator.kt     # логика расчёта расхода
        │   └── Refueling.kt          # модель данных
        └── res/
            ├── layout/               # activity_main, fragment_input,
            │                         # fragment_calculation, item_record
            └── values/               # strings, colors, themes
```

APK можно собрать из командной строки:

```bash
./gradlew assembleDebug        # Linux/macOS
gradlew.bat assembleDebug      # Windows
```

Готовый файл появится в `app/build/outputs/apk/debug/app-debug.apk`.

## Сборка в облаке (GitHub Actions)

Если на этой машине нет доступа к `dl.google.com` (откуда скачивается
Android SDK), а Android-платформа недоступна — удобно собирать APK в облаке
GitHub CI, где SDK и сеть полностью открыты.

В проекте уже лежит готовый workflow `.github/workflows/build.yml`.

Как воспользоваться:

1. Создайте пустой репозиторий на GitHub (GitHub → New repository).
2. Залейте в него содержимое папки `FuelExpense`:

   ```bash
   git init
   git add .
   git commit -m "Fuel Expense app"
   git branch -M main
   git remote add origin https://github.com/<ваш-логин>/<название>.git
   git push -u origin main
   ```

3. На GitHub откройте вкладку **Actions** → слева выберите **Build APK** →
   нажмите **Run workflow** → **Run workflow**.

4. Через несколько минут сборка завершится:
   - вкладка **Actions** покажет зелёную галочку;
   - ниже в разделе **Artifacts** появится архив **fuel-expense-apk**,
     внутри которого лежит готовый `app-debug.apk` — скачайте и установите его
     на телефон (разрешив установку из неизвестных источников).

При ручном запуске workflow дополнительно создаётся GitHub **Release** с этим
APK (тег `v1.0-<номер>`), чтобы файл можно было просто скачать из раздела
**Releases**.

> Требуется рабочий сборки `./gradlew`. Сопровождающие файлы wrapper
> (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`) приложены
> к проекту, поэтому отдельно ничего не нужно указывать.