/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Rostand
 */
public class TimeSlot extends NetworkEvent{
    
    
    private static final AtomicLong TS_COUNT= new AtomicLong(1);
    
    private final long timeSlotValue;

    public TimeSlot() {
        super();
        timeSlotValue=TS_COUNT.getAndIncrement();
    }
    
    public TimeSlot(long sequence) {
        super();
        timeSlotValue=sequence;
    }
    
    public long getTimeSlotValue() {
        return timeSlotValue;
    }
    
    @Override
    public String toString() {
        return "TimeSlot{" + "timestamp="+ timestamp+ '}';
    }
}
