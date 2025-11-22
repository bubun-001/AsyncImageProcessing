package com.image.imageprocessing.events.cache;

import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.cache.TileKey;
import com.image.imageprocessing.events.Event;

public record CacheEvictionEvent(TileKey key, ImageData value) implements Event {
}

