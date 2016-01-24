package CEP.sidhdhiExtention;

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
@SiddhiExtension(namespace = "madis", function = "isNearStation")
public class IsNearStation extends FunctionExecutor {
    Attribute.Type returnType;
    private static final double DISTANCE_THRESHOLD = 35;
    private static final double EARTH_RADIUS = 6371;//in kilometers
    private double latA = 0, lonA = 0, latB = 0, lonB = 0, dLat = 0, dLng = 0, a = 0, c = 0, dist = 0;

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
        boolean isNear = false;
        if ((o instanceof Object[]) && ((Object[]) o).length == 4) {
            latA = Double.parseDouble(String.valueOf(((Object[]) o)[0]));
            lonA = Double.parseDouble(String.valueOf(((Object[]) o)[1]));
            latB = Double.parseDouble(String.valueOf(((Object[]) o)[2]));
            lonB = Double.parseDouble(String.valueOf(((Object[]) o)[3]));
            dLat = Math.toRadians(latB - latA);
            dLng = Math.toRadians(lonB - lonA);

            //calculates the actual distance
            a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latA)) * Math.cos(Math.toRadians(latB)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            dist = (double) (EARTH_RADIUS * c);

            if (dist < DISTANCE_THRESHOLD) {
                isNear = true;
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
