package CEP.dataFeed;

import CEP.cepProcessing.WeatherAlerts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ruveni on 15/11/15.
 */

/*
    Madis Data Fields
    stationId, date, Time, dewTemperature, relativeHumidity, seaPressure, stationPressure, airTemperature, windDirection, windSpeed, latitude, longitude
 */
public class MadisDataFeed  implements Runnable{
    private WeatherAlerts weatherAlert;

    public MadisDataFeed(WeatherAlerts weatherAlerts){
        this.weatherAlert=weatherAlerts;
    }

    @Override
    public void run() {
        Path path = Paths.get("/home/ruveni/Data/chicago.csv");
        try (Stream<String> lines = Files.lines(path)) {
            String[] lineArray = lines.collect(Collectors.toList()).toArray(new String[0]);
            for (int i = 0; i < lineArray.length; i++) {
                String[] stringData = lineArray[i].split(",");
                try{
                    weatherAlert.SendDataToCEP(stringData[0].trim(),stringData[1]+" "+stringData[2],Double.parseDouble(stringData[5]),Double.parseDouble(stringData[6]),Double.parseDouble(stringData[7]),Double.parseDouble(stringData[8]),Double.parseDouble(stringData[9]),Double.parseDouble(stringData[10]),Double.parseDouble(stringData[11]),Double.parseDouble(stringData[12]),Double.parseDouble(stringData[13]));
                }catch(Exception e){

                }
            }

        } catch (IOException ex) {

        }
    }
}
