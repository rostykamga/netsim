/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netsim.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;
import javax.imageio.ImageIO;
import netsim.NetSim;
import netsim.model.Network;
import netsim.model.Simulation;
import netsim.model.SimulationListener;
import netsim.model.Station;
import netsim.view.controls.Switch;
import netsim.view.controls.Computer;
import netsim.view.controls.Link;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * FXML Controller class
 *
 * @author Rostand
 */
public class SimulationController implements Initializable, SimulationListener{
    
    final IntegerProperty timeslotDuration = new SimpleIntegerProperty(1000);
    final IntegerProperty nbStations = new SimpleIntegerProperty(6);
    final IntegerProperty frameSize = new SimpleIntegerProperty(10);
    final IntegerProperty maxInternal = new SimpleIntegerProperty(10);
    final IntegerProperty maxRetransmission = new SimpleIntegerProperty(15);
    final DoubleProperty frameProbability = new SimpleDoubleProperty(0.1);
    
    private static final int DEFAULT_TIMESLOT_DURATION=1000;
    private static final int DEFAULT_NB_STATIONS=6;
    private static final int DEFAULT_FRAMESIZE=10;
    private static final int DEFAULT_MAX_INTERVAL=10;
    private static final int DEFAULT_MAX_RETRANSMISSION=15;
    private static final double DEFAULT_FRAME_PROBABILITY=0.1;

    @FXML private Line legendTransmitting;
    @FXML private Line legendIdle;
    @FXML private Line legendAwaiting;
    @FXML private Line legendCollision;
    
    @FXML private MenuItem menuNewSimulation;
    
    @FXML private CheckMenuItem menuShowDevProperties;
    @FXML private CheckMenuItem menuShowSimulationSettings;
    
    @FXML private AnchorPane simulationSettingsPane;
    @FXML private AnchorPane devicePropertiesPane;
    
    // Station result
    @FXML private Label labGenerated;
    @FXML private Label labTransmitted;
    @FXML private Label labReceived;
    @FXML private Label labAvg;
    @FXML private Label labCollison;
    @FXML private Label labLost;
    @FXML private Label labRetransmitted;
    @FXML private Label labAwaiting;
    
    // Global results
    @FXML private Label labEllapsed;
//    @FXML private Label labSimulationStatus;
    @FXML private Label labFrameGenerated;
    @FXML private Label labAvgFrame;
    @FXML private Label labFrameTransmitted;
    @FXML private Label labFrameLost;
    @FXML private Label labFrameAwt;
    @FXML private Label labChannelUse;
    @FXML private Label labChannelWasteIdle;
    @FXML private Label labChannelWasteCollision;
    @FXML private Label labThroughput;
    @FXML private Label labRetransmissionOverhead;
    @FXML private Label labDelay;
    
    @FXML private TextField txtSlotDuration;
    @FXML private TextField txtFrameProbability;
    
    @FXML private TextField txtStations;
    @FXML private TextField txtFrameSize;
    @FXML private TextField txtMaxInterval;
    @FXML private TextField txtMaxRetrans;
    
    @FXML private Button btnPlay;
    @FXML private Button btnStop;
    
    @FXML private Accordion accordion;
    
    @FXML private Pane workPane;
    
    @FXML private LineChart<String, Number> framesChart;
    @FXML private LineChart<String, Number> avgFrameChart;
    @FXML private LineChart<String, Number> channelChart;
    @FXML private LineChart<String, Number> throughputChart;
    @FXML private LineChart<String, Number> retransmissionChart;
    
    private final ObjectProperty<Simulation> currentSimulation= new SimpleObjectProperty<>(null);
    private final ObjectProperty<Station> selectedStation= new SimpleObjectProperty<>(null);
    private final NumberStringConverter converter= new NumberStringConverter();
    private ChangeListener<Simulation> currentSimulationListener;
    private final BooleanProperty simulationRunningProperty= new SimpleBooleanProperty(false);
    
    private Map<String, TitledPane> accordionElements= new HashMap<>();
    
    private final XYChart.Series frameGeneratedSerie = new XYChart.Series();
    private final XYChart.Series frameTransmittedSerie = new XYChart.Series();
    private final XYChart.Series frameLostSerie = new XYChart.Series();
    private final XYChart.Series frameAwaitingSerie = new XYChart.Series();
    
    XYChart.Series avgFrameAwaitingSerie = new XYChart.Series();
    
    private final XYChart.Series channelUseSerie = new XYChart.Series();
    private final XYChart.Series channelWasteIdleSerie = new XYChart.Series();
    private final XYChart.Series channelWasteCollisionSerie = new XYChart.Series();
    
    XYChart.Series channelThroughputSerie = new XYChart.Series();
    
    private final XYChart.Series retransmissionOverheadSerie = new XYChart.Series();
    private final XYChart.Series delaySerie = new XYChart.Series();
        
        
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        menuNewSimulation.disableProperty().bind(simulationRunningProperty);
        
        simulationSettingsPane.managedProperty().bind(menuShowSimulationSettings.selectedProperty());
        simulationSettingsPane.visibleProperty().bind(menuShowSimulationSettings.selectedProperty());
        
        devicePropertiesPane.managedProperty().bind(menuShowDevProperties.selectedProperty());
        devicePropertiesPane.visibleProperty().bind(menuShowDevProperties.selectedProperty());
        
        btnPlay.disableProperty().bind(simulationRunningProperty);
        btnStop.disableProperty().bind(simulationRunningProperty.not());
        
        selectedStation.addListener((ObservableValue<? extends Station> observable, Station oldValue, Station seleted) -> {
            selectedStationChanged(seleted);
        });
        
        currentSimulationListener= (ObservableValue<? extends Simulation> observable, Simulation oldValue, Simulation newValue) -> {
            currentSimulationChanged(oldValue, newValue);
        };
        
        currentSimulation.addListener(currentSimulationListener);
        
        initChats();
        initLabels();
        initLegend();
        handleNewSimulationMenu();
    }  
    
    private void initLegend(){
        
        legendTransmitting.getStrokeDashArray().setAll(10.0, 5.0);
        legendTransmitting. setStroke(Color.LIME);
        
        legendIdle.getStrokeDashArray().setAll(10.0, 5.0);
        legendIdle. setStroke(Color.GRAY.deriveColor(0, 1, 1, 0.5));
        
        legendAwaiting.getStrokeDashArray().setAll(10.0, 5.0);
        legendAwaiting. setStroke(Color.BLUE);
        
        legendCollision.getStrokeDashArray().setAll(10.0, 5.0);
        legendCollision. setStroke(Color.RED);

    }
    
    private void initLabels(){
        labEllapsed.setText("");
        labFrameGenerated.setText("");
        labAvgFrame.setText("");
        labFrameTransmitted.setText("");
        labFrameLost.setText("");
        labFrameAwt.setText("");
        labChannelUse.setText("");
        labChannelWasteIdle.setText("");
        labChannelWasteCollision.setText("");
        labThroughput.setText("");
        labRetransmissionOverhead.setText("");
        labDelay.setText("");
    }
    
    private void initChats(){
        
        frameGeneratedSerie.setName("Generated");
        frameTransmittedSerie.setName("Transmitted");
        frameLostSerie.setName("Lost");
        frameAwaitingSerie.setName("Awaiting");
        //avgFrameAwaitingSerie.setName("My portfolio");
        channelUseSerie.setName("Channel Utilization");
        channelWasteIdleSerie.setName("Channel Waste/Idle");
        channelWasteCollisionSerie.setName("Channel Waste/Collision");
        //channelThroughputCollisionSerie.setName("My portfolio");
        retransmissionOverheadSerie.setName("Retransmission Overhead");
        delaySerie.setName("Delay");
        
        framesChart.setCreateSymbols(false);
        avgFrameChart.setCreateSymbols(false);
        channelChart.setCreateSymbols(false);
        throughputChart.setCreateSymbols(false);
        retransmissionChart.setCreateSymbols(false);
        
        framesChart.getData().addAll(frameGeneratedSerie, frameTransmittedSerie, frameLostSerie, frameAwaitingSerie);
        avgFrameChart.getData().add(avgFrameAwaitingSerie);
        avgFrameChart.setLegendVisible(false);
        channelChart.getData().addAll(channelUseSerie, channelWasteIdleSerie, channelWasteCollisionSerie);
        throughputChart.getData().add(channelThroughputSerie);
        throughputChart.setLegendVisible(false);
        retransmissionChart.getData().addAll(retransmissionOverheadSerie, delaySerie);
    }
    
    private void handleComputerSelected(Computer c, boolean selected){
        
        if(selected){
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(NetSim.class.getResource("view/StationStatView.fxml"));
                AnchorPane root = loader.load();
                StationStatController controller= loader.getController();
                
                controller.bindToStation(c.getStation());
                
                TitledPane statsPane= new TitledPane(c.getTitle(), root);
                accordionElements.put(c.getTitle(), statsPane);
                accordion.getPanes().add(statsPane);
                accordion.setExpandedPane(statsPane);
            } catch (IOException ex) {
                Logger.getLogger(SimulationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            TitledPane statsPane= accordionElements.get(c.getTitle());
            accordion.getPanes().remove(statsPane);
        }
    }

    @Override
    public void updateStep(Simulation.StepResult result) {
        if(result!=null){
            
            Platform.runLater(()->{
                labEllapsed.setText(""+result.s_timeslot);
                labFrameGenerated.setText(""+result.s_totalFrameGenerated);
                labAvgFrame.setText( String.format("%6.4f", result.s_avgFrameGenerated));
                labFrameTransmitted.setText(""+result.s_totalFrameTransmitted);
                labFrameLost.setText(""+result.s_totalFrameLost);
                labFrameAwt.setText(""+result.s_totalFrameAwaiting);
                labChannelUse.setText( String.format("%6.4f", result.s_channelUtilization));
                labChannelWasteIdle.setText( String.format("%6.4f", result.s_channelWasteIdle));
                labChannelWasteCollision.setText( String.format("%6.4f", result.s_channelWasteCollision));
                labThroughput.setText( String.format("%6.4f", result.s_throughput));
                labRetransmissionOverhead.setText( String.format("%6.4f", result.s_retransmissionOverhead));
                labDelay.setText( String.format("%6.4f", result.s_delay));
                
                String ts= result.s_timeslot+"";
                
                frameGeneratedSerie.getData().add(new XYChart.Data(ts, result.s_totalFrameGenerated));
                frameTransmittedSerie.getData().add(new XYChart.Data(ts, result.s_totalFrameTransmitted));
                frameLostSerie.getData().add(new XYChart.Data(ts, result.s_totalFrameLost));
                frameAwaitingSerie.getData().add(new XYChart.Data(ts, result.s_totalFrameAwaiting));
                
                avgFrameAwaitingSerie.getData().add(new XYChart.Data(ts, result.s_avgFrameGenerated));
                
                channelUseSerie.getData().add(new XYChart.Data(ts, result.s_channelUtilization));
                channelWasteIdleSerie.getData().add(new XYChart.Data(ts, result.s_channelWasteIdle));
                channelWasteCollisionSerie.getData().add(new XYChart.Data(ts, result.s_channelWasteCollision));
                
                channelThroughputSerie.getData().add(new XYChart.Data(ts, result.s_throughput));
                
                retransmissionOverheadSerie.getData().add(new XYChart.Data(ts, result.s_retransmissionOverhead));
                delaySerie.getData().add(new XYChart.Data(ts, result.s_delay));
            });
        }
    }
    
    public void stopAllSimulations(){
        if(currentSimulation.get()!=null){
            currentSimulation.get().pause();
        }
    }
    
    @FXML private void handleExitMenu(){
        netsim.NetSim.exitApp();
    }
    
    @FXML private void handleNewSimulationMenu(){

        currentSimulation.set(null);
        workPane.getChildren().clear();
        
        txtFrameProbability.setText(DEFAULT_FRAME_PROBABILITY+"");
        txtFrameSize.setText(DEFAULT_FRAMESIZE+"");
        txtMaxInterval.setText(DEFAULT_MAX_INTERVAL+"");
        txtMaxRetrans.setText(DEFAULT_MAX_RETRANSMISSION+"");
        txtSlotDuration.setText(DEFAULT_TIMESLOT_DURATION+"");
        txtStations.setText(DEFAULT_NB_STATIONS+"");
        setSimulationSettingsDisable(false);
    }
    
    private void currentSimulationChanged(Simulation old, Simulation newSim){
        
        if(old!=null){
            old.removeSimulationListener(this);
        }
        if(newSim!=null){
            newSim.addSimulationListener(this);
        }
    }
    
    
    @FXML private void handleBtnStop(){
        
        simulationRunningProperty.set(false);
        currentSimulation.get().pause();
    }
    
    private Simulation createSimulation(){
        
        Network.Settings settings= new Network.Settings();
        String currentField="";
        try{
            currentField="Frame generation probability";
            settings.frameGenerationProbability= Double.parseDouble(txtFrameProbability.getText());
            
            currentField="Frame size";
            settings.frameSize= Integer.parseInt(txtFrameSize.getText());
            
            currentField="Maximum interval";
            settings.maxRandInterval=Integer.parseInt(txtMaxInterval.getText());
            
            currentField="Maximum retransmission count";
            settings.maxRetransmission= Integer.parseInt(txtMaxRetrans.getText());
            
            currentField="Time slot duration";
            settings.timeSlotDuration= Integer.parseInt(txtSlotDuration.getText());
            if(settings.timeSlotDuration<500){
                currentField="Time slot duration must be greater than 500ms";
                throw new IllegalArgumentException();
            }
            
            currentField="Number of station";
            settings.nbStations= Integer.parseInt(txtStations.getText());
            if(settings.nbStations<2){
                currentField="Number of station must be greater than 2";
                throw new IllegalArgumentException();
            }
        }
        catch(IllegalArgumentException ex){
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setTitle("invalid number ");
            alert.setHeaderText("Please fix the following error");
            alert.setContentText("The "+currentField+" you typed is invalid");
            alert.showAndWait();
            return null;
        }
        setSimulationSettingsDisable(true);
        return new Simulation(settings);
    }
    
    @FXML private void handleBtnPlay(){
        
        Simulation sim= currentSimulation.get();
        if(sim==null){
            sim=createSimulation();
        }
        if(sim==null){
            return;
        }
        currentSimulation.set(sim);
        
        List<Station> stations= sim.network.getStationsList();
        List<Computer> computersInNetwork= new ArrayList<>();
        stations.forEach((s) -> {
            Computer c= new Computer(s, "Station "+s.getId());
            c.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                handleComputerSelected(c, newValue);
            });
            computersInNetwork.add(c);
        });
        
        
        Switch mySwitch= new Switch(sim.network.getBus());
        
        // layout the switch to the center of the workpane
        double workSpaceWidth= workPane.getLayoutBounds().getWidth();
        double workSpaceHeight= workPane.getLayoutBounds().getHeight();
        mySwitch.relocate((workSpaceWidth+mySwitch.getWidth())/2, (workSpaceHeight+mySwitch.getHeight())/2);
        workPane.getChildren().add(mySwitch);

        // Layout work stations
        if(!computersInNetwork.isEmpty()){
            int nbStation= computersInNetwork.size();
            double stationWidth= computersInNetwork.get(0).getWidth();

            int nbEltRow1= nbStation/2;
            double totalEltWidthRow1= nbEltRow1*stationWidth;
            
            double firstRowSpacing= (workSpaceWidth-totalEltWidthRow1)/(nbEltRow1+2);
            double x1=firstRowSpacing;
            double y1=10;
            for(int i=0; i<nbEltRow1; i++){
                Computer station= computersInNetwork.get(i);
                station.relocate(x1, y1);
                x1+=firstRowSpacing+stationWidth;
                workPane.getChildren().add(station);
            }
            // ======================== SECOND ROW ======================
            int nbEltRow2= nbStation-nbEltRow1;
            double totalEltWidthRow2= nbEltRow2*stationWidth;
            
            double secondRowSpacing= (workSpaceWidth-totalEltWidthRow2)/(nbEltRow2+2);
            double x2=secondRowSpacing;
            double y2=workSpaceHeight-100;
            
            for(int i=nbEltRow1; i<nbStation; i++){
                Computer station= computersInNetwork.get(i);
                station.relocate(x2, y2);
                x2+=secondRowSpacing+stationWidth;
                workPane.getChildren().add(station);
            }
            
            for(Computer cm : computersInNetwork){
            
                Link l= new Link(cm, mySwitch);
                workPane.getChildren().add(l);
                l.toBack();
                cm.toFront();
            }

            mySwitch.toFront();
            
            simulationRunningProperty.set(true);
            setSimulationSettingsDisable(true);
            sim.play();
        }
    }
    
    private void setSimulationSettingsDisable(boolean disable){
        txtSlotDuration.setDisable(disable);
        txtFrameProbability.setDisable(disable);
        txtFrameSize.setDisable(disable);
        txtMaxInterval.setDisable(disable);
        txtMaxRetrans.setDisable(disable);
        txtStations.setDisable(disable);
    }
    
    private void selectedStationChanged(Station s){
        
        if(s!=null){
            labGenerated.textProperty().unbind();
            labTransmitted.textProperty().unbind();
            labReceived.textProperty().unbind();
            labAvg.textProperty().unbind();
            labCollison.textProperty().unbind();
            labLost.textProperty().unbind();
            labRetransmitted.textProperty().unbind();
            labAwaiting.textProperty().unbind();
            
            labGenerated.textProperty().bindBidirectional(s.frameGeneratedProperty(), converter);
            labTransmitted.textProperty().bindBidirectional(s.frameTransmittedProperty(), converter);
            labReceived.textProperty().bindBidirectional(s.frameReceivedProperty(), converter);
            labAvg.textProperty().bindBidirectional(s.avgFramePerTimeSlotProperty(), converter);
            labCollison.textProperty().bindBidirectional(s.collisionsProperty(), converter);
            labLost.textProperty().bindBidirectional(s.frameLostCollisionProperty(), converter);
            labRetransmitted.textProperty().bindBidirectional(s.frameRetransmittedProperty(), converter);
            labAwaiting.textProperty().bindBidirectional(s.frameAwaitingProperty(), converter);
            
        }
    }
    
    @FXML private void handleExportPDFBtn(){
        
        
        FileChooser chooser= new FileChooser();
        chooser.setTitle("Export as PDF");

        chooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        );                 
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Printable Document Format", "*.pdf")
        );

        File f= chooser.showSaveDialog(NetSim.primaryStage);

        if(f!=null){
             Node[]charts= {framesChart, avgFrameChart, channelChart, throughputChart, retransmissionChart};
             WritableImage[] images=new WritableImage[charts.length];
            for(int i=0; i<charts.length; i++){
                Node chart= charts[i];
                images[i] = chart.snapshot(new SnapshotParameters(), null);
            }
                
            Runnable r= ()->{
                try{
                    PDDocument document = new PDDocument();

//                    Node[]charts= {framesChart, avgFrameChart, channelChart, throughputChart, retransmissionChart};

                    for(WritableImage image: images){
                        //WritableImage image = chart.snapshot(new SnapshotParameters(), null);
                        File file = File.createTempFile(""+System.currentTimeMillis(), ".png"); 
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

                        PDPage page = new PDPage();

                        //Creating PDImageXObject object
                        PDImageXObject pdImage = PDImageXObject.createFromFile(file.getAbsolutePath(),document);

                        //creating the PDPageContentStream object
                        PDPageContentStream contents = new PDPageContentStream(document, page);

                        //Drawing the image in the PDF document
                        PDRectangle mediaBox = page.getMediaBox();

                        float x = (mediaBox.getWidth() - pdImage.getWidth()) / 2;
                        float y = (mediaBox.getHeight() - pdImage.getHeight()) / 2;
                        contents.drawImage(pdImage, x, y);
                        //contents.drawImage(pdImage, 70, 250);

                        System.out.println("Image inserted");

                        //Closing the PDPageContentStream object

                        document.addPage(page);
                        contents.close();
                    }

                    document.save(f);
                    document.close();
                 }
                catch(Exception ex){
                    Alert alert= new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("I/O Error");
                    alert.setHeaderText("The following error occured when trying to export charts");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            };
            new Thread(r).start();
        }
    }
    
}