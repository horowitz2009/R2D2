package com.horowitz.commons;

import java.io.IOException;

public interface Deserializable {
  
  void deserialize(Deserializer deserializer) throws IOException;
  
}
