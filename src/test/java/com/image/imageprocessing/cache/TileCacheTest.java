package com.image.imageprocessing.cache;

mport com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.events.EventBus;
import com.image.imageprocessing.events.cache.CacheCapacityReachedEvent;
import com.image.imageprocessing.events.cache.CacheEvictionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


public class TileCacheTest {


    static void setUpCapacity() {
        System.setProperty("tile.cache.capacity","1");
    }

    @Test
    void  putAndGetSameImageData() {
        TileCache cache = TileCache.getInstance();
        Tilekey key = new TileKey("img-1",1L,0,0,10,"filter","os");
        BufferedImage image = new BufferedImage(10, 10 , BufferedImage.TYPE_INT_RGB);
        ImageData data = new ImageData(img, 0 , 0 , 10 , 10);


        cache.put(key, data);
        ImageData fromCache = cache.get(key);

        assertNotNull(fromCache);
        assertEquals(data, fromCache);

    }


    @Test
    void  shouldPublishEventsOnEviction(){
        TileCache cache = TileCache.getInstance();
        EventBus eventBus = EventBus.getInstance();

        AtomicBoolean capacityReached = new AtomicBoolean(false);
        AtomicBoolean evictionHappened = new AtomicBoolean(false);

        bus.subscribe(CacheCapacityReachedEvent.class, evt-> capacityReached.set(true));
        bus.subscribe(CacheEvictionEvent.class, evt ->evictionHappened.set(true));

        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ImageData first = new ImageData(img, 0, 0 , 10, 10);
        ImageData second = new ImageData(img, 10, 0 , 10, 10);

        TileKey key1 = new TileKey("img-1", 1L,0, 0, 10, "filter", "os");
        TileKey key2 = new TileKey("img-1", 1L, 1, 0 , 10, "filter", "os");

        cache.put(key1, first);
        cache.put(key2, second);

        assertTrue(capacityReached.get() , "Capacity reached event should be published");
        assertTrue(evictionHappened.get(), "Eviction should be published");
        assertNull(cache.get(key1), "oldest entry to be evicted");
        assertNotNull(cache.get(key2), "Add newest entry to cache");


    }


}