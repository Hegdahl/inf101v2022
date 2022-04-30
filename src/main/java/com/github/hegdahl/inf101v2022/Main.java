package com.github.hegdahl.inf101v2022;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Entry point for the program.
 * 
 * <p>Dispatches to `Host` or `Join`
 * depending on the subcommand used.
 */
public class Main {

  /**
   * Implemented by `Host` and `Join`
   * to take control of the program.
   */
  public interface SubcommandHandler {
    /**
     * Perform the subcommand given
     * the processed command line arguments.
     * 
     * @param ns object contianing processed arguments.
     */
    public void main(Namespace ns);
  }

  /**
   * Either start hosting a game or connect to one,
   * depending on command line arguments.
   * 
   * <p>For a more detailed description,
   * use `./resulting_executable --help`
   * or `./resulting_executable SUBCOMMAND --help`.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    ArgumentParser rootParser = ArgumentParsers.newFor("inf101v2022").build()
        .description("Framework for board-like games in the terminal.");
    
    Subparsers subparsers = rootParser.addSubparsers().title("subcommands")
        .description("valid subcommands").help("additional help")
        .metavar("COMMAND");

    Subparser hostParser = subparsers.addParser("host")
        .help("Host a game server.")
        .setDefault("handler", new Host());

    hostParser.addArgument("rulesPath")
      .type(String.class)
      .help(".class file contianing the ruleset for the game.");

    hostParser.addArgument("-p", "--port")
      .type(Short.class)
      .help("Which port to bind the server to.")
      .setDefault((short) 8080);

    Subparser joinParser = subparsers.addParser("join")
        .help("Join a game hosted on some server.")
        .setDefault("handler", new Join());

    joinParser.addArgument("username")
        .type(String.class)
        .help("The name the other players see you as.");

    joinParser.addArgument("address")
        .type(String.class)
        .help("Which IP address to connect to");

    joinParser.addArgument("-p", "--port")
      .type(Short.class)
      .help("Which port to connect to.")
      .setDefault((short) 8080);


    Namespace ns = rootParser.parseArgsOrFail(args);

    ((SubcommandHandler) ns.get("handler")).main(ns);
  }

}
