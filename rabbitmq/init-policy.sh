#!/bin/sh
set -eu

rabbitmq-server &
rabbit_pid=$!
trap 'kill -TERM "$rabbit_pid" 2>/dev/null || true; wait "$rabbit_pid" 2>/dev/null || true' TERM INT

until rabbitmq-diagnostics -q check_running >/dev/null 2>&1; do
  sleep 1
done

rabbitmqctl import_definitions /etc/rabbitmq/definitions.json
wait "$rabbit_pid"
