package com.image.imageprocessing.cache;

public record TileKey(
        String imageId,
        long imageVersion,
        int tileX,
        int tileY,
        int tileSize,
        String filterId,
        String processorMode
) {
}