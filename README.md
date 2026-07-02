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

uvicorn main:app --host 0.0.0.0 --port 8002 --reload
```

Порт 8002 выбран, так как 8000 на этой машине занят другим процессом; при
необходимости используйте любой свободный порт — просто держите его
согласованным с `BASE_URL` в Android-проекте (см. ниже).

После запуска:

- API доступно на `http://localhost:8002`
- Веб-панель оператора: `http://localhost:8002/admin`
- Документация Swagger: `http://localhost:8002/docs`
- WebSocket оператора: `ws://localhost:8002/ws/operator`

### 1.4. Проверка

```bash
curl -X POST http://localhost:8002/api/auth/login \
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
   - `http://10.0.2.2:8002/` — по умолчанию, работает для **эмулятора Android**
     и обращается к backend на хост-машине (localhost:8002).
   - Для **физического устройства** замените на `http://<IP-адрес-компьютера>:8002/`,
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
├── .gitignore
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
│   ├── certs/
│   │   └── generate_cert.sh        (server.key/server.crt — .gitignore, не коммитятся)
│   ├── nginx/
│   │   └── operator.conf
│   ├── migrations/
│   │   ├── 001_add_object_type_direction.sql
│   │   └── 002_add_threat_level.sql
│   ├── operator.service            (systemd unit)
│   ├── deploy.sh
│   ├── requirements.txt
│   ├── init_db.sql
│   └── .env.example
└── android/
    ├── settings.gradle.kts
    ├── build.gradle.kts
    ├── gradle.properties           (SERVER_IP/SERVER_URL/WS_URL/CERT_PIN)
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/example/operator/
            │   ├── OperatorApp.kt
            │   ├── auth/AuthManager.kt
            │   ├── model/Models.kt, Selection.kt
            │   ├── network/ApiService.kt, RetrofitClient.kt, WebSocketManager.kt
            │   ├── security/TlsManager.kt
            │   ├── data/ (local/, repository/) — офлайн-очередь Room
            │   ├── workers/SyncWorker.kt
            │   ├── service/LocationService.kt
            │   └── ui/ (LoginActivity.kt, MainActivity.kt, QueueActivity.kt, ...)
            └── res/
                ├── layout/ (activity_login.xml, activity_main.xml, dialog_*.xml, ...)
                ├── values/ (strings.xml, colors.xml, themes.xml)
                ├── xml/network_security_config.xml
                ├── raw/server_cert.crt
                └── drawable/, mipmap-anydpi-v26/
```

---

## 5. Безопасность и продакшн-заметки

- Пароли хранятся как bcrypt-хеш (модуль `bcrypt`, без `passlib` — см. `backend/auth.py`).
- JWT подписывается `SECRET_KEY` из `.env`, срок действия — 24 часа
  (`ACCESS_TOKEN_EXPIRE_MINUTES`).
- `CORS` в `main.py` ограничен списком `ALLOWED_ORIGINS` из `.env`.
- Сетевой доступ на Android описан в `res/xml/network_security_config.xml`:
  локальный эмулятор (`10.0.2.2`) ходит по обычному HTTP, а прод-домен —
  только по TLS с доверием исключительно самоподписанному сертификату (см.
  раздел 6 ниже). Бланкетный `android:usesCleartextTraffic` из манифеста убран.
- Токен на клиенте нигде не логируется; `HttpLoggingInterceptor` включён
  только в debug-сборке (`BuildConfig.DEBUG`).

---

## 6. HTTPS/WSS для продакшна

Архитектура: `Android/браузер → HTTPS+WSS (TLS 1.3, Nginx, :443) → HTTP (localhost) → FastAPI (:8000, 127.0.0.1 only) → PostgreSQL (localhost only)`.
FastAPI никогда не выставлен наружу напрямую — TLS терминируется на Nginx,
а бэкенд слушает только `127.0.0.1`.

### 6.1. Генерация сертификата

Самоподписанный сертификат подходит для закрытой сети (без платного CA):
Android явно доверяет именно ему, а не системным корневым сертификатам.

```bash
cd backend/certs
./generate_cert.sh <IP_ИЛИ_ДОМЕН_СЕРВЕРА>   # например 192.168.1.100
```

Скрипт создаёт `server.crt` / `server.key` (10 лет) с SAN на IP — Android
(начиная с API 24) проверяет только SAN, Common Name недостаточно — и печатает:

- SHA-256 fingerprint сертификата (для сверки при первом подключении);
- SHA-256 pin в base64 для Certificate Pinning — понадобится в шаге 6.3.

### 6.2. Развёртывание Nginx (Ubuntu/Debian)

```bash
sudo bash backend/deploy.sh <IP_СЕРВЕРА>
```

Скрипт ставит nginx/PostgreSQL, копирует приложение в `/opt/operator`,
генерирует сертификат, разворачивает `backend/nginx/operator.conf`
(HTTP:80 → редирект на HTTPS, HTTPS:443 + WSS с TLS 1.2/1.3), инициализирует
БД и поднимает `backend/operator.service` (systemd) — FastAPI слушает только
`127.0.0.1:8000` (см. `if __name__ == "__main__"` в `main.py`).

Обновите `ALLOWED_ORIGINS` в `.env` на боевом сервере на
`https://<IP_СЕРВЕРА>` (пример — в `.env.example`).

### 6.3. Настройка Android-клиента на TLS

1. Скопируйте `backend/certs/android_trust.crt` в
   `android/app/src/main/res/raw/server_cert.crt`.
2. В `android/app/src/main/res/xml/network_security_config.xml` замените
   placeholder-IP `192.168.1.100` на реальный IP/домен сервера — значения
   `domain` в network-security-config должны быть литералами, их нельзя
   прочитать из `gradle.properties`.
3. В `android/gradle.properties` заполните:
   ```properties
   SERVER_IP=<IP_СЕРВЕРА>
   SERVER_URL=https://<IP_СЕРВЕРА>
   WS_URL=wss://<IP_СЕРВЕРА>/ws/operator
   CERT_PIN=<вывод generate_cert.sh, "SHA-256 pin для Certificate Pinning">
   ```
4. Пересоберите приложение. `TlsManager` (см.
   `android/.../security/TlsManager.kt`) сам включает TLS + доверие только
   нашему сертификату + Certificate Pinning, как только `SERVER_URL`
   начинается с `https` — для локальной разработки против `10.0.2.2` (обычный
   HTTP) ничего дополнительно делать не нужно, TLS-код просто не активируется.

### 6.4. Смена сертификата

Сертификат генерируется на 10 лет, но его придётся переиздать раньше, если
сменился IP/домен сервера, ключ скомпрометирован, либо вы просто делаете
плановую ротацию. Порядок действий:

1. На сервере: `cd /opt/operator/certs && sudo bash generate_cert.sh <новый_IP>`
   — перезапишет `server.crt`/`server.key` и покажет новый fingerprint и pin.
2. `sudo systemctl reload nginx` (сертификат читается при старте воркеров nginx).
3. В Android-проекте:
   - обновите `android/app/src/main/res/raw/server_cert.crt` новым `android_trust.crt`;
   - обновите `CERT_PIN` в `gradle.properties` новым значением pin;
   - если менялся IP/домен — обновите `SERVER_IP`/`SERVER_URL`/`WS_URL` в
     `gradle.properties` **и** домен в `network_security_config.xml` (литерал,
     см. 6.3).
   - пересоберите и переустановите APK на всех устройствах операторов —
     старые сборки с прежним pin/сертификатом откажутся подключаться
     (это ожидаемое поведение Certificate Pinning, а не баг).
4. Проверка с сервера: `curl -vk https://<IP_СЕРВЕРА>/health` — в выводе
   `openssl`/curl должен быть виден новый fingerprint.

> Если после смены сертификата операторы получают ошибку SSL-хендшейка —
> почти наверняка забыт один из трёх шагов выше (сертификат в `res/raw`,
> `CERT_PIN`, домен в `network_security_config.xml`), а не проблема сети.
