package com.horowitz.commons;

import java.io.IOException;

public interface Deserializer {

  void deserialize(Deserializable deserializable) throws IOException;
}
