# Framework for Online Board Games in the Terminal

This program allows you to host or join
a board game with rules according to
a class that extends `Game`.

[Documentation](https://hegdahl.github.io/inf101v2022/html/annotated.html)

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

The main executable will be called `inf101v2022.out`.


## Windows

With maven installed, run `mvn package`.

This will build an executable .jar file which is the host and join program,
and also functions as the library when building a game.

The path for the jar file will be `target\inf101v2022-{version}-SNAPSHOT-jar-with-dependencies.jar`.
I suggest making a copy with a shorter name in the base of the directory.

To build a game, run
```powershell
javac path\to\the\game.java -d where\to\put\the\game -cp the_jar_file.jar
```

The jar can not be ran directly in the terminal,
but is instead ran like this
```powershell
java -jar the_jar_file.jar args
```

where args are arguemnts as described in the `--help` outputs
at the start of this document.
