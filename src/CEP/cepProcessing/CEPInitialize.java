package CEP.cepProcessing;

import CEP.sidhdhiExtention.LocationCoordinates;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruveni on 08/12/15.
 */
public class CEPInitialize {

    public SiddhiManager CEPInit() {

        //configuration to add siddhi extension
        List extensionClasses = new ArrayList();
        extensionClasses.add(CEP.sidhdhiExtention.IsNearStation.class);
        extensionClasses.add(LocationCoordinates.class);

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();
        siddhiConfiguration.setSiddhiExtensions(extensionClasses);

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        //stream definitions
        siddhiManager.defineStream("define stream reflectStream (reflexMatrix string )  ");
        siddhiManager.defineStream("define stream boundaryStream ( filePath string, boundary string )  ");
        //siddhiManager.defineStream("define stream WeatherStream (stationId string, dateTime string, dewTemperature double, relativeHumidity double, seaPressure double, pressure double, temperature double, windDirection double, windSpeed double, latitude double, longitude double) ");
        siddhiManager.defineStream("define stream WeatherStream (stationId string, latitude double, longitude double, liftedIndex double, helicity double, inhibition double) ");
        siddhiManager.defineStream("define stream FilterStream (stationId string, dateTime string,latitude double, longitude double) ");
        //siddhiManager.defineStream("define stream FilteredStream (streamId string, stationId string, dateTime string,latitude double, longitude double) ");
        siddhiManager.defineStream("define stream FilteredDataStream (streamId string, stationId string, latitude double, longitude double) ");
        siddhiManager.defineStream("define stream DataBoundary (minLatitude double, maxLatitude double, minLongitude double, maxLongitude double, dataCount long) ");
        siddhiManager.defineStream("define stream CheckBatch (coordinates string) ");

        return siddhiManager;
    }
}
