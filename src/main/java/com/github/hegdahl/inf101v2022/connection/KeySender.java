package com.github.hegdahl.inf101v2022.connection;

import java.io.BufferedWriter;
import java.io.IOException;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

public class KeySender extends Thread {

  private Screen screen;
  private BufferedWriter writer;
  private boolean shouldExit = false;

  public KeySender(Screen screen, BufferedWriter writer) {
    this.screen = screen;
    this.writer = writer;
  }

  public void close() {
    shouldExit = true;
  }

  @Override
  public void run() {
    while (!shouldExit) {
      KeyStroke keyStroke = null;
      try {
        keyStroke = screen.pollInput();
      } catch (IOException e) {
      }
      if (keyStroke == null) {
        Thread.yield();
        continue;
      }

      KeyType keyType = keyStroke.getKeyType();
      String keyStr = null;
      if (keyType == KeyType.Character)
        keyStr = "c " + keyStroke.getCharacter() + '\n';
      else
        keyStr = "s " + keyType.ordinal() + '\n';
      
      try {
        writer.write(keyStr);
        writer.flush();
      } catch (IOException e) {
      }
    }
  }

}
