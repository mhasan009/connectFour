package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
//	Game Rules
	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int Circle_Diameter = 80;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";
	//private static String Player_One = "Player One";
	//private static String Player_Two = "Player Two";
	private boolean isPlayerOneTurn = true; //Game Rules
	private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS]; //For structural changes: for developers

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscsPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField playerOneTextField;
	@FXML
	public TextField playerTwoTextField;
	@FXML
	public Button setNamesButton;

	private boolean isAllowedToInsert = true;  //Flag to avoid multiple discs of same color to be inserted

	@FXML
	public Label playerTurnLabel;

	public void createPlayground(){
Shape rectangleWithHoles = createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles,0,1); //Add rectangleWithHoles to Grid Pane
		//Now call createPlayground() in Main (Inside start function)
		List<Rectangle> rectangleList = createClickableColumns();
//		Using ForEach loop to add all rectangular strips
		for (Rectangle rectangle:rectangleList
		     ) {
			rootGridPane.add(rectangle,0,1);
		}
Platform.runLater(() -> setNamesButton.requestFocus());
	}

	private Shape createGameStructuralGrid(){
		Shape rectangleWithHoles = new Rectangle((COLUMNS+1) * Circle_Diameter,(ROWS+1) * Circle_Diameter);
//COLUMNS+1 and ROWS+1 -> To increase Rectangle's dimension (1 Column and 1 Row increased)
		for(int row = 0; row < ROWS; row++){
			for(int col = 0; col < COLUMNS; col++){
				Circle circle = new Circle();
				circle.setRadius(Circle_Diameter/2);
				circle.setCenterX(Circle_Diameter/2);
				circle.setCenterY(Circle_Diameter/2);
				circle.setSmooth(true);  //To rectify blurry edges of circles
				circle.setTranslateX(col * (Circle_Diameter+5) + Circle_Diameter/4); //Subtract all circles from X Direction
				circle.setTranslateY(row * (Circle_Diameter+5) + Circle_Diameter/4); //Subtract all circles from Y Direction
//				Circle_Diameter+5 -> To make space between circles
//				Circle_Diameter/4 -> To make space from Top and Left side of Rectangle
				rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle); //rectangleWithHoles - circle
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles; //Return shape
	}

	private List<Rectangle> createClickableColumns(){
		List<Rectangle> rectangleList = new ArrayList();
		for(int col = 0; col < COLUMNS; col++) {
			Rectangle rectangle = new Rectangle(Circle_Diameter, (ROWS + 1) * Circle_Diameter);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (Circle_Diameter+5) + Circle_Diameter / 4); //To make rectangular strip appear on the first column
//			Add -> col * (Circle_Diameter+5) -> To make one rectangular strip appear on one column

//			To introduce Hover effect
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

//			Introducing Click Event on columns
			final int column = col;
			rectangle.setOnMouseClicked(event -> {

					if (isAllowedToInsert) {
						isAllowedToInsert = false;  //Stop the user to insert multiple discs
						insertDisc(new Disc(isPlayerOneTurn), column);
					}

			});
			rectangleList.add(rectangle);
		}
			return rectangleList;
//		Call createClickableColumns() inside createPlayground()
	}

	private void insertDisc(Disc disc, int column){
//Placing discs on top
		int row = ROWS - 1;  //Index starts from zero
		while (row >= 0){
			//Check emptiness of array
			//if(insertedDiscsArray[row][column] == null)
			if(getDiscIfPresent(row,column) == null)
				break; //If space is empty : When we get an empty space, break the loop
				row--; //If space is not empty : Decrement the counter

		}
		//If row is full
		if(row < 0) {  //Column is full, can't insert disc
			return; //Do nothing
		}

//		disc -> inserted by player
//		Place the disc within insertedDiscsArray
		insertedDiscsArray[row][column] = disc; //For structural changes: for developers
		insertedDiscsPane.getChildren().add(disc); //Adding discs to Pane: Make disc visually appear to player
		disc.setTranslateX(column * (Circle_Diameter + 5) + Circle_Diameter/4); //To make disc appear on desired location
		//Variable used in Lambda Expression should be final or effectively final: row cannot be used directly, currentRow is used to end game
		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);  //Animation: Falling appearance
		/*disc.setTranslateY(5 * (Circle_Diameter + 5) + Circle_Diameter/4);*/ //To make disc appear at the bottom
translateTransition.setToY(row * (Circle_Diameter + 5) + Circle_Diameter/4);

//To toggle between players
		translateTransition.setOnFinished(event -> {
			isAllowedToInsert = true; //Finally when disc is dropped, allow next player to insert disc

//			To End the game after getting 4 connected discs
			if(gameEnded(currentRow,column)){  //This method return boolean value: Change void to boolean in gameEnded method
gameOver();     //If the game is Ended, call gameOver method, declare the Winner, then execute return;
return;     //After return; No code will be further executed
			}

			isPlayerOneTurn = !isPlayerOneTurn;  //When turn changes, color of disc changes
			playerNameLabel.setText(isPlayerOneTurn? playerOneTextField.getText() : playerTwoTextField.getText()); //To change the player's name according to turn
		});

translateTransition.play(); //Play animation.
	}
//	End the Game
	private boolean gameEnded(int row, int column){  //row, column where the last disc was inserted
		//Vertical Points
		//Column value same, Row value changes
//		Example: Player has inserted his last disc at row = 2 and column = 3
//		Then we need to find the address of all the elements of that column
//Index of each element present in column[row][column] : 0,3  1,3  2,3  3,3  4,3  5,3
//We need a class that will hold the pair values (0,3  1,3  2,3 .....) -> Point2D class
//Point2D class holds the value in terms of x and y coordinate (here we use x for row and y for column)
		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3,row + 3) //Range of row values = 0,1,2,3,4,5
										.mapToObj(r -> new Point2D(r,column))  //mapToObj Return list of 0,3  1,3  2,3  3,3  4,3  5,3 | r: Changing | column: Constant
										.collect(Collectors.toList());
//		For Horizontal Combination
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3,column + 3) //Range of column
				.mapToObj(col -> new Point2D(row,col))  //mapToObj Return list of 0,3  1,3  2,3  3,3  4,3  5,3 | row: Constant | col: Changing
				.collect(Collectors.toList());
//		For Diagonal Combination
		//diagonal1
		Point2D startPoint1 = new Point2D(row - 3,column + 3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());
		//diagonal2
		Point2D startPoint2 = new Point2D(row - 3,column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i,i))
				.collect(Collectors.toList());

//		For possible combinations
		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints) || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);
return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0; //We need to create a chain of 4
		for (Point2D point:points
		     ) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();
//At this (rowIndexForArray,columnIndexForArray) index of row and column, let us find if disc is present or not
	//Disc disc = insertedDiscsArray[rowIndexForArray][columnIndexForArray];
			Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexForArray);
	if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn){    //If the last inserted disc belongs to the current Player
		chain++;
		if(chain == 4){
			return true; //If combination of 4 is created, return true to the calling method | Don't use: break;
		}

	} else{
		chain = 0; //Again start creating the chain | We have not got the right combination yet
	}
		}
		return false; //We have not got combination of 4 disc
	}
//To prevent ArrayIndexOutOfBoundsException
	private Disc getDiscIfPresent(int row, int column){
//Checking if row index and column index are within Bounds Of Array
		if(row >= ROWS || row < 0 || column >= COLUMNS || column < 0) //If row or column index is invalid then return null
			return null;
		return insertedDiscsArray[row][column]; //else return the element at this position within Array
	}
	//Deciding Winner!	| Define gameOver method
	private void gameOver(){
String winner = isPlayerOneTurn? playerOneTextField.getText() : playerTwoTextField.getText();
		System.out.println("Winner is: " + winner);
//		Winner Dialog
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("Winner is:" + winner);
		alert.setContentText("Want to play again?");
		ButtonType yesButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		alert.getButtonTypes().setAll(yesButton,noButton);
		Platform.runLater(() -> {       //The code will be executed only after the Animation has ended
			Optional<ButtonType> buttonClicked = alert.showAndWait();  //Returns which Button is actually Clicked
//Optional is a class. It can store any kind of Object.
			if(buttonClicked.isPresent() && buttonClicked.get() == yesButton){  //buttonClicked.isPresent() -> If Button is present in Optional class
				//User chose Yes, so Reset the game
				resetGame();
			} else {
				//User chose No, so Exit the game
				Platform.exit();  //Close application
				System.exit(0); //Close all threads
			}
		});
	}

	public void resetGame() {
		insertedDiscsPane.getChildren().clear();    //Remove all inserted Discs from Pane
		for (int row = 0; row < insertedDiscsArray.length; row++){
			for(int col = 0; col < insertedDiscsArray[row].length; col++){
//These two loops structurally make all the elements of insertedDiscsArray[][] null
				insertedDiscsArray[row][col] = null;
			}
		}
		isPlayerOneTurn = true;  //Let Player One start the game
		playerNameLabel.setText(playerOneTextField.getText());
		createPlayground();  //Prepare a fresh Playground
	}

	//	class Disc is introduced to decide the color of disc on the basis of Player's Turn
	private static class Disc extends Circle{
		private final boolean isPlayerOneMove;
		public Disc(boolean isPlayerOneMove){                       //Constructor
this.isPlayerOneMove = isPlayerOneMove;
setRadius(Circle_Diameter/2);
setFill(isPlayerOneMove? Color.valueOf(discColor1) : Color.valueOf(discColor2));
setCenterX(Circle_Diameter/2);
setCenterY(Circle_Diameter/2);
		}
	}
public void nameInputs(){
		String input1 = playerOneTextField.getText();
		String input2 = playerTwoTextField.getText();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setNamesButton.setOnAction(event -> {
			nameInputs();
			playerNameLabel.setText(playerOneTextField.getText());
		});

	}
}
