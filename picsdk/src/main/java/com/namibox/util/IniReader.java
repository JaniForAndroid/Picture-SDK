package com.namibox.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IniReader {

  /**
   * 整个ini的引用
   */
  private Map<String, Map<String, List<String>>> map = null;
  /**
   * 当前Section的引用
   */
  private String currentSection = null;

  /**
   * 读取
   */
  public IniReader(String path) {
    map = new HashMap<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(path));
      read(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public IniReader(InputStream inputStream) {
    map = new HashMap<>();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      read(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * 读取文件
   */
  private void read(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      parseLine(line);
    }
  }

  /**
   * 转换
   */
  private void parseLine(String line) {
    line = line.replaceAll(" ", "");
    if (line.matches("^\\#.*$")) {
      //此部分为注释
    } else if (line.matches("^\\[\\S+\\]$")) {
      // section
      String section = line.replaceFirst("^\\[(\\S+)\\]$", "$1");
      addSection(map, section);
    } else if (line.matches("^\\S+=.*$")) {
      // key ,value
      int i = line.indexOf("=");
      String key = line.substring(0, i).trim();
      String value = line.substring(i + 1).trim();
      addKeyValue(map, currentSection, key, value);
    }
  }


  /**
   * 增加新的Key和Value
   */
  private void addKeyValue(Map<String, Map<String, List<String>>> map,
      String currentSection, String key, String value) {
    if (!map.containsKey(currentSection)) {
      return;
    }

    Map<String, List<String>> childMap = map.get(currentSection);

    if (!childMap.containsKey(key)) {
      List<String> list = new ArrayList<>();
      list.add(value);
      childMap.put(key, list);
    } else {
      childMap.get(key).add(value);
    }
  }


  /**
   * 增加Section
   */
  private void addSection(Map<String, Map<String, List<String>>> map,
      String section) {
    if (!map.containsKey(section)) {
      currentSection = section;
      Map<String, List<String>> childMap = new HashMap<>();
      map.put(section, childMap);
    }
  }

  /**
   * 获取配置文件指定Section和指定子键的值
   */
  public List<String> get(String section, String key) {
    if (map.containsKey(section)) {
      return get(section).containsKey(key) ?
          get(section).get(key) : null;
    }
    return null;
  }


  /**
   * 获取配置文件指定Section的子键和值
   */
  public Map<String, List<String>> get(String section) {
    return map.containsKey(section) ? map.get(section) : null;
  }

  /**
   * 获取这个配置文件的节点和值
   */
  public Map<String, Map<String, List<String>>> get() {
    return map;
  }

}
