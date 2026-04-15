package com.aidom.api.global.config;

public final class ElasticsearchIndexConstants {

  public static final String FACILITY_INDEX_ALIAS = "facilities";
  public static final String FACILITY_INDEX_PREFIX = "facilities-v";
  public static final String FACILITY_INDEX_VERSION = "1";
  public static final String FACILITY_PRIMARY_INDEX =
      FACILITY_INDEX_PREFIX + FACILITY_INDEX_VERSION;

  private ElasticsearchIndexConstants() {}
}
