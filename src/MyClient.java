import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import sun.org.mozilla.javascript.internal.json.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;

/**
 * Created by ruveni on 9/2/15.
 */
public class MyClient {
    public static void main(String args[]){
        Timer timer=new Timer();
        timer.scheduleAtFixedRate(new TaskRepeat(),10000,5000);
    }
}

class TaskRepeat extends TimerTask{
    public void run(){
        MyMethod();
    }

    public void MyMethod(){
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=39&lon=139");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String finalOutput="";
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                //System.out.println(output);
                finalOutput=output;
            }

            conn.disconnect();

            //String finalOutput="{\"coord\":{\"lon\":139.78,\"lat\":38.82},\"weather\":[{\"id\":803,\"main\":\"Clouds\",\"description\":\"broken clouds\",\"icon\":\"04n\"}],\"base\":\"stations\",\"main\":{\"temp\":295.54,\"pressure\":1009,\"humidity\":73,\"temp_min\":295.15,\"temp_max\":296.15},\"visibility\":10000,\"wind\":{\"speed\":2.1,\"deg\":90},\"clouds\":{\"all\":75},\"dt\":1441274400,\"sys\":{\"type\":1,\"id\":7605,\"message\":0.0113,\"country\":\"JP\",\"sunrise\":1441224678,\"sunset\":1441271321},\"id\":1863282,\"name\":\"Hamanaka\",\"cod\":200}";
            JSONParser parser=new JSONParser();
            Object obj=parser.parse(finalOutput);
            JSONObject jsonObj=(JSONObject)obj;

            String temperatureData=jsonObj.get("main").toString();
            String windData=jsonObj.get("wind").toString();
            String cloudsData=jsonObj.get("clouds").toString();

            JSONObject tempObj=(JSONObject)parser.parse(temperatureData);
            String temperature=tempObj.get("temp").toString();
            String pressure=tempObj.get("pressure").toString();
            String humidity=tempObj.get("humidity").toString();

            JSONObject windObject=(JSONObject)parser.parse(windData);
            String windSpeed=windObject.get("speed").toString();
            String windDirection=windObject.get("deg").toString();

            JSONObject cloudObject=(JSONObject)parser.parse(cloudsData);
            String cloudsAll=cloudObject.get("all").toString();

            System.out.println(finalOutput);
        }
        catch (MalformedURLException e) {

            e.printStackTrace();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
