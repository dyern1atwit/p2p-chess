# P2P Chess
### Created by P2Peers for Network Programming (COMP-2100) at Wentworth Institute of Technology

This is a simple peer-to-peer chess game designed to take the hassle out of connecting to a server every time you and your friend want to play a game of chess.
## Starting the game

To start the game, type in the port that you wish your friend to connect to you on. The game will then give you your local IP address and your external IP 
address (if you happen to have the port you specified forwared.) You can move the chat window to wherever you'd like. The player that created the game
will be set as white.

## Connecting to a Player

To connect to someone else, enter their IP and port into the connection box and click the connect button. This will then connect you to the other client and you
can begin playing the game.
#### Note:
IP and port are inputted all together separated by a colon (e.g. `192.168.1.10:1234`)

## Playing the game

To play the game, simply click on a piece of yours to move it somewhere. Valid squares will light up green, and squares where you can take another piece
will light up red. If you change your mind on what piece you want to move, click the currently selected piece again to de-select it and select another piece.
If you try to move a piece that would cause you to be in check or keep you in check, the game will not allow it and you will have to either move that piece
elsewhere, or de-select it and select another piece.

Every move that happens in the game will result in an event in the game chat so that you can keep track of your moves.

## Winning/Losing the game

In order to win, you must put your opponenet in a place where they cannot move out of check, aka checkmate. Once your opponent is in check, if they have no moves
that can move them out of check they must press the "Forfeit" button (or be stuck indefinitely.) If you are in check and you have no valid moves, you must do
the same. 

You can also press the "Forfiet" button at any time to end the game and declare your opponent as the winner. You can then exit the game, and if you so wish,
start it up again to play once more.
