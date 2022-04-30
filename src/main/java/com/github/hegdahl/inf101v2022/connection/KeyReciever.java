package com.github.hegdahl.inf101v2022.connection;

import com.github.hegdahl.inf101v2022.Game;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class KeyReciever extends Thread {

  int id;
  Game game;
  Scanner reader;
  boolean shouldExit = false;
  CountDownLatch onExit;

  /**
   * Listenes to key events from a connected player.
   * 
   * @param id     unique id for the connection
   *               keys are lstened to from
   * @param game   which game to report key events to
   * @param reader stream object to read key events from
   * @param onExit Latch used as a callback to tell
   *               invoker that the the loop finished
   */
  public KeyReciever(int id, Game game, Scanner reader, CountDownLatch onExit) {
    this.id = id;
    this.game = game;
    this.reader = reader;
    this.onExit = onExit;
  }

  public void close() {
    shouldExit = true;
  }

  @Override
  public void run() {
    try {
      while (!shouldExit) {
        
        KeyStroke keyStroke = null;
        if (reader.next().equals("c")) {
          keyStroke = KeyStroke.fromString(reader.next());
        } else {
          keyStroke = new KeyStroke(KeyType.values()[reader.nextInt()]);
        }
        
        game.trigger(id, keyStroke);
      }
    } catch (NoSuchElementException e) {
      System.err.println("Failed to read from socket " + id + ". Assuming it disconnected");
    } finally {
      onExit.countDown();
    }
  }
}
