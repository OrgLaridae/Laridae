package CEP.sidhdhiExtention;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

/**
 * Created by chamil on 12/9/15.
 */

@SiddhiExtension(namespace = "madis", function = "boundary")
public class WeatherBoundary extends FunctionExecutor {
    Attribute.Type returnType;
    private long activatedAt = Long.MAX_VALUE;
    double minLatitude = 90, maxLatitude = -90, minLongitude = 180, maxLongitude = -180;//boundary values
    private static final int TIME_GAP=5;//in minutes

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        //sets the start time
        activatedAt=System.currentTimeMillis();
        returnType= Attribute.Type.STRING;
    }

    @Override
    protected Object process(Object o) {
        if ((o instanceof Object[]) && ((Object[]) o).length == 2) {

            //gets the parameters sent
            double lat=Double.parseDouble(String.valueOf(((Object[]) o)[0]));
            double lon=Double.parseDouble(String.valueOf(((Object[]) o)[1]));

            if((System.currentTimeMillis()-activatedAt)>=(TIME_GAP*60*1000)){
                //resets the boundary values
                minLatitude = 90;
                maxLatitude = -90;
                minLongitude = 180;
                maxLongitude = -180;
                //resets the timer
                activatedAt=System.currentTimeMillis();
            }

            //calculates the boundary
            if(lat>maxLatitude){
                maxLatitude=lat;
            }
            if(lat<minLatitude){
                minLatitude=lat;
            }
            if(lon>maxLongitude){
                maxLongitude=lon;
            }
            if(lon<minLongitude){
                minLongitude=lon;
            }
        }

        //returns the calculated boundary
        return minLatitude+" "+maxLatitude+" "+minLongitude+" "+maxLongitude;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
