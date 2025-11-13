#!/bin/bash

# Script to start Prometheus
# Usage: ./start-prometheus.sh /path/to/prometheus

PROMETHEUS_DIR="$1"
CONFIG_FILE="$(pwd)/prometheus.yml"

if [ -z "$PROMETHEUS_DIR" ]; then
    echo "Usage: ./start-prometheus.sh /path/to/prometheus"
    echo ""
    echo "Example:"
    echo "  ./start-prometheus.sh ~/Downloads/prometheus-2.45.0.darwin-arm64"
    echo ""
    echo "Or if prometheus is in your PATH:"
    if command -v prometheus &> /dev/null; then
        echo "Starting Prometheus from PATH..."
        prometheus --config.file="$CONFIG_FILE" --storage.tsdb.path="$(pwd)/prometheus-data" &
        echo "Prometheus started! PID: $!"
        echo "Access Prometheus UI at: http://localhost:9090"
        exit 0
    else
        echo "Prometheus not found in PATH. Please provide the path."
        exit 1
    fi
fi

if [ ! -d "$PROMETHEUS_DIR" ]; then
    echo "Error: Directory not found: $PROMETHEUS_DIR"
    exit 1
fi

PROMETHEUS_BIN="$PROMETHEUS_DIR/prometheus"
if [ ! -f "$PROMETHEUS_BIN" ]; then
    echo "Error: prometheus binary not found at: $PROMETHEUS_BIN"
    exit 1
fi

echo "Starting Prometheus from: $PROMETHEUS_DIR"
echo "Config file: $CONFIG_FILE"
echo ""

cd "$PROMETHEUS_DIR"
./prometheus --config.file="$CONFIG_FILE" --storage.tsdb.path="$(pwd)/../prometheus-data" &

PROMETHEUS_PID=$!
echo "Prometheus started! PID: $PROMETHEUS_PID"
echo ""
echo "Access Prometheus UI at: http://localhost:9090"
echo "Check targets at: http://localhost:9090/targets"
echo ""
echo "To stop Prometheus, run: kill $PROMETHEUS_PID"
