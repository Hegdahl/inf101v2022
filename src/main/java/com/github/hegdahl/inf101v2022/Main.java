package com.github.hegdahl.inf101v2022;

import java.io.IOException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class Main {

  public interface SubcommandHandler {
    public void main(Namespace ns) throws IOException;
  }
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

    try {
      ((SubcommandHandler)ns.get("handler")).main(ns);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
      System.err.println(e.getCause());
      System.exit(1);
    }
  }

}
