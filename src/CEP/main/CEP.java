package CEP.main;

import CEP.cepProcessing.CEPInitialize;
import CEP.dataFeed.CSVFileReader;
import CEP.cepProcessing.WeatherAlerts;
import GUI.Main;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Created by ruveni on 31/10/15.
 */
public class CEP {
    public static void run(Main main){
        //initialize the CEP preconditions
        CEPInitialize cep=new CEPInitialize();
        SiddhiManager siddhiManager=cep.CEPInit();
        WeatherAlerts weatherAlerts=new WeatherAlerts(siddhiManager,main);

        //csv data feed - grib data and feeds to the CEP engine
        CSVFileReader csvReader=new CSVFileReader(weatherAlerts);
        Thread csvThread=new Thread(csvReader);
        csvThread.start();
    }
}