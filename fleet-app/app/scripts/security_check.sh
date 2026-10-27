#!/bin/bash
set -e

echo "=== [SECURITY AUDIT] Iniciando Verificação de Segurança ==="

ERRORS=0
MANIFEST_FILE="app/src/main/AndroidManifest.xml"
SRC_DIR="app/src/main/java"

#  Endurecimento do Manifest
if grep -q 'android:allowBackup="true"' "$MANIFEST_FILE"; then
    echo "::error::[SEC-01] android:allowBackup está definido como TRUE no Manifest!"
    ERRORS=$((ERRORS + 1))
fi

if grep -q 'android:usesCleartextTraffic="true"' "$MANIFEST_FILE"; then
    echo "::error::[SEC-02] android:usesCleartextTraffic está definido como TRUE no Manifest!"
    ERRORS=$((ERRORS + 1))
fi

# Procura chamadas de Log ou println que referenciem campos de identificador (.id, deviceId, id)
# Ignora linhas explicitamente marcadas com '// sec-ignore'
AUDIT_LOGS=$(grep -rnE "(Log\.[vdiwe]|println).*\b(device\.id|deviceId|id)\b" "$SRC_DIR" | grep -v "// sec-ignore" || true)

if [ -n "$AUDIT_LOGS" ]; then
    echo "::error::[SEC-03] Possível vazamento de ID de dispositivo detectado em logs:"
    echo "$AUDIT_LOGS"
    ERRORS=$((ERRORS + 1))
fi

if [ $ERRORS -gt 0 ]; then
    echo "=== [SECURITY AUDIT] FALHA: $ERRORS violação(ões) encontrada(s). ==="
    exit 1
else
    echo "=== [SECURITY AUDIT] SUCESSO: Nenhuma violação detectada. ==="
    exit 0
fi