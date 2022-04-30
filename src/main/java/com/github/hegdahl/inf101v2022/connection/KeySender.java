package com.github.hegdahl.inf101v2022.connection;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.io.BufferedWriter;
import java.io.IOException;

public class KeySender extends Thread {

  private Screen screen;
  private BufferedWriter writer;

  public KeySender(Screen screen, BufferedWriter writer) {
    this.screen = screen;
    this.writer = writer;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      KeyStroke keyStroke = null;
      try {
        keyStroke = screen.pollInput();
      } catch (IOException e) {
        System.err.println(e);
        System.err.println("Failed reading keystroke.");
      }
      if (keyStroke == null) {
        Thread.yield();
        continue;
      }

      KeyType keyType = keyStroke.getKeyType();
      String keyStr = null;
      if (keyType == KeyType.Character) {
        keyStr = "c " + keyStroke.getCharacter() + '\n';
      } else {
        keyStr = "s " + keyType.ordinal() + '\n';
      }
      
      try {
        writer.write(keyStr);
        writer.flush();
      } catch (IOException e) {
        System.err.println(e);
        System.err.println("Failed sending keystroke.");
      }
    }
  }

}
