package com.namibox.videocache;

import static com.namibox.videocache.Preconditions.checkNotNull;

import com.namibox.util.Logger;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.HttpUrl;

/**
 * Proxy for {@link Source} with caching support ({@link Cache}).
 * <p/>
 * Can be used only for sources with persistent data (that doesn't change with time).
 * Method {@link #read(byte[], long, int)} will be blocked while fetching data from source.
 * Useful for streaming something with caching e.g. streaming video/audio etc.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class ProxyCache {

    private static final int MAX_READ_SOURCE_ATTEMPTS = 1;

    private final Source source;
    private final Cache cache;
    private final Object wc = new Object();
    private final Object stopLock = new Object();
    private final AtomicInteger readSourceErrorsCount;
    private volatile Thread sourceReaderThread;
    private volatile boolean stopped;
    private volatile int percentsAvailable = -1;

    public ProxyCache(Source source, Cache cache) {
        this.source = checkNotNull(source);
        this.cache = checkNotNull(cache);
        this.readSourceErrorsCount = new AtomicInteger();
    }

    public int read(byte[] buffer, long offset, int length) throws ProxyCacheException {
        ProxyCacheUtils.assertBuffer(buffer, offset, length);

        readSourceAsync();
        while (!stopped && !cache.isCompleted() && cache.available() < (offset + length)) {
            waitForSourceData();
            checkReadSourceErrorsCount();
        }
        int read = cache.read(buffer, offset, length);
        if (cache.isCompleted() && percentsAvailable != 100) {
            onSourceRead();
        }
        return read;
    }

    private void checkReadSourceErrorsCount() throws ProxyCacheException {
        int errorsCount = readSourceErrorsCount.get();
        if (errorsCount >= MAX_READ_SOURCE_ATTEMPTS) {
            readSourceErrorsCount.set(0);
            throw new ProxyCacheException("Error reading source " + errorsCount + " times");
        }
    }

    public void shutdown() {
        synchronized (stopLock) {
            Logger.w("Shutdown sourceReaderThread: " + sourceReaderThread);
            try {
                stopped = true;
                if (sourceReaderThread != null) {
                    sourceReaderThread.interrupt();
                }
                cache.close();
            } catch (ProxyCacheException e) {
                onError(e);
            }
        }
    }

    private synchronized void readSourceAsync() throws ProxyCacheException {
        boolean readingInProgress = sourceReaderThread != null && sourceReaderThread.getState() != Thread.State.TERMINATED;
        if (!stopped && !cache.isCompleted() && !readingInProgress) {
            sourceReaderThread = new Thread(new SourceReaderRunnable(), "Source reader for " + source);
            sourceReaderThread.start();
        }
    }

    private void waitForSourceData() throws ProxyCacheException {
        synchronized (wc) {
            try {
                wc.wait(1000);
            } catch (InterruptedException e) {
                throw new ProxyCacheException("Waiting source data is interrupted!", e);
            }
        }
    }

    private void notifyNewCacheDataAvailable(long cacheAvailable, long sourceAvailable, String host, int readBytes, String eTag, String responseUrl) {
        onCacheAvailable(cacheAvailable, sourceAvailable, host, readBytes, eTag, responseUrl);

        synchronized (wc) {
            wc.notifyAll();
        }
    }

    protected void onCacheAvailable(long cacheAvailable, long sourceLength, String host, int readBytes, String eTag, String responseUrl) {
        boolean zeroLengthSource = sourceLength == 0;
        int percents = zeroLengthSource ? 100 : (int) ((float) cacheAvailable / sourceLength * 100);
        boolean sourceLengthKnown = sourceLength >= 0;
        if (sourceLengthKnown) {
            //Logger.d("onCacheAvailable----cacheAvailable=" + cacheAvailable + ", sourceLength=" + sourceLength);
            CacheInfo cacheInfo = new CacheInfo();
            cacheInfo.percentsAvailable = percents;
            cacheInfo.host = host;
            cacheInfo.realReadBytes = readBytes;
            cacheInfo.eTag = eTag;
            cacheInfo.responseUrl = responseUrl;
            onCachePercentsAvailableChanged(cacheInfo);
        }
        percentsAvailable = percents;
    }

    protected void onCachePercentsAvailableChanged(CacheInfo cacheInfo) {
    }

    private void readSource() {
        long sourceAvailable = -1;
        long offset = 0;
        String host = null;
        String eTag = null;
        String responseUrl = null;
        try {
            offset = cache.available();
            source.open(offset);
            sourceAvailable = source.length();
            Logger.d("开始下载缓存    起始位置=" + offset + ", 总长度=" + sourceAvailable);
            byte[] buffer = new byte[ProxyCacheUtils.DEFAULT_BUFFER_SIZE];
            int readBytes;
            int realReadBytes = 0;
            try {
                OkHttpUrlSource okHttpUrlSource = (OkHttpUrlSource) source;
                HttpUrl url = okHttpUrlSource.getResponse().request().url();
                host = url.host();
                eTag = okHttpUrlSource.getResponse().header("Etag");
                responseUrl = url.toString();
                host = url.host();
            } catch (Exception e) {
                e.printStackTrace();
            }
            while ((readBytes = source.read(buffer)) != -1) {
                synchronized (stopLock) {
                    if (isStopped()) {
                        return;
                    }
                    cache.append(buffer, readBytes);
                }
                offset += readBytes;
                realReadBytes += readBytes;
                notifyNewCacheDataAvailable(offset, sourceAvailable, host, realReadBytes, eTag, responseUrl);
            }
            tryComplete();
            onSourceRead();
        } catch (Throwable e) {
            if (e instanceof InterruptedProxyCacheException) {
                Logger.w("ProxyCache is interrupted");
            } else {
                readSourceErrorsCount.incrementAndGet();
                onError(e);
            }
        } finally {
            closeSource();
            notifyNewCacheDataAvailable(offset, sourceAvailable, null, 0, null, null);
        }
    }

    private void onSourceRead() {
        // guaranteed notify listeners after source read and cache completed
        CacheInfo cacheInfo = new CacheInfo();
        cacheInfo.percentsAvailable = 100;
        onCachePercentsAvailableChanged(cacheInfo);
    }

    private void tryComplete() throws ProxyCacheException {
        synchronized (stopLock) {
            Logger.e("tryComplete----cache.available()=" + cache.available()
                + ", source.length()=" + source.length() + ", isStopped=" + isStopped() + ", stopped=" + stopped);
            if (!isStopped() && cache.available() >= source.length()) {
                cache.complete();
            }
        }
    }

    private boolean isStopped() {
        return Thread.currentThread().isInterrupted() || stopped;
    }

    private void closeSource() {
        try {
            source.close();
        } catch (ProxyCacheException e) {
            onError(new ProxyCacheException("Error closing source " + source, e));
        }
    }

    protected final void onError(final Throwable e) {
        Logger.e(e, "ProxyCache onError:" + this);
    }

    private class SourceReaderRunnable implements Runnable {

        @Override
        public void run() {
            readSource();
        }
    }
}
