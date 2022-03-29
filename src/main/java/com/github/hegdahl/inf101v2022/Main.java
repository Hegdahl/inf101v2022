package com.github.hegdahl.inf101v2022;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public class Main {

  public static void main(String[] args) {
    ArgumentParser parser = ArgumentParsers.newFor("prog").build()
      .description("Just checking that dependencies load.");
    try {
      parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
    }
  }
}
