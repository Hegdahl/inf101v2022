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