package CEP.sidhdhiExtention;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ruveni on 1/8/16.
 */
@SiddhiExtension(namespace = "weather", function = "coordinates")
public class LocationCoordinates extends FunctionExecutor {
    Attribute.Type returnType;
    private long activatedAt = Long.MAX_VALUE;
    private StringBuilder builder;
    private static final int TIME_GAP=1;//in minutes
    private String filePath="/home/ruveni/Data/Test.txt";
    File file=new File(filePath);

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        //sets the start tim
        activatedAt=System.currentTimeMillis();
        returnType= Attribute.Type.STRING;
        builder=new StringBuilder();
    }

    @Override
    protected Object process(Object o) {
        if ((o instanceof Object[]) && ((Object[]) o).length == 2) {

            //gets the parameters sent
            double lat=Double.parseDouble(String.valueOf(((Object[]) o)[0]));
            double lon=Double.parseDouble(String.valueOf(((Object[]) o)[1]));

            if((System.currentTimeMillis()-activatedAt)>=(TIME_GAP*60*1000)){
                BufferedWriter bwr = null;
                try {
                    bwr = new BufferedWriter(new FileWriter(file));
                    //write contents of StringBuilder to a file
                    bwr.write(builder.toString());
                    bwr.flush();
                    bwr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //resets the boundary values
                builder=new StringBuilder();
                //resets the timer
                activatedAt=System.currentTimeMillis();
            }

            //locationCoord=locationCoord+lat+":"+lon+",";
            builder.append(lat+":"+lon+"\n");
        }

        //returns the calculated boundary
        return filePath;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
