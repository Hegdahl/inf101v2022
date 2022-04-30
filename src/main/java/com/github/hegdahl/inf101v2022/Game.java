package com.github.hegdahl.inf101v2022;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.lanterna.input.KeyStroke;

public abstract class Game {
  
  public interface Model {

  }

  public interface View {
    void paint();
  }

  public interface Controller {

  }

  abstract protected Model makeModel(int numberOfPlayers);
  abstract protected View makeView(int playerIndex);
  abstract protected Controller makeController(int playerIndex);

  abstract public int minPlayers();
  abstract public int maxPlayers();
  
  abstract public int screenWidth();
  abstract public int screenHeight();

  protected Model model;
  protected View[] views;
  protected Controller[] controllers;

  private Map<Integer, String> usernames;

  public final void init(int numberOfPlayers) {
    if (minPlayers() > numberOfPlayers || numberOfPlayers > maxPlayers())
      throw new IllegalArgumentException("Number of players must be in [0, 1]");

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

  public final boolean paint(int id, ScreenBuffer canvas) {
    System.err.println("Game::paint " + id + ":" + canvas);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
    return true;
  }

  public final void registerUser(int id, String username) {
    usernames.put(id, username);
    System.err.printf("\"%s\" connected.\n", username);
    logOnline();
  }

  public final void unregisterUser(int id) {
    System.err.printf("\"%s\" disconnected.\n", usernames.get(id));
    usernames.remove(id);
    logOnline();
  }

  public final void logOnline() {
    System.err.println("Currently connected: [");
    for (String username : usernames.values())
      System.err.println("    " + username);
    System.err.println("]");
  }

  public final String getUsername(int id) {
    return usernames.get(id);
  }

  public final boolean finished() {
    return false;
  }

}
