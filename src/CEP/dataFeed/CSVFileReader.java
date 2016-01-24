package CEP.dataFeed;

import CEP.cepProcessing.WeatherAlerts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ruveni on 1/8/16.
 */
public class CSVFileReader implements Runnable{
    private WeatherAlerts weatherAlert;
    public CSVFileReader(WeatherAlerts weatherAlerts){
        this.weatherAlert=weatherAlerts;
    }

    @Override
    public void run() {
        String cvsSplitBy = ",";
        String[] liftedData=null,helicityData=null,inhibitData=null;

        //paths to the csv files
        Path liftedPath = Paths.get("src/CEP/csvFiles/Best(4layer)liftedindex.csv");
        Path helicityPath = Paths.get("src/CEP/csvFiles/stormrelativehelicity.csv");
        Path inhibitPath = Paths.get("src/CEP/csvFiles/convectiveInhibition.csv");

        //reads the csv data files
        try{
            Stream<String> liftedLines = Files.lines(liftedPath);
            Stream<String> helicityLines = Files.lines(helicityPath);
            Stream<String> inhibitLines = Files.lines(inhibitPath);

            String[] liftedArray = liftedLines.collect(Collectors.toList()).toArray(new String[0]);
            String[] helicityArray = helicityLines.collect(Collectors.toList()).toArray(new String[0]);
            String[] inhibitArray = inhibitLines.collect(Collectors.toList()).toArray(new String[0]);

            for (int i = 1; i < liftedArray.length; i++) {
                liftedData = liftedArray[i].split(cvsSplitBy);
                helicityData = helicityArray[i].split(cvsSplitBy);
                inhibitData = inhibitArray[i].split(cvsSplitBy);

                //sends data to the cep
                //System.out.println(liftedData[2]+" "+liftedData[3]+" ");
                weatherAlert.SendDataToCEP(liftedData[2]+":"+liftedData[3],Double.parseDouble(liftedData[2]),Double.parseDouble(liftedData[3]),Double.parseDouble(liftedData[4]),Double.parseDouble(helicityData[4]),Double.parseDouble(inhibitData[4]));
            }
            System.out.println("done");

        } catch (IOException ex) {

        }
    }
}
