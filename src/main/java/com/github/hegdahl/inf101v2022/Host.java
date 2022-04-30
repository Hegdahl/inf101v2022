package com.github.hegdahl.inf101v2022;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import net.sourceforge.argparse4j.inf.Namespace;

public class Host implements Main.SubcommandHandler {

  class ClientHandler implements Runnable {
    Game game;
    String username;
    Scanner reader;
    BufferedWriter writer;

    ClientHandler(Game game, String username, Scanner reader, BufferedWriter writer) {
      this.game = game;
      this.username = username;
      this.reader = reader;
      this.writer = writer;
    }

    public void run() {
      try {

        boolean isChar = reader.next().equals("c");

        KeyStroke keyStroke = null;
        if (isChar)
          keyStroke = KeyStroke.fromString(reader.next());
        else
          keyStroke =new KeyStroke(KeyType.values()[reader.nextInt()]);
          
        System.err.println(keyStroke);

        writer.write("0\n");
        writer.flush();

        reader.close();
        writer.close();
      } catch (IOException e) {
      }
    }

  };

  @Override
  public void main(Namespace ns) throws IOException {

    String rulesPath = ns.getString("rulesPath");
    System.err.println("rulesPath: " + rulesPath);

    File rulesFile = new File(rulesPath);
    if (!rulesFile.exists()) {
      System.err.println("Error: Could not find file '" + rulesFile + "'");
      System.exit(1);
    }

    if (!rulesFile.isFile()) {
      System.err.println("Error: '" + rulesFile + "' is not a file.");
      System.exit(1);
    }

    URL rulesURL = null;
    try {
      rulesURL = rulesFile.getParentFile().toURI().toURL();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.exit(1);
    }

    URLClassLoader classLoader = null;
    classLoader = URLClassLoader.newInstance(new URL[] { rulesURL });

    Game game = null;
    try {
      String rulesClassName = "TicTacToe";
      Class<?> rulesClass = classLoader.loadClass(rulesClassName);
      Constructor<?> constructor = rulesClass.getConstructor();
      game = (Game) constructor.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println(e.getCause());
      System.exit(1);
    }

    final int minPlayers = game.minPlayers();
    final int maxPlayers = game.maxPlayers();

    short port = ns.getShort("port");

    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("Listening on port " + port);

    ArrayList<Thread> threads = new ArrayList<>();

    AtomicInteger nConnected = new AtomicInteger(0);
    AtomicInteger nReady = new AtomicInteger(0);

    Supplier<Boolean> ready = () -> {
      int num = nReady.get();
      int den = nConnected.get();
      if (num != den)
        return false;
      return minPlayers <= num && num <= maxPlayers;
    };

    while (!ready.get()) {
      Socket clientSocket = serverSocket.accept();
      Scanner reader = new Scanner(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

      try {
        String username = reader.nextLine();
        System.err.printf("\"%s\" connected from %s:%s\n",
            username, clientSocket.getInetAddress(), clientSocket.getPort());

        Thread thread = new Thread(new ClientHandler(game, username, reader, writer));
        thread.start();
        threads.add(thread);
      } catch (NoSuchElementException e) {
      } catch (IllegalStateException e) {
      }
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
      }
    }

    serverSocket.close();
  }
}
