package CEP.sidhdhiExtention;

import CEP.cepProcessing.CEPEnvironment;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

/**
 * Created by ruveni on 08/12/15.
 */

//latitudes and longitude values of two different stations are passed as parameters
//calculates the distance in between the two points
//checks the calculated distance is more than a defined threshold value
@SiddhiExtension(namespace = "weather", function = "isNearStation")
public class IsNearStation extends FunctionExecutor {
    Attribute.Type returnType;
    private double latA = 0, lonA = 0, latB = 0, lonB = 0, dLat = 0, dLng = 0, a = 0, c = 0, dist = 0;
    boolean isNear=false;

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        returnType = Attribute.Type.BOOL;
        for (Attribute.Type attributeType : types) {
            if (attributeType == Attribute.Type.DOUBLE) {
                break;
            } else {
                throw new QueryCreationException("Attributes should be type of DOUBLE");
            }
        }
    }

    @Override
    protected Object process(Object o) {
        isNear = false;
        if ((o instanceof Object[]) && ((Object[]) o).length == 4) {
            latA = Double.parseDouble(String.valueOf(((Object[]) o)[0]));
            lonA = Double.parseDouble(String.valueOf(((Object[]) o)[1]));
            latB = Double.parseDouble(String.valueOf(((Object[]) o)[2]));
            lonB = Double.parseDouble(String.valueOf(((Object[]) o)[3]));
//            dLat = Math.toRadians(latB - latA);
//            dLng = Math.toRadians(lonB - lonA);
//
//            //calculates the actual distance
//            a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latA)) * Math.cos(Math.toRadians(latB)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
//            c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//            dist = (double) (EARTH_RADIUS * c);

            if(latA==latB && lonA==lonB){
                isNear=false;
            }else{
                double theta = lonB - lonA;
                double dist = Math.sin(deg2rad(latB)) * Math.sin(deg2rad(latA)) + Math.cos(deg2rad(latB)) * Math.cos(deg2rad(latA)) * Math.cos(deg2rad(theta));
                dist = Math.acos(dist);
                dist = rad2deg(dist);
                dist = dist * 60 * 1.1515*1.609344;

                if (dist < CEPEnvironment.DISTANCE_THRESHOLD) {
                    isNear = true;
                }
            }
        }
        return isNear;
    }

    @Override
    public void destroy() {

    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
