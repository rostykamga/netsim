/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author Rostand
 */
public class TimeSlotGenerator implements Runnable{

    private final List<TimeSlotListener> listeners;
    private final AtomicBoolean running;
    private final AtomicLong TS_SEQUENCE;
    private int timeslotDuration;
    
    public TimeSlotGenerator(){
        listeners= new ArrayList<>();
        running= new AtomicBoolean(false);
        TS_SEQUENCE= new AtomicLong(1);
    }
    public TimeSlotGenerator(long sequence_start){
        this();
        TS_SEQUENCE.set(sequence_start);
    }
    
    public long getCurrentSequence(){
        return TS_SEQUENCE.get();
    }
    
    public int getTimeSlotDuration() {
        return timeslotDuration;
    }

    public void setTimeSlotDuration(int value) {
        timeslotDuration= value;
    }

    
    public void addTimeSlotListener(TimeSlotListener l){
        listeners.add(l);
    }
    
    public void removeTimeSlotListener(TimeSlotListener l){
        listeners.remove(l);
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean _running) {
        this.running.set(_running);
    }
    
    private void fireTimeSlotGenerated( TimeSlot ts){
      
        listeners.forEach(l->{l.handleTimeSlot(ts);});
    }
    

    @Override
    public void run() {
        while(isRunning()){
            fireTimeSlotGenerated(new TimeSlot(TS_SEQUENCE.getAndIncrement()));
            try {
                Thread.sleep(this.timeslotDuration);
            } catch (InterruptedException ex) {
                Logger.getLogger(TimeSlotGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
