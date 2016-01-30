package GUI;

import CEP.customEvents.AlertEvent;
import CEP.customEvents.Boundary;
import CEP.customEvents.BoundaryEvent;
import CEP.customEvents.Location;
import CEP.main.CEP;
import ML.GMMClass;
import ML.KMeansClass;
import WRF.NamelistCalc;
import WRF.RunScript;
import WRF.WRFEnvironment;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.StatusBar;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Box;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

/**
 * This example demonstrates the simplest possible way to create a WorldWind application.
 *
 * @version $Id: SimplestPossibleExample.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Main extends JFrame {
    JButton DataLoadButton;
    JButton CEPButton;
    JButton MLButton;
    JButton WRFButton;

    ImageIcon arrowIcon;

    WorldWindowGLCanvas worldWindow;
    StatusBar mapStatusBar;

    RenderableLayer dataPointLayer;
    RenderableLayer dataBoundLayer;
    RenderableLayer CEPBoundLayer;
    RenderableLayer MLBoundLayer;


    ArrayList<Vector> dataPoints;
    ArrayList<Boundary> boundaryArray;
    ArrayList<Double> clusterTimeArray;
    Boundary boundary;
    RunScript runScript;
    private double cepTime=0;
    private double mlTime=0;
    private double wrfTime=0;
    private long cepStart=0;
    private long cepEnd=0;
    //ShapeAttributes normalAttributes;

    public Main() {

        initUI();
    }

    private static final String APP_NAME = "Laridae";

    static {
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    private void initUI() {


        JPanel basic = new JPanel();
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));
        add(basic);

        basic.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttons = initButtons();


        basic.add(buttons);
        basic.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel map = new JPanel();

        worldWindow = new WorldWindowGLCanvas();
        worldWindow.setPreferredSize(new Dimension(900, 600));
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        m.setGlobe(new EarthFlat());
        worldWindow.setModel(m);//new BasicModel());
        worldWindow.addSelectListener(new ClickAndGoSelectListener(worldWindow, WorldMapLayer.class));

        ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
        insertBeforeCompass(worldWindow, viewControlsLayer);
        worldWindow.addSelectListener(new ViewControlsSelectListener(worldWindow, viewControlsLayer));


        map.add(worldWindow);

        basic.add(map);
        mapStatusBar = new StatusBar();
        add(mapStatusBar, BorderLayout.PAGE_END);
        mapStatusBar.setEventSource(worldWindow);

        dataBoundLayer = new RenderableLayer();
        CEPBoundLayer = new RenderableLayer();
        dataPointLayer = new RenderableLayer();
        MLBoundLayer = new RenderableLayer();

        insertBeforeCompass(worldWindow, CEPBoundLayer);
        insertBeforeCompass(worldWindow, dataBoundLayer);
        insertBeforeCompass(worldWindow, MLBoundLayer);
        insertBeforeCompass(worldWindow, dataPointLayer);

        setMinimumSize(new Dimension(600, 600));
        setTitle(APP_NAME);
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }

    private void loadData() {
        //createPoint();
    }

    private void runCEP() {
        cepStart=System.currentTimeMillis();
        CEP.run(this);
    }

    private void runML() {
        long start=System.currentTimeMillis();
        boundaryArray = new ArrayList<>();
        ArrayList<Vector>[] clusters = GMMClass.run(dataPoints);
        //ArrayList<Vector>[] clusters = KMeansClass.run(dataPoints, 20, 1);

        for (int i = 0; i < clusters.length; i++) {
            Vector[] bounds = KMeansClass.getBounds(clusters[i], 360, 180);
            double lat1 = bounds[0].apply(0) - 90;
            double lon1 = bounds[0].apply(1) - 180;
            double lat2 = bounds[1].apply(0) - 90;
            double lon2 = bounds[1].apply(1) - 180;

            ShapeAttributes attributes = configureBounderyAttributes(Material.ORANGE, Material.ORANGE);
            boundaryArray.add(new Boundary(lat1, lat2, lon1, lon2));

            LatLon upperLeft = LatLon.fromDegrees(lat1, lon1);
            LatLon lowerRight = LatLon.fromDegrees(lat2, lon2);
            System.out.println("Lon : " + lon1 + " " + lon2);
            System.out.println("Lat : " + lat1 + " " + lat2);
            createBoundery(MLBoundLayer, attributes, upperLeft, lowerRight, "ML boundary");
        }
        MLButton.setEnabled(false);
        long end=System.currentTimeMillis();
        mlTime=(end-start)/1000.0;
    }

    private void runWRF(ArrayList<Boundary> boundaryArray) {
        clusterTimeArray=new ArrayList<>();
        WRFEnvironment wrfEnvironment=new WRFEnvironment();
        long start=System.currentTimeMillis();
        for (int i = 0; i < boundaryArray.size(); i++) {
            long startCluster=System.currentTimeMillis();
            boundary = boundaryArray.get(i);
            System.out.println(boundary.getMinLatitude()+" "+boundary.getMaxLatitude()+" "+boundary.getMinLongitude()+" "+boundary.getMaxLongitude());
            wrfEnvironment.setE_sn(String.valueOf(NamelistCalc.get_e_ns(boundary,wrfEnvironment.getResolution())));
            wrfEnvironment.setE_we(String.valueOf(NamelistCalc.get_e_we(boundary,wrfEnvironment.getResolution())));
            wrfEnvironment.setRef_lat(NamelistCalc.get_refLat(boundary));
            wrfEnvironment.setRef_lon(NamelistCalc.get_refLon(boundary));

            runScript = new RunScript();

            //initially change the namelist.wps and namelist.input files according to the parameters set
            runScript.changeNamelistWPS(wrfEnvironment.getInputWPSPath(),wrfEnvironment.getStartDate(),wrfEnvironment.getEndDate(),wrfEnvironment.getMaxDom(),wrfEnvironment.getIntervalSeconds(),wrfEnvironment.getE_we(),wrfEnvironment.getE_sn(),wrfEnvironment.getGeo_data_path(),wrfEnvironment.getPrefix(),wrfEnvironment.getRef_lat(),wrfEnvironment.getRef_lon());
            runScript.changeNamelipsInput(wrfEnvironment.getNamelistWRFPath(),wrfEnvironment.getRunDays(),wrfEnvironment.getRunHours(),wrfEnvironment.getStartYear(),wrfEnvironment.getStartMonth(),wrfEnvironment.getStartDay(),wrfEnvironment.getStartHour(),wrfEnvironment.getEndYear(),wrfEnvironment.getEndMonth(),wrfEnvironment.getEndtDay(),wrfEnvironment.getEndHour(),wrfEnvironment.getIntervalSeconds(),wrfEnvironment.getMaxDom(),wrfEnvironment.getE_we(),wrfEnvironment.getE_sn(),wrfEnvironment.getNum_metgrid_levels(),wrfEnvironment.getNum_metgrid_soil_levels());

            //run the WRF using the shell scripts
            runScript.runScript("sh /Users/vidu/Documents/Development/Final_Year_Project/Development/Laridae/src/WRF/autoauto.sh");
            long endCluster=System.currentTimeMillis();
            double clusterTime=(endCluster-startCluster)/1000.0;
            clusterTimeArray.add(clusterTime);
        }
        long end=System.currentTimeMillis();
        wrfTime=(end-start)/1000.0;
        System.out.println("CEP Time : "+cepTime+" seconds");
        System.out.println("ML Time : "+mlTime+" seconds");
        System.out.println("WRF Time : "+wrfTime+" seconds");
        for(int i=0;i<clusterTimeArray.size();i++){
            System.out.println("Cluster "+(i+1)+" : "+clusterTimeArray.get(i));
        }

        System.out.println("Total Time : "+(cepTime+mlTime+wrfTime)+" seconds");
    }

    public void handleAlertEvent(AlertEvent e) {
        ArrayList<Location> array = e.getCoordinates();
        dataPoints = new ArrayList<>();
        ShapeAttributes attributes;

        for (Location co : array) {
            double lat = Double.parseDouble(co.getLatitude());
            double lon = Double.parseDouble(co.getLongitude());

            attributes = configurePointAttributes(Material.RED);
            createPoint(dataPointLayer, attributes, LatLon.fromDegrees(lat, lon));

            double latPositive = lat + 90;
            double lonPositive = lon + 180;

            dataPoints.add(Vectors.dense(lonPositive, latPositive));
        }
//        System.out.println(dataPoints.size());

//        String string = e.getCoordinates();
//        String[] coordinates = string.split(",");
//
//        dataPoints = new ArrayList<>();
//
//        for(String co:coordinates){
//            String[] latlon = co.split(":");
//            double lat = Double.parseDouble(latlon[0]);
//            double lon = Double.parseDouble(latlon[1]);
//
//            ShapeAttributes attributes = configurePointAttributes(Material.RED);
//            createPoint(dataPointLayer, attributes, LatLon.fromDegrees(lat, lon));
//
//            double latPositive = lat+90;
//            double lonPositive = lon+180;
//            dataPoints.add(Vectors.dense(lonPositive, latPositive));
//        }
        cepEnd=System.currentTimeMillis();
        cepTime=(Math.abs(cepStart-cepEnd))/1000.0;
        CEPButton.setEnabled(false);
    }

    public void handelBoundaryEvent(BoundaryEvent e) {
        String boundary = e.getBoundary();
        String[] values = boundary.split(" ");
        double minLat = Double.parseDouble(values[0]);
        double maxLat = Double.parseDouble(values[1]);
        double minLon = Double.parseDouble(values[2]);
        double maxLon = Double.parseDouble(values[3]);

        ShapeAttributes attributes = configureBounderyAttributes(Material.GRAY, Material.LIGHT_GRAY);

        LatLon upperLeft = LatLon.fromDegrees(maxLat, minLon);
        LatLon lowerRight = LatLon.fromDegrees(minLat, maxLon);
        CEPBoundLayer.removeAllRenderables();
        createBoundery(CEPBoundLayer, attributes, upperLeft, lowerRight, "CEP boundary");

        CEPButton.setEnabled(false);
    }

    private ShapeAttributes configureShapeAttributes
            (Material outerColor, Material innerColor, double innerOpacity, double outerOpacity) {

        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(innerColor);
        attributes.setOutlineOpacity(outerOpacity);
        attributes.setInteriorOpacity(innerOpacity);
        attributes.setOutlineMaterial(outerColor);
        attributes.setOutlineWidth(1);
        attributes.setDrawOutline(true);
        attributes.setDrawInterior(true);
        attributes.setEnableLighting(true);

        return attributes;
    }

    private ShapeAttributes configureBounderyAttributes(Material outerColor, Material innerColor) {
        return configureShapeAttributes(outerColor, innerColor, 0.5, 0.7);
    }

    private ShapeAttributes configurePointAttributes(Material color) {
        return configureShapeAttributes(color, color, 1, 1);
    }

    private void createBoundery(RenderableLayer layer, ShapeAttributes attributes, LatLon upperLeft, LatLon lowerRight, String displayName) {

        double lat1 = upperLeft.getLatitude().getDegrees();
        double lon1 = upperLeft.getLongitude().getDegrees();
        double lat2 = lowerRight.getLatitude().getDegrees();
        double lon2 = lowerRight.getLongitude().getDegrees();

        ArrayList<Position> pathPositions = new ArrayList<Position>();
        pathPositions.add(Position.fromDegrees(lat1, lon1, 3e4));
        pathPositions.add(Position.fromDegrees(lat1, lon2, 3e4));
        pathPositions.add(Position.fromDegrees(lat2, lon2, 3e4));
        pathPositions.add(Position.fromDegrees(lat2, lon1, 3e4));
        gov.nasa.worldwind.render.Polygon pgon = new gov.nasa.worldwind.render.Polygon(pathPositions);
        pgon.setValue(AVKey.DISPLAY_NAME, displayName);

        pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        pgon.setAttributes(attributes);
        layer.addRenderable(pgon);
        worldWindow.redraw();
    }

    private void createPoint(RenderableLayer layer, ShapeAttributes attributes, LatLon pointLatLon) {

        SurfaceCircle point = new SurfaceCircle(pointLatLon, 10000);

        point.setAttributes(attributes);
        layer.addRenderable(point);
        worldWindow.redraw();
    }

    private JPanel initButtons() {
        JPanel buttons = new JPanel();

        DataLoadButton = new JButton("Load Weather Data");
        CEPButton = new JButton("Run CEP");
        MLButton = new JButton("Run ML algorithms");
        WRFButton = new JButton("Run WRF model");

        DataLoadButton.setMaximumSize(new Dimension(500, 50));
        CEPButton.setMaximumSize(new Dimension(500, 50));
        MLButton.setMaximumSize(new Dimension(500, 50));
        WRFButton.setMaximumSize(new Dimension(500, 50));

        DataLoadButton.addActionListener(e -> loadData());
        CEPButton.addActionListener(e -> runCEP());
        MLButton.addActionListener(e -> runML());
        WRFButton.addActionListener(e -> runWRF(boundaryArray));

        try {
            BufferedImage image = ImageIO.read(new File("resources/arrow.png"));
            Image dimage = image.getScaledInstance(100, 50, Image.SCALE_SMOOTH);
            arrowIcon = new ImageIcon(dimage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel arrow1 = new JLabel(arrowIcon);
        JLabel arrow2 = new JLabel(arrowIcon);
        JLabel arrow3 = new JLabel(arrowIcon);

        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        buttons.add(Box.createRigidArea(new Dimension(20, 0)));

        buttons.add(DataLoadButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(arrow1);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(CEPButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(arrow2);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(MLButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(arrow3);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(WRFButton);

        buttons.add(Box.createRigidArea(new Dimension(20, 0)));

        return buttons;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            JFrame frame = new Main();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }


}
