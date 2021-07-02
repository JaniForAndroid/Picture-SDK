package com.namibox.videocache;

import static com.namibox.videocache.Preconditions.checkNotNull;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.namibox.util.Logger;
import com.namibox.videocache.file.FileCache;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client for {@link HttpProxyCacheServer}
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
final class HttpProxyCacheServerClients {

    private final AtomicInteger clientsCount = new AtomicInteger(0);
    private final String url;
    private volatile HttpProxyCache proxyCache;
    private final List<CacheListener> listeners = new CopyOnWriteArrayList<>();
    private final CacheListener uiCacheListener;
    private final Config config;

    public HttpProxyCacheServerClients(String url, Config config) {
        this.url = checkNotNull(url);
        this.config = checkNotNull(config);
        this.uiCacheListener = new UiListenerHandler(url, listeners);
    }

    public void processRequest(GetRequest request, Socket socket) throws ProxyCacheException, IOException {
        try {
            startProcessRequest();
            Logger.d("processRequest: " + request);
            proxyCache.processRequest(request, socket);
        } finally {
            finishProcessRequest();
        }
    }

    private synchronized void startProcessRequest() throws ProxyCacheException {
        shutdownProxyCache();
        int i = clientsCount.incrementAndGet();
        Logger.d("startProcessRequest: " + i);
        proxyCache = proxyCache == null ? newHttpProxyCache() : proxyCache;
    }

    private synchronized void finishProcessRequest() {
        int i = clientsCount.decrementAndGet();
        Logger.d("finishProcessRequest: " + i);
        if (i <= 0) {
            shutdownProxyCache();
        }
    }

    public void registerCacheListener(CacheListener cacheListener) {
        if (!listeners.contains(cacheListener)) {
            listeners.add(cacheListener);
        }
    }

    public void unregisterCacheListener(CacheListener cacheListener) {
        listeners.remove(cacheListener);
    }

    public void shutdown() {
        Logger.d("shutdown");
        listeners.clear();
        shutdownProxyCache();
        clientsCount.set(0);
    }

    public void shutdownProxyCache() {
        Logger.w("shutdownProxyCache: " + proxyCache);
        if (proxyCache != null) {
            proxyCache.registerCacheListener(null);
            proxyCache.shutdown();
            proxyCache = null;
        }
    }

    public int getClientsCount() {
        return clientsCount.get();
    }

    private HttpProxyCache newHttpProxyCache() throws ProxyCacheException {
        //HttpUrlSource source = new HttpUrlSource(url, config.sourceInfoStorage, config.headerInjector);
        OkHttpUrlSource source = new OkHttpUrlSource(url, config.sourceInfoStorage, config.headerInjector);
        File file = config.generateCacheFile(url);
        FileCache cache = new FileCache(file, config.diskUsage);
        HttpProxyCache httpProxyCache = new HttpProxyCache(source, cache, config);
        httpProxyCache.registerCacheListener(uiCacheListener);
        Logger.d("newHttpProxyCache: url=" + url + ", file=" + file + ", exists=" + file.exists());
        return httpProxyCache;
    }

    private static final class UiListenerHandler extends Handler implements CacheListener {

        private final String url;
        private final List<CacheListener> listeners;

        public UiListenerHandler(String url, List<CacheListener> listeners) {
            super(Looper.getMainLooper());
            this.url = url;
            this.listeners = listeners;
        }

        @Override
        public void onContentLength(long length, String contentType) {
            Message message = obtainMessage(1);
            message.obj = length;
            Bundle data = new Bundle();
            data.putString("contentType", contentType);
            message.setData(data);
            sendMessage(message);
        }

        @Override
        public void onCacheAvailable(CacheInfo cacheInfo) {
            Message message = obtainMessage(0);
            cacheInfo.url = url;
            message.obj = cacheInfo;
            sendMessage(message);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    for (CacheListener cacheListener : listeners) {
                        cacheListener.onCacheAvailable((CacheInfo) msg.obj);
                    }
                    break;
                case 1:
                    String contentType = msg.getData().getString("contentType");
                    for (CacheListener cacheListener : listeners) {
                        cacheListener.onContentLength((Long) msg.obj, contentType);
                    }
                    break;
            }

        }
    }
}
