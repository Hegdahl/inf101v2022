package com.github.hegdahl.inf101v2022.connection;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ScreenReciever extends Thread {

  Screen screen;
  Scanner reader;

  public ScreenReciever(Screen screen, Scanner reader) {
    this.screen = screen;
    this.reader = reader;
  }

  @Override
  public void run() {
    TerminalSize terminalSize = screen.getTerminalSize();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        TerminalSize newTerminalSize = screen.doResizeIfNecessary();
        if (newTerminalSize != null) {
          terminalSize = newTerminalSize;
        }

        screen.clear();

        int h = reader.nextInt();
        int w = reader.nextInt();

        int upPad = Math.max((terminalSize.getRows() - h) / 2, 0);
        int leftPad = Math.max((terminalSize.getColumns() - w) / 2, 0);

        for (int i = 0; i < h; ++i) {
          for (int j = 0; j < w; ++j) {
            char c = (char) reader.nextInt();
            TextColor.RGB fg = new TextColor.RGB(
                reader.nextInt(), reader.nextInt(), reader.nextInt());
            TextColor.RGB bg = new TextColor.RGB(
                reader.nextInt(), reader.nextInt(), reader.nextInt());
            screen.setCharacter(leftPad + j, upPad + i,
                TextCharacter.fromCharacter(c, fg, bg)[0]);
          }
        }

        try {
          screen.refresh();
        } catch (IOException e) {
          System.err.println(e);
          System.err.println("Failed refreshing the screen.");
        }
        Thread.yield();
      }
    } catch (NoSuchElementException e) {
      System.err.println("Failed reading new frame. Assuming the game finished.");
    }
  }
}
