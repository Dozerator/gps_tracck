#!/bin/bash
# Генерирует самоподписанный TLS-сертификат для закрытой сети (без платных CA).
# Использование:
#   ./generate_cert.sh [IP_ИЛИ_ДОМЕН_СЕРВЕРА]
# Если аргумент не передан, используется значение SERVER_IP ниже.

set -e

SERVER_IP="${1:-192.168.1.100}"   # IP сервера в локальной сети — замени на свой
CERT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DAYS=3650                          # 10 лет

mkdir -p "$CERT_DIR"

# Конфиг с SAN (Subject Alternative Names) — обязателен для Android:
# начиная с API 24 Android игнорирует Common Name и проверяет только SAN.
cat > "$CERT_DIR/cert.conf" << EOF
[req]
default_bits       = 4096
prompt             = no
default_md         = sha256
distinguished_name = dn
x509_extensions    = v3_req

[dn]
C  = RU
ST = Russia
L  = Moscow
O  = OperatorApp
OU = Security
CN = $SERVER_IP

[v3_req]
subjectAltName = @alt_names
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth

[alt_names]
IP.1 = $SERVER_IP
IP.2 = 127.0.0.1
DNS.1 = localhost
EOF

# Генерация ключа и сертификата
openssl req -x509 \
    -newkey rsa:4096 \
    -keyout "$CERT_DIR/server.key" \
    -out    "$CERT_DIR/server.crt" \
    -days   "$DAYS" \
    -nodes \
    -config "$CERT_DIR/cert.conf"

# Экспорт для Android (формат .crt/PEM уже подходит для network_security_config)
cp "$CERT_DIR/server.crt" "$CERT_DIR/android_trust.crt"

echo ""
echo "✓ Сертификат создан: $CERT_DIR/server.crt"
echo "✓ Ключ (храни в секрете): $CERT_DIR/server.key"
echo "✓ Для Android скопируй в app/src/main/res/raw/server_cert.crt:"
echo "  $CERT_DIR/android_trust.crt"
echo ""
echo "  SHA-256 fingerprint сертификата:"
openssl x509 -in "$CERT_DIR/server.crt" -fingerprint -sha256 -noout
echo ""
echo "  SHA-256 pin для Certificate Pinning (CERT_PIN в gradle.properties):"
openssl x509 -in "$CERT_DIR/server.crt" -pubkey -noout \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -binary \
  | base64
