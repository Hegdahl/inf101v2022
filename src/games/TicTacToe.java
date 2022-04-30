import com.github.hegdahl.inf101v2022.Game;
import com.github.hegdahl.inf101v2022.ScreenBuffer;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.function.Supplier;

public class TicTacToe extends Game {

  @Override
  public int minPlayers() {
    return 2;
  }

  @Override
  public int maxPlayers() {
    return 2;
  }

  class Model implements Game.Model {

    enum PlayerTag { X, O, NONE }

    PlayerTag[][] board;
    
    Model(int numberOfPlayers) {
      assert numberOfPlayers == 2;
      board = new PlayerTag[3][3];
      for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 3; ++j) {
          board[i][j] = PlayerTag.NONE;
        }
      }
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

    boolean myTurn(int userIndex) {
      if (winner() != PlayerTag.NONE) {
        return false;
      }

      int emptyCount = 0;
      for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 3; ++j) {
          if (board[i][j] == PlayerTag.NONE) {
            emptyCount += 1;
          }
        }
      }

      if (emptyCount == 0) {
        return false;
      }
      
      return (3 * 3 - emptyCount) % 2 == userIndex;
    }

    public boolean attemptTurn(int userIndex, int row, int column) {
      if (!myTurn(userIndex)) {
        return false;
      }

      if (board[row][column] != PlayerTag.NONE) {
        return false;
      }

      board[row][column] = PlayerTag.values()[userIndex];
      return true;
    }
  }

  class View extends Game.View {

    private Model model;

    View(Model model, int userIndex) {
      super(userIndex);
      this.model = model;
    }

    @Override
    public void paint(ScreenBuffer canvas) {
      canvas.clearAndResize(10, 15);

      Model.PlayerTag winner = model.winner();

      ScreenBuffer.Color white = new ScreenBuffer.Color(255, 255, 255);
      ScreenBuffer.Color black = new ScreenBuffer.Color(0, 0, 0);
      ScreenBuffer.Color red = new ScreenBuffer.Color(255, 50, 50);
      ScreenBuffer.Color green = new ScreenBuffer.Color(50, 255, 50);
      ScreenBuffer.Color blue = new ScreenBuffer.Color(50, 50, 255);

      if (winner == Model.PlayerTag.NONE) {
        if (model.myTurn(userIndex)) {
          canvas.write(0, 0, "   Your turn   ", white, black);
        } else if (model.myTurn(userIndex ^ 1)) {
          canvas.write(0, 0, "Opponent's turn", black, white);
        } else {
          canvas.write(0, 0, "     DRAW      ", black, white);
        }
      } else {
        if (winner.ordinal() == userIndex) {
          canvas.write(0, 0, "   YOU WON!    ", black, green);
        } else {
          canvas.write(0, 0, "   YOU LOST!   ", black, red);
        }
      }

      for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 3; ++j) {

          int cellIndex = j + 9 - 2 - 3 * i;

          canvas.write(i * 3 + 1, 5 * j, "┏━━━┓", white, black);
          canvas.write(i * 3 + 2, 5 * j, "┃ " + cellIndex + " ┃", white, black);
          canvas.write(i * 3 + 3, 5 * j, "┗━━━┛", white, black);

          switch (model.board[i][j]) {
            case X:
              canvas.write(i * 3 + 2, 5 * j + 2, "X", red, black);
              break;
          
            case O:
              canvas.write(i * 3 + 2, 5 * j + 2, "O", blue, black);
              break;

            default:
              break;
          }
        }
      }
    }

  }

  class Controller extends Game.Controller {

    private Model model;

    private class AttemptTurn implements Supplier<Boolean> {

      int row;
      int column;

      AttemptTurn(int row, int column) {
        this.row = row;
        this.column = column;
      }

      @Override
      public Boolean get() {
        return model.attemptTurn(userIndex, row, column);
      }

    }
    
    Controller(Model model, int userIndex) {
      super(userIndex);
      this.model = model;

      registerKeybind(KeyStroke.fromString("7"), new AttemptTurn(0, 0));
      registerKeybind(KeyStroke.fromString("8"), new AttemptTurn(0, 1));
      registerKeybind(KeyStroke.fromString("9"), new AttemptTurn(0, 2));

      registerKeybind(KeyStroke.fromString("4"), new AttemptTurn(1, 0));
      registerKeybind(KeyStroke.fromString("5"), new AttemptTurn(1, 1));
      registerKeybind(KeyStroke.fromString("6"), new AttemptTurn(1, 2));

      registerKeybind(KeyStroke.fromString("1"), new AttemptTurn(2, 0));
      registerKeybind(KeyStroke.fromString("2"), new AttemptTurn(2, 1));
      registerKeybind(KeyStroke.fromString("3"), new AttemptTurn(2, 2));
    }

  }

  @Override
  protected Game.Model makeModel(int numberOfPlayers) {
    return new Model(numberOfPlayers);
  }

  @Override
  protected Game.View makeView(Game.Model model, int userIndex) {
    return new View(Model.class.cast(model), userIndex);
  }

  @Override
  protected Game.Controller makeController(Game.Model model, int userIndex) {
    return new Controller(Model.class.cast(model), userIndex);
  }
}
