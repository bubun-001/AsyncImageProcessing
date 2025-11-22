package com.image.imageprocessing.events.processing;

import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.cache.TileKey;
import com.image.imageprocessing.events.Event;

public record TileProcessedEvent(TileKey key, ImageData data, long durationNanos) implements Event {
}

