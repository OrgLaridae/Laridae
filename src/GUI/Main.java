package GUI;

import CEP.cepProcessing.CEPEnvironment;
import CEP.customEvents.AlertEvent;
import CEP.customEvents.Boundary;
import CEP.customEvents.BoundaryEvent;
import CEP.customEvents.Location;
import CEP.main.CEP;
import ML.Common;
import ML.GMMClass;
import ML.KMeansClass;
import WRF.NamelistCalc;
import WRF.RunScript;
import WRF.WRFEnvironment;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.StatusBar;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Box;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import Util.*;

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
    private double cepTime = 0;
    private double mlTime = 0;
    private double wrfTime = 0;
    private long cepStart = 0;
    private long cepEnd = 0;

    private Globe globe;

    String[] liftedArray;
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
        globe = new Earth();
        FlatGlobe flatGlobe = new EarthFlat();
        flatGlobe.setProjection(FlatGlobe.PROJECTION_MERCATOR);
        m.setGlobe(globe);

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
//
//        ShapeAttributes attributes = configureBounderyAttributes(Material.GRAY, Material.LIGHT_GRAY);
//        createBoundery(CEPBoundLayer, attributes, LatLon.fromDegrees(50,-100), 20, 100000, "CEP boundary");
//
//        ShapeAttributes attributes = configureBounderyAttributes(Material.GRAY, Material.LIGHT_GRAY);
//        LatLon middle = LatLon.fromDegrees(50, -100);
//        SurfaceQuad quad = new SurfaceQuad(middle, 10000000, 5000000);
//
//        //gov.nasa.worldwind.render.Polygon pgon = new gov.nasa.worldwind.render.Polygon(pathPositions);
//        quad.setValue(AVKey.DISPLAY_NAME, "Name");
//
//        //pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
//        quad.setAttributes(attributes);
//        CEPBoundLayer.addRenderable(quad);
//        worldWindow.redraw();

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
        String cvsSplitBy = ",";
        String[] liftedData = null;

        java.nio.file.Path liftedPath = Paths.get(CEPEnvironment.LIFTED_INDEX_FILE_PATH);
        try {
            Stream<String> liftedLines = Files.lines(liftedPath);
            liftedArray = liftedLines.collect(Collectors.toList()).toArray(new String[0]);
            for (int i = 1; i < liftedArray.length; i++) {
                liftedData = liftedArray[i].split(cvsSplitBy);
                ShapeAttributes attributes = configurePointAttributes(Material.CYAN);
                createPoint(dataPointLayer, attributes, LatLon.fromDegrees(Double.parseDouble(liftedData[2]), Double.parseDouble(liftedData[3])));
            }
        } catch (IOException ex) {

        }
        DataLoadButton.setEnabled(false);
    }

    private void runCEP() {
        cepStart = System.currentTimeMillis();
        CEPButton.setEnabled(false);
        CEP.run(this);
    }

    private void runML() {
        long start = System.currentTimeMillis();
        boundaryArray = new ArrayList<>();
        ArrayList<Vector>[] clusters = GMMClass.run(dataPoints);
//        ArrayList<Vector>[] clusters = KMeansClass.run(dataPoints, 20, 1);
        ShapeAttributes attributes;

        for (int j = 0; j < clusters.length; j++) {

            attributes = configurePointAttributes(Colors.colors[j]);
            for (Vector position :
                    clusters[j]) {
                double lat = position.apply(0) - 90;
                double lon = position.apply(1) - 180;
                createPoint(dataPointLayer, attributes, LatLon.fromDegrees(lat, lon));
            }
        }


        for (int i = 0; i < clusters.length; i++) {
            Vector[] bounds = Common.getBounds(clusters[i]);
            double lat1 = bounds[0].apply(0) - 90;
            double lon1 = bounds[0].apply(1) - 180;
            double lat2 = bounds[1].apply(0) - 90;
            double lon2 = bounds[1].apply(1) - 180;


            attributes = configureBounderyAttributes(Material.ORANGE, Material.ORANGE);
            boundaryArray.add(new Boundary(lat1, lat2, lon1, lon2));

            LatLon upperLeft = LatLon.fromDegrees(lat1, lon1);
            LatLon lowerRight = LatLon.fromDegrees(lat2, lon2);
            System.out.println("Lon : " + lon1 + " " + lon2);
            System.out.println("Lat : " + lat1 + " " + lat2);
            createBoundery(MLBoundLayer, attributes, upperLeft, lowerRight, "ML boundary");
        }
        MLButton.setEnabled(false);
        long end = System.currentTimeMillis();
        mlTime = (end - start) / 1000.0;
    }

    private void runWRF(){

        try {
            Socket socket = new Socket("192.168.8.102", 4444);

            if(socket!=null) {
                OutputStream out = socket.getOutputStream();

                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(boundaryArray);

                oos.flush();
                oos.close();
                out.close();
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("Network Unreachable");
        }

//        FileOutputStream fos = new FileOutputStream("C:\\test2.xml");
//        fos.write(bytes);
    }
//    private void runWRF(ArrayList<Boundary> boundaryArray) {
//        clusterTimeArray = new ArrayList<>();
//        WRFEnvironment wrfEnvironment = new WRFEnvironment();
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < boundaryArray.size(); i++) {
//            long startCluster = System.currentTimeMillis();
//            boundary = boundaryArray.get(i);
//            //System.out.println(boundary.getMinLatitude()+" "+boundary.getMaxLatitude()+" "+boundary.getMinLongitude()+" "+boundary.getMaxLongitude());
//            wrfEnvironment.setRef_lat(NamelistCalc.get_refLat(boundary));
//            wrfEnvironment.setRef_lon(NamelistCalc.get_refLon(boundary));
//
////            wrfEnvironment.setPole_lat(NamelistCalc.get_pole_lat(boundary));
////            wrfEnvironment.setPole_lon(NamelistCalc.get_pole_lon());
////            wrfEnvironment.setStand_lon(NamelistCalc.get_standard_lon(boundary));
////            wrfEnvironment.setMap_proj("lat-lon");
////            wrfEnvironment.setDx(0.26);//default : 30000
////            wrfEnvironment.setDy(0.26);
////            wrfEnvironment.setResolution(0.26);
//            wrfEnvironment.setE_sn(String.valueOf(NamelistCalc.get_e_ns(boundary, (int)wrfEnvironment.getResolution())));
//            wrfEnvironment.setE_we(String.valueOf(NamelistCalc.get_e_we(boundary, (int)wrfEnvironment.getResolution())));
//
//            /*
//            e_we = 74, 112,
//e_sn = 61, 97,
//geog_data_res = '10m','2m',
//dx = 0.15,
//dy = 0.15,
//map_proj = 'lat-lon',
//pole_lat = 90,
//pole_lon = 180,
//ref_lat = 53.5,
//ref_lon = -4.0,
//stand_lon = 0.0,
//
//dx = 0.2695,
// dy = 0.2713,
// map_proj = 'lat-lon',
// ref_lat = 45.28,
// ref_lon = -131.66,
// truelat1  =  30.0,
// truelat2  =  60.0,
// stand_lon = 131.66,
// pole_lat = 44.72,
// pole_lon = 180.0,
//             */
////            wrfEnvironment.setE_we("74");
////            wrfEnvironment.setE_sn("61");
////            wrfEnvironment.setDx(0.2695);
////            wrfEnvironment.setDy(0.2695);
////            wrfEnvironment.setMap_proj("lat-lon");
////            wrfEnvironment.setPole_lat(44.72);
////            wrfEnvironment.setPole_lon(180);
////            wrfEnvironment.setRef_lat(45.28);
////            wrfEnvironment.setRef_lon(-131.66);
////            wrfEnvironment.setStand_lon(131.66);
//
////            wrfEnvironment.setE_sn(String.valueOf(NamelistCalc.get_e_ns_latlon(boundary,wrfEnvironment.getResolution())));
////            wrfEnvironment.setE_we(String.valueOf(NamelistCalc.get_e_we_latlon(boundary,wrfEnvironment.getResolution())));
////
//
//            runScript = new RunScript();
//
//            //initially change the namelist.wps and namelist.input files according to the parameters set
//            runScript.changeNamelistWPS(wrfEnvironment.getInputWPSPath(), wrfEnvironment.getStartDate(), wrfEnvironment.getEndDate(), wrfEnvironment.getMaxDom(), wrfEnvironment.getIntervalSeconds(), wrfEnvironment.getE_we(), wrfEnvironment.getE_sn(), wrfEnvironment.getGeo_data_path(), wrfEnvironment.getPrefix(), wrfEnvironment.getRef_lat(), wrfEnvironment.getRef_lon(), wrfEnvironment.getPole_lat(), wrfEnvironment.getPole_lon(), wrfEnvironment.getStand_lon(), wrfEnvironment.getMap_proj(), wrfEnvironment.getDx(), wrfEnvironment.getDy());
//            runScript.changeNamelipsInput(wrfEnvironment.getNamelistWRFPath(), wrfEnvironment.getRunDays(), wrfEnvironment.getRunHours(), wrfEnvironment.getStartYear(), wrfEnvironment.getStartMonth(), wrfEnvironment.getStartDay(), wrfEnvironment.getStartHour(), wrfEnvironment.getEndYear(), wrfEnvironment.getEndMonth(), wrfEnvironment.getEndtDay(), wrfEnvironment.getEndHour(), wrfEnvironment.getIntervalSeconds(), wrfEnvironment.getMaxDom(), wrfEnvironment.getE_we(), wrfEnvironment.getE_sn(), wrfEnvironment.getNum_metgrid_levels(), wrfEnvironment.getNum_metgrid_soil_levels(), wrfEnvironment.getDx(), wrfEnvironment.getDy());
//
//            //run the WRF using the shell scripts
//            runScript.runScript("sh /home/ruveni/IdeaProjects/Laridae/src/WRF/autoauto.sh");
//            long endCluster = System.currentTimeMillis();
//            double clusterTime = (endCluster - startCluster) / 1000.0;
//            clusterTimeArray.add(clusterTime);
//
//            //find a file name with wrfout_d01_2006-12-19_12:00:00
//            File dir = new File(wrfEnvironment.getTest_em_real_path());
//            File[] foundFiles = dir.listFiles((dir1, name) -> {
//                return name.startsWith("wrfout_");
//            });
//
//            if (foundFiles.length >= 1) {
//                File oldName = new File(foundFiles[0].getPath());
//                File newName = new File(wrfEnvironment.getTest_em_real_path() + "wrfOut" + (i + 1));
//                if (oldName.renameTo(newName)) {
//                    System.out.println("renamed");
//                } else {
//                    System.out.println("Error");
//                }
//            }
//        }
//        long end = System.currentTimeMillis();
//        wrfTime = (end - start) / 1000.0;
//        System.out.println("CEP Time : " + cepTime + " seconds");
//        System.out.println("ML Time : " + mlTime + " seconds");
//        System.out.println("WRF Time : " + wrfTime + " seconds");
//        for (int i = 0; i < clusterTimeArray.size(); i++) {
//            System.out.println("Cluster " + (i + 1) + " : " + clusterTimeArray.get(i));
//        }
//
////        System.out.println("Total Time : " + (cepTime + mlTime + wrfTime) + " seconds");
//    }

    public void handleAlertEvent(AlertEvent e) {
        ArrayList<Location> array = e.getCoordinates();
        dataPoints = new ArrayList<>();
        ShapeAttributes attributes;

        for (Location co : array) {
            double lat = Double.parseDouble(co.getLatitude());
            double lon = Double.parseDouble(co.getLongitude());

            double latPositive = lat + 90;
            double lonPositive = lon + 180;

            double x = Double.parseDouble(co.getLambertX());
            double y = Double.parseDouble(co.getLambertY());

            attributes = configurePointAttributes(Material.RED);
            createPoint(dataPointLayer, attributes, LatLon.fromDegrees(lat, lon));

            //System.out.println(x + "   " +y);

            System.out.println(latPositive + " " + lonPositive);
            dataPoints.add(Vectors.dense(latPositive, lonPositive));
            //dataPoints.add(Vectors.dense(lat, ));
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
        cepEnd = System.currentTimeMillis();
        cepTime = (Math.abs(cepStart - cepEnd)) / 1000.0;
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

        double middleLat = (maxLat+minLat)/2;
        double middleLon = (maxLon+minLon)/2;

        LatLon middle = LatLon.fromDegrees(middleLat, middleLon);

        LatLon top = LatLon.fromDegrees(maxLat, middleLon);
        LatLon bottom = LatLon.fromDegrees(minLat, middleLon);
        LatLon left = LatLon.fromDegrees(middleLat, maxLon);
        LatLon right = LatLon.fromDegrees(middleLat, minLon);
        double height = LatLon.greatCircleDistance(top, bottom).getRadians()* (globe.getRadiusAt(top)+globe.getRadiusAt(bottom))/2;
        double width = LatLon.greatCircleDistance(left, right).getRadians()*(globe.getRadiusAt(left)+globe.getRadiusAt(right))/2;
        LatLon upperLeft = LatLon.fromDegrees(maxLat, minLon);
        LatLon lowerRight = LatLon.fromDegrees(minLat, maxLon);
        //CEPBoundLayer.removeAllRenderables();
        //createBoundery(CEPBoundLayer, attributes, upperLeft, lowerRight, "CEP boundary");
        //createBoundery(CEPBoundLayer, attributes, middle, height, width, "CEP boundary");

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
        SurfacePolygon pgon= new SurfacePolygon(pathPositions);
        //gov.nasa.worldwind.render.Polygon pgon = new gov.nasa.worldwind.render.Polygon(pathPositions);
        pgon.setValue(AVKey.DISPLAY_NAME, displayName);

        //pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        pgon.setAttributes(attributes);
        layer.addRenderable(pgon);
        worldWindow.redraw();
    }

    private void createBoundery(RenderableLayer layer, ShapeAttributes attributes, LatLon middle, double height, double width, String displayName) {

        System.out.println("width "+width+" height"+height);
        SurfaceQuad quad = new SurfaceQuad(middle, width*1000, height*1000);

        //gov.nasa.worldwind.render.Polygon pgon = new gov.nasa.worldwind.render.Polygon(pathPositions);
        quad.setValue(AVKey.DISPLAY_NAME, displayName);

        //pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        quad.setAttributes(attributes);
        layer.addRenderable(quad);
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
        WRFButton.addActionListener(e -> runWRF());

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