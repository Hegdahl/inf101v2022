package com.github.hegdahl.inf101v2022;

public class ScreenBuffer {

  public static class Color {
    public final int red;
    public final int green;
    public final int blue;

    /**
     * Color represented by red, green and blue
     * values in the range [0, 256).
    */
    public Color(int red, int green, int blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
    }

    @Override
    public String toString() {
      return String.format("%s %s %s", red, green, blue);
    }
  }

  public static class Cell {
    public final char ch;
    Color foreground;
    Color background;

    /**
     * Represents a character with a foreground and background color.
     */
    public Cell(char ch, Color foreground, Color background) {
      this.ch = ch;
      this.foreground = foreground;
      this.background = background;
    }

    public Cell(char ch, int fr, int fg, int fb, int br, int bg, int bb) {
      this(ch, new Color(fr, fg, fb), new Color(br, bg, bb));
    }

    @Override
    public String toString() {
      return String.format("%s %s %s", (int)ch, foreground, background);
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
    assertWithinBounds(row, column);
    return cells[row][column];
  }

  public void set(int row, int column, Cell cell) {
    assertWithinBounds(row, column);
    cells[row][column] = cell;
  }

  /**
   * Write horizontally.
   * 
   * @param row        vertical position of text
   * @param column     horizontal position of start of text
   * @param text       what to write
   * @param foreground color of the characters
   * @param background color of the backgroudn
   */
  public void write(int row, int column, String text, Color foreground, Color background) {
    for (int i = 0; i < text.length(); ++i) {
      set(row, column + i, new Cell(text.charAt(i), foreground, background));
    }
  }

  private void assertWithinBounds(int row, int column) {
    if (0 > row || row >= height()) {
      throw new IndexOutOfBoundsException(String.format(
          "The row %s is out of bounds for a buffer of height %s.",
          row, height()));
    }
    if (0 > column || column >= width()) {
      throw new IndexOutOfBoundsException(String.format(
          "The column %s is out of bounds for a buffer of width %s.",
          column, width()));
    }
  }
}
