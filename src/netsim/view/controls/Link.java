/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.view.controls;

import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import netsim.model.Station;

/**
 *
 * @author Rostand
 */
public class Link extends Line{
    
    private final Computer myComputer;
    private final Switch mySwitch;
    
    public Link(Computer c, Switch s){
        
        this.myComputer= c;
        this.mySwitch= s;
        customize();
    }
    
    private void customize(){

        setStroke(Color.DARKGRAY);
        setStrokeWidth(2);
        setStrokeLineCap(StrokeLineCap.BUTT);
        getStrokeDashArray().setAll(10.0, 5.0);
        setMouseTransparent(true);

        startXProperty().bind(myComputer.layoutXProperty().add(myComputer.widthProperty().divide(2)));
        startYProperty().bind( myComputer.layoutYProperty().add(myComputer.heightProperty().divide(2)));
        
        endXProperty().bind(mySwitch.layoutXProperty().add(mySwitch.widthProperty().divide(2)));
        endYProperty().bind(mySwitch.layoutYProperty().add(mySwitch.heightProperty().divide(2)));
        
        myComputer.getStation().deviceStatusProperty().addListener((ObservableValue<? extends Station.STATUS> observable, Station.STATUS oldValue, Station.STATUS newValue) -> {
            switch(newValue){
                case COLLISION:{
                    setStroke(Color.RED);
                    break;
                }
                case IDLE:{
                    setStroke(Color.DARKGRAY);
                    break;
                }
                case TRANSMITING:{
                    setStroke(Color.LIME);
                    break;
                }
                case AWAITING:{
                    setStroke(Color.BLUE);
                    break;
                }
            }
        });

    }
}
