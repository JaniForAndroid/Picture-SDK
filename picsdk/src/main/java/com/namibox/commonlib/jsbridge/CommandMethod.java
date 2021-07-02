package com.namibox.commonlib.jsbridge;

import java.lang.reflect.Method;

/**
 * Create time: 2020/4/10.
 */
public class CommandMethod {
  final Method method;
  final String command;
  final String callback;
  String methodString;

  public CommandMethod(Method method, String command, String callback) {
    this.method = method;
    this.command = command;
    this.callback = callback;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof CommandMethod) {
      checkMethodString();
      CommandMethod otherSubscriberMethod = (CommandMethod)other;
      otherSubscriberMethod.checkMethodString();
      return methodString.equals(otherSubscriberMethod.methodString);
    } else {
      return false;
    }
  }

  private synchronized void checkMethodString() {
    if (methodString == null) {
      // Method.toString has more overhead, just take relevant parts of the method
      StringBuilder builder = new StringBuilder(64);
      builder.append(method.getDeclaringClass().getName());
      builder.append('#').append(method.getName());
      builder.append('#').append(command);
      builder.append('#').append(callback);
      methodString = builder.toString();
    }
  }

  @Override
  public String toString() {
    checkMethodString();
    return methodString;
  }

  @Override
  public int hashCode() {
    return method.hashCode();
  }
}
