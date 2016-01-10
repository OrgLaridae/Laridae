package CEP.sidhdhiExtention;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

/**
 * Created by ruveni on 1/8/16.
 */
@SiddhiExtension(namespace = "weather", function = "coordinates")
public class LocationCoordinates extends FunctionExecutor {
    Attribute.Type returnType;
    private long activatedAt = Long.MAX_VALUE;
    private String locationCoord="";
    private static final int TIME_GAP=1;//in minutes

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
                locationCoord="";
                //resets the timer
                activatedAt=System.currentTimeMillis();
            }

            locationCoord=locationCoord+lat+":"+lon+",";
        }

        //returns the calculated boundary
        return locationCoord;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
