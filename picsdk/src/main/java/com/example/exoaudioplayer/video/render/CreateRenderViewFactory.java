package com.example.exoaudioplayer.video.render;

import android.content.Context;
import com.example.exoaudioplayer.aduio.base.Constants;

public class CreateRenderViewFactory extends RenderViewFactory {

    public static CreateRenderViewFactory getInstance() {
        return new CreateRenderViewFactory();
    }

    @Override
    public IRenderView createRenderView(Context context, int type) {
        if (type == Constants.SURFACE) {
            return new SurfaceRenderView(context);
        } else if (type == Constants.TEXTURE) {
            return new TextureRenderView(context);
        } else {
            return new TextureRenderView(context);
        }
    }
}
