package com.image.imageprocessing.cache;

public record TileKey(
 String imageId, // parent image ID
 long imageVersion,
 int tileX,
 int tileY,
 int tileSize,
 String filterId,
 String processorMode


){}