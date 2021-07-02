package com.namibox.commonlib.jsbridge;

import android.webkit.WebView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.util.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create time: 2020/4/10.
 */
public class JSBridge {
  private final Map<Object, List<CommandMethod>> subscribers;
  private static final Map<Class<?>, List<CommandMethod>> METHOD_CACHE = new ConcurrentHashMap<>();

  private static final int POOL_SIZE = 4;
  private static final FindState[] FIND_STATE_POOL = new FindState[POOL_SIZE];

  public JSBridge() {
    subscribers = new HashMap<>();
  }

  static class FindState {
    final List<CommandMethod> subscriberMethods = new ArrayList<>();

    Class<?> subscriberClass;
    Class<?> clazz;
    boolean skipSuperClasses;

    void initForSubscriber(Class<?> subscriberClass) {
      this.subscriberClass = clazz = subscriberClass;
      skipSuperClasses = false;
    }

    void recycle() {
      subscriberMethods.clear();
      subscriberClass = null;
      clazz = null;
      skipSuperClasses = false;
    }

    void moveToSuperclass() {
      if (skipSuperClasses) {
        clazz = null;
      } else {
        clazz = clazz.getSuperclass();
        String clazzName = clazz.getName();
        /** Skip system classes, this just degrades performance. */
        if (clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("android.")) {
          clazz = null;
        }
      }
    }
  }

  public void register(Object subscriber) {
    Class<?> subscriberClass = subscriber.getClass();
    List<CommandMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
    if (subscriberMethods == null) {
      FindState findState = prepareFindState();
      findState.initForSubscriber(subscriberClass);
      while (findState.clazz != null) {
        findUsingReflectionInSingleClass(findState);
        findState.moveToSuperclass();
      }
      subscriberMethods = getMethodsAndRelease(findState);
    }
    if (subscriberMethods.isEmpty()) {
      Logger.e("Subscriber " + subscriberClass
          + " and its super classes have no public methods with the @JSCommand annotation");
    } else {
      METHOD_CACHE.put(subscriberClass, subscriberMethods);
    }
    subscribers.put(subscriber, subscriberMethods);
  }

  public synchronized void unregister(Object subscriber) {
    subscribers.remove(subscriber);
  }

  public synchronized boolean isRegistered(Object subscriber) {
    return subscribers.containsKey(subscriber);
  }


  private List<CommandMethod> getMethodsAndRelease(FindState findState) {
    List<CommandMethod> subscriberMethods = new ArrayList<>(findState.subscriberMethods);
    findState.recycle();
    synchronized (FIND_STATE_POOL) {
      for (int i = 0; i < POOL_SIZE; i++) {
        if (FIND_STATE_POOL[i] == null) {
          FIND_STATE_POOL[i] = findState;
          break;
        }
      }
    }
    return subscriberMethods;
  }

  private FindState prepareFindState() {
    synchronized (FIND_STATE_POOL) {
      for (int i = 0; i < POOL_SIZE; i++) {
        FindState state = FIND_STATE_POOL[i];
        if (state != null) {
          FIND_STATE_POOL[i] = null;
          return state;
        }
      }
    }
    return new FindState();
  }

  private void findUsingReflectionInSingleClass(FindState findState) {
    Method[] methods;
    try {
      methods = findState.clazz.getDeclaredMethods();
    } catch (Throwable th) {
      methods = findState.clazz.getMethods();
      findState.skipSuperClasses = true;
    }
    for (Method method : methods) {
      int modifiers = method.getModifiers();
      JSCommand annotation = method.getAnnotation(JSCommand.class);
      if ((modifiers & Modifier.PUBLIC) != 0 && annotation != null) {
        String command = annotation.command();
        String callback = annotation.callback();
        findState.subscriberMethods.add(new CommandMethod(method, command, callback));
      }
    }
  }

  public String callJava(JSHost jsHost, JsonObject jsonObj, String command) throws Exception {
    String result = "{}";
    boolean invoke = false;
    for (Object subscriber : subscribers.keySet()) {
      List<CommandMethod> methods = subscribers.get(subscriber);
      for (CommandMethod commandMethod : methods) {
          if (commandMethod.command.equals(command)) {
            Method method = commandMethod.method;
            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Class<?> returnType = method.getReturnType();
            Object returnResult;
            if (parameterTypes.length == 0) {
              returnResult = method.invoke(subscriber);
            } else {
              Object[] args = new Object[parameterTypes.length];
              for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                if (WebView.class.isAssignableFrom(parameterType)) {
                  args[i] = jsHost.getWebView();
                } else if (JSCallback.class.isAssignableFrom(parameterType)) {
                  args[i] = new JSCallback(jsHost.getWebView(), commandMethod.callback);
                } else if (parameterType.isAssignableFrom(jsHost.getJsHost().getClass())) {
                  args[i] = jsHost.getJsHost();
                } else if (hasAnnotation(parameterAnnotation, JSMessage.class)) {
                  if (String.class.isAssignableFrom(parameterType)) {
                    args[i] = jsonObj.toString();
                  } else if (JsonObject.class.isAssignableFrom(parameterType)) {
                    args[i] = jsonObj;
                  } else {
                    Object obj = new Gson().fromJson(jsonObj, parameterType);
                    args[i] = obj;
                  }
                }
              }
              returnResult = method.invoke(subscriber, args);
            }
            invoke = true;
            if (String.class.equals(returnType)) {
              result = (String) returnResult;
            }
          }
      }
    }
    if (!invoke) {
      throw new IllegalStateException("命令未找到执行方法：" + command);
    }
    return result;
  }

  private <T extends Annotation> boolean hasAnnotation(
      Annotation[] annotations, Class<T> annotationClass) {
    if (annotations == null) return false;
    for (Annotation annotation : annotations) {
      if (annotationClass.isInstance(annotation)) {
        return true;
      }
    }
    return false;
  }
}
