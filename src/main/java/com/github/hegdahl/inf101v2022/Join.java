package com.github.hegdahl.inf101v2022;

import com.github.hegdahl.inf101v2022.connection.KeySender;
import com.github.hegdahl.inf101v2022.connection.ScreenReciever;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import net.sourceforge.argparse4j.inf.Namespace;

public class Join implements Main.SubcommandHandler {

  @Override
  public void main(Namespace ns) {

    String username = ns.getString("username");
    for (int i = 0; i < username.length(); ++i) {
      if (username.charAt(i) == '\n') {
        throw new IllegalArgumentException("Username must not include newlines.");
      }
    }

    String address = ns.getString("address");
    short port = ns.getShort("port");

    Screen screen = null;
    try {
      screen = (new DefaultTerminalFactory()).createScreen();
      screen.startScreen();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed starting screen.");
      System.exit(1);
    }

    screen.setCursorPosition(null);

    Socket socket = null;
    try {
      socket = new Socket(address, port);
    } catch (IOException e) {
      System.err.println(e);
      System.err.printf("Failed connecting to %s:%s.\n", address, port);
      System.exit(1);
    }

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(
        new OutputStreamWriter(socket.getOutputStream()));
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed creating stream writer.");
      System.exit(1);
    }

    try {
      writer.write(username + '\n');
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed registering.");
      System.exit(1);
    }
    KeySender keySender = new KeySender(screen, writer);
    keySender.start();

    Scanner reader = null;
    try {
      reader = new Scanner(new BufferedReader(
        new InputStreamReader(socket.getInputStream())));
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed creating stream reader.");
      System.exit(1);
    }

    ScreenReciever screenReciever = new ScreenReciever(screen, reader);
    screenReciever.start();

    try {
      screenReciever.join();
    } catch (InterruptedException e) {
      System.err.println("screenReciever was interrupted.");
    }

    try {
      keySender.close();
      keySender.join();
    } catch (InterruptedException e) {
      System.err.println("keySender was interrupted.");
    }

    boolean closingOK = true;

    try {
      screen.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed closing screen.");
      closingOK = false;
    }

    reader.close();

    try {
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed closing writer.");
      closingOK = false;
    }

    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed closing socket.");
      closingOK = false;
    }

    if (!closingOK) {
      System.exit(1);
    }
  }

}
