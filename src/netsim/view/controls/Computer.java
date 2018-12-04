/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.view.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import netsim.model.Station;

/**
 *
 * @author Rostand
 */
public class Computer extends VBox{
    
    public static final String IMG_URL= "file:desktop.png";
    public static final int DEF_WIDTH=80;
    public static final int DEF_HEIGHT=90;
    private final BooleanProperty selected = new SimpleBooleanProperty();

    
    private final Label myLabel;
    private final ImageView myImageView;
    private final Station myStation;

    private double currentX = 0;
    private double currentY = 0;

    private double _mouseX = 0;
    private double _mouseY = 0;
    private boolean mouseDragged=false;
    private final GridPane container= new GridPane();
    
    public Computer(Station s, String title){
        
        myLabel= new Label(title);
//        myImageView= new ImageView(new Image(IMG_URL,DEF_WIDTH, DEF_HEIGHT*0.75, true, true));
        myImageView= new ImageView(new Image(IMG_URL,DEF_WIDTH, DEF_HEIGHT*0.75, true, true));
        myStation=s;
        selected.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean isSelected) -> {
            setHover(!isSelected);
            if(isSelected){
                getStyleClass().add("computer-selected");
            }
            else{
                getStyleClass().remove("computer-selected");
            }
        });
        
        buildNode();
    }
    
    public String getTitle(){
        return myLabel.getText();
    }
    
    
    
    private void buildNode(){
        
        setFillWidth(true);
        setVgrow(container, Priority.ALWAYS);
        
        GridPane.setHalignment(myImageView, HPos.CENTER); // To align horizontally in the cell
        GridPane.setValignment(myImageView, VPos.CENTER); // To align vertically in the cell
        
        GridPane.setHalignment(myLabel, HPos.CENTER); // To align horizontally in the cell
        GridPane.setHgrow(myLabel, Priority.ALWAYS); //
        GridPane.setMargin(myLabel, new Insets(0, 0, 10, 0));
        
        myLabel.getStyleClass().add("computer-label");
        getStyleClass().add("computer");
        
        setPrefSize(DEF_WIDTH, DEF_HEIGHT);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
       
        myLabel.setTooltip(new Tooltip("Click to select or drag and drop somewhere else"));
        myLabel.setContentDisplay(ContentDisplay.CENTER);
        
        container.add(myImageView, 0, 0, 1, 5);
        container.add(myLabel, 0, 6);
        
        getChildren().add(container);
        
        setOnMousePressed(pressMouse());
        
	setOnMouseDragged(dragMouse());
        
        setOnMouseReleased((MouseEvent event) -> {
            if(event.getButton()==MouseButton.PRIMARY && !mouseDragged){
                setSelected(!isSelected());
            }
        });

    }
    

    public Station getStation() {
        return myStation;
    }
    
    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    /**
	 * Creates an event handler that handles a mouse press on the node.
	 * 
	 * @return the event handler.
	 */
    private EventHandler<MouseEvent> pressMouse() {
            
        EventHandler<MouseEvent> mousePressHandler = (MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                mouseDragged=false;
                // get the current mouse coordinates according to the scene.
                _mouseX = event.getSceneX();
                _mouseY = event.getSceneY();
                
                // get the current coordinates of the draggable node.
                currentX = getLayoutX();
                currentY = getLayoutY();
            }
        };

        return mousePressHandler;
    }

    /**
     * Creates an event handler that handles a mouse drag on the node.
     * 
     * @return the event handler.
     */
    private EventHandler<MouseEvent> dragMouse() {
        EventHandler<MouseEvent> dragHandler = (MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                mouseDragged=true;
                // find the delta coordinates by subtracting the new mouse
                // coordinates with the old.
                double deltaX = event.getSceneX() - _mouseX;
                double deltaY = event.getSceneY() - _mouseY;
                
                // add the delta coordinates to the node coordinates.
                currentX += deltaX;
                currentY += deltaY;
                
                // set the layout for the draggable node.
                setLayoutX(currentX);
                setLayoutY(currentY);
                
                // get the latest mouse coordinate.
                _mouseX = event.getSceneX();
                _mouseY = event.getSceneY();
                event.consume();
            }
        };
        return dragHandler;
    }
    
}
