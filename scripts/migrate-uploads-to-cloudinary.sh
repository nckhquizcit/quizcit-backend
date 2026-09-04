#!/usr/bin/env bash
set -euo pipefail

# Script to trigger migration of local media in uploads/ to Cloudinary
# Usage:
#   ./scripts/migrate-uploads-to-cloudinary.sh [API_BASE_URL] [ADMIN_TOKEN]
# Example:
#   ./scripts/migrate-uploads-to-cloudinary.sh http://localhost:8080/api "eyJhbGciOi..."
# Or interactive login:
#   ./scripts/migrate-uploads-to-cloudinary.sh http://localhost:8080/api

API_BASE_URL="${1:-http://localhost:8080/api}"
API_BASE_URL="${API_BASE_URL%/}" # Remove trailing slash if any
ADMIN_TOKEN="${2:-}"

if [[ -z "$ADMIN_TOKEN" ]]; then
  echo "=================================================="
  echo "  QuizCIT Media Migration to Cloudinary"
  echo "=================================================="
  echo "Target API: $API_BASE_URL"
  echo ""
  read -r -p "Admin Email: " ADMIN_EMAIL
  read -r -s -p "Admin Password: " ADMIN_PASSWORD
  echo ""

  echo "Authenticating admin user..."
  LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$ADMIN_EMAIL\", \"password\": \"$ADMIN_PASSWORD\"}")

  ADMIN_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4 || true)

  if [[ -z "$ADMIN_TOKEN" ]]; then
    ADMIN_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || true)
  fi

  if [[ -z "$ADMIN_TOKEN" ]]; then
    echo "Đăng nhập thất bại. Phản hồi từ server:"
    echo "$LOGIN_RESPONSE"
    exit 1
  fi
  echo "Đăng nhập thành công! Bắt đầu kích hoạt tiến trình migrate..."
fi

echo "Calling migration endpoint: $API_BASE_URL/admin/uploads/migrate-to-cloudinary..."
MIGRATION_RESULT=$(curl -s -X POST "$API_BASE_URL/admin/uploads/migrate-to-cloudinary" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json")

echo ""
echo "=== KẾT QUẢ MIGRATE ==="
if command -v jq >/dev/null 2>&1; then
  echo "$MIGRATION_RESULT" | jq .
else
  echo "$MIGRATION_RESULT"
fi
