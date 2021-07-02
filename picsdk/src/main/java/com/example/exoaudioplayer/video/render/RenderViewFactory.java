package com.example.exoaudioplayer.video.render;

import android.content.Context;

/**
 * 此接口用于扩展自己的渲染View。使用方法如下：
 * 1.继承IRenderView实现自己的渲染View。
 * 2.重写createRenderView返回步骤1的渲染View。
 */
public abstract class RenderViewFactory {

    public abstract IRenderView createRenderView(Context context,int type);

}
