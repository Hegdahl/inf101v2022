package com.github.hegdahl.inf101v2022;

import com.github.hegdahl.inf101v2022.connection.KeyReciever;
import com.github.hegdahl.inf101v2022.connection.ScreenSender;

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

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Used when the program is started with the `host` subcommand.
 * 
 * <p>Starts a server accepting connections from players.
 */
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

      if (!game.registerUser(id, username)) {
        System.err.println("Could not register the user.");
        reader.close();
        try {
          writer.close();
        } catch (IOException e) {
          System.err.println("Failed closing writer.");
        }
        try {
          socket.close();
        } catch (IOException e) {
          System.err.println("Failed closing socket.");
        }
        return;
      }

      KeyReciever keyRecieverThread = new KeyReciever(id, game, reader, exitLatch);
      ScreenSender screenSenderThread = new ScreenSender(id, game, writer, exitLatch);

      keyRecieverThread.start();
      screenSenderThread.start();

      try {

        try {
          exitLatch.await();
        } catch (InterruptedException e) {
          interrupt();
          return;
        } finally {
          System.err.printf("\"%s\" disconnected.\n",
              game.getUsername(id));
          screenSenderThread.interrupt();
          keyRecieverThread.interrupt();
        }

        try {
          screenSenderThread.join();
        } catch (InterruptedException e) {
          interrupt();
          return;
        }

        try {
          keyRecieverThread.join();
        } catch (InterruptedException e) {
          interrupt();
          return;
        }

      } finally {
        game.unregisterUser(id);
      }
    }
  }

  @Override
  public void main(Namespace ns) {

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

    URL rulesUrl = null;
    try {
      rulesUrl = rulesFile.getParentFile().toURI().toURL();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.exit(1);
    }

    URLClassLoader classLoader = null;
    classLoader = URLClassLoader.newInstance(new URL[] { rulesUrl });

    Game game = null;
    try {
      String rulesClassName = rulesFile.getName();
      int dotPosition = rulesClassName.lastIndexOf('.');
      rulesClassName = rulesClassName.substring(0, dotPosition);

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

    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.exit(1);
    }
    System.out.println("Listening on port " + port);

    ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    while (!game.finished()) {
      try {
        ClientHandler clientHandler = new ClientHandler(game, serverSocket.accept());
        clientHandler.start();
        clientHandlers.add(clientHandler);
      } catch (IOException e) {
        System.err.println(e);
      }
    }

    for (ClientHandler clientHandler : clientHandlers) {
      clientHandler.close();
    }

    for (ClientHandler clientHandler : clientHandlers) {
      try {
        clientHandler.join();
      } catch (InterruptedException e) {
        System.err.printf("%s was interrupted.", clientHandler.id);
      }
    }

    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println("Failed closing server.");
    }
  }
}
