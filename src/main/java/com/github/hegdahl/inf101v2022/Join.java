package com.github.hegdahl.inf101v2022;

import com.github.hegdahl.inf101v2022.connection.KeySender;
import com.github.hegdahl.inf101v2022.connection.ScreenReciever;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import net.sourceforge.argparse4j.inf.Namespace;

public class Join implements Main.SubcommandHandler {

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

    KeySender keySender = new KeySender(screen, writer);
    ScreenReciever screenReciever = new ScreenReciever(screen, reader);

    keySender.start();
    screenReciever.start();

    try {
      screenReciever.join();
    } catch (InterruptedException e) {
    }

    try {
      keySender.close();
      keySender.join();
    } catch (InterruptedException e) {
    }

    screen.close();

    reader.close();
    writer.close();

    socket.close();
  }

}
