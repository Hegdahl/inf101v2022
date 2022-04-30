package com.github.hegdahl.inf101v2022;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import net.sourceforge.argparse4j.inf.Namespace;

public class Join implements Main.SubcommandHandler {

  class KeyListener implements Runnable {
    Screen screen;
    BufferedWriter writer;
    boolean done = false;

    KeyListener(Screen screen, BufferedWriter writer) {
      this.screen = screen;
      this.writer = writer;
    }

    public void run() {
      while (!done) {
        try {
          KeyStroke keyStroke = screen.pollInput();
          if (keyStroke == null) {
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            continue;
          }

          KeyType keyType = keyStroke.getKeyType();
          if (keyType == KeyType.Character)
            writer.write("c " + keyStroke.getCharacter() + '\n');
          else
            writer.write("s " + keyType.ordinal() + '\n');
          writer.flush();
        } catch (IOException e) {
        }
      }
    }
  };

  @Override
  public void main(Namespace ns) throws IOException {

    String username = ns.getString("username");
    for (int i = 0; i < username.length(); ++i)
      if (username.charAt(i) == '\n')
        throw new IllegalArgumentException("Username must not include newlines.");

    String address = ns.getString("address");
    short port = ns.getShort("port");

    Socket socket = new Socket(address, port);
    Scanner reader = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    writer.write(username + '\n');
    writer.flush();

    Screen screen = (new DefaultTerminalFactory()).createScreen();

    screen.startScreen();
    screen.setCursorPosition(null);

    KeyListener keyListener = new KeyListener(screen, writer);
    Thread keyListenerThread = new Thread(keyListener);
    keyListenerThread.start();

    Random random = new Random();
    TerminalSize terminalSize = screen.getTerminalSize();
    for (int column = 0; column < terminalSize.getColumns(); column++) {
      for (int row = 0; row < terminalSize.getRows(); row++) {
        screen.setCharacter(column, row, TextCharacter.fromCharacter(' ',
            TextColor.ANSI.DEFAULT, TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)])[0]);
      }
    }
    screen.refresh();

    mainLoop: while (true) {

      int command = reader.nextInt();

      switch (command) {
        case 0:
          break mainLoop;
      }

    }

    keyListener.done = true;
    try {
      keyListenerThread.join();
    } catch (InterruptedException e) {
    }

    screen.close();

    reader.close();
    writer.close();

    socket.close();
  }

}
