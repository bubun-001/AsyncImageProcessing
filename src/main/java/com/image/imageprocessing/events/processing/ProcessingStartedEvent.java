package com.image.imageprocessing.events.processing;

import com.image.imageprocessing.events.Event;

public record ProcessingStartedEvent(String processorType, String imageId, int totalTiles) implements Event {
}

