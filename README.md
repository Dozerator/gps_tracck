# Operator — трекинг местоположения (Android + FastAPI + веб-панель)

Система из трёх частей:

- **backend/** — FastAPI + PostgreSQL + WebSocket API
- **backend/static/index.html** — веб-панель оператора (Leaflet + OpenStreetMap)
- **android/** — Android-приложение (Kotlin, OSMDroid, без Google Maps)

---

## 1. Backend (FastAPI + PostgreSQL)

### 1.1. Требования

- Python 3.11+
- PostgreSQL 14+

### 1.2. Установка PostgreSQL и инициализация БД

```bash
# Войти под суперпользователем postgres и выполнить скрипт инициализации
psql -U postgres -f backend/init_db.sql
```

Скрипт создаёт базу `operator_db`, таблицы `users` / `location_points` и тестового
пользователя:

```
login:    admin
password: admin123
```

Затем создайте отдельного пользователя БД для приложения (пример):

```sql
CREATE USER operator_user WITH PASSWORD 'operator_pass';
GRANT ALL PRIVILEGES ON DATABASE operator_db TO operator_user;
\c operator_db
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO operator_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO operator_user;
```

### 1.3. Установка зависимостей и запуск

```bash
cd backend
python -m venv venv
# Windows:
venv\Scripts\activate
# Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt

copy .env.example .env      # Windows
# cp .env.example .env      # Linux/macOS
# отредактируйте .env: DATABASE_URL, SECRET_KEY

uvicorn main:app --host 0.0.0.0 --port 8001 --reload
```

Порт 8001 выбран, так как 8000 на этой машине занят другим процессом; при
необходимости используйте любой свободный порт — просто держите его
согласованным с `BASE_URL` в Android-проекте (см. ниже).

После запуска:

- API доступно на `http://localhost:8001`
- Веб-панель оператора: `http://localhost:8001/admin`
- Документация Swagger: `http://localhost:8001/docs`
- WebSocket оператора: `ws://localhost:8001/ws/operator`

### 1.4. Проверка

```bash
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"login\": \"admin\", \"password\": \"admin123\"}"
```

В ответ придёт `access_token` (JWT, срок жизни 24 часа).

---

## 2. Веб-панель оператора

Отдельной установки не требует — раздаётся backend'ом по адресу `/admin`
(файл `backend/static/index.html`). Достаточно открыть страницу в браузере —
подключение к WebSocket устанавливается автоматически.

Возможности:

- Полноэкранная карта Leaflet/OSM
- Автоматическое появление маркера и центрирование карты при получении новой точки
- Всплывающее уведомление о новой точке
- Сворачиваемая боковая панель со списком точек за сессию
- Кнопка "Очистить карту"

---

## 3. Android-приложение

### 3.1. Требования

- Android Studio (Koala/2024.1 или новее)
- JDK 17
- Android SDK: compileSdk/targetSdk 34, minSdk 26 (Android 8.0)

### 3.2. Открытие проекта

1. Откройте папку `android/` в Android Studio через **File → Open**.
2. Дождитесь синхронизации Gradle (при первом открытии Android Studio сама
   создаст недостающий `gradle-wrapper.jar`, если он отсутствует).
3. Проверьте `BASE_URL` в `android/app/build.gradle.kts`:
   - `http://10.0.2.2:8001/` — по умолчанию, работает для **эмулятора Android**
     и обращается к backend на хост-машине (localhost:8001).
   - Для **физического устройства** замените на `http://<IP-адрес-компьютера>:8001/`,
     находясь в одной Wi-Fi сети с сервером.
4. Запустите приложение на эмуляторе/устройстве (Run ▶).

### 3.3. Тестовый вход

```
Логин:  admin
Пароль: admin123
```

### 3.4. Разрешения

При первом запуске приложение запросит:

- геолокацию (точную и/или приблизительную);
- фоновую геолокацию (Android 10+, отдельный системный диалог "Разрешить всегда");
- уведомления (Android 13+, требуется для foreground-сервиса).

Без разрешения на геолокацию карта отобразится, но определение позиции и
отправка точек будут недоступны.

### 3.5. Как это работает

- `LoginActivity` — экран входа, сохраняет JWT в `SharedPreferences` и делает
  автологин при повторном запуске, если токен не истёк.
- `MainActivity` — карта OSMDroid на весь экран, синяя точка — позиция
  пользователя, обновляемая в реальном времени.
- `LocationService` — Foreground Service с `FusedLocationProviderClient`,
  публикует координаты через `StateFlow`, показывает постоянное уведомление.
- Кнопка "Отметить точку" — ставит маркер на карте, показывает диалог
  подтверждения с координатами, при подтверждении отправляет
  `POST /api/location/point` и перекрашивает маркер в зелёный.

---

## 4. Структура репозитория

```
Operator/
├── README.md
├── backend/
│   ├── main.py
│   ├── config.py
│   ├── database.py
│   ├── models.py
│   ├── schemas.py
│   ├── auth.py
│   ├── websocket_manager.py
│   ├── routers/
│   │   ├── auth.py
│   │   └── location.py
│   ├── static/
│   │   └── index.html
│   ├── requirements.txt
│   ├── init_db.sql
│   └── .env.example
└── android/
    ├── settings.gradle.kts
    ├── build.gradle.kts
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/example/operator/
            │   ├── OperatorApp.kt
            │   ├── auth/AuthManager.kt
            │   ├── model/Models.kt
            │   ├── network/ApiService.kt
            │   ├── network/RetrofitClient.kt
            │   ├── service/LocationService.kt
            │   └── ui/LoginActivity.kt, MainActivity.kt
            └── res/
                ├── layout/ (activity_login.xml, activity_main.xml, dialog_confirm.xml)
                ├── values/ (strings.xml, colors.xml, themes.xml)
                └── drawable/, mipmap-anydpi-v26/
```

---

## 5. Безопасность и продакшн-заметки

- Пароли хранятся как bcrypt-хеш (`passlib`).
- JWT подписывается `SECRET_KEY` из `.env`, срок действия — 24 часа
  (`ACCESS_TOKEN_EXPIRE_MINUTES`).
- `CORS` в `main.py` ограничен списком `ALLOWED_ORIGINS` из `.env`.
- `android:usesCleartextTraffic="true"` в манифесте включён для локальной
  разработки по HTTP. Для продакшна разверните backend по HTTPS и уберите
  этот флаг.
- Токен на клиенте нигде не логируется; `HttpLoggingInterceptor` включён
  только в debug-сборке (`BuildConfig.DEBUG`).
