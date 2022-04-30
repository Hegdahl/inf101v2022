package com.github.hegdahl.inf101v2022;

public class ScreenBuffer {

  public class Cell {
    public final char c;
    public final int fr, fg, fb;
    public final int br, bg, bb;
    Cell(char c, int fr, int fg, int fb, int br, int bg, int bb) {
      this.c = c;
      this.fr = fr;
      this.fg = fg;
      this.fb = fb;
      this.br = br;
      this.bg = bg;
      this.bb = bb;
    }

    @Override
    public String toString() {
      return (int)c + " "
          + fr + " " + fg + " " + fb + " "
          + br + " " + bg + " " + bb + " ";
    }
  };

  private Cell[][] cells;

  public ScreenBuffer(int height, int width) {
    cells = new Cell[height][width];
    for (int i = 0; i < height; ++i)
      for (int j = 0; j < width; ++j)
        cells[i][j] = new Cell('Q', 255, 0, 0, 0, 0, 255);
  }

  public Cell get(int row, int column) {
    return cells[row][column];
  }

  public void set(int row, int column, Cell cell) {
    cells[row][column] = cell;
  }

}
