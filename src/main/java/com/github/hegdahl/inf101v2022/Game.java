package com.github.hegdahl.inf101v2022;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Master object for holding a model, view, and controller
 * specified a descendant class.
 */
public abstract class Game {
  
  /**
   * Interface for classes
   * that represent game state,
   * with methods acting on the game state.
   * 
   * <p>The model is given to the constructor
   * for both view and controller.
   */
  public interface Model {

  }

  /**
   * Interface for classes whose
   * job is to create a visual
   * representation of a model
   * for a specific player.
   */
  public abstract class View {

    /**
     * Number in [0, numberOfPlayers)
     * representing which user uses
     * this view to request frames.
     */
    protected final int userIndex;

    /**
     * Creates a View for the given user.
     * 
     * @param userIndex number in [0, numberOfPlayers)
     *                  representing which user uses
     *                  this view to request frames
     */
    public View(int userIndex) {
      this.userIndex = userIndex;
    }

    /**
     * Creates frames of the game state of the model
     * (should be stored by the descendant
     * class to avoid type casting).
     * 
     * @param canvas where to store the frame
     */
    public abstract void paint(ScreenBuffer canvas);

  }

  /**
   * Interface for classes whose
   * job is to map key input from
   * a specific user to state modifyin
   * methods on a model.
   */
  public abstract class Controller {

    /**
     * Number in [0, numberOfPlayers)
     * representing which user is responsible
     * for keystrokes sent to this controller.
     */
    protected final int userIndex;

    private Map<KeyStroke, List<Supplier<Boolean>>> keybinds;

    /**
     * Constructs the controller for the given user.
     * 
     * @param userIndex number in [0, numberOfPlayers)
     *                  representing which user is responsible
     *                  for keystrokes sent to this controller.
     */
    public Controller(int userIndex) {
      this.userIndex = userIndex;
      keybinds = new HashMap<>();
    }

    /**
     * Binds an action to a key.
     * 
     * <p>The action should return true
     * if it succeeded, and false otherwise
     * to allow more actions to be bound to
     * the same key.
     * 
     * @param keyStroke the key that triggers will trigger this action
     * @param action    function object containing the method
     */
    protected void registerKeybind(KeyStroke keyStroke, Supplier<Boolean> action) {
      keybinds.put(keyStroke, new ArrayList<>());
      keybinds.get(keyStroke).add(action);
    }

    /**
     * Tell the controller a key was hit
     * attempt doing actions bound to that key
     * until one returns true indicating success.
     * 
     * @param keyStroke the key that was hit
     */
    public boolean triggerKey(KeyStroke keyStroke) {
      List<Supplier<Boolean>> actions = keybinds.get(keyStroke);
      if (actions != null) {
        for (Supplier<Boolean> action : actions) {
          if (action.get()) {
            return true;
          }
        }
      }
      return false;
    }

  }

  /**
   * Wrapper for the constructor of the
   * `Model` implementation of the game.
   * 
   * <p>It is used to allow type safe
   * dynamic loading of game rules.
   */
  protected abstract Model makeModel(int numberOfPlayers);

  /**
   * Wrapper for the constructor of the
   * `View` implementation of the game.
   * 
   * <p>It is used to allow type safe
   * dynamic loading of game rules.
   * 
   * @param model       reference to the model this view should
   *                    create frames for.
   * @param playerIndex number in [0, numberOfPlayers)
   *                    representing the player that the
   *                    created view should make frames for
   */
  protected abstract View makeView(Model model, int playerIndex);

  /**
   * Wrapper for the constructor of the
   * `Controller` implementation of the game.
   * 
   * <p>It is used to allow type safe
   * dynamic loading of game rules.
   * 
   * @param model       reference to the model this
   *                    view should do request actions on
   * @param playerIndex number in [0, numberOfPlayers)
   *                    representing the player that the
   *                    created controller should act as
   */
  protected abstract Controller makeController(Model model, int playerIndex);

  /**
   * Sets a minimum for how many players
   * there can be in a single game.
   */
  public abstract int minPlayers();

  /**
   * Sets a maximum for how many players
   * there can be in a single game.
   */
  public abstract int maxPlayers();
  
  private Model model;
  private View[] views;
  private Controller[] controllers;

  private Map<Integer, String> usernames;
  private Set<Integer> readyUsers;
  private Map<Integer, Integer> userIndieces;

  long stateVersion = 0;
  long[] lastSeenVersion;
  Object stateVersionTrigger = new Object();

  private final synchronized void init() {
    if (model != null) {
      throw new IllegalStateException("Game already started.");
    }

    if (readyUsers.size() != usernames.size()) {
      throw new IllegalStateException("Not all users are ready.");
    }

    int numberOfPlayers = usernames.size();

    if (minPlayers() > numberOfPlayers || numberOfPlayers > maxPlayers()) {
      throw new IllegalStateException(String.format(
          "Number of players must be in [%s, %s].", minPlayers(), maxPlayers()));
    }

    for (int id : usernames.keySet()) {
      int userIndex = userIndieces.size();
      userIndieces.put(id, userIndex);
    }

    model = makeModel(numberOfPlayers);
    views = new View[numberOfPlayers];
    controllers = new Controller[numberOfPlayers];

    for (int playerIndex = 0; playerIndex < numberOfPlayers; ++playerIndex) {
      views[playerIndex] = makeView(model, playerIndex);
      controllers[playerIndex] = makeController(model, playerIndex);
    }

    lastSeenVersion = new long[numberOfPlayers];
  }

  /**
   * Constructs the game in
   * a lobby-like state where
   * new connections are allowed
   * until the game starts.
   */
  public Game() {
    usernames = new HashMap<>();
    readyUsers = new HashSet<>();
    userIndieces = new HashMap<>();
  }

  /**
   * Tell the threads that are waiting
   * for a new frame that the game
   * state chnaged (and is thus in a new version).
   */
  private final void incrementVersion() {
    synchronized (stateVersionTrigger) {
      ++stateVersion;
      stateVersionTrigger.notifyAll();
    }
  }

  /**
   * Report that a key was pressed.
   * 
   * @param id        unique id for the user that pressed the key
   * @param keyStroke the key that was pressed
   */
  public final synchronized void triggerKey(int id, KeyStroke keyStroke) {
    if (model == null) {
      // waiting for game to start

      if (keyStroke.getKeyType() == KeyType.Enter) {
        // toggle readiness

        if (!readyUsers.add(id)) {
          readyUsers.remove(id);
        }

        if (readyUsers.size() == usernames.size()
            && minPlayers() <= readyUsers.size()
            && readyUsers.size() <= maxPlayers()) {
          init();
        }

        incrementVersion();
      }

    } else {

      int userIndex = userIndieces.get(id);
      if (controllers[userIndex].triggerKey(keyStroke)) {
        incrementVersion();
      }

    }
  }

  /**
   * Request for a new frame
   * to be stored in canvas.
   * 
   * <p>Blocks until there is new game state.
   * 
   * @param id              unique id for the user requesting the frame
   * @param canvas          where to store the frame.
   * @param lastSeenVersion number representing the game state
   *                        at the last frame given to this user
   */
  public final long paint(int id, ScreenBuffer canvas, long lastSeenVersion)
      throws InterruptedException {

    synchronized (stateVersionTrigger) {
      while (lastSeenVersion == stateVersion) {
        stateVersionTrigger.wait();
      }
    }

    synchronized (this) {

      if (model == null) {

        // waiting for game to start
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<ScreenBuffer.Color> foregrounds = new ArrayList<>();
        ArrayList<ScreenBuffer.Color> backgrounds = new ArrayList<>();

        lines.add(this.getClass().getName());
        foregrounds.add(new ScreenBuffer.Color(255, 255, 255));
        backgrounds.add(new ScreenBuffer.Color(0, 0, 0));

        lines.add("Press <enter> to toggle readiness");
        foregrounds.add(new ScreenBuffer.Color(0, 0, 0));
        backgrounds.add(new ScreenBuffer.Color(255, 255, 255));

        if (minPlayers() > usernames.size()) {
          lines.add(String.format(
              "Too few players. At least %s are needed.",
              minPlayers()));
          foregrounds.add(new ScreenBuffer.Color(0, 0, 0));
          backgrounds.add(new ScreenBuffer.Color(255, 255, 255));
        } else if (usernames.size() > maxPlayers()) {
          lines.add(String.format(
              "Too many players. At most %s can play.",
              maxPlayers()));
          foregrounds.add(new ScreenBuffer.Color(0, 0, 0));
          backgrounds.add(new ScreenBuffer.Color(255, 255, 255));
        } else if (readyUsers.size() != usernames.size()) {
          lines.add("Waiting for all players to be ready.");
          foregrounds.add(new ScreenBuffer.Color(0, 0, 0));
          backgrounds.add(new ScreenBuffer.Color(255, 255, 255));
        }

        for (Map.Entry<Integer, String> entry : usernames.entrySet()) {
          int otherID = entry.getKey();
          String username = entry.getValue();

          if (otherID == id) {
            username += " (you) are";
          } else {
            username += " is";
          }

          if (readyUsers.contains(otherID)) {
            lines.add(String.format("%s ready", username));
            foregrounds.add(new ScreenBuffer.Color(0, 0, 0));
            backgrounds.add(new ScreenBuffer.Color(50, 255, 50));
          } else {
            lines.add(String.format("%s not ready", username));
            foregrounds.add(new ScreenBuffer.Color(0, 0, 0));
            backgrounds.add(new ScreenBuffer.Color(255, 50, 50));
          }
        }

        int height = lines.size();
        int width = 0;
        for (String line : lines) {
          width = Math.max(width, line.length());
        }

        canvas.clearAndResize(height, width);
        
        for (int i = 0; i < height; ++i) {
          String line = lines.get(i);
          line += " ".repeat(width - line.length());
          canvas.write(i, 0, line, foregrounds.get(i), backgrounds.get(i));
        }

      } else {

        int userIndex = userIndieces.get(id);
        views[userIndex].paint(canvas);

      }

      return stateVersion;
    }
  }

  /**
   * Inform the game that a new user connected.
   * 
   * <p>Does nothing if the game has already started.
   * 
   * @param id       unique id representing the user
   * @param username name picked by the user,
   *                 not neccesarily unique.
   * 
   * @return true if the registration succeeded.
   */
  public final synchronized boolean registerUser(int id, String username) {
    if (model != null) {
      return false;
    }

    usernames.put(id, username);
    incrementVersion();
    return true;
  }

  /**
   * Inform the game that a user disconnected.
   * 
   * @param id unique id representing the user
   */
  public final synchronized void unregisterUser(int id) {
    usernames.remove(id);
    readyUsers.remove(id);

    if (usernames.size() == readyUsers.size()
        && minPlayers() <= usernames.size()
        && usernames.size() <= maxPlayers()) {
      init();
    }

    incrementVersion();
  }

  /**
   * Finds the username for a connected
   * user given the user's unique id.
   */
  public final synchronized String getUsername(int id) {
    return usernames.get(id);
  }

  /**
   * Finds the username for a connected
   * user given the user's unique id.
   */
  public synchronized boolean finished() {
    return false;
  }

}
