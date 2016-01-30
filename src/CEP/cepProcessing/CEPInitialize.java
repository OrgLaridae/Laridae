package CEP.cepProcessing;

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

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();
        siddhiConfiguration.setSiddhiExtensions(extensionClasses);

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        //stream definitions
        siddhiManager.defineStream("define stream WeatherStream (stationId string, latitude double, longitude double, liftedIndex double, helicity double, inhibition double) ");
        siddhiManager.defineStream("define stream FilteredDataStream (streamId string, stationId string, latitude double, longitude double) ");
        siddhiManager.defineStream("define stream DataBoundary (minLatitude double, maxLatitude double, minLongitude double, maxLongitude double, dataCount long) ");

        return siddhiManager;
    }
}
