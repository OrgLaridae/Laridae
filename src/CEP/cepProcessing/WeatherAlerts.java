package CEP.cepProcessing;

import CEP.customEvents.AlertEvent;
import CEP.customEvents.BoundaryEvent;
import CEP.customEvents.Location;
import GUI.Main;
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
    Dew point temperature @ Isobaric surface (K) 850hPa
    Dew point temperature @ Isobaric surface (K) 700hPa
    Dew point temperature @ Isobaric surface (K) 500hPa
    Temperature @ Isobaric surface (K) 500hPa
    Temperature @ Isobaric surface (K) 850hPa
    Temperature @ Isobaric surface (K) 700hPa
    Storm relative helicity @ Layer between 2 specified height level above ground layer (m2/s2) 500m
    Convective inhibition @ Ground or water surface (J/kg)
    Precipitable water @ Entire atmosphere (kg/m2)
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
        coordinates=new ArrayList<>();
        coordString=new StringBuilder();
        madisInputHandler = siddhiManager.getInputHandler("WeatherStream");
        checkLiftedIndex();
        checkHelicity();
        checkInhibition();
        //sendFilteredWeatherData();
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
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) " +
                "select 'A' as streamId, A.stationId, A.latitude, A.longitude " +
                "insert into FilteredDataStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                int k = inEvents.length;
                //System.out.println("Lifted Index : " + inEvents[k - 1].getData(1));

                //adds the filtered coordinate to the array list
                if(coordString.indexOf(inEvents[k - 1].getData(2)+":"+inEvents[k - 1].getData(3))<0){
                    coordString.append(inEvents[k - 1].getData(2)+":"+inEvents[k - 1].getData(3)+",");
                    coordinates.add(new Location(String.valueOf(inEvents[k - 1].getData(2)),String.valueOf(inEvents[k - 1].getData(3))));
                }
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
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) " +
                "select 'E' as streamId, A.stationId,A.latitude,A.longitude " +
                "insert into FilteredDataStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                int k = inEvents.length;
                //System.out.println("Helicity Index : " + inEvents[k - 1].getData(1));

                //adds the filtered coordinate to the array list
                if(coordString.indexOf(inEvents[k - 1].getData(2)+":"+inEvents[k - 1].getData(3))<0){
                    coordString.append(inEvents[k - 1].getData(2)+":"+inEvents[k - 1].getData(3)+",");
                    coordinates.add(new Location(String.valueOf(inEvents[k - 1].getData(2)),String.valueOf(inEvents[k - 1].getData(3))));
                }
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
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) " +
                "select 'F' as streamId, A.stationId,A.latitude,A.longitude " +
                "insert into FilteredDataStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                int k = inEvents.length;
                //System.out.println("Inhibition Index : " + inEvents[k - 1].getData(1));

                //adds the filtered coordinate to the array list
                if(coordString.indexOf(inEvents[k - 1].getData(2)+":"+inEvents[k - 1].getData(3))<0){
                    coordString.append(inEvents[k - 1].getData(2)+":"+inEvents[k - 1].getData(3)+",");
                    coordinates.add(new Location(String.valueOf(inEvents[k - 1].getData(2)),String.valueOf(inEvents[k - 1].getData(3))));
                }
            }
        });
    }


    //Total Totals Index
    //temperature parameters in Kelvin
    //TT > 44 = possible thunderstorms, slight chance of severe TT > 50 = moderate chance of severe thunderstorms TT > 55 = strong chance of severe thunderstorms
    //TT = Td850 + T850 - 2(T500) or (Td850 - T500) + (T850 - T500)
    public void checkTotalsIndex() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream #window.unique(stationId) as A " +
                "join WeatherStream #window.unique(stationId) as B " +
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) and A.stationId != B.stationId and madis:isNearTimestamp(A.dateTime,B.dateTime) and (A.dewTemp850 + A.temp850 - 2*A.temp500) > 44 " +
                "select 'B' as streamID, A.stationId,A.dateTime,A.latitude,A.longitude " +
                "insert into FilteredStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                System.out.print("Totals Index : ");
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
    }

    //K Index
    //K = T850 - T500 + Td850 - (T700 - Td700)
    //temperature parameters in Kelvin
    //K==26-30 40-60% Air mass thunderstorm probability
    public void checkKIndex() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream #window.unique(stationId) as A " +
                "join WeatherStream #window.unique(stationId) as B " +
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) and A.stationId != B.stationId and madis:isNearTimestamp(A.dateTime,B.dateTime) and (A.temp850 - A.temp500 + A.dewTemp850 - (A.temp700 - A.dewTemp700)) > 25 " +
                "select 'C' as streamID, A.stationId,A.dateTime,A.latitude,A.longitude " +
                "insert into FilteredStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                System.out.print("K Index : ");
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
    }

    //Humidity Index HI
    //HI = (T 850 − T d,850 ) + (T 700 − T d,700 ) + (T 500 − T d,500 )
    //Threshold is 30K - Temperature parameters in Kelvin

    public void checkHumidityIndex() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream #window.unique(stationId) as A " +
                "join WeatherStream #window.unique(stationId) as B " +
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) and A.stationId != B.stationId and madis:isNearTimestamp(A.dateTime,B.dateTime) and ((A.temp850 - A.dewTemp850) + (A.temp700 - A.dewTemp700) + (A.temp500 - A.dewTemp500)) < 30 " +
                "select 'D' as streamID, A.stationId,A.dateTime,A.latitude,A.longitude " +
                "insert into FilteredStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                System.out.print("K Index : ");
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
    }

    //PRECIPITABLE WATER (PW) (kgm-2 or mm)
    //Precipitable water @ Entire atmosphere (kg/m2)
    //Threshold is 16mm

    public void checkPecipitableWater() {
        String checkIndex = siddhiManager.addQuery("from WeatherStream [precipitableWater>16] #window.unique(stationId) as A " +
                "join WeatherStream[precipitableWater>16] #window.unique(stationId) as B " +
                "on madis:isNearStation(A.latitude,A.longitude,B.latitude,B.longitude) and A.stationId != B.stationId and madis:isNearTimestamp(A.dateTime,B.dateTime) " +
                "select 'G' as streamID, A.stationId,A.latitude,A.longitude " +
                "insert into FilteredStream ;");

        siddhiManager.addCallback(checkIndex, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                System.out.print("Precipitable Water Index : ");
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
    }

    public void precipitableWaterBoundary() {
        String calBoundary = siddhiManager.addQuery("from PrecipitableWaterStream #window.timeBatch( " + CEPEnvironment.TIME_GAP + " min ) " +
                "select min(latitude) as minLatitude, max(latitude) as maxLatitude, min(longitude) as minLongitude, max(longitude) as maxLongitude, count(stationId) as dataCount " +
                "insert into DataBoundary for all-events ; ");

        siddhiManager.addCallback(calBoundary, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                System.out.print("Precipitable Water Boundary : ");
//                EventPrinter.print(inEvents);
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
                try{
                    int k = inEvents.length;
                    System.out.print("Resulting Boundary : ");
                    //format minLat maxLat minLon maxLon
                    //Eg Resulting Boundary : 53.915516 59.833235 -148.537948 -54.56872
                    String output = inEvents[k - 1].getData(0) + " " + inEvents[k - 1].getData(1) + " " + inEvents[k - 1].getData(2) + " " + inEvents[k - 1].getData(3);
                    System.out.println(inEvents[k - 1].getData(0) + " " + inEvents[k - 1].getData(1) + " " + inEvents[k - 1].getData(2) + " " + inEvents[k - 1].getData(3));
                    BoundaryEvent event = new BoundaryEvent(this, output);
                    //add the code to notify the event lister

                    //invokes the listener to detect the filtered coordinates
                    listener.handelBoundaryEvent(event);
                    AlertEvent eventt = new AlertEvent(this, coordinates);
                    listener.handleAlertEvent(eventt);

                    //resets the values
                    coordinates=new ArrayList<>();
                    coordString=new StringBuilder();
                }catch (Exception e){
                    //null pointer exception
                    //no data to process for CEP
                }

            }
        });
    }

    //sends the data filtered as a string composed with location coordinates
//    public void sendFilteredWeatherData() {
//        String calBoundary = siddhiManager.addQuery("from FilteredDataStream #window.timeBatch( " + CEPEnvironment.TIME_GAP + " min ) " +
//                "select weather:coordinates(latitude,longitude) as coordinates " +
//                "insert into CheckBatch for all-events ; ");
//
//        siddhiManager.addCallback(calBoundary, new QueryCallback() {
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                AlertEvent event = new AlertEvent(this, CEPEnvironment.COORDINATE_FILE_PATH);
//                listener.handleAlertEvent(event);
//            }
//        });
//    }

}