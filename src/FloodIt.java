import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // The four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.setAdjacents(left, top, right, bottom);

  }

  // EFFECT: Sets the adjacent cells for this cell
  void setAdjacents(Cell left, Cell top, Cell right, Cell bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  // Draws this cell scaled up by 50
  WorldImage drawCell() {
    return new RectangleImage(50, 50, OutlineMode.SOLID, this.color);
  }

  // EFFECT: updates the neighbors when this cell has been flooded
  void updateNeighbors(Color color) {
    // flood check left
    if (this.left != null && this.left.color.equals(color) && !this.left.flooded) {
      this.left.flooded = true;
    }
    // flood check left
    if (this.top != null && this.top.color.equals(color) && !this.top.flooded) {
      this.top.flooded = true;
    }
    // flood check left
    if (this.right != null && this.right.color.equals(color) && !this.right.flooded) {
      this.right.flooded = true;
    }
    // flood check left
    if (this.bottom != null && this.bottom.color.equals(color) && !this.bottom.flooded) {
      this.bottom.flooded = true;
    }
  }

}

// Represents our game world 
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board;
  // Size * size will give us the gameboard
  int size;
  // How many steps the users has left
  int steps;
  // Total amount of steps to win
  int maxSteps;
  // keeps track of the number of colors
  int numColors;
  // List of available colors
  ArrayList<Color> loc = new ArrayList<Color>(
      Arrays.asList(Color.yellow, Color.blue, Color.green, Color.red, Color.pink, Color.orange));
  // Tracks the double value of seconds passed
  double tickTrack = 0.0;
  // Tracks the seconds elapsed
  int secondsElapsed = 0;
  // font size of the timer
  int timerSize;
  // font size of the end message
  int endSize;

  FloodItWorld(int size, int numColors) {
    this.size = size;
    this.numColors = numColors;
    // Generates the max steps from size and numColors
    this.maxSteps = (size * 2) - 3 + this.numColors;
    // Building the board
    this.board = this.buildBoard(size);
    // set all the adjacent cells for each cell on the board
    this.setAdjacents();

    // set the timer size to fit the given board size
    if (this.size > 2) {
      timerSize = 25;
    }
    else {
      timerSize = 20;
    }

    // set the ending message size to fit the given board size
    if (this.size > 3) {
      endSize = 20;
    }
    else {
      endSize = 18;
    }
  }

  // builds the board for this game given the size
  ArrayList<Cell> buildBoard(int size) {
    ArrayList<Cell> result = new ArrayList<Cell>(size * size);
    // Build the board
    for (int i = 0; i < size; i += 1) {
      for (int j = 0; j < size; j += 1) {
        // make sure the first cell is flooded
        if (i == 0 && j == 0) {
          result.add(new Cell(i, j, this.chooseColor(), true, null, null, null, null));
        }
        else {
          result.add(new Cell(i, j, this.chooseColor(), false, null, null, null, null));
        }
      }
    }
    return result;
  }

  // Chooses a random color for each cell
  Color chooseColor() {
    Random rand = new Random();
    // using a seed for testing purposes
    // Random rand = new Random(5);
    return this.loc.get(rand.nextInt(this.numColors));
  }

  // EFFECT: Sets the adjacent cells for all cells on this board
  void setAdjacents() {
    Cell left;
    Cell top;
    Cell right;
    Cell bottom;

    int currentIndex = 0;

    for (int i = 0; i < this.size; i += 1) {
      for (int j = 0; j < this.size; j += 1) {
        currentIndex = (j * this.size) + i;
        // Sets the left
        if (i == 0) {
          left = null;
        }
        else {
          left = this.board.get(currentIndex - 1);
        }

        // Sets the top
        if (j == 0) {
          top = null;
        }
        else {
          top = this.board.get(currentIndex - this.size);
        }

        // Sets the right
        if (i == this.size - 1) {
          right = null;
        }
        else {
          right = this.board.get(currentIndex + 1);

        }

        // Sets the bottom
        if (j == this.size - 1) {
          bottom = null;

        }
        else {
          bottom = this.board.get(currentIndex + this.size);
        }

        this.board.get(currentIndex).setAdjacents(left, top, right, bottom);
      }

    }

  }

  // Draws this current cell configuration
  WorldImage drawCurrentBoard() {

    // ACC: Keeps track of the board built so far
    WorldImage boardAcc = new EmptyImage();
    // ACC: Keeps track of the current row built so fars
    WorldImage colAcc = new EmptyImage();
    // the current index on the board
    int currentIndex = 0;
    // Builds rows bottom to top
    for (int i = this.size - 1; i >= 0; i -= 1) {
      colAcc = new EmptyImage();
      // Builds each cell left to right
      for (int j = 0; j < this.size; j += 1) {
        currentIndex = (i * this.size) + j;
        // rowAcc = new BesideImage(rowAcc, this.board.get(currentIndex).drawCell());
        colAcc = new AboveImage(colAcc, this.board.get(currentIndex).drawCell());
      }

      // boardAcc = new AboveImage(rowAcc, boardAcc);
      boardAcc = new BesideImage(colAcc, boardAcc);
    }

    return boardAcc.movePinholeTo(new Posn(0, 0));
  }

  // Visualize the current scene of the game
  public WorldScene makeScene() {

    // Black border of our game
    WorldImage border = new RectangleImage(50 * (this.size + 2), 50 * (this.size + 2),
        OutlineMode.SOLID, Color.black).movePinholeTo(new Posn(0, 0));
    // Cell configuration of the game
    WorldImage currentBoard = this.drawCurrentBoard();
    // Counts the amount of steps used and remaining
    WorldImage stepCount = new TextImage(
        String.valueOf(this.steps) + "/" + String.valueOf(this.maxSteps), 25, FontStyle.BOLD,
        Color.white).movePinholeTo(new Posn(0, 0));
    // Counts the time elapsed in seconds (with appropriate font size)
    WorldImage timeCount = new TextImage("Seconds passed: " + String.valueOf(this.secondsElapsed),
        this.timerSize, Color.white).movePinholeTo(new Posn(0, 0));

    // draw the title and size of this game
    WorldImage titleImageLeft = new RotateImage(
        new TextImage("FloodIt!", this.timerSize, Color.MAGENTA).movePinholeTo(new Posn(0, 0)),
        270);
    WorldImage titleImageRight = new RotateImage(
        new TextImage("size: " + String.valueOf(this.size) + "x" + String.valueOf(this.size),
            this.timerSize, Color.MAGENTA).movePinholeTo(new Posn(0, 0)),
        90);

    // Visualizes our current game scene
    WorldScene current = new WorldScene(50 * (this.size + 2), 50 * (this.size + 2));
    current.placeImageXY(border, (50 * (this.size + 2)) / 2, (50 * (this.size + 2)) / 2);
    current.placeImageXY(currentBoard, ((50 * (this.size + 2)) / 2), ((50 * (this.size + 2)) / 2));
    current.placeImageXY(stepCount, (50 * (this.size + 2)) / 2, (int) (50 * (this.size + 1.5)));
    current.placeImageXY(timeCount, (50 * (this.size + 2)) / 2, (int) (50 * (0.5)));
    current.placeImageXY(titleImageLeft, 25, 50 * (this.size + 2) / 2);
    current.placeImageXY(titleImageRight, 50 * (this.size + 2) - 25, 50 * (this.size + 2) / 2);

    return current;

  }

  // visualize the end scene of the game
  public WorldScene makeEndScene() {
    // Black border of our game
    WorldImage border = new RectangleImage(50 * (this.size + 2), 50 * (this.size + 2),
        OutlineMode.SOLID, Color.black).movePinholeTo(new Posn(0, 0));
    // Cell configuration of the game
    WorldImage currentBoard = this.drawCurrentBoard();
    // Counts the amount of steps used and remaining
    WorldImage stepCount = new TextImage(
        String.valueOf(this.steps) + "/" + String.valueOf(this.maxSteps), 25, Color.white)
            .movePinholeTo(new Posn(0, 0));
    // The final message for the game
    WorldImage winOrLose = new EmptyImage();
    if (!this.allFloodedCheck() && this.steps > this.maxSteps) {
      winOrLose = new TextImage("Fail. Level = " + this.playerLevel(), this.endSize, Color.white);
    }
    else if (this.allFloodedCheck()) {
      winOrLose = new TextImage("Win! Level = " + this.playerLevel(), this.endSize, Color.white);
    }

    // draw the title and size of this game
    WorldImage titleImageLeft = new RotateImage(
        new TextImage("FloodIt!", this.timerSize, Color.MAGENTA).movePinholeTo(new Posn(0, 0)),
        270);
    WorldImage titleImageRight = new RotateImage(
        new TextImage("size: " + String.valueOf(this.size) + "x" + String.valueOf(this.size),
            this.timerSize, Color.MAGENTA).movePinholeTo(new Posn(0, 0)),
        90);

    // Visualizes the game's end scene
    WorldScene current = new WorldScene(50 * (this.size + 2), 50 * (this.size + 2));
    current.placeImageXY(border, (50 * (this.size + 2)) / 2, (50 * (this.size + 2)) / 2);
    current.placeImageXY(currentBoard, ((50 * (this.size + 2)) / 2), ((50 * (this.size + 2)) / 2));
    current.placeImageXY(stepCount, (50 * (this.size + 2)) / 2, (int) (50 * (this.size + 1.5)));
    current.placeImageXY(winOrLose, (50 * (this.size + 2)) / 2, (int) (50 * (0.5)));
    current.placeImageXY(titleImageLeft, 25, 50 * (this.size + 2) / 2);
    current.placeImageXY(titleImageRight, 50 * (this.size + 2) - 25, 50 * (this.size + 2) / 2);

    return current;
  }

  // EFFECT: Modifies the current world for rendering
  public void onTick() {
    // Adds .25 seconds to tickTrack
    this.tickTrack += 0.25;
    // Updates secondsElapsed corresponding to the current tickTrack
    secondsElapsed = (int) this.tickTrack;

    // update this game every tick
    this.updateGame();

  }

  // returns the corresponding cell on this game according to the given posn
  Cell getCellFromPosn(Posn posn) {
    Cell result = null;
    for (Cell cell : this.board) {
      if (
          // check x range
          (cell.x <= ((posn.x - 50) / 50)) && (cell.x >= ((posn.x - 50) / 50))
          // check y range
          && (cell.y <= ((posn.y - 50) / 50)) && (cell.y >= ((posn.y - 50) / 50))) {
        result = cell;
      }
    }
    return result;
  }

  // EFFECT: Updates this game with the set current flooding color
  void updateGame() {
    Color floodColor = this.board.get(0).color;
    for (Cell c : this.board) {
      if (c.flooded) {
        c.color = floodColor;
        c.updateNeighbors(floodColor);
      }
    }
    this.makeScene();
  }

  // EFFECT: When the mouse is clicked, updates the color of the top left cell
  // to the new flooding color. Ignores the click if invalid.
  public void onMouseClicked(Posn posn) {
    Cell clickedCell = null;
    // make sure it's in range
    if (posn.x < 50 || posn.x > ((this.size * 50) + 50) || posn.y < 50
        || posn.y > ((this.size * 50) + 50)) {
      // ignore the click
    }
    else {
      // increment the step count by one for each valid click
      this.steps += 1;
      clickedCell = this.getCellFromPosn(posn);
      // set the first cell to the new flooding color
      if (clickedCell != null) {
        Cell temp = this.board.get(0);
        temp.color = clickedCell.color;
        this.board.set(0, temp);
      }
    }
  }

  // EFFECT: if the 'r' key is pressed, restart this game
  // otherwise ignore the key event
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = this.buildBoard(this.size);
      this.setAdjacents();
      this.steps = 0;
      this.tickTrack = 0;
      this.secondsElapsed = 0;
    }
    else {
      // ignore the key
    }
  }

  // check if the board has all been flooded
  boolean allFloodedCheck() {
    boolean result = true;
    for (Cell c : this.board) {
      result = c.flooded && result;
    }
    return result;
  }

  // check if the game has ended
  public WorldEnd worldEnds() {
    if (this.allFloodedCheck() || this.steps > this.maxSteps) {
      return new WorldEnd(true, this.makeEndScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // what is this player's level
  String playerLevel() {
    if (this.maxSteps - this.steps <= 0) {
      return "Noob";
    }
    else if (this.maxSteps - this.steps >= this.maxSteps * 0.5) {
      return "FloodIt Master";
    }
    else if (this.maxSteps - this.steps >= this.maxSteps * 0.35) {
      return "Expert";
    }
    else if (this.maxSteps - this.steps >= this.maxSteps * 0.25) {
      return "Skilled";
    }
    else if (this.maxSteps - this.steps >= this.maxSteps * 0.1) {
      return "Beginner";
    }
    else {
      return "";
    }
  }

}

// Class to represent our examples of our game & tests
class ExamplesFloodIt {

  // Examples of game
  FloodItWorld testGame;
  FloodItWorld testGame1;
  FloodItWorld testGame2;
  FloodItWorld testGame3;
  FloodItWorld testGame4;
  FloodItWorld testGame5;
  FloodItWorld testGame6;
  FloodItWorld testGame7;

  // Examples of cells
  Cell c1;
  Cell c2;
  Cell c3;

  // Initialize the example data
  void initData() {
    this.testGame = new FloodItWorld(2, 3);
    this.testGame1 = new FloodItWorld(2, 3);
    this.testGame2 = new FloodItWorld(6, 3);
    this.testGame3 = new FloodItWorld(6, 6);
    this.testGame4 = new FloodItWorld(10, 6);
    this.testGame5 = new FloodItWorld(2, 6);
    this.testGame6 = new FloodItWorld(3, 3);
    this.testGame7 = new FloodItWorld(4, 1);

    // Examples of cells
    this.c1 = new Cell(0, 0, Color.blue, false, null, null, null, null);
    this.c2 = new Cell(0, 0, Color.green, false, null, null, null, null);
    this.c3 = new Cell(0, 0, Color.red, false, null, null, null, null);

  }

  // Tests for drawCell
  void testDrawCell(Tester t) {
    // -- ensure the initial conditions --
    this.initData();
    // test for drawCell
    t.checkExpect(this.c1.drawCell(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.blue));
    t.checkExpect(this.c2.drawCell(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.green));
    t.checkExpect(this.c3.drawCell(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.red));

  }

  // Tests for set adjacent
  void testSetAdjacent(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // Check the data before
    t.checkExpect(this.c1.left, null);
    t.checkExpect(this.c2.bottom, null);

    // Run the code to modify the state
    this.c1.setAdjacents(this.c3, null, null, null);
    this.c2.setAdjacents(null, null, null, this.c3);

    // Test the change was made
    t.checkExpect(this.c1.left, this.c3);
    t.checkExpect(this.c2.bottom, this.c3);
  }

  // test method chooseColor
  void testChooseColor(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // using a seed for random and testing the code we wrote in chooseColor, since
    // we don't have a seed in the actual
    // method itself
    t.checkExpect(this.testGame.loc.get(new Random(5).nextInt(this.testGame.numColors)),
        Color.green);
    t.checkExpect(this.testGame.loc.get(new Random(6).nextInt(this.testGame2.numColors)),
        Color.blue);
    t.checkExpect(this.testGame.loc.get(new Random(7).nextInt(this.testGame4.numColors)),
        Color.pink);
  }

  // tests for getCellFromPosn
  void testGetCellFromPosn(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // the tests for getCellFromPosn(Posn ...)
    t.checkExpect(this.testGame4.getCellFromPosn(new Posn(54, 60)), this.testGame4.board.get(0));
    t.checkExpect(this.testGame4.getCellFromPosn(new Posn(178, 50)), this.testGame4.board.get(20));
    t.checkExpect(this.testGame4.getCellFromPosn(new Posn(364, 410)), this.testGame4.board.get(67));
    t.checkExpect(this.testGame4.getCellFromPosn(new Posn(276, 530)), this.testGame4.board.get(49));
  }

  // tests for update neighbors
  void testUpdateNeighbors(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // -- modify data --
    Cell c4 = new Cell(0, 0, Color.blue, false, null, null, null, null);
    // check the data before hand
    t.checkExpect(c4.flooded, false);
    // set c1s bottom to c4
    c1.setAdjacents(null, null, null, c4);
    // call updateNeighbors on c1
    c1.updateNeighbors(Color.blue);

    // -- check the change --
    // check to see that c1s bottom cell is now flooded
    t.checkExpect(c1.bottom.flooded, true);
    // check to see that c4 was modified itself
    t.checkExpect(c4.flooded, true);
  }

  // tests for onMouseClicked
  void testOnMouseClicked(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // call the method
    this.testGame.onMouseClicked(new Posn(120, 60));

    // check the change
    // the color of the first flooded cell should now equal the color of the clicked
    // cell
    t.checkExpect(this.testGame.board.get(0).color,
        this.testGame.getCellFromPosn(new Posn(120, 60)).color);
  }

  // tests for onKeyEvent
  void testOnKeyEvent(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // add a step in the game and check that it was added
    this.testGame1.steps += 1;
    t.checkExpect(this.testGame1.steps, 1);

    // call the method with wrong input and check it makes no change
    this.testGame1.onKeyEvent("b");
    t.checkExpect(this.testGame1.steps, 1);

    // call the method with r and check it resets
    this.testGame1.onKeyEvent("r");
    t.checkExpect(this.testGame1.steps, 0);
  }

  // tests for allFloodedCheck
  void testAllFloodedCheck(Tester t) {
    // -- ensure the initial conditions --
    this.initData();

    // change all the cells in testGame's board to flooded
    ArrayList<Cell> newBoard = new ArrayList<Cell>();
    ArrayList<Cell> temp = this.testGame.board;
    for (Cell c : temp) {
      newBoard.add(new Cell(c.x, c.y, c.color, true, c.left, c.top, c.right, c.top));
    }
    this.testGame.board = newBoard;

    // run the method on testGame
    t.checkExpect(this.testGame.allFloodedCheck(), true);
  }

  // tests for drawCurrentBoard (using a Random seed of 5)
  // commenting out these tests for game play - they pass with seed of 5
  /*
  void testDrawCurrentBoard(Tester t) {
    this.initData();
    t.checkExpect(this.testGame.drawCurrentBoard(),
        new BesideImage(
            new AboveImage(
                new AboveImage(new EmptyImage(),
                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)),
                new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)),
            new BesideImage(
                new AboveImage(
                    new AboveImage(new EmptyImage(),
                        new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)),
                new EmptyImage())));
  }*/

  // tests for makeScene (using a Random seed of 5)
  // commenting out these tests for game play - they pass with seed of 5
  /*
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene result = new WorldScene(200, 200);
    result.placeImageXY(new RectangleImage(200, 200, OutlineMode.OUTLINE, Color.black), 100, 100);
    result.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.black), 100, 100);
    result.placeImageXY(this.testGame.drawCurrentBoard(), 100, 100);
    result.placeImageXY(new TextImage("0/4", 25.0, FontStyle.REGULAR, Color.white), 100, 175);
    result.placeImageXY(new TextImage("Seconds passed: 0", 20.0, FontStyle.REGULAR, Color.white),
        100, 25);
    result.placeImageXY(new RotateImage(
           new TextImage("FloodIt!",
            20.0, FontStyle.REGULAR,
            Color.magenta), 270.0), 25, 100);
    result.placeImageXY(new RotateImage(
           new TextImage("size: 2x2",
            20.0, FontStyle.REGULAR,
            Color.magenta), 90.0), 175, 100);
    t.checkExpect(this.testGame.makeScene(), result);
  }*/

  // tests for makeEndScene (using a Random seed of 5)
  // commenting out these tests for game play - they pass with seed of 5
  /*
  void testMakeEndScene(Tester t) {
    this.initData();
    WorldScene result = new WorldScene(200, 200);
    result.placeImageXY(new RectangleImage(200, 200, OutlineMode.OUTLINE, Color.black), 100, 100);
    result.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.black), 100, 100);
    result.placeImageXY(
        new BesideImage(
            new AboveImage(
                new AboveImage(new EmptyImage(),
                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)),
                new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)),
            new BesideImage(new AboveImage(
                new AboveImage(new EmptyImage(),
                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.green)),
                new RectangleImage(50, 50, OutlineMode.SOLID, Color.GREEN)), new EmptyImage())),
        100, 100);
    result.placeImageXY(new TextImage("0/4", 25.0, FontStyle.REGULAR, Color.white), 100, 175);
    result.placeImageXY(new EmptyImage(), 100, 25);
  
    t.checkExpect(this.testGame.makeEndScene(), result);
  }*/

  // Test for drawBoard
  void testBigBang(Tester t) {
    // -- ensure the initial conditions --
    this.initData();
    // make the bigbang world
    FloodItWorld f = this.testGame4;
    int worldWidth = (50 * (this.testGame4.size + 2));
    int worldHeight = (50 * (this.testGame4.size + 2));
    double tickRate = 0.25;
    f.bigBang(worldWidth, worldHeight, tickRate);
  }

}
