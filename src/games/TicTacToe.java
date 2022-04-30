import com.github.hegdahl.inf101v2022.Game;

import java.io.Console;

public class TicTacToe extends Game {

  @Override
  public int minPlayers() {
    return 2;
  }

  @Override
  public int maxPlayers() {
    return 2;
  }

  @Override
  public int screenHeight() {
    return 3;
  }

  @Override
  public int screenWidth() {
    return 6;
  }

  class Model implements Game.Model {

    enum PlayerTag { NONE, X, O }

    PlayerTag[][] board;
    
    Model(int numberOfPlayers) {
      assert numberOfPlayers == 2;
      board = new PlayerTag[3][3];
    }

    private boolean checkWon(PlayerTag player) {

      outer: for (int row = 0; row < 3; ++row) {
        for (int col = 0; col < 3; ++col) {
          if (board[row][col] != player) {
            continue outer;
          }
        }
        return true;
      }

      outer: for (int col = 0; col < 3; ++col) {
        for (int row = 0; row < 3; ++row) {
          if (board[row][col] != player) {
            continue outer;
          }
        }
        return true;
      }

      boolean diag = true;
      for (int i = 0; i < 3; ++i) {
        diag &= board[i][i] == player;
      }
      if (diag) {
        return true;
      }

      diag = true;
      for (int i = 0; i < 3; ++i) {
        diag &= board[i][3 - i - 1] == player;
      }
      if (diag) {
        return true;
      }

      return false;
    }

    private PlayerTag winner() {
      boolean playerXWon = checkWon(PlayerTag.X);
      boolean playerOWon = checkWon(PlayerTag.O);

      if (playerXWon && playerOWon) {
        throw new IllegalStateException("Both players completed 3 in a row.");
      }
      
      if (playerXWon) {
        return PlayerTag.X;
      }

      if (playerOWon) {
        return PlayerTag.O;
      }

      return PlayerTag.NONE;
    }

  }

  class View implements Game.View {

    final int playerIndex;

    View(int playerIndex) {
      this.playerIndex = playerIndex;
    }
    
    public void paint() {

    }

  }

  class Controller implements Game.Controller {
    
    final int playerIndex;

    Controller(int playerIndex) {
      this.playerIndex = playerIndex;
    }

  }

  @Override
  protected Game.Model makeModel(int numberOfPlayers) {
    return new Model(numberOfPlayers);
  }

  @Override
  protected Game.View makeView(int playerIndex) {
    return new View(playerIndex);
  }

  @Override
  protected Game.Controller makeController(int playerIndex) {
    return new Controller(playerIndex);
  }
}
