/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.List;

/**
 *
 * @author Rostand
 */
public class Network {
    
    public static class Settings{
        public  double  frameGenerationProbability;
        public  int nbStations ;
        public  int frameSize ;
        public  int maxRandInterval;
        public  int maxRetransmission;
        public  int timeSlotDuration;
    }

    private final Settings settings;
    private  final  Bus bus;
    
    public Network(Bus mybus, Settings set){
        bus= mybus;
        settings=set;
        buildNetwork();
    }
    
    private void buildNetwork(){
        bus.setNetwork(this);
        
        for(int i=1; i<=settings.nbStations; i++){
            Station s= new Station(this);
            bus.connectStation(s);
        }
        
    }
    

    public  Bus getBus(){
        return bus;
    }
    
    public List<Station> getStationsList() {
        return bus.getStationsList();
    }

    public double getFrameGenerationProbability() {
        return settings.frameGenerationProbability;
    }

    public void setFrameGenerationProbability(double frameGenerationProbability) {
        this.settings.frameGenerationProbability = frameGenerationProbability;
    }

    public int getFrameSize() {
        return settings.frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.settings.frameSize = frameSize;
    }

    public int getMaxRandInterval() {
        return settings.maxRandInterval;
    }

    public void setMaxRandInterval(int maxRandInterval) {
        this.settings.maxRandInterval = maxRandInterval;
    }

    public int getMaxRetransmission() {
        return settings.maxRetransmission;
    }

    public void setMaxRetransmission(int maxRetransmission) {
        this.settings.maxRetransmission = maxRetransmission;
    }

    public int getTimeSlotDuration() {
        return settings.timeSlotDuration;
    }

    public void setTimeSlotDuration(int timeSlotDuration) {
        this.settings.timeSlotDuration = timeSlotDuration;
    }

    public int getNbStations() {
        return settings.nbStations;
    }
    
}
