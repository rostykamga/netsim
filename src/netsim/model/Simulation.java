/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Rostand
 */
public class Simulation implements TimeSlotListener{

    
    public static enum STATUS{
        RUNNING,
        PAUSED,
        NEW
    }
    
    public static class StepResult{
        public  int s_timeslot;
        public  Bus.STATUS s_state;
        public  int s_totalFrameGenerated;
        public  int s_totalFrameLost;
        public  double s_avgFrameGenerated;
        public  int s_totalFrameAwaiting;
        public  int s_totalFrameTransmitted ;
        public  double s_throughput ;
        public  double s_channelUtilization ;
        public  double s_channelWasteIdle ;
        public  double s_channelWasteCollision ;
        public  double s_retransmissionOverhead;
        public  double s_delay;
    }
    
    public  final Network network;
    private final ExecutorService executorService;
    private TimeSlotGenerator tsGenerator;
    private final ObjectProperty<STATUS> state = new SimpleObjectProperty<>(STATUS.NEW);
    
    private final  List<SimulationListener> listeners;
    
    public Simulation(Network.Settings netSettings){

        network= new Network(new Bus(), netSettings);
        
        executorService= Executors.newCachedThreadPool();
        listeners=new ArrayList<>();
    }
    
    public void addSimulationListener(SimulationListener l){
        listeners.add(l);
    }
    public void removeSimulationListener(SimulationListener l){
        listeners.remove(l);
    }
    
    @Override
    public void handleTimeSlot(TimeSlot ts) {
        
        network.getStationsList().forEach((s) -> {
            executorService.execute(s.getActivityForTimeSlot(ts));
        });
        executorService.execute(network.getBus().getActivityForTimeSlot(ts));
        
        StepResult result= new StepResult();
        result.s_avgFrameGenerated= network.getBus().getAvgFrameGenerated();
        result.s_channelUtilization= network.getBus().getChannelUtilization();
        result.s_channelWasteCollision= network.getBus().getChannelWasteCollision();
        result.s_channelWasteIdle= network.getBus().getChannelWasteIdle();
        result.s_state= network.getBus().getBusState();
        result.s_throughput= network.getBus().getThroughput();
        result.s_timeslot= network.getBus().getTimeSlotCount();
        result.s_totalFrameAwaiting= network.getBus().getTotalFrameAwaiting();
        result.s_totalFrameGenerated= network.getBus().getTotalFrameGenerated();
        result.s_totalFrameLost= network.getBus().getTotalFrameLost();
        result.s_totalFrameTransmitted= network.getBus().getTotalFrameTransmitted();
        result.s_delay= network.getBus().getDelay();
        result.s_retransmissionOverhead= network.getBus().getRetransmissionOverhead();
        
        listeners.forEach(l->{l.updateStep(result);});
    }
    
    public void play(){
        if(tsGenerator==null)
            tsGenerator= new TimeSlotGenerator();
        else
            tsGenerator= new TimeSlotGenerator(tsGenerator.getCurrentSequence());
        tsGenerator.setTimeSlotDuration(network.getTimeSlotDuration());
        tsGenerator.addTimeSlotListener(this);
        tsGenerator.setRunning(true);
        executorService.execute(tsGenerator);
        setState(STATUS.RUNNING);
    }
    
    public void pause(){
        if(tsGenerator!=null)
            tsGenerator.setRunning(false);
        
    }
    
    public void exit(){
        if(tsGenerator!=null){
            tsGenerator.setRunning(false);
        }
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public STATUS getState() {
        return state.get();
    }

    private void setState(STATUS value) {
        state.set(value);
    }

    public ObjectProperty stateProperty() {
        return state;
    }
    
    public TimeSlotGenerator getTimeSlotGenerator(){
        return tsGenerator;
    }
    
}
