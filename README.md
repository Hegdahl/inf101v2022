# Framework for Online Board Games in the Terminal

This program allows you to host or join
a board game with rules according to
a class that extends `Game`.

```bash
./inf101v2022 --help
```
```
usage: inf101v2022 [-h] COMMAND ...

Framework for board-like games in the terminal.

named arguments:
  -h, --help             show this help message and exit

subcommands:
  valid subcommands

  COMMAND                additional help
    host                 Host a game server.
    join                 Join a game hosted on some server.
```

```bash
./inf101v2022 host --help
```
```
usage: inf101v2022 host [-h] [-p PORT] rulesPath

positional arguments:
  rulesPath              .class file contianing the ruleset for the game.

named arguments:
  -h, --help             show this help message and exit
  -p PORT, --port PORT   Which port to bind the server to.
```

```bash
./inf101v2022 join --help
```
```
usage: inf101v2022 join [-h] [-p PORT] username address

positional arguments:
  username               The name the other players see you as.
  address                Which IP address to connect to

named arguments:
  -h, --help             show this help message and exit
  -p PORT, --port PORT   Which port to connect to.
```

# Example Usage

https://user-images.githubusercontent.com/48063801/166123263-654e20c4-46f6-46b9-9857-e18ebd5340dd.mp4


# Building

## Linux (and probably but not tested MacOS)

With maven installed, it should work to run `./build.sh`.

This will build the project itself in addition to every game in the folder `/src/games`.

## Windows

With maven installed, run `mvn package`.

This will build an executable .jar file which is the host and join program,
and also functions as the library when building a game.

To build a game, run
```cmd
javac path\to\the\game.java -d where\to\put\the\game -cp path\to\the\.jar
```
