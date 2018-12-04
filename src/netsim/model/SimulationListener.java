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
public interface SimulationListener {
    
    public void updateStep(Simulation.StepResult result);
}
