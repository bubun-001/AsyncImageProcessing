package com.image.imageprocessing.events.processing;

import com.image.imageprocessing.events.Event;

public record ProcessingErrorEvent(String processorType, Throwable error) implements Event {
}

