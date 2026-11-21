#!/usr/bin/env bash
# Race checks from MED-26. Run on a clean database with the app on localhost:8080:
#   docker compose down -v && docker compose up -d --wait, start the app, then: bash wyscig.sh
# Expected:
#   booking race: 20 x 200 and 20 x 409 (Visit.version, optimistic locking)
#   slot race:     1 x 201 and 14 x 409 (unique doctor_id + starts_at, changeset 22)
set -u

BASE_URL=${BASE_URL:-http://localhost:8080}
ADMIN_EMAIL=${ADMIN_EMAIL:-admin}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-admin}
PATIENT_EMAIL=${PATIENT_EMAIL:-anna.nowak@example.com}
PATIENT_PASSWORD=${PATIENT_PASSWORD:-Silne123!}

token() {
    curl -s -X POST "$BASE_URL/auth/token" -H 'Content-Type: application/json' \
        -d "{\"email\":\"$1\",\"password\":\"$2\"}" | sed -nE 's/.*"token":"([^"]+)".*/\1/p'
}

ADMIN_TOKEN=$(token "$ADMIN_EMAIL" "$ADMIN_PASSWORD")
PATIENT_TOKEN=$(token "$PATIENT_EMAIL" "$PATIENT_PASSWORD")
if [ -z "$ADMIN_TOKEN" ] || [ -z "$PATIENT_TOKEN" ]; then
    echo "Login failed: is the app running on $BASE_URL?" >&2
    exit 1
fi

echo "Booking race: patients 1 and 2 book each of 20 slots at the same time"
ids=()
for i in $(seq 1 20); do
    day=$(printf '2030-06-%02d' "$i")
    id=$(curl -s -X POST "$BASE_URL/visits" \
        -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
        -d "{\"doctorId\":1,\"startsAt\":\"${day}T10:00:00\",\"endsAt\":\"${day}T10:30:00\"}" \
        | sed -nE 's/.*"id":([0-9]+).*/\1/p')
    if [ -z "$id" ]; then
        echo "Could not create the slot on $day: run the script on a clean database" >&2
        exit 1
    fi
    ids+=("$id")
done
for id in "${ids[@]}"; do
    curl -s -o /dev/null -w '%{http_code}\n' -X PATCH "$BASE_URL/visits/$id/book/1" \
        -H "Authorization: Bearer $PATIENT_TOKEN" &
    curl -s -o /dev/null -w '%{http_code}\n' -X PATCH "$BASE_URL/visits/$id/book/2" \
        -H "Authorization: Bearer $PATIENT_TOKEN" &
    wait
done | sort | uniq -c

echo "Slot race: 15 requests create the same slot of doctor 1 at the same time"
{
    for i in $(seq 1 15); do
        curl -s -o /dev/null -w '%{http_code}\n' -X POST "$BASE_URL/visits" \
            -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
            -d '{"doctorId":1,"startsAt":"2030-07-01T10:00:00","endsAt":"2030-07-01T10:30:00"}' &
    done
    wait
} | sort | uniq -c
