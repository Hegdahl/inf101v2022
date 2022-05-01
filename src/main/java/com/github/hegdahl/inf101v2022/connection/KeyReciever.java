package com.github.hegdahl.inf101v2022.connection;

import com.github.hegdahl.inf101v2022.Game;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Listenes to key events sent by `KeySender`
 * from a connected player on the network
 * and forwards them to the game.
 */
public class KeyReciever extends Thread {

  int id;
  Game game;
  Scanner reader;
  CountDownLatch onExit;

  /**
   * Constructs the KeyReciever without starting
   * the forwarding.
   * 
   * <p>The forwarding is done in the current thread using
   * .run(), or in a new thread usign .start().
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

  /**
   * Listen for keys until interrupted.
   */
  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        
        KeyStroke keyStroke = null;
        String keyStart = reader.next();
        if (keyStart.equals("c")) {
          keyStroke = KeyStroke.fromString(reader.next());
        } else if (keyStart.equals("s")) {
          keyStroke = new KeyStroke(KeyType.values()[reader.nextInt()]);
        } else {
          keyStroke = KeyStroke.fromString(" ");
        }
        
        game.triggerKey(id, keyStroke);
      }
    } catch (NoSuchElementException e) {
      System.err.println("Failed to read from socket " + id + ". Assuming it disconnected");
    } finally {
      onExit.countDown();
    }
  }
}
