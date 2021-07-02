package com.namibox.videocache.sourcestorage;

import android.content.Context;
import android.content.SharedPreferences;
import com.namibox.videocache.ProxyCacheUtils;
import com.namibox.videocache.SourceInfo;

/**
 * {@link SourceInfoStorage} that does nothing.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class PrefSourceInfoStorage implements SourceInfoStorage {
    private final SharedPreferences pref;

    public PrefSourceInfoStorage(Context context) {
        pref = context.getSharedPreferences("video_cache_info", Context.MODE_PRIVATE);
    }

    @Override
    public SourceInfo get(String url) {
        String name = ProxyCacheUtils.computeMD5(url);
        long length = pref.getLong("video_length_" + name, 0L);
        String mime = pref.getString("video_mime_" + name, null);
        return new SourceInfo(url, length, mime);
    }

    @Override
    public void put(String url, SourceInfo sourceInfo) {
        String name = ProxyCacheUtils.computeMD5(url);
        pref.edit()
            .putLong("video_length_" + name, sourceInfo.length)
            .putString("video_mime_" + name, sourceInfo.mime)
            .putString("video_url_" + name, sourceInfo.url)
            .apply();
    }

    @Override
    public void release() {
    }
}
