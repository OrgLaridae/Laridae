import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import sun.org.mozilla.javascript.internal.json.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONObject;

/**
 * Created by ruveni on 9/2/15.
 */
public class MyClient {
    public static void main(String args[]){
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
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                finalOutput=output;
            }

            conn.disconnect();


            JSONParser parser=new JSONParser();
            Object obj=parser.parse(finalOutput);
            JSONObject jsonObj=(JSONObject)obj;
            /////

            //String temperature=jsonObj.get("");

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
