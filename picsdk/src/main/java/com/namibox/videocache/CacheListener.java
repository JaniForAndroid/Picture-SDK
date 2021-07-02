package com.namibox.videocache;

/**
 * Listener for cache availability.
 *
 * @author Egor Makovsky (yahor.makouski@gmail.com)
 * @author Alexey Danilov (danikula@gmail.com).
 */
public interface CacheListener {

    void onContentLength(long length, String contentType);

    void onCacheAvailable(CacheInfo cacheInfo);
}
