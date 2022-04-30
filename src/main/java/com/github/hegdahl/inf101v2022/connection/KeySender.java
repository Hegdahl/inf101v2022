package com.github.hegdahl.inf101v2022.connection;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Forward key events to the host's
 * `KeyReciever` over the network.
 */
public class KeySender extends Thread {

  private Screen screen;
  private BufferedWriter writer;

  /**
   * Constructs the KeyReciever without starting
   * the forwarding.
   * 
   * <p>The forwarding is done in the current thread using
   * .run(), or in a new thread usign .start().
   * 
   * @param screen lanterna Screen object to listen to key events from.
   * @param writer stream object to write the key strokes to.
   */
  public KeySender(Screen screen, BufferedWriter writer) {
    this.screen = screen;
    this.writer = writer;
  }

  /**
   * Send keystrokes to the server until interrupted.
   */
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
