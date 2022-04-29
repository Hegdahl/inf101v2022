package com.github.hegdahl.inf101v2022;

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

  abstract protected int minPlayers();
  abstract protected int maxPlayers();

  protected Model model;
  protected View[] views;
  protected Controller[] controllers;

  public void init(int numberOfPlayers) {
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

}
