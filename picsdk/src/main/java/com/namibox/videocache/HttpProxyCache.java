package com.namibox.videocache;

import static com.namibox.videocache.ProxyCacheUtils.DEFAULT_BUFFER_SIZE;

import android.text.TextUtils;
import com.namibox.util.Logger;
import com.namibox.videocache.file.FileCache;
import com.namibox.videocache.file.PartFileCache;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.HttpUrl;

/**
 * {@link ProxyCache} that read http url and writes data to {@link Socket}
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class HttpProxyCache extends ProxyCache {

    private static final float NO_CACHE_BARRIER = .01f;

    private final OkHttpUrlSource source;
    private final FileCache cache;
    private final Config config;
    private CacheListener listener;
    private volatile Thread sourceThread;
    private PartFileCache partCache;
    private final Object wc = new Object();
    private long lastReportTime;
    private long partCacheOffset;

    public HttpProxyCache(OkHttpUrlSource source, FileCache cache, Config config) {
        super(source, cache);
        this.cache = cache;
        this.source = source;
        this.config = config;
    }

    public void registerCacheListener(CacheListener cacheListener) {
        this.listener = cacheListener;
    }

    public void processRequest(GetRequest request, Socket socket)
        throws IOException, ProxyCacheException {
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        String responseHeaders = newResponseHeaders(request);
        out.write(responseHeaders.getBytes("UTF-8"));

        long offset = request.rangeOffset;
        Logger.d("proxy response header>>>>>>>\n" + responseHeaders);
        if (isUseCache(request)) {
            Logger.d("######################完整缓存######################");
            responseWithCache(out, offset);
        } else {
            Logger.d("######################局部缓存######################");
            //responseWithoutCache(out, offset);
            responseWithPartCache(out, offset);
        }
    }

    private boolean isUseCache(GetRequest request) throws ProxyCacheException {
        //return true;
        long sourceLength = source.length();
        long cacheAvailable = cache.available();
        Logger.e("rangeOffset=" + request.rangeOffset
            + ", sourceLength=" + sourceLength + ", cacheAvailable=" + cacheAvailable);
        // do not use cache for partial requests which too far from available cache. It seems user seek video.
        return sourceLength <= 0 || !request.partial
            || request.rangeOffset <= cacheAvailable + sourceLength * NO_CACHE_BARRIER;
    }

    private String newResponseHeaders(GetRequest request) throws IOException, ProxyCacheException {
        String mime = source.getMime();
        boolean mimeKnown = !TextUtils.isEmpty(mime);
        //FIXME cache isCompleted, but cache.available() == 0
        long length = cache.isCompleted() ? cache.available() : source.length();
        boolean lengthKnown = length >= 0;
        long contentLength = request.partial ? length - request.rangeOffset : length;
        boolean addRange = lengthKnown && request.partial;
        if (listener != null) {
            listener.onContentLength(length, mime);
        }
        return new StringBuilder()
            .append(request.partial ? "HTTP/1.1 206 PARTIAL CONTENT\n" : "HTTP/1.1 200 OK\n")
            .append("Accept-Ranges: bytes\n")
            .append(lengthKnown ? format("Content-Length: %d\n", contentLength) : "")
            .append(
                addRange ? format("Content-Range: bytes %d-%d/%d\n", request.rangeOffset, length - 1,
                    length) : "")
            .append(mimeKnown ? format("Content-Type: %s\n", mime) : "")
            .append("\n") // headers end
            .toString();
    }

    private void responseWithCache(OutputStream out, long offset)
        throws ProxyCacheException, IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readBytes;
        while ((readBytes = read(buffer, offset, buffer.length)) != -1) {
            out.write(buffer, 0, readBytes);
            offset += readBytes;
        }
        out.flush();
        Logger.d("responseWithCache----flush-----readBytes=" + readBytes);
    }

    private void responseWithoutCache(OutputStream out, long offset)
        throws ProxyCacheException, IOException {
        OkHttpUrlSource newSourceNoCache = new OkHttpUrlSource(this.source);
        try {
            newSourceNoCache.open((int) offset);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int readBytes;
            while ((readBytes = newSourceNoCache.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
                offset += readBytes;
            }
            out.flush();
        } finally {
            Logger.d("close newSourceNoCache");
            newSourceNoCache.close();
        }
    }

    private void responseWithPartCache(OutputStream out, long offset)
        throws ProxyCacheException, IOException {
        partCache = null;
        partCacheOffset = 0;
        readPartSourceAsync(offset);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readBytes;
        long totalRead = 0;
        while ((readBytes = readFromPartCache(buffer, totalRead, buffer.length)) != -1) {
            out.write(buffer, 0, readBytes);
            totalRead += readBytes;
        }
        out.flush();
    }

    private int readFromPartCache(byte[] buffer, long totalRead, int length)
        throws ProxyCacheException {
        while (sourceThread != null && !sourceThread.isInterrupted()
            && (partCache == null || partCache.available() < partCacheOffset + totalRead + length)) {
            waitForPartCache();
        }
        int read = -1;
        if (partCache != null) {
            read = partCache.read(buffer, partCacheOffset + totalRead, length);
        }
        return read;
    }

    private void waitForPartCache() throws ProxyCacheException {
        synchronized (wc) {
            try {
                wc.wait(1000);
            } catch (InterruptedException e) {
                throw new ProxyCacheException("Waiting source data is interrupted!", e);
            }
        }
    }

    private void notifyPartCacheAvailable(boolean finished, long cacheAvailable, long contentLength,
        OkHttpUrlSource source, String host, long realReadBytes, String eTag, String responseUrl) {
        int percents = contentLength == 0 ? 100 : (int) ((float) cacheAvailable / contentLength * 100);
        long time = System.currentTimeMillis();
        boolean report = time - lastReportTime >= 1000;
        if (finished || (contentLength >= 0 && report) || percents == 100) {
            if (listener != null) {
                CacheInfo cacheInfo = new CacheInfo();
                cacheInfo.cacheFile = partCache.file;
                cacheInfo.url = source.getUrl();
                cacheInfo.percentsAvailable = percents;
                cacheInfo.host = host;
                cacheInfo.realReadBytes = realReadBytes;
                cacheInfo.eTag = eTag;
                cacheInfo.responseUrl = responseUrl;
                listener.onCacheAvailable(cacheInfo);
            }
            lastReportTime = time;
        }
        synchronized (wc) {
            wc.notifyAll();
        }
    }

    private void readPartSourceAsync(final long offset) throws ProxyCacheException {
        if (sourceThread != null) {
            sourceThread.interrupt();
        }
        sourceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.d("开启局部缓存线程sourceThread，offset=" + offset);
                OkHttpUrlSource newSourceNoCache = new OkHttpUrlSource(source);
                long contentLength = -1;
                long totalRead = 0;
                long realRead = 0;
                long openOffset = offset;
                String host = null;
                String eTag = null;
                String responseUrl = null;
                try {
                    queryPartCacheFile(source.getUrl(), offset);
                    if (partCache == null) {
                        File file = generatePartFile(source.getUrl(), offset);
                        Logger.d("创建新的局部文件: " + file.getName());
                        partCache = new PartFileCache(file);
                    } else {
                        openOffset = offset + partCache.file.length() - partCacheOffset;
                    }
                    contentLength = newSourceNoCache.length();
                    if (openOffset < contentLength) {
                        Logger.d("开始下载位置：" + openOffset + ", contentLength=" + contentLength);
                        newSourceNoCache.open((int) openOffset);
                        totalRead = openOffset;
                        contentLength = newSourceNoCache.length();
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int readBytes = 0;
                        while (!Thread.currentThread().isInterrupted()
                            && (readBytes = newSourceNoCache.read(buffer)) != -1) {
                            partCache.append(buffer, readBytes);
                            realRead += readBytes;
                            totalRead += readBytes;
                            try {
                                if (host == null) {
                                    eTag = newSourceNoCache.getResponse().header("Etag");
                                    HttpUrl url = newSourceNoCache.getResponse().request().url();
                                    responseUrl = url.toString();
                                    host = url.host();
                                    Logger.d("上报cdn信息，host  = " + host + ", etag = " + eTag + ", responseUrl = "
                                        + responseUrl);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Logger.d("上报cdn信息，get host failed， error = " + e.getMessage());
                            }
                            notifyPartCacheAvailable(false, totalRead, contentLength, newSourceNoCache, host,
                                realRead, eTag, responseUrl);
                        }
                        Logger.d("局部缓存complete. readBytes=" + readBytes);
                    } else {
                        Logger.d("局部缓存已完成，不必下载：" + openOffset + ", contentLength=" + contentLength);
                        totalRead = contentLength;
                        notifyPartCacheAvailable(false, totalRead, contentLength, newSourceNoCache, null,
                            realRead, null, null);
                    }
                    partCache.complete();
                } catch (Throwable e) {
                    onError(e);
                } finally {
                    Logger.d("局部缓存线程sourceThread结束");
                    sourceThread = null;
                    try {
                        newSourceNoCache.close();
                    } catch (ProxyCacheException e) {
                        onError(new ProxyCacheException("Error closing source " + newSourceNoCache, e));
                    }
                    notifyPartCacheAvailable(true, totalRead, contentLength, newSourceNoCache, null,
                        realRead, null, null);
                }
            }
        });
        sourceThread.start();
    }

    private void queryPartCacheFile(String url, long offset) throws ProxyCacheException {
        String name = ProxyCacheUtils.computeMD5(url);
        final Pattern fileNamePattern = Pattern.compile(name + "_offset_(\\d+)");
        File[] partFiles = config.cacheRoot.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        if (partFiles != null) {
            for (File partFile : partFiles) {
                Matcher matcher = fileNamePattern.matcher(partFile.getName());
                if (matcher.matches()) {
                    long fileOffset = Long.valueOf(matcher.group(1));
                    long fileLength = partFile.length();
                    if (offset >= fileOffset && offset <= fileLength + fileOffset) {
                        Logger.d("命中局部缓存文件: " + partFile.getName());
                        partCache = new PartFileCache(partFile);
                        partCacheOffset = offset - fileOffset;
                        Logger.d("局部缓存起始offset=" + partCacheOffset + " 已缓存：" + fileLength);
                        return;
                    }
                }
            }
        }
    }

    private File generatePartFile(String url, long offset) {
        String name = ProxyCacheUtils.computeMD5(url);
        return new File(config.cacheRoot, name + "_offset_" + offset);
    }

    private String format(String pattern, Object... args) {
        return String.format(Locale.US, pattern, args);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            if (sourceThread != null) {
                Logger.d("shutdown sourceThread: " + sourceThread);
                sourceThread.interrupt();
                sourceThread = null;
            }
            if (partCache != null) {
                partCache.close();
                partCache = null;
            }
        } catch (ProxyCacheException e) {
            onError(e);
        }
    }

    @Override
    protected void onCachePercentsAvailableChanged(CacheInfo cacheInfo) {
        long time = System.currentTimeMillis();
        boolean report = time - lastReportTime >= 1000;
        if (!report && cacheInfo.percentsAvailable != 100) {
            return;
        }
        lastReportTime = time;
        cacheInfo.cacheFile = cache.file;
        cacheInfo.url = source.getUrl();
        if (listener != null) {
            listener.onCacheAvailable(cacheInfo);
        }
    }
}
