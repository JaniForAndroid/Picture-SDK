# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#手动启用support keep注解
#http://tools.android.com/tech-docs/support-annotations
#-dontskipnonpubliclibraryclassmembers
#-printconfiguration
#-keep,allowobfuscation @interface android.support.annotation.Keep
#
#-keep @android.support.annotation.Keep class *
#-keepclassmembers class * {
#    @android.support.annotation.Keep *;
#}

###########sdk
-keep public class sdk.callback.** { *; }
-keep public class sdk.model.** { *; }
-keep public class sdk.** { *; }
-keep class com.example.picsdk.model.** { *; }
-keep class com.example.picsdk.event.** { *; }
-keep class com.example.exoaudioplayer.video.model.** { *; }


-keep class support.uraroji.garage.android.lame.** {*;}

# 保留注解不混淆
-keepattributes *Annotation*,InnerClasses
# 避免混淆泛型
-keepattributes Signature
# 保留代码行号，方便异常信息的追踪
-keepattributes SourceFile,LineNumberTable
#避免混淆自定义控件类的 get/set 方法和构造函数
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#避免资源混淆
-keep class **.R$* {*;}
#避免layout中onclick方法（android:onclick="onClick"）混淆
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
#避免Parcelable混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#OkHttp3混淆配置
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**
#Retrofit2混淆配置
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#RxJava、RxAndroid混淆配置
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#Glide混淆配置
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#Gson混淆配置
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

#EventBus3混淆配置
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

##################aiEngine
-keep class com.iflytek.**{*;}
-keep class com.chivox.** { *; }
-keep class com.tt.** { *; }
-keep class com.xs.** { *; }
-keep class com.constraint.** { *; }
-keep class com.core.** { *; }
-dontwarn com.tt.**
-dontwarn com.xs.**
-dontwarn com.constraint.**
-dontwarn com.core.**

##################commonlib
-keepclassmembers class * {
    @com.namibox.commonlib.jsbridge.JSCommand *;
}
-keep class com.namibox.commonlib.event.** { *; }
-keep class com.namibox.commonlib.model.** { *; }

##################greendao
-keep class com.namibox.greendao.entity.** { *; }

##################ffmpeg
-keep class com.uraroji.garage.android.lame.** {*;}
-keep class sffmpegandroidtranscoder.** {*;}
-keep class android.support.v8.renderscript.** { *; }
-keep class com.sina.**{*;}

##################hfx
-keep class com.namibox.hfx.bean.** { *; }
-keep class com.namibox.hfx.event.** { *; }
-keep class com.namibox.hfx.utils.** { *; }

-keep class com.namibox.imageselector.bean.** { *; }

-printmapping mapping.txt

