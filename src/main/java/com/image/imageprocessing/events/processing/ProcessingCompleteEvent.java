package com.image.imageprocessing.events.processing;

import com.image.imageprocessing.events.Event;

public record ProcessingCompleteEvent(String processorType, int totalTiles, long totalTimeMs, long averageMs)
        implements Event {
}

