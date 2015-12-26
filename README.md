# Thud
A Java implementation of Terry Pratchett's Discworld chesslike game, Thud!

## The game
[Thud](https://en.wikipedia.org/wiki/Games_of_the_Discworld#Thud) is a game mentioned in Terry Pratchett's 34th Discworld novel, *Thud!*.
Trevor Truran created the complete rules of the game.

## Prerequisites
The application is packaged as a multiplatform jar (Java ARchive). You'll find some instructions on how to lauch the jar [here](http://docs.oracle.com/javase/tutorial/deployment/jar/run.html).
You'll need a JRE install, at least Java 8, which you can download [here](http://www.java.com/fr/download).

The application is standalone, and stores a single configuration file alongside the jar file.

## How to play

### The board
When started, a new board is loaded:

![alt tag](https://github.com/Saucistophe/Thud/blob/master/Images/board.png)

Each dwarf is pictured with a **D**, each troll with a **T**, and non-usable squares are greyed out.
By default, dwarves play first.

The currently playing side is displayed in the top-left corner (the green rounded rectangle on the picture below).
When hovering one of the pieces of the currently moving side, possible moves are highlighted.

![alt tag](https://github.com/Saucistophe/Thud/blob/master/Images/board-colors.png)

If you're playing a troll, and can capture one out of several dwarves, they are highlighted in yellow to let you choose your victim. Clicking anywhere else cancels the move.

![alt tag](https://github.com/Saucistophe/Thud/blob/master/Images/board-captures.png)

### The AI

A basic AI is implemented. Due to the high branching factor, writing an efficient AI for Thud is much more difficult than for chess, and it will sometimes seem dumb; it's a fun challenge for a beginner though.
To let the AI play the next move, simply press space - and be patient. A dwarf opening move, on the default depth setting, takes about 30 seconds to compute on decent hardware.

### Settings

The *Settings* menu allows you to customize useful settings, most notably the AI reflection depth. Be careful, values above the default value of 3 may very well hang the game).
In the *File* menu, you can save your current game for later use. You can also load a saved game, or a custom game.

The games are saved as a very straightforward format, that will allow you to create your own boards:
```

░░░DD DD░░░
░░D     D░░
░D       D░
D   TTT   D
    TXT    
D   TTT   D
░D       D░
░░D     D░░
░░░DD DD░░░
```

The only thing to note is that the first (upper left) character will always be turned to a non-playable square; however, if your saved file has a **T** or **D** in this location, it will set the side currently playing.

## Credits
Most infos about the game, its rules, and the usual strategies come from Oograh Boike's unofficial Thud page, and from Boike himself.
With the official website of the game (Thudgame.com) down, your most up-to-date source for the rules is probably Wikipedia.
