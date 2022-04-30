package com.github.hegdahl.inf101v2022.connection;

import com.github.hegdahl.inf101v2022.Game;
import com.github.hegdahl.inf101v2022.ScreenBuffer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Sends frames to a `ScreenReciever`
 * over the network.
 */
public class ScreenSender extends Thread {

  int id;
  Game game;
  BufferedWriter writer;
  CountDownLatch onExit;

  /**
   * Constructs the ScreenSender without starting
   * the forwarding.
   * 
   * <p>The forwarding is done in the current thread using
   * .run(), or in a new thread usign .start().
   * 
   * @param id     unique id for the connection
   *               the screen is sent to
   * @param game   the game to get a visual representation of
   * @param writer Stream object to send the screen data to
   * @param onExit Latch used as a callback to tell
   *               invoker that the the loop finished
   */
  public ScreenSender(int id, Game game, BufferedWriter writer, CountDownLatch onExit) {
    this.id = id;
    this.game = game;
    this.writer = writer;
    this.onExit = onExit;
  }

  /**
   * Send frames until interrupted.
   */
  @Override
  public void run() {
    ScreenBuffer screen = new ScreenBuffer();
    long lastSeenVersion = -1;

    try {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          lastSeenVersion = game.paint(id, screen, lastSeenVersion);
        } catch (InterruptedException e) {
          interrupt();
          return;
        }

        int h = screen.height();
        int w = screen.width();
        writer.write(String.format("%s %s ", h, w));
        for (int i = 0; i < h; ++i) {
          for (int j = 0; j < w; ++j) {
            writer.write(screen.get(i, j) + " ");
          }
        }
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
