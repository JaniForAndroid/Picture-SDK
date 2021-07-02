package com.namibox.videocache;

import static com.namibox.videocache.Preconditions.checkNotNull;
import static com.namibox.videocache.ProxyCacheUtils.DEFAULT_BUFFER_SIZE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import android.text.TextUtils;
import com.namibox.util.Logger;
import com.namibox.util.network.NetWorkHelper;
import com.namibox.videocache.headers.EmptyHeadersInjector;
import com.namibox.videocache.headers.HeaderInjector;
import com.namibox.videocache.sourcestorage.SourceInfoStorage;
import com.namibox.videocache.sourcestorage.SourceInfoStorageFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * {@link Source} that uses http resource as source for {@link ProxyCache}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class OkHttpUrlSource implements Source {

    private static final int MAX_REDIRECTS = 5;
    private final Call.Factory callFactory;
    private final CacheControl cacheControl = CacheControl.FORCE_NETWORK;
    private InputStream inputStream;
    private Response response;
    private final SourceInfoStorage sourceInfoStorage;
    private final HeaderInjector headerInjector;
    private SourceInfo sourceInfo;

    public OkHttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public OkHttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this(url, sourceInfoStorage, new EmptyHeadersInjector());
    }

    public OkHttpUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = checkNotNull(sourceInfoStorage);
        this.headerInjector = checkNotNull(headerInjector);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo :
            new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(url));
        callFactory = initCallFactory();
    }

    public OkHttpUrlSource(OkHttpUrlSource source) {
        this.sourceInfo = source.sourceInfo;
        this.sourceInfoStorage = source.sourceInfoStorage;
        this.headerInjector = source.headerInjector;
        callFactory = initCallFactory();
    }

    private Call.Factory initCallFactory() {
        //todo
        return NetWorkHelper.getOkHttpBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();
    }

    @Override
    public synchronized long length() throws ProxyCacheException {
        if (sourceInfo.length == 0) {
            fetchContentInfo();
        }
        return sourceInfo.length;
    }

    @Override
    public void open(long offset) throws ProxyCacheException {
        try {
            response = openConnection(offset);
            if (response.body() == null) {
                throw new ProxyCacheException("Empty response for " + sourceInfo.url + " with offset " + offset);
            }
            String mime = response.body().contentType().toString();
            inputStream = new BufferedInputStream(response.body().byteStream(), DEFAULT_BUFFER_SIZE);
            long length = readSourceAvailableBytes(response, offset);
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + sourceInfo.url + " with offset " + offset, e);
        }
    }

    private long readSourceAvailableBytes(Response response, long offset) throws IOException {
        long contentLength = response.body().contentLength();
        return response.code() == HTTP_OK ? contentLength
                : response.code() == HTTP_PARTIAL ? contentLength + offset : sourceInfo.length;
    }

    @Override
    public void close() throws ProxyCacheException {
        Logger.d("close: " + this);
        if (response != null) {
            response.body().close();
        }
    }

    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        if (inputStream == null) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url + ": connection is absent!");
        }
        try {
            return inputStream.read(buffer, 0, buffer.length);
        } catch (InterruptedIOException e) {
            throw new InterruptedProxyCacheException("Reading source " + sourceInfo.url + " is interrupted", e);
        } catch (IOException e) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url, e);
        }
    }

    private void fetchContentInfo() throws ProxyCacheException {
        Logger.d("fetchContentInfo: " + sourceInfo.url);
        InputStream inputStream = null;
        Response response = null;
        try {
            response = openConnection(0);
            long length = response.body().contentLength();
            String mime = response.body().contentType().toString();
            Logger.d("fetchContentInfo: length=" + length + ", mime=" + mime);
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
            inputStream = response.body().byteStream();
        } catch (IOException e) {
            Logger.e("Error fetching info from " + sourceInfo.url);
        } finally {
            ProxyCacheUtils.close(inputStream);
            if (response != null) {
                response.close();
            }
        }
    }

    private Response openConnection(long offset) throws IOException, ProxyCacheException {
        int redirectCount = 0;
        String url = getUrl();
        Response response;
        do {
            response = callFactory.newCall(makeRequest(url, offset)).execute();
            Logger.e("New Call>>>>>>" + url);
            Logger.e("******request header*******\n" + response.request().headers());
            Logger.e("******response header******\n" + response.headers());
            if (response.isRedirect()) {
                url = response.header("Location");
                redirectCount++;
                response.close();
            }
            if (redirectCount > MAX_REDIRECTS) {
                throw new ProxyCacheException("Too many redirects: " + redirectCount);
            }
        } while (response.isRedirect());
        return response;
    }

    private Request makeRequest(String url, long offset) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        Request.Builder builder = new Request.Builder().url(httpUrl);
        injectCustomHeaders(builder, url);
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        if (offset > 0) {
            builder.addHeader("Range", "bytes=" + offset + "-");
        }
        Request request = builder.build();
        return request;
    }

    private void injectCustomHeaders(Request.Builder builder, String url) {
        Map<String, String> extraHeaders = headerInjector.addHeaders(url);
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
    }

    public synchronized String getMime() throws ProxyCacheException {
        if (TextUtils.isEmpty(sourceInfo.mime)) {
            fetchContentInfo();
        }
        return sourceInfo.mime;
    }

    public String getUrl() {
        return sourceInfo.url;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "HttpUrlSource{sourceInfo='" + sourceInfo + "}";
    }
}
