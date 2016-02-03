package CEP.cepProcessing;

import CEP.customEvents.AlertEvent;
import CEP.customEvents.BoundaryEvent;
import CEP.customEvents.Location;
import GUI.Main;
import Util.LatLonUtil;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;


/**
 * Created by ruveni on 15/11/15.
 */
/*
    Necessary GRIB parameters
    =================================
    Best 4 layer lifted index @ Layer between 2 level at pressure difference from ground to level layer (K)
    Storm relative helicity @ Layer between 2 specified height level above ground layer (m2/s2) 500m
    Convective inhibition @ Ground or water surface (J/kg)
 */
public class WeatherAlerts {
    private SiddhiManager siddhiManager;
    private InputHandler madisInputHandler;
    private ArrayList<Location> coordinates;
    private StringBuilder coordString;


    Main listener;

    public WeatherAlerts(SiddhiManager siddhiManager, Main listener) {
        this.listener = listener;
        this.siddhiManager = siddhiManager;
        coordinates = new ArrayList<>();
        coordString = new StringBuilder();
        madisInputHandler = siddhiManager.getInputHandler("WeatherStream");
        checkLiftedIndex();
        checkHelicity();
        checkInhibition();
        calculateCommonBoundary();
    }

    public void SendDataToCEP(String stationId, double latitude, double longitude, double liftedIndex, double helicity, double inhibition) {
        try {
            madisInputHandler.send(new Object[]{stationId, latitude, longitude, liftedIndex, helicity, inhibition});
        } catch (Exception e) {

        }
    }

    //TEMPERATURE AND HUMIDITY RELATED INDICES

    //gets the Lifted Index and checks whether it is positive or negative
    //If the value is negative, the probability of occuring a thunderstorm is greater
    public void checkLiftedIndex() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream [liftedIndex<" + CEPEnvironment.THRESHOLD_LIFTED_INDEX + "] #window.length(50) as A " +
                "join WeatherStream[liftedIndex<" + CEPEnvironment.THRESHOLD_LIFTED_INDEX + "] #window.length(50) as B " +
                "on weather:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) " +
                "select 'A' as streamId, A.stationId, A.latitude, A.longitude " +
                "insert into FilteredDataStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                processData(inEvents);
            }
        });
    }

    //WIND RELATED INDICES

    //Storm Relative Helicity SRH
    //Storm relative helicity @ Layer between 2 specified height level above ground layer (m2/s2)
    //threshold value is 150 m2/s2

    public void checkHelicity() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream [helicity>" + CEPEnvironment.THRESHOLD_HELICITY + "] #window.length(50) as A " +
                "join WeatherStream[helicity>" + CEPEnvironment.THRESHOLD_HELICITY + "] #window.length(50) as B " +
                "on weather:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) " +
                "select 'E' as streamId, A.stationId,A.latitude,A.longitude " +
                "insert into FilteredDataStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                processData(inEvents);
            }
        });
    }

    //COMPLEX PARAMETERS

    //Convective Inhibition - CIN
    //Convective inhibition @ Ground or water surface (J/kg) parameter
    //threshold value is 15J/kg

    public void checkInhibition() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream [inhibition<(" + CEPEnvironment.THRESHOLD_INHIBITION + ")] #window.length(50) as A " +
                "join WeatherStream[inhibition<(" + CEPEnvironment.THRESHOLD_INHIBITION + ")] #window.length(50) as B " +
                "on weather:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) " +
                "select 'F' as streamId, A.stationId,A.latitude,A.longitude " +
                "insert into FilteredDataStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                processData(inEvents);
            }
        });
    }

    //calculate the common boundary
    public void calculateCommonBoundary() {
        String calBoundary = siddhiManager.addQuery("from FilteredDataStream #window.timeBatch( " + CEPEnvironment.TIME_GAP + " sec ) " +
                "select min(latitude) as minLatitude, max(latitude) as maxLatitude, min(longitude) as minLongitude, max(longitude) as maxLongitude, count(stationId) as dataCount " +
                "insert into DataBoundary for all-events ; ");

        siddhiManager.addCallback(calBoundary, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                try {
                    int k = inEvents.length;
                    System.out.print("Resulting Boundary : ");
                    //format minLat maxLat minLon maxLon
                    //Eg Resulting Boundary : 53.915516 59.833235 -148.537948 -54.56872
                    String output = inEvents[k - 1].getData(0) + " " + inEvents[k - 1].getData(1) + " " + inEvents[k - 1].getData(2) + " " + inEvents[k - 1].getData(3);
                    System.out.println(inEvents[k - 1].getData(0) + " " + inEvents[k - 1].getData(1) + " " + inEvents[k - 1].getData(2) + " " + inEvents[k - 1].getData(3));

                    //events to invoke the gui
                    AlertEvent eventt = new AlertEvent(this, coordinates);
                    listener.handleAlertEvent(eventt);

                    BoundaryEvent event = new BoundaryEvent(this, output);
                    listener.handelBoundaryEvent(event);

                    //resets the values
                    coordinates = new ArrayList<>();
                    coordString = new StringBuilder();
                } catch (Exception e) {
                    //null pointer exception
                    //no data to process for CEP
                }

            }
        });
    }

    public void processData(Event[] inEvents) {
        try {
            int k = inEvents.length;

            //adds the filtered coordinate to the arrEvent[] inEventsay list
            if (coordString.indexOf(inEvents[k - 1].getData(2) + ":" + inEvents[k - 1].getData(3)) < 0) {
                coordString.append(inEvents[k - 1].getData(2) + ":" + inEvents[k - 1].getData(3) + ",");
                String latitude = String.valueOf(inEvents[k - 1].getData(2));
                String longitude = String.valueOf(inEvents[k - 1].getData(3));
                double[] lambertValues = LatLonUtil.latLonToLambert(Math.toRadians(Double.parseDouble(latitude)), Math.toRadians(Double.parseDouble(longitude)), 10, 30, 60, 10);
                coordinates.add(new Location(latitude, longitude, String.valueOf(lambertValues[0]), String.valueOf(lambertValues[1])));
            }
        } catch (Exception e) {
            //null pointer exception
            //no data to be processed
        }
    }
}