package CEP.sidhdhiExtention;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chamil on 12/9/15.
 */

@SiddhiExtension(namespace = "madis", function = "boundary")
public class MadisBoundry extends FunctionExecutor {
    final int MINUTE_GAP = 5;
    Attribute.Type returnType;

    protected Object process(double lat, double lon, String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Date benchMarkTime = null;
        double minLat=0, minLon=0,maxLat=0, maxLon =0;
        String boundry;

        if(benchMarkTime== null){
            benchMarkTime = getTimestamp(timestamp);
            minLat = maxLat = lat;
            minLon = maxLon = lon;

        }
        else if((getTimestamp(timestamp).getTime() - benchMarkTime.getTime()) > MINUTE_GAP*60*60){ // should change
           benchMarkTime = getTimestamp(timestamp);
            boundry = minLat + " " + maxLat + " " + minLon + " " + maxLon;
            minLat = maxLat = lat;
            minLon = maxLon = lon;
            return boundry;
        }
        else{
            if (lat > maxLat) {
                maxLat = lat;
            }
            if (lat < minLat) {
                minLat = lat;
            }
            if (lon > maxLon) {
                maxLon = lon;
            }
            if (lon < minLon) {
                minLon = lon;
            }
        }
        return "";
    }

    private Date getTimestamp(String dateString){

        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Date dt = null;
        try {
            dt = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        returnType= Attribute.Type.STRING;
    }

    @Override
    protected Object process(Object o) {

        if ((o instanceof Object[]) && ((Object[]) o).length == 3) {
            double lat=Double.parseDouble(String.valueOf(((Object[]) o)[0]));
            double lon=Double.parseDouble(String.valueOf(((Object[]) o)[1]));
            String timestamp=String.valueOf(((Object[]) o)[2]);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            Date benchMarkTime = null;
            double minLat=0, minLon=0,maxLat=0, maxLon =0;
            String boundry;

            if(benchMarkTime== null){
                benchMarkTime = getTimestamp(timestamp);
                minLat = maxLat = lat;
                minLon = maxLon = lon;

            }
            else if((getTimestamp(timestamp).getTime() - benchMarkTime.getTime()) > MINUTE_GAP*60*60){ // should change
                benchMarkTime = getTimestamp(timestamp);
                boundry = minLat + " " + maxLat + " " + minLon + " " + maxLon;
                minLat = maxLat = lat;
                minLon = maxLon = lon;
                return boundry;
            }
            else{
                if (lat > maxLat) {
                    maxLat = lat;
                }
                if (lat < minLat) {
                    minLat = lat;
                }
                if (lon > maxLon) {
                    maxLon = lon;
                }
                if (lon < minLon) {
                    minLon = lon;
                }
            }

        }
        return "";
    }

    @Override
    public void destroy() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
