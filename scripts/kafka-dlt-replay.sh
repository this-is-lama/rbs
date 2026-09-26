#!/usr/bin/env bash
# Переотправка сообщений из dead-letter topic (DLT) обратно в основной топик Kafka.
#
# Когда запускать: ПОСЛЕ того, как причина ошибок устранена (БД поднялась, баг исправлен и задеплоен).
# Иначе сообщения снова пройдут повторы (1с → 10с → 100с) и вернутся в DLT.
#
# Дубли писем не страшны: notification-service проверяет messageId в своей БД и уже обработанное пропускает.
# Из DLT ничего не удаляется (хранится 30 дней) — скрипт только копирует сообщения в основной топик.
# Скрипт помнит, что уже переотправил (consumer group dlt-replay-<topic>): повторный запуск берёт только новые.
#
# Запуск из корня проекта (Git Bash / WSL / сервер, нужен docker, Kafka должна быть запущена):
#   bash scripts/kafka-dlt-replay.sh booking-created-topic           # показать, что лежит в DLT (ничего не отправляет)
#   bash scripts/kafka-dlt-replay.sh booking-created-topic --send    # переотправить новые сообщения из DLT
#   COMPOSE_FILE=docker-compose.prod.yml bash scripts/kafka-dlt-replay.sh booking-cancelled-topic --send
#
# Переотправить заново то, что уже переотправлялось (сбросить закладку группы):
#   docker compose -f docker-compose.local.yml exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
#     --bootstrap-server kafka:29092 --group dlt-replay-booking-created-topic \
#     --topic booking-created-topic-dlt --reset-offsets --to-earliest --execute

set -euo pipefail

# Git Bash на Windows иначе превратит /opt/kafka/... в C:/Program Files/Git/opt/kafka/...
export MSYS_NO_PATHCONV=1

TOPIC="${1:-}"
MODE="${2:-}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.local.yml}"
BOOTSTRAP="kafka:29092"          # внутренний listener, одинаковый в local / infra / prod
BIN="/opt/kafka/bin"
WAIT_MS=10000                    # если 10 с нет новых сообщений — считаем, что DLT дочитан
SEP=$'\t'                        # разделитель ключа и значения: в JSON и UUID сырой табуляции не бывает

usage() { sed -n '2,19p' "$0" | sed 's/^# \{0,1\}//'; exit 1; }

[[ -z "$TOPIC" ]] && usage
[[ -n "$MODE" && "$MODE" != "--send" ]] && usage
if [[ "$TOPIC" == *-dlt || "$TOPIC" == *-retry-* ]]; then
  echo "Укажи основной топик, например booking-created-topic (без -dlt / -retry-N)"; exit 1
fi

DLT="${TOPIC}-dlt"
GROUP="dlt-replay-${TOPIC}"

kafka() { docker compose -f "$COMPOSE_FILE" exec -T kafka "$@"; }

if [[ -z "$MODE" ]]; then
  echo "== Все сообщения в $DLT (ключ, время, JSON). Ничего не отправляется."
  kafka "$BIN/kafka-console-consumer.sh" --bootstrap-server "$BOOTSTRAP" --topic "$DLT" \
    --from-beginning --timeout-ms "$WAIT_MS" \
    --property print.key=true --property print.timestamp=true 2>/dev/null || true
  echo
  echo "== Сколько ещё НЕ переотправлено (столбец LAG; пусто — скрипт с --send ещё не запускался):"
  kafka "$BIN/kafka-consumer-groups.sh" --bootstrap-server "$BOOTSTRAP" --describe --group "$GROUP" 2>/dev/null || true
  echo
  echo "Переотправить: bash $0 $TOPIC --send"
  exit 0
fi

TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

echo "== Читаю новые сообщения из $DLT (группа $GROUP)..."
# --from-beginning с --group действует только при первом запуске, дальше чтение идёт с закладки группы
kafka "$BIN/kafka-console-consumer.sh" --bootstrap-server "$BOOTSTRAP" --topic "$DLT" \
  --group "$GROUP" --from-beginning --timeout-ms "$WAIT_MS" \
  --property print.key=true --property key.separator="$SEP" > "$TMP" 2>/dev/null || true

COUNT="$(grep -c . "$TMP" || true)"
if [[ "$COUNT" -eq 0 ]]; then
  echo "В $DLT нет новых сообщений — переотправлять нечего."
  exit 0
fi

echo "== Отправляю $COUNT сообщений в $TOPIC..."
kafka "$BIN/kafka-console-producer.sh" --bootstrap-server "$BOOTSTRAP" --topic "$TOPIC" \
  --property parse.key=true --property key.separator="$SEP" < "$TMP"

echo "Готово: $COUNT сообщений из $DLT переотправлено в $TOPIC."
echo "Проверь логи notification-service: сообщения должны обработаться, а не вернуться в DLT."
