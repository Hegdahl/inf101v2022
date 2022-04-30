package com.github.hegdahl.inf101v2022;

public class ScreenBuffer {

  public class Cell {
    public final char ch;
    public final int fr;
    public final int fg;
    public final int fb;
    public final int br;
    public final int bg;
    public final int bb;

    Cell(char ch, int fr, int fg, int fb, int br, int bg, int bb) {
      this.ch = ch;
      this.fr = fr;
      this.fg = fg;
      this.fb = fb;
      this.br = br;
      this.bg = bg;
      this.bb = bb;
    }

    @Override
    public String toString() {
      return (int) ch + " "
          + fr + " " + fg + " " + fb + " "
          + br + " " + bg + " " + bb + " ";
    }
  }

  private Cell[][] cells;

  /**
   * Change the dimensions of the ScreenBuffer
   * and reset every cell.
   * 
   * @param height how many rows to make
   * @param width  how many columns to make
   */
  public void clearAndResize(int height, int width) {
    cells = new Cell[height][width];
    for (int i = 0; i < height; ++i) {
      for (int j = 0; j < width; ++j) {
        cells[i][j] = new Cell(' ', 255, 0, 0, 0, 0, 0);
      }
    }
  }

  /**
   * Get how many rows are in the buffer.
   */
  public int height() {
    if (cells == null) {
      return 0;
    }
    return cells.length;
  }

  /**
   * Get how many columns are in the buffer.
   */
  public int width() {
    if (height() == 0) {
      return 0;
    }
    return cells[0].length;
  }

  public Cell get(int row, int column) {
    return cells[row][column];
  }

  public void set(int row, int column, Cell cell) {
    cells[row][column] = cell;
  }

}
