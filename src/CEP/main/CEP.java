package CEP.main;

import CEP.cepProcessing.CEPInitialize;
import CEP.dataFeed.CSVFileReader;
import CEP.dataFeed.RadarDataFeed;
import CEP.dataFeed.MadisDataFeed;
import CEP.cepProcessing.WeatherAlerts;
import GUI.Main;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Created by ruveni on 31/10/15.
 */
public class CEP {
    public static void run(Main main){
        CEPInitialize cep=new CEPInitialize();
        SiddhiManager siddhiManager=cep.CEPInit();
        WeatherAlerts weatherAlerts=new WeatherAlerts(siddhiManager,main);


        //radar data feed
        RadarDataFeed dataFeed = new RadarDataFeed(weatherAlerts);
        Thread feedThread = new Thread(dataFeed);
        feedThread.start();

        //madis data feed
//        MadisDataFeed madisDataFeed=new MadisDataFeed(weatherAlerts);
//        Thread madisThread=new Thread(madisDataFeed);
//        madisThread.start();

        //csv data feed - grib data
        CSVFileReader csvReader=new CSVFileReader(weatherAlerts);
        Thread csvThread=new Thread(csvReader);
        csvThread.start();
    }
    //bhjgj
}