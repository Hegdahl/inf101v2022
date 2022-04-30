package com.github.hegdahl.inf101v2022.connection;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

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
      while (true) {
        TerminalSize newTerminalSize = screen.doResizeIfNecessary();
        if (newTerminalSize != null)
          terminalSize = newTerminalSize;

        int h = reader.nextInt();
        int w = reader.nextInt();

        int uPad = (terminalSize.getRows() - h) / 2;
        int lPad = (terminalSize.getColumns() - w) / 2;

        for (int i = 0; i < h; ++i)
          for (int j = 0; j < w; ++j) {
            char c = (char) reader.nextInt();
            TextColor.RGB fg = new TextColor.RGB(
                reader.nextInt(), reader.nextInt(), reader.nextInt());
            TextColor.RGB bg = new TextColor.RGB(
                reader.nextInt(), reader.nextInt(), reader.nextInt());
            screen.setCharacter(lPad + j, uPad + i,
                TextCharacter.fromCharacter(c, fg, bg)[0]);
          }

        try {
          screen.refresh();
        } catch (IOException e) {
        }
        Thread.yield();
      }
    } catch (NoSuchElementException e) {
    }
  }
}
