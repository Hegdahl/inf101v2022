package com.github.hegdahl.inf101v2022.connection;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import com.github.hegdahl.inf101v2022.Game;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public class KeyReciever extends Thread {

  int id;
  Game game;
  Scanner reader;
  boolean shouldExit = false;
  CountDownLatch onExit;

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
        if (reader.next().equals("c"))
          keyStroke = KeyStroke.fromString(reader.next());
        else
          keyStroke = new KeyStroke(KeyType.values()[reader.nextInt()]);
        
        game.trigger(id, keyStroke);

      }
    } catch (NoSuchElementException e) {
      System.err.println("Failed to read from socket " + id + ". Assuming it disconnected");
    } finally {
      onExit.countDown();
    }
  }
}
