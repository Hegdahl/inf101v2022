package com.github.hegdahl.inf101v2022.connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.github.hegdahl.inf101v2022.Game;
import com.github.hegdahl.inf101v2022.ScreenBuffer;

public class ScreenSender extends Thread {

  int id;
  Game game;
  BufferedWriter writer;
  CountDownLatch onExit;
  boolean shouldExit = false;

  public ScreenSender(int id, Game game, BufferedWriter writer, CountDownLatch onExit) {
    this.id = id;
    this.game = game;
    this.writer = writer;
    this.onExit = onExit;
  }

  public void close() {
    shouldExit = true;
  }
  
  @Override
  public void run() {
    int h = game.screenHeight();
    int w = game.screenWidth();
    ScreenBuffer screen = new ScreenBuffer(h, w);

    try {
      while (!shouldExit) {
        game.paint(id, screen);
        writer.write(h + " " + w + " ");
        for (int i = 0; i < h; ++i)
          for (int j = 0; j < w; ++j)
            writer.write(screen.get(i, j).toString());
        writer.write('\n');
        writer.flush();
      }
    } catch (IOException e) {
      System.err.println("Failed to write to socket " + id + ". Assuming it disconnected");
    } finally {
      onExit.countDown();
    }
  }

}
