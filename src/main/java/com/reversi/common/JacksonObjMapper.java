package com.reversi.common;

import com.fasterxml.jackson.databind.ObjectMapper;

// the jackson object mapper singleton
public class JacksonObjMapper {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static ObjectMapper get() { return mapper; }
}
