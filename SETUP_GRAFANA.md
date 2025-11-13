# Setting Up Prometheus and Grafana for Image Processing Metrics

## Quick Setup (Using Docker - Recommended)

### 1. Start Prometheus

```bash
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

### 2. Start Grafana

```bash
docker run -d \
  --name grafana \
  -p 3000:3000 \
  grafana/grafana
```

### 3. Access Services

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (default login: admin/admin)

## Manual Setup (Without Docker)

### Install Prometheus

1. Download from https://prometheus.io/download/
2. Extract and run:
   ```bash
   ./prometheus --config.file=prometheus.yml
   ```

### Install Grafana

1. Download from https://grafana.com/grafana/download
2. Install and start Grafana service
3. Access at http://localhost:3000

## Configure Grafana

### Step 1: Add Prometheus Data Source

1. Go to Grafana → Configuration → Data Sources
2. Click "Add data source"
3. Select "Prometheus"
4. Set URL: `http://localhost:9090`
5. Click "Save & Test"

### Step 2: Create Dashboard

1. Go to Dashboards → New Dashboard
2. Add panels with these queries:

#### Panel 1: Average Time per Tile
```
image_processing_last_avg_ms
```

#### Panel 2: Total Tiles Processed
```
image_processing_tiles_total
```

#### Panel 3: Processing Runs
```
image_processing_runs_total
```

#### Panel 4: Tile Duration (95th percentile)
```
histogram_quantile(0.95, rate(image_processing_tile_duration_seconds_bucket[5m]))
```

#### Panel 5: Tile Processing Rate
```
rate(image_processing_tiles_total[1m])
```

#### Panel 6: Total Processing Time
```
image_processing_last_total_time_ms
```

#### Panel 7: Max Tile Duration
```
image_processing_tile_duration_seconds_max * 1000
```

### Step 3: Compare Processors

To compare different thread types, use the `processor` label:

```
image_processing_last_avg_ms{processor="virtual"}
image_processing_last_avg_ms{processor="os"}
image_processing_last_avg_ms{processor="hybrid"}
```

## Verify Setup

1. Make sure your Java application is running (metrics server on port 9100)
2. Check Prometheus: http://localhost:9090/targets (should show "UP")
3. Check metrics: http://localhost:9090/graph?g0.expr=image_processing_tiles_total
4. View in Grafana: Create panels and see real-time metrics!

## Troubleshooting

- **Can't connect to metrics**: Ensure Java app is running and metrics server started
- **No data in Grafana**: Check Prometheus is scraping (http://localhost:9090/targets)
- **Port conflicts**: Change ports in docker run commands or config files

