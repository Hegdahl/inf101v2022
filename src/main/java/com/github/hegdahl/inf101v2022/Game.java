package com.github.hegdahl.inf101v2022;

import com.googlecode.lanterna.input.KeyStroke;

import java.util.HashMap;
import java.util.Map;

public abstract class Game {
  
  public interface Model {

  }

  public interface View {
    void paint();
  }

  public interface Controller {

  }

  protected abstract Model makeModel(int numberOfPlayers);

  protected abstract View makeView(int playerIndex);

  protected abstract Controller makeController(int playerIndex);

  public abstract int minPlayers();

  public abstract int maxPlayers();
  
  protected Model model;
  protected View[] views;
  protected Controller[] controllers;

  private Map<Integer, String> usernames;

  private boolean gameStarted = false;

  private final synchronized void init() {
    if (gameStarted) {
      throw new IllegalStateException("Game already started");
    }
    gameStarted = true;

    int numberOfPlayers = usernames.size();

    if (minPlayers() > numberOfPlayers || numberOfPlayers > maxPlayers()) {
      throw new IllegalStateException(String.format(
          "Number of players must be in [%s, %s]", minPlayers(), maxPlayers()));
    }

    model = makeModel(numberOfPlayers);
    views = new View[numberOfPlayers];
    controllers = new Controller[numberOfPlayers];

    for (int playerIndex = 0; playerIndex < numberOfPlayers; ++playerIndex) {
      views[playerIndex] = makeView(playerIndex);
      controllers[playerIndex] = makeController(playerIndex);
    }
  }

  public Game() {
    usernames = new HashMap<>();
  }

  public final void trigger(int id, KeyStroke keyStroke) {
    System.err.println("Game::trigger " + id + ":" + keyStroke);
  }

  /**
   * Request for a new frame
   * to be stored in canvas.
   * 
   * @param id     unique id for the user requesting the frame
   * @param canvas where to store the frame.
   */
  public final boolean paint(int id, ScreenBuffer canvas) throws InterruptedException {
    System.err.println("Game::paint " + id + ":" + canvas);
    Thread.sleep(1000);
    return true;
  }

  /**
   * Inform the game that a new user connected.
   * 
   * @param id       unique id representing the user
   * @param username name picked by the user,
   *                 not neccesarily unique.
   */
  public final synchronized void registerUser(int id, String username) {
    usernames.put(id, username);
    System.err.printf("\"%s\" connected.\n", username);
    logOnline();
  }

  /**
   * Inform the game that a user disconnected.
   * 
   * @param id unique id representing the user
   */
  public final synchronized void unregisterUser(int id) {
    System.err.printf("\"%s\" disconnected.\n", usernames.get(id));
    usernames.remove(id);
    logOnline();
  }

  /**
   * Print a list of currently connected users to stderr.
   */
  public final synchronized void logOnline() {
    System.err.println("Currently connected: [");
    for (String username : usernames.values()) {
      System.err.println("    " + username);
    }
    System.err.println("]");
  }

  public final String getUsername(int id) {
    return usernames.get(id);
  }

  public final boolean finished() {
    return false;
  }

}
