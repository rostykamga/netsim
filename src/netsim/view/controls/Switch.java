/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.view.controls;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import netsim.model.Bus;

/**
 *
 * @author Rostand
 */
public class Switch extends Rectangle{
    
    private final List<Line> connections= new ArrayList<>();
    
    private double currentX = 0;
    private double currentY = 0;

    private double _mouseX = 0;
    private double _mouseY = 0;
    
    
    private final Bus myBus;
    
    public Switch(Bus b){
        
        super();
        myBus= b;
        buildNode();
    }
    

    public Bus getBus() {
        return myBus;
    }
    
    
    
    private void buildNode(){
        
        setStroke(Color.GRAY);
        setFill(Color.GRAY);
        setStrokeWidth(3);
        
        setWidth(100);
        setHeight(40);
        
        setOnMousePressed(pressMouse());
        
	setOnMouseDragged(dragMouse());
        
        myBus.busStateProperty().addListener((ObservableValue<? extends Bus.STATUS> observable, Bus.STATUS oldValue, Bus.STATUS newValue) -> {
            switch(newValue){
                case COLLISION: setFill(Color.RED);
                     setStroke(Color.RED);
                break;
                case IDLE:
                    setFill(Color.GRAY);
                    setStroke(Color.GRAY);
                    break;
                default:
                    setFill(Color.LIME);
                    setStroke(Color.LIME);
            }
        });
    }
    
      /**
	 * Creates an event handler that handles a mouse press on the node.
	 * 
	 * @return the event handler.
	 */
    private EventHandler<MouseEvent> pressMouse() {
            
        EventHandler<MouseEvent> mousePressHandler = (MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) {
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
            }
        };
        return dragHandler;
    }
    
}
