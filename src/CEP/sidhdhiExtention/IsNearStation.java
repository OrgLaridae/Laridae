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

            if(latA==latB && lonA==lonB){
                //if two given points are the same
                isNear=false;
            }else{
                //calculates the distance between two location points
                double theta = lonB - lonA;
                double dist = Math.sin(Math.toRadians(latB)) * Math.sin(Math.toRadians(latA)) + Math.cos(Math.toRadians(latB)) * Math.cos(Math.toRadians(latA)) * Math.cos(Math.toRadians(theta));
                dist = Math.acos(dist);
                dist = Math.toDegrees(dist);
                //in kilo meters
                dist = dist * 60 * 1.1515*1.609344;

                //compares the calculated value with a threshold value
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

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
