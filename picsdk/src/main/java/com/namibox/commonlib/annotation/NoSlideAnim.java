package com.namibox.commonlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wzp on 2019/9/30
 * 标记继承自AbsFoundationActivity的子类不使用滑入滑出切换动画
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoSlideAnim {

}
