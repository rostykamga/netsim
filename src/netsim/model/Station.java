/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Rostand
 */
public class Station extends ActiveDevice{
    
    public static enum STATUS{
        TRANSMITING,
        AWAITING,
        IDLE,
        COLLISION
    }
    
    private final Random RD= new Random();
    private static final AtomicInteger ID_SEQUENCE= new AtomicInteger(1);
    
    private final int id;
    private int currentFrameCollisionCount;
    private int awaitTime=0;// The number of time slot to wait before any transmission
    private int slotEllapsed=0;
    private final IntegerProperty timeSlotCount = new SimpleIntegerProperty(0); 
    private final ObjectProperty<Frame> currentFrame=new SimpleObjectProperty<>();
    private final ObjectProperty<STATUS> deviceStatus=new SimpleObjectProperty<>(STATUS.IDLE);
    
    private final IntegerProperty frameReceived = new SimpleIntegerProperty(0);
    private final IntegerProperty frameTransmitted = new SimpleIntegerProperty(0);
    private final IntegerProperty frameLostCollision = new SimpleIntegerProperty(0);
    private final IntegerProperty frameLostOverflow = new SimpleIntegerProperty(0);
    private final IntegerProperty frameCollision = new SimpleIntegerProperty(0);
    private final IntegerProperty frameGenerated = new SimpleIntegerProperty(0);
    private final DoubleProperty  avgFramePerTimeSlot = new SimpleDoubleProperty(-1.0);
    
    private final IntegerProperty frameSuccessfullyRetransmitted = new SimpleIntegerProperty(0);
    private final IntegerProperty frameRetransmissionCount = new SimpleIntegerProperty(0);
    private final IntegerProperty frameAwaiting = new SimpleIntegerProperty();
    
    
    private boolean collisionOccured=false;
    
    private int retransmissionCount;
    
    
    public Station(Network net) {
        
        id= ID_SEQUENCE.getAndIncrement();
        network=net;
        
        timeSlotCount.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if(newValue.doubleValue()!=0)
                Platform.runLater(()->{
                    avgFramePerTimeSlot.set(frameGenerated.get()/newValue.doubleValue());
                });
        });
        
        frameGenerated.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if(timeSlotCount.get()!=0)
                Platform.runLater(()->{
                    avgFramePerTimeSlot.set(newValue.intValue()/timeSlotCount.get());
                });
        });
        
        frameAwaiting.bind(Bindings.when(Bindings.isNull(currentFrame)).then(0).otherwise(1));
        go2IdleState();
    }
    
    @Override
    public Runnable getActivityForTimeSlot(TimeSlot ts){
        timeSlotCount.set(timeSlotCount.get()+1);
        
        return super.getActivityForTimeSlot(ts);
    }
    
    
    /**
     * clears the current transmission and reset transmission variables
     */
    private void go2IdleState(){
        
        deviceStatus.set(STATUS.IDLE);
        collisionOccured=false;
        currentFrameCollisionCount=0;
        awaitTime=0;
        slotEllapsed=0;
        Platform.runLater(()->{ currentFrame.set(null);});
        retransmissionCount=0;
    }
    
    
    public void notifyCollision(){
        collisionOccured=true;
        deviceStatus.set(STATUS.COLLISION);
        currentFrameCollisionCount++;
        incProperty(frameCollision, 1);
    }
    
    private void incProperty(IntegerProperty prop, int inc){
        Platform.runLater(()->{prop.set(prop.get()+inc);});
    }
    
    public void notifyIncommingFrame(Frame frame){
        incProperty(frameReceived, 1);
    }
    
    public void notifyAck(){
        incProperty(frameTransmitted, 1);
        if(awaitTime!=0)
            incProperty(frameSuccessfullyRetransmitted, 1);
        
        go2IdleState();
    }
    
    
    // ==================================================================
    
    @Override
    protected void processTimeSlot(TimeSlot ts){
        
        switch(deviceStatus.get()){
            case IDLE: 
                attemptNewTransmission(ts);
                break;
            case AWAITING: 
                if(slotEllapsed>=awaitTime){// the station waited for the amount of time and is trying to retransmit
                    retransmit();
                }
                else slotEllapsed++;
                
                break;
            case COLLISION:{
                 go2AwaitingState();
            }
        }
    }
    
    private void go2AwaitingState(){
        
        deviceStatus.set(STATUS.AWAITING);
        estimateNextTransmissionSlot();
    }
    
    private void attemptNewTransmission(TimeSlot ts){
        // tries to generate a new frame
        double prob= RD.nextDouble();
        if(prob<=network.getFrameGenerationProbability()){

            // selects a random destinator
            int randomDestinator=-1;
            while(randomDestinator<0 || randomDestinator==id){
                randomDestinator=RD.nextInt(network.getNbStations())+1;
            }
            // creates the frame
            
            currentFrame.set(new Frame(id, randomDestinator, ts));
            //Increment the number of frame generated
            incProperty(frameGenerated, 1);
            transmit(currentFrame.get());
        }
        else{// couldn't generate a frame
            go2IdleState();
        }
    }
    
    private void transmit(Frame fr){
        
        Bus systemBus= network.getBus();
        if(systemBus.getBusState()==Bus.STATUS.IDLE){
            deviceStatus.set(STATUS.TRANSMITING);
            System.out.println("id "+id +" sent frame ");
            systemBus.send(fr);
        }
        else{
            go2AwaitingState();
        }
    }
    
    private void retransmit(){
        slotEllapsed=0;
        incProperty(frameRetransmissionCount, 1);
        retransmissionCount++;
        
        if(retransmissionCount>=network.getMaxRetransmission()){
            if(collisionOccured)
                frameLostCollision.set(frameLostCollision.get()+1);
            else
                frameLostOverflow.set(frameLostOverflow.get()+1);
            go2IdleState();
        }
        else{
            transmit(currentFrame.get());
        }
    }
    
    
    /**
    * This method should be called by the network to notify a collision to the station
    * The “binary exponential backoff” algorithm can be implemented in discrete time as follows. After
       mth collisions, a station waits for a random number of time slots, which is chosen uniformly at
       random from the set {0,1,2,... ,2n−1} where n = min{10,m}, before attempting to retransmit. After
       16 collisions, the station gives up (or simply drops the frame). Note that the maximum
       randomization interval of 210 − 1 and the maximum retransmission count of 15 are changeable
    */
    private void estimateNextTransmissionSlot(){

        slotEllapsed=0;
        
        int n= collisionOccured
                ?Math.min(network.getMaxRandInterval(), currentFrameCollisionCount)
                :network.getMaxRandInterval();
        
        // choose a number between m and n
        int time= RD.nextInt(n+1);
        awaitTime= time==n?(2^time -1):2^time;
        
        System.out.println(String.format("Station %d is going to skip %d time slots", id, awaitTime));
    }
    
    
    // ============================ GETTERS AND SETTERS ==============================

    public int getId() {
        return id;
    }
    

    public IntegerProperty frameReceivedProperty() {
        return frameReceived;
    }
    

    public IntegerProperty frameTransmittedProperty() {
        return frameTransmitted;
    }

    
    public int getTotalFrameLossCollision() {
        return frameLostCollision.get();
    }


    public IntegerProperty frameLostCollisionProperty() {
        return frameLostCollision;
    }
    
    public IntegerProperty frameLostOverflowProperty() {
        return frameLostOverflow;
    }
    
    public ObjectProperty<STATUS> deviceStatusProperty() {
        return deviceStatus;
    }

    public DoubleProperty avgFramePerTimeSlotProperty() {
        return avgFramePerTimeSlot;
    }
    

    public IntegerProperty frameGeneratedProperty() {
        return frameGenerated;
    }
    

    public IntegerProperty collisionsProperty() {
        return frameCollision;
    }
    
    public IntegerProperty frameAwaitingProperty() {
        return frameAwaiting;
    }
    
    public IntegerProperty frameRetransmittedProperty() {
        return frameSuccessfullyRetransmitted;
    }
    
    public IntegerProperty frameRetransmissionCountProperty() {
        return frameRetransmissionCount;
    }
}