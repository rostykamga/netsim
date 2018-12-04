/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.converter.NumberStringConverter;
import netsim.model.Station;

/**
 *
 * @author Rostand
 */
public class StationStatController implements Initializable{
    
    
    @FXML private Label labGenerated;
    @FXML private Label labTransmitted;
    @FXML private Label labReceived;
    @FXML private Label labAvg;
    @FXML private Label labCollison;
    @FXML private Label labLost;
    @FXML private Label labRetransmitted;
    @FXML private Label labAwaiting;
    
    private final NumberStringConverter converter= new NumberStringConverter();
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public void bindToStation(Station s){
        
        labGenerated.textProperty().bindBidirectional(s.frameGeneratedProperty(), converter);
        labTransmitted.textProperty().bindBidirectional(s.frameTransmittedProperty(), converter);
        labReceived.textProperty().bindBidirectional(s.frameReceivedProperty(), converter);
        labAvg.textProperty().bindBidirectional(s.avgFramePerTimeSlotProperty(), converter);
        labCollison.textProperty().bindBidirectional(s.collisionsProperty(), converter);
        labLost.textProperty().bindBidirectional(s.frameLostCollisionProperty(), converter);
        labRetransmitted.textProperty().bindBidirectional(s.frameRetransmittedProperty(), converter);
        labAwaiting.textProperty().bindBidirectional(s.frameAwaitingProperty(), converter);
    }
}
