package com.horowitz.commons;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSettings<T> {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();
private String filename;
  
  public void loadSettings() throws IOException {
    String json = FileUtils.readFileToString(new File(filename));
    //T result = gson.fromJson(json, );
  }
  

}
