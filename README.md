# Image Processing Application

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue?style=for-the-badge&logo=apache-maven)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-4DABF7?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A high-performance, multi-threaded image processing framework with intelligent caching, real-time monitoring, and extensible architecture.**

[Features](#-features) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [Architecture](#-architecture)
- [API Documentation](#-api-documentation)
- [Metrics & Monitoring](#-metrics--monitoring)
- [Development](#-development)
- [Performance Tuning](#-performance-tuning)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

This application is a production-ready image processing framework designed for high-performance, parallel image manipulation. It leverages modern Java 21 features including Virtual Threads (Project Loom) to provide scalable, efficient image processing capabilities.

### Key Highlights

- **Multi-Strategy Processing**: Choose between OS threads, Virtual threads, or Hybrid approaches
- **Intelligent Caching**: LRU-based tile cache with configurable capacity
- **Event-Driven**: Real-time event notifications for monitoring and logging
- **Production Metrics**: Prometheus integration for comprehensive observability
- **Extensible Design**: Plugin-based architecture for filters and processors

---

## ✨ Features

### Core Capabilities

- ✅ **Tile-Based Parallel Processing**: Images are intelligently divided into tiles for optimal parallelization
- ✅ **Multiple Processing Strategies**: 
  - OS Thread Pool (traditional approach)
  - Virtual Threads (Project Loom - Java 21)
  - Hybrid (combines both strategies)
- ✅ **LRU Cache System**: Configurable tile cache with automatic eviction
- ✅ **Real-Time Visualization**: JavaFX-based UI showing processed tiles as they complete
- ✅ **Event Bus Architecture**: Decoupled event system for cache and processing events

### Advanced Features

- 📊 **Prometheus Metrics**: Comprehensive metrics for performance monitoring
- 🔔 **Event Notifications**: Real-time events for cache hits/misses, processing status
- 🎨 **Extensible Filters**: Easy-to-implement filter interface
- 🔧 **Configurable**: System properties and code-level configuration options
- 📈 **Performance Monitoring**: Built-in metrics for tiles processed, cache performance, and timing

---

## 📦 Prerequisites

| Requirement | Version | Notes |
|------------|---------|-------|
| **Java** | 21+ | Required for Virtual Threads support |
| **Maven** | 3.6+ | Build tool |
| **JavaFX** | 21.0.2+ | Included via Maven dependencies |
| **Prometheus** | Latest | Optional, for metrics monitoring |
| **Grafana** | Latest | Optional, for metrics visualization |

### Verify Installation

```bash
java -version    # Should show Java 21 or higher
mvn -version     # Should show Maven 3.6 or higher
```

---

## 🚀 Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd ImageProcessing
mvn clean install
```

### 2. Run the Application

```bash
mvn javafx:run
```

Or using the Maven wrapper:

```bash
./mvnw javafx:run
```

### 3. Access Metrics (Optional)

Once running, access metrics at:
- **Metrics Endpoint**: http://localhost:9100/metrics
- **Prometheus** (if configured): http://localhost:9090
- **Grafana** (if configured): http://localhost:3000

---

## ⚙️ Configuration

### Processor Mode Selection

Edit `src/main/java/com/image/imageprocessing/HelloApplication.java`:

```java
private static final String PROCESSOR_MODE = "os";  // Options: "os", "virtual", "hybrid"
```

| Mode | Description | Best For |
|------|-------------|----------|
| `os` | Traditional OS thread pool | CPU-intensive tasks, predictable workloads |
| `virtual` | Java 21 Virtual Threads | High concurrency, I/O-bound operations |
| `hybrid` | Combination of both | Balanced workloads, mixed operations |

### Cache Configuration

#### Via System Property

```bash
mvn javafx:run -Dexec.args="-Dtile.cache.capacity=10000"
```

#### Via pom.xml

Already configured in `pom.xml`:

```xml
<option>-Dtile.cache.capacity=10000</option>
```

#### Cache Capacity Guidelines

| Image Size | Recommended Cache | Memory Impact |
|-----------|-------------------|---------------|
| Small (< 1MP) | 512 - 1,024 | Low |
| Medium (1-10MP) | 1,024 - 5,120 | Medium |
| Large (> 10MP) | 5,120 - 10,000+ | High |

### Image Path Configuration

Update the image path in `HelloApplication.java`:

```java
BufferedImage image = imageRead.readImage("path/to/your/image.jpg");
```

### Tile Size Configuration

Adjust tile size in the `processImage` call:

```java
processor.processImage(image, 10, imageFilter, drawMultipleImage);
//                                    ^^^
//                              Tile size in pixels
```

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HelloApplication                          │
│  (Main Entry Point, Configuration, Event Listeners)         │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
┌───────▼──────┐ ┌────▼─────┐ ┌─────▼──────────┐
│ ImageReader  │ │  Filter  │ │ ImageProcessor  │
│              │ │           │ │                 │
│ - File I/O   │ │ - Plugin  │ │ - OS Threads    │
│ - Validation │ │ - Extend  │ │ - Virtual       │
└──────────────┘ └───────────┘ │ - Hybrid        │
                               └────────┬────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
            ┌───────▼──────┐    ┌───────▼──────┐   ┌───────▼──────┐
            │  TileCache   │    │  EventBus    │   │   Metrics    │
            │              │    │              │   │              │
            │ - LRU Cache  │    │ - Pub/Sub    │   │ - Prometheus │
            │ - Eviction   │    │ - Events     │   │ - Registry   │
            └──────────────┘    └──────────────┘   └──────────────┘
```

### Component Overview

#### 1. **Image Processing Layer**
- `ImageProcessor` interface defines processing contract
- Three implementations: `OSImageProcessor`, `VirtualImageProcessor`, `HybridImageProcessor`
- Each processor handles tile-based parallel processing

#### 2. **Caching Layer**
- `TileCache`: Thread-safe LRU cache implementation
- `TileKey`: Composite key for cache lookups (image ID, version, coordinates, filter, processor)
- Automatic eviction when capacity is reached

#### 3. **Event System**
- `EventBus`: Singleton pub/sub system
- Event types:
  - Cache events: `CacheStatsEvent`, `CacheEvictionEvent`, `CacheCapacityReachedEvent`
  - Processing events: `ProcessingStartedEvent`, `ProcessingCompleteEvent`, `TileProcessedEvent`, `ProcessingErrorEvent`

#### 4. **Metrics Layer**
- `MetricsRegistry`: Central metrics collection
- `MetricsServer`: HTTP server exposing Prometheus metrics
- `CacheMetrics`: Cache-specific metrics
- `ProcessorMetrics`: Processing performance metrics

#### 5. **UI Layer**
- `DrawMultipleImage`: JavaFX canvas rendering
- Real-time tile drawing as processing completes
- Thread-safe queue for tile rendering

### Processing Flow

```
1. Image Load
   ↓
2. Tile Division (based on tile size)
   ↓
3. Parallel Processing (selected processor strategy)
   ├─→ Check Cache
   ├─→ Process Tile (if cache miss)
   ├─→ Store in Cache
   └─→ Queue for Rendering
   ↓
4. Real-Time Rendering (JavaFX Canvas)
   ↓
5. Event Publishing (EventBus)
   ↓
6. Metrics Collection (Prometheus)
```

---

## 📚 API Documentation

### ImageProcessor Interface

```java
public interface ImageProcessor {
    void processImage(
        BufferedImage image,      // Source image
        int num,                  // Tile size in pixels
        ImageFilter imageFilter,  // Filter to apply
        DrawMultipleImage drawFn  // Rendering callback
    );
}
```

### ImageFilter Interface

```java
public interface ImageFilter {
    BufferedImage filter(BufferedImage image);
}
```

### Implementing a Custom Filter

```java
public class CustomFilter implements ImageFilter {
    @Override
    public BufferedImage filter(BufferedImage originalImage) {
        // Your filter logic here
        BufferedImage filtered = new BufferedImage(
            originalImage.getWidth(),
            originalImage.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        
        // Apply transformations...
        
        return filtered;
    }
}
```

### Using Custom Filter

```java
ImageFilter customFilter = new CustomFilter();
processor.processImage(image, 10, customFilter, drawMultipleImage);
```

### Event Subscription

```java
EventBus eventBus = EventBus.getInstance();

eventBus.subscribe(ProcessingCompleteEvent.class, event -> {
    System.out.println("Processing complete: " + event.totalTiles() + " tiles");
    System.out.println("Average time: " + event.averageMs() + " ms");
});

eventBus.subscribe(CacheStatsEvent.class, event -> {
    System.out.println("Cache hit rate: " + (event.hitRate() * 100) + "%");
});
```

---

## 📊 Metrics & Monitoring

### Available Metrics

The application exposes Prometheus metrics at `http://localhost:9100/metrics`:

#### Processing Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `image_processing_tiles_total` | Counter | Total tiles processed |
| `image_processing_tile_duration_seconds` | Histogram | Tile processing time distribution |
| `image_processing_last_avg_ms` | Gauge | Last average processing time per tile |
| `image_processing_last_total_time_ms` | Gauge | Last total processing time |
| `image_processing_runs_total` | Counter | Number of processing runs |

#### Cache Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `cache_hits_total` | Counter | Total cache hits |
| `cache_misses_total` | Counter | Total cache misses |
| `cache_size` | Gauge | Current cache size |
| `cache_evictions_total` | Counter | Total cache evictions |

### Prometheus Queries

```promql
# Average processing time per tile
image_processing_last_avg_ms

# Processing rate (tiles per second)
rate(image_processing_tiles_total[1m])

# Cache hit rate
cache_hits_total / (cache_hits_total + cache_misses_total)

# 95th percentile tile processing time
histogram_quantile(0.95, rate(image_processing_tile_duration_seconds_bucket[5m]))

# Compare processors
image_processing_last_avg_ms{processor="virtual"}
image_processing_last_avg_ms{processor="os"}
image_processing_last_avg_ms{processor="hybrid"}
```

### Setting Up Monitoring

For detailed Prometheus and Grafana setup instructions, see [SETUP_GRAFANA.md](SETUP_GRAFANA.md).

**Quick Docker Setup:**

```bash
# Start Prometheus
docker run -d --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus

# Start Grafana
docker run -d --name grafana -p 3000:3000 grafana/grafana
```

Access:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (default: admin/admin)

---

## 🛠️ Development

### Project Structure

```
ImageProcessing/
├── src/
│   ├── main/
│   │   ├── java/com/image/imageprocessing/
│   │   │   ├── HelloApplication.java          # Main entry point
│   │   │   ├── cache/                        # Caching system
│   │   │   │   ├── TileCache.java           # LRU cache implementation
│   │   │   │   └── TileKey.java             # Cache key structure
│   │   │   ├── events/                       # Event system
│   │   │   │   ├── EventBus.java            # Pub/sub event bus
│   │   │   │   ├── cache/                   # Cache events
│   │   │   │   └── processing/              # Processing events
│   │   │   ├── Filters/                     # Image filters
│   │   │   │   ├── ImageFilter.java        # Filter interface
│   │   │   │   └── GreyScaleFilter.java    # Grayscale implementation
│   │   │   ├── Image/                       # Image handling
│   │   │   │   ├── DrawMultipleImage.java  # JavaFX rendering
│   │   │   │   └── ImageData.java          # Image data structure
│   │   │   ├── io/                          # I/O operations
│   │   │   │   ├── ImageRead.java          # Image reader
│   │   │   │   └── ImageReadInf.java      # Reader interface
│   │   │   ├── metrics/                     # Metrics collection
│   │   │   │   ├── CacheMetrics.java       # Cache metrics
│   │   │   │   ├── MetricsRegistry.java    # Metrics registry
│   │   │   │   ├── MetricsServer.java      # HTTP metrics server
│   │   │   │   └── ProcessorMetrics.java   # Processor metrics
│   │   │   └── processor/                   # Processing strategies
│   │   │       ├── common/
│   │   │       │   └── ImageProcessor.java # Processor interface
│   │   │       ├── os/                     # OS thread processor
│   │   │       ├── hybrid/                 # Hybrid processor
│   │   │       └── Virtual/                # Virtual thread processor
│   │   └── resources/                       # Resources
│   └── test/                                 # Test directory
├── pom.xml                                   # Maven configuration
├── prometheus.yml                            # Prometheus config
├── SETUP_GRAFANA.md                          # Monitoring setup guide
└── README.md                                 # This file
```

### Building from Source

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Run application
mvn javafx:run
```

### Adding a New Processor

1. Implement `ImageProcessor` interface:

```java
public class CustomProcessor implements ImageProcessor {
    private static final String PROCESSOR_TYPE = "custom";
    
    @Override
    public void processImage(BufferedImage image, int num, 
                            ImageFilter imageFilter, DrawMultipleImage drawFn) {
        // Your processing logic
    }
}
```

2. Add to `HelloApplication.java`:

```java
case "custom" -> new CustomProcessor();
```

### Adding a New Filter

1. Implement `ImageFilter` interface (see [API Documentation](#-api-documentation))
2. Use in `HelloApplication.java`:

```java
ImageFilter myFilter = new MyCustomFilter();
processor.processImage(image, 10, myFilter, drawMultipleImage);
```

---

## ⚡ Performance Tuning

### Processor Selection Guide

| Scenario | Recommended Processor | Rationale |
|----------|----------------------|-----------|
| CPU-intensive filters | `os` | Better thread pool control |
| High concurrency (1000+ tiles) | `virtual` | Lower overhead, better scaling |
| Mixed workloads | `hybrid` | Best of both worlds |
| Memory-constrained | `os` | More predictable memory usage |

### Cache Tuning

**Memory Calculation:**
```
Memory per tile ≈ (tile_size × tile_size × 4 bytes) × cache_capacity
Example: (10 × 10 × 4) × 10,000 = 4 MB
```

**Recommendations:**
- Start with default (512 tiles)
- Monitor cache hit rate via metrics
- Increase if hit rate < 70%
- Decrease if memory is constrained

### Tile Size Optimization

| Image Size | Recommended Tile Size | Tiles Generated |
|-----------|----------------------|-----------------|
| < 1MP | 5-10 pixels | 10,000 - 40,000 |
| 1-10MP | 10-20 pixels | 2,500 - 10,000 |
| > 10MP | 20-50 pixels | 400 - 2,500 |

**Trade-offs:**
- Smaller tiles = More parallelism but more overhead
- Larger tiles = Less overhead but less parallelism

### JVM Tuning

For optimal performance, consider these JVM options:

```bash
mvn javafx:run -Dexec.args="
  -Xmx4G
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -Dtile.cache.capacity=10000
"
```

---

## 🔧 Troubleshooting

### Common Issues

#### Application Won't Start

**Problem:** `UnsupportedClassVersionError` or similar

**Solution:**
```bash
# Verify Java version
java -version  # Must be 21 or higher

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java21
```

#### No Metrics Available

**Problem:** Cannot access http://localhost:9100/metrics

**Solution:**
1. Verify metrics server started: Check console for "Prometheus metrics server started"
2. Check port conflicts: `lsof -i :9100`
3. Verify firewall settings

#### Out of Memory

**Problem:** `OutOfMemoryError: Java heap space`

**Solution:**
1. Reduce cache capacity: `-Dtile.cache.capacity=1000`
2. Increase heap size: `-Xmx4G`
3. Use OS processor instead of virtual (more predictable memory)

#### Image Not Loading

**Problem:** Image file not found or null

**Solution:**
1. Verify file path is absolute or relative to project root
2. Check file permissions
3. Verify image format is supported (JPEG, PNG, etc.)

#### Slow Processing

**Problem:** Processing takes too long

**Solution:**
1. Check cache hit rate (should be > 70% after warm-up)
2. Try different processor mode (virtual vs os)
3. Adjust tile size (smaller = more parallelism)
4. Monitor CPU usage (may be CPU-bound)

### Debug Mode

Enable verbose logging:

```java
// In HelloApplication.java, add before processing:
System.setProperty("java.util.logging.config.file", "logging.properties");
```

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Contribution Process

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes** following the code style
4. **Add tests** for new functionality
5. **Update documentation** as needed
6. **Commit your changes**: `git commit -m 'Add amazing feature'`
7. **Push to branch**: `git push origin feature/amazing-feature`
8. **Open a Pull Request**

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and small
- Handle exceptions appropriately

### Testing

- Add unit tests for new features
- Test with different image sizes
- Verify metrics are updated correctly
- Test cache behavior with various capacities

### Pull Request Checklist

- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests added/updated
- [ ] All tests pass

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **JavaFX Team** for the excellent UI framework
- **Micrometer** for metrics collection
- **Prometheus** for monitoring capabilities
- **Project Loom** for Virtual Threads (Java 21)

---

## 📞 Support

For questions, issues, or contributions:

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-repo/discussions)

---

<div align="center">

**Made with ❤️ using Java 21**

[⬆ Back to Top](#-image-processing-application)

</div>

