package com.namibox.videocache.file;

import android.net.Uri;
import android.text.TextUtils;
import com.namibox.videocache.ProxyCacheUtils;

/**
 * Implementation of {@link FileNameGenerator} that uses MD5 of url as file name
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class Md5FileNameGenerator implements FileNameGenerator {

    private static final int MAX_EXTENSION_LENGTH = 4;

    private boolean isOnlyUrl = false;//生成文件名时，是否只要url部分

    public Md5FileNameGenerator() {
    }

    public Md5FileNameGenerator(boolean isOnlyUrl) {
        this.isOnlyUrl = isOnlyUrl;
    }

    @Override
    public String generate(String url) {
        String extension = getExtension(url);
        String name = ProxyCacheUtils.computeMD5(url);
        if (isOnlyUrl) {
            Uri uri = Uri.parse(url);
            if (uri.getQuery() != null) {
                String urlName = url.replace(uri.getQuery(),"").replace("?","");
                name = ProxyCacheUtils.computeMD5(urlName);
            }
        }

        return TextUtils.isEmpty(extension) ? name : name + "." + extension;
    }

    private String getExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        int slashIndex = url.lastIndexOf('/');
        return dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + MAX_EXTENSION_LENGTH > url.length() ?
                url.substring(dotIndex + 1, url.length()) : "";
    }
}
