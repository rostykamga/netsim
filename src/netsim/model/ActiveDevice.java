/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

/**
 *
 * @author Rostand
 */
public abstract class ActiveDevice{
    
    protected Network network;
    protected Runnable activity;
    private TimeSlot currentTimeSlot;
    
    protected ActiveDevice(){
        activity= () -> {
            processTimeSlot(currentTimeSlot);
        };
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network simulationEnv) {
        this.network = simulationEnv;
    }
    
    public Runnable getActivityForTimeSlot(TimeSlot ts){
        currentTimeSlot=ts;
        return activity;
    }
    
    protected abstract void processTimeSlot(TimeSlot ts);
}