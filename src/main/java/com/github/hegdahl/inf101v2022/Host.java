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
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import com.github.hegdahl.inf101v2022.connection.KeyReciever;
import com.github.hegdahl.inf101v2022.connection.ScreenSender;

import net.sourceforge.argparse4j.inf.Namespace;

public class Host implements Main.SubcommandHandler {

  class ClientHandler extends Thread {
    private static int nextID = 0;

    public final int id;
    private Game game;
    private Socket socket;
    private CountDownLatch exitLatch;

    public ClientHandler(Game game, Socket socket) {
      id = nextID++;
      this.game = game;
      this.socket = socket;
      exitLatch = new CountDownLatch(1);
    }

    public void close() {
      exitLatch.countDown();
    }

    @Override
    public void run() {
      Scanner reader = null;
      BufferedWriter writer = null;
      try {
        reader = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      } catch (IOException e) {
        System.err.println("Failed opening streams for " + id + ".");
        return;
      }

      String username = reader.nextLine();
      System.err.printf("\"%s\" connected from %s:%s\n",
          username, socket.getInetAddress(), socket.getPort());

      game.setUsername(id, username);
      KeyReciever keyRecieverThread = new KeyReciever(id, game, reader);
      ScreenSender screenSenderThread = new ScreenSender(id, game, writer);

      keyRecieverThread.start();
      screenSenderThread.start();

      try {
        exitLatch.await();
      } catch (InterruptedException e) {
      }

      screenSenderThread.close();
      keyRecieverThread.close();

      try {
        screenSenderThread.join();
      } catch (InterruptedException e) {
      }

      try {
        keyRecieverThread.join();
      } catch (InterruptedException e) {
      }
    }
  }

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

    short port = ns.getShort("port");

    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("Listening on port " + port);

    ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    while (!game.finished()) {
      ClientHandler clientHandler = new ClientHandler(game, serverSocket.accept());
      clientHandler.start();
      clientHandlers.add(clientHandler);
    }

    for (ClientHandler clientHandler : clientHandlers)
      clientHandler.close();

    for (ClientHandler clientHandler : clientHandlers) {
      try {
        clientHandler.join();
      } catch (InterruptedException e) {
      }
    }

    serverSocket.close();
  }
}
