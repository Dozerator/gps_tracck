#!/bin/bash
# Полное развёртывание Operator Tracking backend с TLS на чистом Ubuntu/Debian сервере.
# Запускать из каталога backend/: sudo bash deploy.sh [IP_СЕРВЕРА]

set -e

SERVER_IP="${1:-192.168.1.100}"
APP_DIR="/opt/operator"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=== Установка зависимостей ==="
apt-get update
apt-get install -y nginx python3 python3-venv python3-pip openssl postgresql

echo "=== Создание директорий ==="
mkdir -p "$APP_DIR"/{certs,app,logs}

echo "=== Копирование приложения ==="
cp -r "$SCRIPT_DIR"/*.py "$APP_DIR/app/"
cp -r "$SCRIPT_DIR"/routers "$APP_DIR/app/"
cp -r "$SCRIPT_DIR"/static "$APP_DIR/app/"
cp "$SCRIPT_DIR/requirements.txt" "$APP_DIR/app/"

echo "=== Генерация TLS-сертификата ==="
cp "$SCRIPT_DIR/certs/generate_cert.sh" "$APP_DIR/certs/"
(cd "$APP_DIR/certs" && bash generate_cert.sh "$SERVER_IP")

echo "=== Настройка Nginx ==="
# В конфиге пути до сертификата уже указывают на /opt/operator/certs — см. nginx/operator.conf
cp "$SCRIPT_DIR/nginx/operator.conf" /etc/nginx/sites-available/operator
ln -sf /etc/nginx/sites-available/operator /etc/nginx/sites-enabled/operator
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl restart nginx
systemctl enable nginx

echo "=== Установка Python-зависимостей ==="
python3 -m venv "$APP_DIR/app/venv"
"$APP_DIR/app/venv/bin/pip" install --upgrade pip
"$APP_DIR/app/venv/bin/pip" install -r "$APP_DIR/app/requirements.txt"

echo "=== Инициализация БД (если ещё не создана) ==="
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = 'operator_db'" | grep -q 1 \
  || sudo -u postgres psql -f "$SCRIPT_DIR/init_db.sql"

echo "=== Настройка systemd-сервиса ==="
cp "$SCRIPT_DIR/operator.service" /etc/systemd/system/operator.service
chown -R www-data:www-data "$APP_DIR"
systemctl daemon-reload
systemctl enable --now operator
systemctl restart operator

echo ""
echo "✓ Сервер готов!"
echo "  HTTPS: https://$SERVER_IP"
echo "  WSS:   wss://$SERVER_IP/ws/operator"
echo "  FastAPI управляется systemd: systemctl status operator"
echo ""
echo "  Fingerprint/PIN для Android — см. вывод generate_cert.sh выше,"
echo "  либо получите заново командой:"
echo "    openssl x509 -in $APP_DIR/certs/server.crt -pubkey -noout \\"
echo "      | openssl pkey -pubin -outform DER \\"
echo "      | openssl dgst -sha256 -binary | base64"
