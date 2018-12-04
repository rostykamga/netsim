/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.model;

import java.util.Date;

/**
 *
 * @author Turki
 */
public  class NetworkEvent {
    
    public static final NetworkEvent COLLISION= new NetworkEvent() ;
    public static final NetworkEvent ACK= new NetworkEvent() ;
    
    protected final Date timestamp;

    protected NetworkEvent() {
        timestamp= new Date();
    }
     
    public Date getTimestamp() {
        return timestamp;
    }
}
