/**
 * Course: 			CS400 - Fall 2021
 * Program:			Term Project - Social Network
 * Names: 			<Ross Volkmann, McKenna Stillings, Laura Parrella>
 * Wisc Email: 		<rvolkmann@wisc.edu, mstillings@wisc.edu, lparrella@wisc.edu>
 * Web Sources: 	<http://fxexperience.com/2011/12/styling-fx-buttons-with-css/>
 * Personal Help: 	<none>
 */


/**
 * GUI MAJOR TODO:
 * -Increase size of help alert box to display all text 
 * -Set radio buttons so that they change text of submit
 * -Add Exit button that asks if use wants to save before quitting
 * -Create alert box if text input is > 30 characters or more than 1 word
 * -Consider making NetworkManager add/remove classes boolean to handle bad inputs
 * 
 * GUI minor TODO:
 * -Make canvas click detection more... round
 * -Figure out a way to resize canvas dynamically to accomodate large graphs
 */

package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//CS400 Final Project
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;

public class Main extends Application implements EventHandler<ActionEvent> {
	private List<String> args;
	
	//Values and Constants used for the layout
	private static final int WINDOW_WIDTH = 1024;
	private static final int WINDOW_HEIGHT = 600;
	private static final String APP_TITLE = "BadgerNet";
	private static final int GRAPH_HEIGHT = 425;
	private static final double RADIUS = 30;
	
	int hGap = 10; //horizontal gap between columns
	int vGap = 6; //vertical gap between rows
	int inset = 10; //padding around outside of grid
	double width1 = (WINDOW_WIDTH / 4) * 3; //Width of column 0
	double width2 = WINDOW_WIDTH - width1 - hGap - 2*inset; //Width of column 
	int smallMargin = 15;
	
	Color softGold = Color.rgb(253, 236, 166);
	Color mildTeal = Color.rgb(166, 230, 253);
	
	/////////////////////////////////////////////////
	//Declare controls, containers, and global vars//
	/////////////////////////////////////////////////
	Scene mainScene;
	Scene importDialogue, exportDialogue;
	ScrollPane graphControl;

	Button importButton;
	Button exportButton;
	Button clearButton;
	Button undoButton;
	Button helpButton;
	Button submit;
	Button executeFocus;
	RadioButton addRadio;
	RadioButton removeRadio;
	
	GridPane grid;
	Canvas canvas;
	TextField user1Text;
	TextField user2Text;
	TextField userFocusText;
	
	NetworkManager network = new NetworkManager();
	UIDialog uiDialog = new UIDialog();

	//Note - Be CAREFUL where you call/set these.  These should never be updated
	//without also calling drawGraph and they should always be called together
	ArrayList<HashSet<Person>> relationships;
	ArrayList<Person> users;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		args = this.getParameters().getRaw();//save the args

		/////////////////////////////////////////////////
		//Declare the top level controls and containers//
		/////////////////////////////////////////////////
		HBox title = new HBox();
		Label subTitle = new Label("A Social Network");
		graphControl = new ScrollPane();
		VBox sideBar = new VBox();//new VBox();
		HBox bottomBox = new HBox();//new HBox();
		HBox footer = new HBox();
		HBox utilities = new HBox();
		
		//Add labels to HBoxes and set position
		Label titleLabel = new Label("BadgerNet");
		title.getChildren().addAll(titleLabel);
		title.setAlignment(Pos.CENTER);
		Label footerLabel = new Label("An A10 Production");
		footer.getChildren().addAll(footerLabel);
		footer.setAlignment(Pos.CENTER);
		
		//Declare and configure controls for the sideBar
		Label sideLabel1 = new Label("Add or Remove from the graph");
		Label sideLabel2 = new Label("Enter 1 or 2 names");
		user1Text = new TextField();
		user2Text = new TextField();
		submit = new Button("Submit");
		Label sideLabel3 = new Label("View friends of:");
		userFocusText = new TextField();
		user1Text.setPromptText("person 1");
		user2Text.setPromptText("person 2");
		userFocusText.setPromptText("See friends of...");
		executeFocus = new Button("Change");
		
		//Create the radio buttons and contain them in an HBox
		HBox radioButtons = new HBox();
		ToggleGroup selectorGroup = new ToggleGroup();
		addRadio = new RadioButton("Add");
		addRadio.setToggleGroup(selectorGroup);
		removeRadio = new RadioButton("Remove");
		removeRadio.setToggleGroup(selectorGroup);
		addRadio.setSelected(true);
		radioButtons.getChildren().addAll(addRadio, removeRadio);
		radioButtons.setAlignment(Pos.CENTER);
		radioButtons.setSpacing(smallMargin);
		//Configure the sideBar
		sideBar.setAlignment(Pos.TOP_CENTER);
		sideBar.setSpacing(smallMargin);
		//Add controls to the sideBar
		sideBar.getChildren().addAll(sideLabel1, radioButtons, sideLabel2, user1Text, user2Text, 
				submit, sideLabel3, userFocusText, executeFocus);
		
		//Declare controls for the bottomBox
		importButton = new Button("Import Network");
		exportButton = new Button("Export Network");
		clearButton = new Button("Clear Network");
		//Configure bottomBox
		bottomBox.setSpacing(55);
		bottomBox.setAlignment(Pos.CENTER);
		//Add controls to the bottomBox
		bottomBox.getChildren().addAll(importButton, exportButton,clearButton);
		
		//Declare controls for the utilities box
		undoButton = new Button("Undo");
		helpButton = new Button("Help");
		utilities.setSpacing(35);
		utilities.setAlignment(Pos.CENTER);
		utilities.getChildren().addAll(undoButton,helpButton);
		
		/////////////////////////////////////////////
		//Create the root layout pane as a GridPane//
		/////////////////////////////////////////////
		grid = new GridPane(); 
		grid.setHgap(hGap);
		grid.setVgap(vGap);
		grid.setPadding(new Insets(inset,inset,inset,inset)); //JavaFX geometry import
		//grid.setGridLinesVisible(true); //TODO: remove later, used for debugging
		
		//Set the two main column widths -- revise this later if necessary
		grid.getColumnConstraints().add(new ColumnConstraints(width1));
		grid.getColumnConstraints().add(new ColumnConstraints(width2));
		
		//Assign Grid Positions -- reminder: setConstraints(control, col, row)
		GridPane.setConstraints(title, 0, 0);
		GridPane.setConstraints(subTitle, 0, 1);
		GridPane.setConstraints(graphControl, 0, 2);
		GridPane.setConstraints(sideBar,1, 2);
		GridPane.setConstraints(bottomBox, 0, 3);
		GridPane.setConstraints(footer, 0, 4);
		GridPane.setConstraints(utilities, 1, 3);
		
		//Title Position
		GridPane.setColumnSpan(title, 2);
		GridPane.setHalignment(title, HPos.CENTER);
		//Subtitle Position
		GridPane.setColumnSpan(subTitle, 2);
		GridPane.setHalignment(subTitle, HPos.CENTER);
		//SideBox Position 
		GridPane.setRowSpan(sideBar, 2);
		GridPane.setHalignment(sideBar, HPos.CENTER);
		GridPane.setValignment(sideBar, VPos.CENTER);
		//Graph Position
		GridPane.setHalignment(graphControl, HPos.CENTER);
		GridPane.setValignment(graphControl, VPos.CENTER);
		//TODO
		//BottomBox Position
		GridPane.setHalignment(bottomBox, HPos.CENTER);
		//Footer Position
		GridPane.setColumnSpan(footer, 2);
		GridPane.setHalignment(footer, HPos.CENTER);
		
		///////////////////////
		//GRAPH VISUALIZATION//
		///////////////////////
		
		//Initialize new canvas and display the initial starting image
		canvas = new Canvas(width1, GRAPH_HEIGHT);
		
		//Set dimensions of the graphControl
		graphControl.setPrefViewportHeight(canvas.getHeight());
		graphControl.setPrefViewportWidth(canvas.getWidth());
		
		System.out.println(sideBar.getHeight());
		graphControl.setContent(canvas);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.setFont(new Font(11.5));
		//132 is arbitrary constant the approximates length of the displayed String
		gc.fillText("Please import a network from file or add a user to begin.", canvas.getWidth()/2-132 , canvas.getHeight()/2);
		
		///////////////////////////////
		//Add children to grid object//
		///////////////////////////////
		grid.getChildren().addAll(title, subTitle, graphControl, sideBar, bottomBox, footer, utilities);
		
		////////////////////////
		//Initialize the Scene//
		////////////////////////
		mainScene = new Scene(grid, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		//////////////////////////////////////////
		//Associate Controls with Event Handlers//
		//////////////////////////////////////////
		//Buttons
		importButton.setOnAction(this);
		exportButton.setOnAction(this);
		clearButton.setOnAction(this);
		helpButton.setOnAction(this);
		undoButton.setOnAction(this);
		submit.setOnAction(this);
		executeFocus.setOnAction(this);
		
		//Canvas
		canvas.setOnMouseClicked(event -> {//Check if user clicked on a person in the network
			double canvX = event.getX() - (canvas.getWidth() / 2);
			double canvY = event.getY() - (canvas.getHeight() / 2);
			Set<String> keyNames = network.getConnectedUsers().keySet();
			for(String name : keyNames) {
				Person p = network.getConnectedUsers().get(name); 
				double upperXRange = p.getPosX() + .707*RADIUS;
				double lowerXRange = p.getPosX() - .707*RADIUS;
				double upperYRange = p.getPosY() - .707*RADIUS;
				double lowerYRange = p.getPosY() + .707*RADIUS;
				if(canvX > lowerXRange && canvX < upperXRange && 
						canvY < lowerYRange && canvY > upperYRange) {
					network.changeFocus(p.getName());//change to new focusUser
					drawGraph();//redraw the graph with the new focusUser
				}
			}
		});
		
		//Textfield controls and Event handlers
		
		//Apply css ids and classes
		title.setId("title");
		titleLabel.setId("titleLabel");
		subTitle.setId("subTitle");
		bottomBox.setId("bottomBox");
		footer.setId("footer");
		footerLabel.setId("footerLabel");
		grid.setId("grid");
		
		//Apply css to the scene
		String css = this.getClass().getResource("badger.css").toExternalForm();
		mainScene.getStylesheets().add(css);

		// Add the stuff and set the primary stage
		primaryStage.setTitle(APP_TITLE);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);

	}

	public void handle(ActionEvent event) {
		if(event.getSource()==importButton) {
			String importpath = uiDialog.showImportExportBox(grid.getScene().getWindow(), true);
		}
		if(event.getSource()==exportButton) {
			String exportpath = uiDialog.showImportExportBox(grid.getScene().getWindow(), false);
		}
		if(event.getSource()==clearButton) {
			uiDialog.showAlerts(Alert.AlertType.CONFIRMATION, grid.getScene().getWindow(),"Success!","Success! Social Network Cleared.");
			network.clearGraph();
			drawGraph();
		}
		if(event.getSource()==helpButton) {
			uiDialog.showAlerts(Alert.AlertType.INFORMATION, grid.getScene().getWindow(),"How to...",uiDialog.returnHelpText());
		}
		//DEBUG: right now Undo is sort of a garbage pail for debugging, remove all this when implementing Undo
		if(event.getSource()==undoButton) {//button clicked to undo most recent change
			//TODO: Undo the action
			System.out.println("Undo pressed.");
			network.testStaticGraph();
			drawGraph();
		}
		if(event.getSource()==submit) {//button clicked to add or remove graph elements
			executeSubmission();
		}
		if(event.getSource()==executeFocus) {//button clicked to change focus user
			String text3 = userFocusText.getText().trim();
			if(!network.validString(text3) || !network.getAllUsers().contains(text3)) {//if Change Focus input is invalid or not in the graph...
				//TODO: Alert the user regarding the input.
			}else {
			network.changeFocus(text3);
			drawGraph();
			}
		}
			
	}
	
	/**
	 * Handles behavior of clicking "Submit" and processing the text fields to add
	 * or remove users/relationships from the graph
	 */
	private void executeSubmission() {
		String text1 = user1Text.getText().trim();
		String text2 = user2Text.getText().trim();

		if (addRadio.isSelected()) {// add mode
			if (!text1.isBlank() && text2.isBlank()) {// only user1Text is populated
				if (!network.validString(text1)) {
					// TODO: alert reminding user of formatting limits
				} else {
					network.addUser(text1);
					drawGraph();
				}
			} else if (text1.isBlank() && !text2.isBlank()) {// only user2Text is populated
				if (!network.validString(text2)) {
					// TODO: alert reminding user of formatting limits
				} else {
					network.addUser(text2);
					drawGraph();
				}
			} else if (!text1.isBlank() && !text2.isBlank()) {// both fields have text
				if (!network.validString(text1) || !network.validString(text2)) {
					// TODO: alert reminding user of formatting limits
				}else {
					network.addFriendship(text1, text2);
					drawGraph();
				}
			} else {
				
			}
		} else {// remove mode
			if (!text1.isBlank() && text2.isBlank()) {// only user1Text is populated
				if (!network.validString(text1)) {
					// TODO: alert reminding user of formatting limits
				}else {
					network.removeUser(text1);
					drawGraph();
				}
				
			} else if (text1.isBlank() && !text2.isBlank()) {// only user2Text is populated
				if (!network.validString(text2)) {
					// TODO: alert reminding user of formatting limits
				}else {
					network.removeUser(text2);
					drawGraph();
				}
			} else if (!text1.isBlank() && !text2.isBlank()) {// both fields have text
				if (!network.validString(text1) || !network.validString(text2)) {
					// TODO: alert reminding user of formatting limits
				}else {
					System.out.println("remove reached");
					network.removeFriendship(text1, text2);
					drawGraph();
				}
			} else {
				//TODO:??
			}
		}
		// Blank out text fields
		user1Text.setText("");
		user2Text.setText("");
	}// executeSubmission
	
    
    private void drawGraph() {
    	//Initialize a fresh canvas, leave the old one for garbage collection
    	//canvas = new Canvas(width1, GRAPH_HEIGHT);
    	//Redefine the GraphicsContext and clear the old canvas
    	GraphicsContext gc = canvas.getGraphicsContext2D();
    	gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	
    	double midX = canvas.getWidth() / 2;
    	double midY = canvas.getHeight() / 2;
    	
    	//Get list of relationship sets from Network Manager given a root node
    	relationships = network.getFriendships();
    	HashMap<String,Person> users = network.getConnectedUsers();
    	
    	//HashSet<Person> visited = new HashSet<Person>(); //Keeps track of which lines have been drawn.
    	
		/*
		 * //DEBUG: Remove later for (int i = 0; i < relationships.size(); i++) {
		 * System.out.print("Relationship " + i + " contains:"); for (Person p :
		 * relationships.get(i)) { System.out.print(p.getName()); }
		 * System.out.println(); }
		 */
        
		// for each relationship
		for (int i = 0; i < relationships.size(); i++) {
			String[] persons = new String[2];
			double[] coords = new double[4];
			boolean secondLoop = false;
			for (Person p : relationships.get(i)) {// there should only ever be 2 Person items in a set
				if (secondLoop == false) {
					persons[0] = p.getName();
					coords[0] = p.getPosX();
					coords[1] = p.getPosY();
				} else {
					persons[1] = p.getName();
					coords[2] = p.getPosX();
					coords[3] = p.getPosY();
				}
				secondLoop = true;
			} // draw the line between the points
			gc.setStroke(Color.BLACK);
			gc.setLineWidth(1.5);
			//System.out.println("Printing " + persons[0] + " to " + persons[1]);
			gc.strokeLine(midX + coords[0], midY + coords[1], midX + coords[2], midY + coords[3]);
		} // relationship

		// Draw a circle for each person
		double yNameOffset = 3.75;//arbitrary value that seems to drop the text far enough
		
		Set<String> connectedUserNames = users.keySet();
		for(String name : connectedUserNames) {
			Person p = users.get(name);
			if(p.getName().equals(network.getFocus())) {//if the person is the focusUser draw them in softGold
				gc.setFill(softGold);	
			}else {//otherwise draw the circle as mildTeal
			gc.setFill(mildTeal);
			}
			//fillOval(x coord, y coord, oval width, oval height), circles are drawn from upper left corner so subtract radius to center
			gc.fillOval(midX + (p.getPosX() - RADIUS), midY + (p.getPosY()-RADIUS), 2*RADIUS, 2*RADIUS);
			gc.setFill(Color.BLACK);//set text color to black
			gc.setFont(new Font(12.5));
			double xNameOffset = 3.5*(p.getName().length());//place text on -x axis as a function of its length
			gc.fillText(p.getName(), midX + p.getPosX() - xNameOffset, midY + p.getPosY() + yNameOffset);
		}
		//System.out.println("Debug: bottom of draw");
		// Provide graphControl with reference to new canvas.
		//graphControl.setContent(canvas);
	}//drawGraph
    
}//GUI
