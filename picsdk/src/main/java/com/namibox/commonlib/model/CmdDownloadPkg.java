package com.namibox.commonlib.model;

import java.util.List;

/**
 * Create time: 2020/4/13.
 */
public class CmdDownloadPkg extends BaseCmd {
  public String packageName;
  public String downloadurl;
  public List<String> packages;
}
