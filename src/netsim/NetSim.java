/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netsim.controller.SimulationController;

/**
 *
 * @author Rostand
 */
public class NetSim extends Application {
    
    public static Stage primaryStage;
    private static SimulationController simController;
     
    
    @Override
    public void start(Stage stage) throws Exception {
        
        primaryStage=stage;
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(NetSim.class.getResource("view/SimulationView.fxml"));
        Parent root = loader.load();
        simController= loader.getController();
        
        Scene scene = new Scene(root);
        
        primaryStage.getIcons().add(new Image("file:logo.png"));
            
        stage.setMaximized(true);
        
        stage.setTitle("Network Simulator v1.0");
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                exitApp();
            }
        });
        stage.setScene(scene);
        stage.show();
    }
    
    public static void exitApp(){
        primaryStage.hide();
        if(simController!=null)
            simController.stopAllSimulations();
        
        Platform.exit();
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
