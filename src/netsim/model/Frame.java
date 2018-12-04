/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.Objects;

/**
 *
 * @author Rostand
 */
public class Frame{
    
    private int sender;
    private int destination;
    private final TimeSlot timeSlot;

    /**
     * Creates an empty frame
     * @param timeSlot
     */
    public Frame(TimeSlot timeSlot) {
        super();
        this.timeSlot=timeSlot;
    }

    /**
     * Create a frame and set its sender and destinator
     * @param sender
     * @param dest 
     * @param _timeSlot 
     */
    public Frame(int sender, int dest, TimeSlot _timeSlot) {
        this(_timeSlot);
        this.sender = sender;
        this.destination = dest;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int dest) {
        this.destination = dest;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.sender;
        hash = 97 * hash + this.destination;
        hash = 97 * hash + Objects.hashCode(this.timeSlot);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Frame other = (Frame) obj;
        if (this.sender != other.sender) {
            return false;
        }
        if (this.destination != other.destination) {
            return false;
        }
        if (!Objects.equals(this.timeSlot, other.timeSlot)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Frame{" + "sender=" + sender + ", destinator=" + destination + ", timeSlot=" + timeSlot + '}';
    }
    
}
