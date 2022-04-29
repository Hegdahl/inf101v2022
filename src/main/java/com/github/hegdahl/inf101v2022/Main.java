package com.github.hegdahl.inf101v2022;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {

  public static void main(String[] args) {
    ArgumentParser parser = ArgumentParsers.newFor("prog").build()
      .description("Just checking that dependencies load.");
    parser.addArgument("rules_path")
      .type(String.class)
      .help(".class file contianing the ruleset for the game.");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (HelpScreenException e) {
      parser.handleError(e);
      System.exit(0);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

    String rulesPath = ns.getString("rules_path");
    File rulesFile = new File(rulesPath);
    if (!rulesFile.exists()) {
      System.err.println("Error: Could not find file '" + rulesFile + "'");
      System.exit(1);
    }

    if (!rulesFile.isFile()) {
      System.err.println("Error: '" + rulesFile + "' is not a file.");
      System.exit(1);
    }

    URL rulesURL = null;
    try {
      rulesURL = rulesFile.getParentFile().toURI().toURL();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.exit(1);
    }

    URLClassLoader classLoader = null;
    classLoader = URLClassLoader.newInstance(new URL[] {rulesURL});

    RuleSet rules = null;
    try {
      String rulesClassName = "TicTacToe";
      Class<?> rulesClass = classLoader.loadClass(rulesClassName);
      Constructor<?> constructor = rulesClass.getConstructor();
      rules = (RuleSet)constructor.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e);
      System.exit(1);
    }

    rules.hello();
  }
}
