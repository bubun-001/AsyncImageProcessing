#!/bin/bash

# Script to configure Grafana for Image Processing Metrics
# This script adds Prometheus as a data source and imports the dashboard

GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASS="admin"
PROMETHEUS_URL="http://localhost:9090"

echo "Setting up Grafana for Image Processing Metrics..."
echo ""

# Wait for Grafana to be ready
echo "Waiting for Grafana to be ready..."
for i in {1..30}; do
    if curl -s -f "$GRAFANA_URL/api/health" > /dev/null 2>&1; then
        echo "✓ Grafana is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ Grafana failed to start. Please check if Grafana is running."
        exit 1
    fi
    sleep 1
done

# Get API key (login and create API key)
echo ""
echo "Step 1: Creating API key..."

# Login to get session
SESSION=$(curl -s -X POST "$GRAFANA_URL/api/login" \
    -H "Content-Type: application/json" \
    -d "{\"user\":\"$GRAFANA_USER\",\"password\":\"$GRAFANA_PASS\"}" \
    | grep -o '"message":"[^"]*"' | cut -d'"' -f4)

if [ -z "$SESSION" ]; then
    echo "Note: You may need to set the default password on first login."
    echo "Please visit http://localhost:3000 and login with admin/admin"
    echo "Then change the password when prompted."
    echo ""
    echo "After that, you can manually:"
    echo "1. Go to Configuration → Data Sources → Add Prometheus"
    echo "2. Set URL: http://localhost:9090"
    echo "3. Click Save & Test"
    echo "4. Go to Dashboards → Import"
    echo "5. Upload grafana-dashboard.json"
    exit 0
fi

echo "✓ Logged in successfully"
echo ""
echo "Step 2: Adding Prometheus data source..."

# Add Prometheus data source
RESPONSE=$(curl -s -X POST "$GRAFANA_URL/api/datasources" \
    -H "Content-Type: application/json" \
    -H "Cookie: grafana_session=$SESSION" \
    -d "{
        \"name\": \"Prometheus\",
        \"type\": \"prometheus\",
        \"url\": \"$PROMETHEUS_URL\",
        \"access\": \"proxy\",
        \"isDefault\": true
    }")

if echo "$RESPONSE" | grep -q "datasource"; then
    echo "✓ Prometheus data source added successfully!"
else
    echo "Note: Data source might already exist or you need to configure manually."
    echo "Please visit: http://localhost:3000/connections/datasources/new"
    echo "Select Prometheus and set URL to: http://localhost:9090"
fi

echo ""
echo "Step 3: Dashboard setup"
echo "To import the dashboard:"
echo "1. Go to http://localhost:3000/dashboards"
echo "2. Click 'New' → 'Import'"
echo "3. Upload the file: grafana-dashboard.json"
echo "   (located in: $(pwd)/grafana-dashboard.json)"
echo ""
echo "Or use the dashboard ID if available."
echo ""
echo "✓ Setup instructions complete!"
echo ""
echo "Access Grafana at: http://localhost:3000"
echo "Default login: admin / admin"

