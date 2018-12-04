/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Rostand
 */
public class Bus extends ActiveDevice{

    public static enum STATUS{
        COLLISION,
        TRANSMISSION,
        IDLE
    }
    
    // Bus variables
    private int currentTransmissionEllapsedTimeSlot=0;
    private final  ObjectProperty<STATUS> busState= new SimpleObjectProperty<>();
    private final ConcurrentLinkedQueue<Frame> buffer= new ConcurrentLinkedQueue<>();
    private  final ObservableList<Station> stationsList= FXCollections.observableArrayList();
    
//  Statistics variables;
    private final IntegerProperty totalFrameGenerated = new SimpleIntegerProperty();
    private final IntegerProperty totalFrameLost = new SimpleIntegerProperty();
    private final DoubleProperty  avgFrameGenerated = new SimpleDoubleProperty();
    private final IntegerProperty totalFrameAwaiting = new SimpleIntegerProperty();
    private final IntegerProperty totalFrameTransmitted = new SimpleIntegerProperty();
    private final IntegerProperty totalFrameRetransmission = new SimpleIntegerProperty();
    private final IntegerProperty timeSlotCount = new SimpleIntegerProperty();
    private final IntegerProperty transmissionTsCount = new SimpleIntegerProperty();// Timeslot used for transmission
    private final IntegerProperty idleTsCount = new SimpleIntegerProperty(); // timeslot used when idle
    private final IntegerProperty collisionTsCount = new SimpleIntegerProperty(); // timeslot used on collision
    private final DoubleProperty throughput = new SimpleDoubleProperty(); 
    private final DoubleProperty channelUtilization = new SimpleDoubleProperty();
    private final DoubleProperty channelWasteIdle = new SimpleDoubleProperty();
    private final DoubleProperty channelWasteCollision = new SimpleDoubleProperty();
    private final LongProperty totalDelay = new SimpleLongProperty();
    private final DoubleProperty avgDelay = new SimpleDoubleProperty();
    private final DoubleProperty retransmissionOverhead = new SimpleDoubleProperty();
    
    NumberBinding totalFrameCountExpression= new SimpleIntegerProperty(0).add(0);
    NumberBinding totalFrameLossExpression= new SimpleIntegerProperty(0).add(0);
    NumberBinding totalFrameAwtExpression= new SimpleIntegerProperty(0).add(0);
    NumberBinding totalFrameTransExpression= new SimpleIntegerProperty(0).add(0);
    NumberBinding totalFrameRetransExpression= new SimpleIntegerProperty(0).add(0);
    
    public Bus(){
        
        throughput.bind(Bindings.when(Bindings.equal(0, totalFrameTransmitted))
                                .then(0.0)
                                .otherwise(totalFrameTransmitted.divide(timeSlotCount.multiply(1.0))));
        
        retransmissionOverhead.bind(Bindings.when(Bindings.equal(0, totalFrameTransmitted))
                                .then(0.0)
                                .otherwise(totalFrameRetransmission.divide(totalFrameTransmitted.multiply(1.0))));
        
        avgDelay.bind(Bindings.when(Bindings.equal(0, totalFrameTransmitted))
                                .then(0.0)
                                .otherwise(totalDelay.divide(totalFrameTransmitted.multiply(1.0))));
        
        channelUtilization.bind(Bindings.when(Bindings.equal(0, timeSlotCount))
                                .then(0.0)
                                .otherwise(transmissionTsCount.divide(timeSlotCount.multiply(1.0))));
        
        channelWasteIdle.bind(Bindings.when(Bindings.equal(0, timeSlotCount))
                                .then(0.0)
                                .otherwise(idleTsCount.divide(timeSlotCount.multiply(1.0))));
        
        channelWasteCollision.bind(Bindings.when(Bindings.equal(0, timeSlotCount))
                                .then(0.0)
                                .otherwise(collisionTsCount.divide(timeSlotCount.multiply(1.0))));
        
        avgFrameGenerated.bind(Bindings.when(Bindings.equal(0, timeSlotCount))
                                .then(0.0)
                                .otherwise(totalFrameGenerated.divide(timeSlotCount.multiply(1.0))));
        
        busState.set(STATUS.IDLE);
        
//        busState.addListener((ObservableValue<? extends STATUS> observable, STATUS oldValue, STATUS newValue) -> {
//            if(newValue==STATUS.COLLISION){
//                resetTransmission();
//            }
//        });
    }
    
    public  Station getStationByID(int id){
        for (Station s : stationsList) {
            if(s.getId()==id)
                return s;
        }
        return null;
    }
    
    public void connectStation(Station station){
       
        // run stations
        totalFrameGenerated.unbind();
        totalFrameLost.unbind();
        totalFrameAwaiting.unbind();
        totalFrameRetransmission.unbind();
        
        totalFrameCountExpression= totalFrameCountExpression.add(station.frameGeneratedProperty());
        totalFrameLossExpression= totalFrameLossExpression
                .add(station.frameLostCollisionProperty())
                .add(station.frameLostOverflowProperty());
        totalFrameAwtExpression= totalFrameAwtExpression.add(station.frameAwaitingProperty());
        totalFrameTransExpression= totalFrameTransExpression.add(station.frameTransmittedProperty());
        totalFrameRetransExpression= totalFrameRetransExpression.add(station.frameRetransmissionCountProperty());
        
        totalFrameGenerated.bind(totalFrameCountExpression);
        totalFrameLost.bind(totalFrameLossExpression);
        totalFrameAwaiting.bind(totalFrameAwtExpression);
        totalFrameRetransmission.bind(totalFrameRetransExpression);
        
        stationsList.add(station);
    }
    
    public List<Station> getStationsList(){
        return stationsList;
    }
    
    public void disconnectStation(Station s){
        stationsList.remove(s);
    }
    
    
//    private void doReset(){
//        busState.set(STATUS.IDLE);
//        totalFrameTransmitted.set(0);
//        timeSlotCount.set(0);
//        transmissionTsCount.set(0);
//        collisionTsCount.set(0);
//        idleTsCount.set(0);
//        totalDelay.set(0);
//        currentTransmissionEllapsedTimeSlot=0;
//        buffer.clear();
//    }
    
    /**
     * Function to run when the bus recieves a time slot event
     * @param ts 
     */
    @Override
    protected void processTimeSlot(TimeSlot ts) {
        
        timeSlotCount.set(timeSlotCount.get()+1);

        switch(busState.get()){
            case COLLISION:
                collisionTsCount.set(collisionTsCount.get()+1);
                for(Frame fr: buffer){
                    getStationByID(fr.getSender()).notifyCollision();
                }
                resetTransmission();
                break;
            case TRANSMISSION:{
                transmissionTsCount.set(transmissionTsCount.get()+1);
                currentTransmissionEllapsedTimeSlot++;
                
                if(currentTransmissionEllapsedTimeSlot>=network.getFrameSize()){
                    // if the frame has spent the required amount of timeslot in the bus, then deliver it
                    Frame frame= buffer.poll();
                    getStationByID(frame.getDestination()).notifyIncommingFrame(frame);
                    getStationByID(frame.getSender()).notifyAck();
                    
                    totalDelay.set(totalDelay.get()+(ts.getTimeSlotValue()-frame.getTimeSlot().getTimeSlotValue()));
                    totalFrameTransmitted.set(totalFrameTransmitted.get()+1);
                    resetTransmission();
                }
                break;
            }
            case IDLE:{
                idleTsCount.set(idleTsCount.get()+1);
                break;
            }
            default:
                System.out.println("Wonderful situation !");
        }
        
        updateBufferStatus();
    }
    
    private void resetTransmission(){
        buffer.clear();
        currentTransmissionEllapsedTimeSlot=0;
        busState.set(STATUS.IDLE);
    }
    
    private void updateBufferStatus(){
        switch(buffer.size()){
            case 0:
                busState.set(STATUS.IDLE);
                break;
            case 1:
                busState.set(STATUS.TRANSMISSION);
                break;
            default:
                busState.set(STATUS.COLLISION);
                break;
        }
    }
    
    
    public void send(Frame fr){
        buffer.add(fr);
        //updateBufferState();
    }

    public STATUS getBusState() {
        return busState.get();
    }
    public ObjectProperty<STATUS> busStateProperty() {
        return busState;
    }
    
    public double getThroughput() {
        return throughput.get();
    }


    public DoubleProperty throughputProperty() {
        return throughput;
    }

    public int getTotalFrameTransmitted() {
        return totalFrameTransmitted.get();
    }


    public IntegerProperty totalFrameTransmittedProperty() {
        return totalFrameTransmitted;
    }
    
    public int getTimeSlotCount() {
        return timeSlotCount.get();
    }

    public IntegerProperty timeSlotCountProperty() {
        return timeSlotCount;
    }
    
    
    public double getChannelWasteCollision() {
        return channelWasteCollision.get();
    }

    public DoubleProperty channelWasteCollisionProperty() {
        return channelWasteCollision;
    }
    

    public double getChannelWasteIdle() {
        return channelWasteIdle.get();
    }

    public DoubleProperty channelWasteIdleProperty() {
        return channelWasteIdle;
    }
    

    public double getChannelUtilization() {
        return channelUtilization.get();
    }

    public DoubleProperty channelUtilizationProperty() {
        return channelUtilization;
    }
    
    public double getAvgFrameGenerated() {
        return avgFrameGenerated.get();
    }
    
    public int getTotalFrameAwaiting() {
        return totalFrameAwaiting.get();
    }
    
    public int getTotalFrameGenerated() {
        return totalFrameGenerated.get();
    }

    public int getTotalFrameLost() {
        return totalFrameLost.get();
    }
    
    public double getDelay() {
        return avgDelay.get();
    }
    
    public double getRetransmissionOverhead() {
        return retransmissionOverhead.get();
    }
}
