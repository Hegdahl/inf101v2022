import com.github.hegdahl.inf101v2022.Game;
import com.github.hegdahl.inf101v2022.ScreenBuffer;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.function.Supplier;

import javax.swing.plaf.metal.MetalBorders.PaletteBorder;

/**
 * Nim, a single player game to play against
 * the computer or another humam (easy to make a bot for).
 * 
 * <p>The game consists of several rows of tokens.
 * 
 * <p>In one turn you may remove any positive number of
 * tokens tokens as long as they are from the same row.
 * 
 * <p>The one to remove the last token wins.
 */
public class Nim extends Game {

  @Override
  public int minPlayers() {
    return 1;
  }

  @Override
  public int maxPlayers() {
    return 2;
  }

  class Model implements Game.Model {

    private final boolean againstAI;
    private int[] rows;
    private int turns;
    private int focusedRow;
    private int focusedCount;
    
    Model(int numberOfPlayers) {
      againstAI = numberOfPlayers == 1;
      rows = new int[]{1, 3, 5, 7};
      turns = 0;
      focusedRow = 0;
      focusedCount = 0;
    }

    int height() {
      return rows.length;
    }

    int width() {
      int max = 0;
      for (int row : rows) {
        max = Math.max(max, row);
      }
      return max;
    }

    int getRow(int row) {
      return rows[row];
    }

    int getFocusedRow() {
      return focusedRow;
    }

    int getFocusedCount() {
      return focusedCount;
    }

    boolean checkWon(int userIndex) {
      if (width() != 0) {
        return false;
      }
      return turns % 2 != userIndex;
    }

    boolean myTurn(int userIndex) {
      if (width() == 0) {
        return false;
      }
      return turns % 2 == userIndex;
    }

    boolean attemptChangeFocusedRow(int userIndex, int delta) {
      if (!myTurn(userIndex)) {
        return false;
      }

      if (delta == 0) {
        return false;
      }

      focusedRow += delta;
      focusedRow %= height();
      if (focusedRow < 0) {
        focusedRow += height();
      }
      focusedCount = Math.min(focusedCount, rows[focusedRow]);

      return true;
    }

    boolean attemptChangeFocusedCount(int userIndex, int delta) {
      if (!myTurn(userIndex)) {
        return false;
      }

      int newFocusedCount = Math.max(0, Math.min(rows[focusedRow],
          focusedCount + delta));
      
      if (newFocusedCount == focusedCount) {
        return false;
      }

      focusedCount = newFocusedCount;
      return true;
    }

    boolean attemptTurn(int userIndex) {
      if (!myTurn(userIndex)) {
        return false;
      }

      if (focusedCount == 0) {
        return false;
      }

      rows[focusedRow] -= focusedCount;
      focusedCount = 0;
      ++turns;

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
      canvas.clearAndResize(model.rows.length * 2, Math.max(15, model.width() * 2 + 1));

      boolean won = model.checkWon(userIndex);
      boolean lost = model.checkWon(userIndex ^ 1);

      ScreenBuffer.Color white = new ScreenBuffer.Color(255, 255, 255);
      ScreenBuffer.Color black = new ScreenBuffer.Color(0, 0, 0);
      ScreenBuffer.Color red = new ScreenBuffer.Color(255, 50, 50);
      ScreenBuffer.Color green = new ScreenBuffer.Color(50, 255, 50);
      ScreenBuffer.Color blue = new ScreenBuffer.Color(50, 50, 255);

      if (won) {
        canvas.write(0, 0, "YOU WON!       ", black, green);
      } else if (lost) {
        canvas.write(0, 0, "YOU LOST!      ", black, red);
      } else if (model.myTurn(userIndex)) {
        canvas.write(0, 0, "Your turn      ", white, black);
      } else {
        canvas.write(0, 0, "Opponent's turn", black, white);
      }

      for (int i = 0; i < model.height(); ++i) {
        for (int j = 0; j < model.getRow(i); ++j) {
          canvas.write(i + 1, 2 * j + 1, "*", white, black);
        }
      }

      int focusedRow = model.getFocusedRow();
      int focusedCount = model.getFocusedCount();
      if (focusedCount == 0) {
        canvas.write(focusedRow + 1, 0,
            "|", blue, black);
      } else {
        canvas.write(focusedRow + 1, 0,
            "[", blue, black);
        canvas.write(focusedRow + 1,  2 * focusedCount,
            "]", blue, black);
        for (int j = 0; j < focusedCount; ++j) {
          canvas.write(focusedRow + 1, 2 * j + 1, "*", blue, black);
        }
      }
    }

  }

  class Controller extends Game.Controller {

    private Model model;

    Controller(Model model, int userIndex) {
      super(userIndex);
      this.model = model;

      registerKeybind(new KeyStroke(KeyType.ArrowUp), () -> {
        return this.model.attemptChangeFocusedRow(userIndex, -1);
      });

      registerKeybind(new KeyStroke(KeyType.ArrowDown), () -> {
        return this.model.attemptChangeFocusedRow(userIndex, 1);
      });

      registerKeybind(new KeyStroke(KeyType.ArrowLeft), () -> {
        return this.model.attemptChangeFocusedCount(userIndex, -1);
      });

      registerKeybind(new KeyStroke(KeyType.ArrowRight), () -> {
        return this.model.attemptChangeFocusedCount(userIndex, 1);
      });

      Supplier<Boolean> confirmTurn = () -> {
        return this.model.attemptTurn(userIndex);
      };

      registerKeybind(new KeyStroke(KeyType.Enter), confirmTurn);
      registerKeybind(KeyStroke.fromString(" "), confirmTurn);
    }

  }

  @Override
  protected Game.Model makeModel(int numberOfPlayers) {
    return new Model(numberOfPlayers);
  }

  /**
   * Wrapper for the View constructor.
   * The cast is safe because the given
   * model is guaranteed to be made from `makeModel`.
   * 
   * @param model     Model created by `makeModel`.
   * @param userIndex Which user will use the created view.
   */
  @Override
  protected Game.View makeView(Game.Model model, int userIndex) {
    return new View(Model.class.cast(model), userIndex);
  }

  /**
   * Wrapper for the Controller constructor.
   * The cast is safe because the given
   * model is guaranteed to be made from `makeModel`.
   * 
   * @param model     Model created by `makeModel`.
   * @param userIndex Which user will use the created controller.
   */
  @Override
  protected Game.Controller makeController(Game.Model model, int userIndex) {
    return new Controller(Model.class.cast(model), userIndex);
  }
}
