package CEP.sidhdhiExtention;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ruveni on 08/12/15.
 */

/*
    Should pass date and time as strings as parametrs
    Converts to milliseconds
 */
@SiddhiExtension(namespace = "madis", function = "isNearTimestamp")
public class IsNearTimestamp extends FunctionExecutor{
    Attribute.Type returnType;
    private Date dateA,dateB;
    private long dateAMilli,dateBMilli;
    private SimpleDateFormat sdf;
    private static final long TIME_DIFFERENCE_THRESHOLD=60;//in minutes

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        returnType = Attribute.Type.BOOL;
        sdf=new SimpleDateFormat("dd/MM/yyyy hh:mm");
        for (Attribute.Type attributeType : types) {
            if (attributeType == Attribute.Type.STRING) {
                break;
            } else {
                throw new QueryCreationException("Attributes should be type of STRING");
            }
        }
    }

    @Override
    protected Object process(Object o) {
        boolean isNear=false;
        if ((o instanceof Object[]) && ((Object[]) o).length == 2) {
            //parses strings to Date type
            try {
                dateA = sdf.parse(String.valueOf(((Object[]) o)[0]));
                dateB = sdf.parse(String.valueOf(((Object[]) o)[1]));
            }catch (Exception e){

            }
            //converts the dates to milliseconds
            dateAMilli=dateA.getTime();
            dateBMilli=dateB.getTime();
            long timeDif;

            //checks the difference is closer to the threshold value
            if(dateAMilli>dateBMilli){
                timeDif=dateAMilli-dateBMilli;
                if((timeDif/(60*1000))<TIME_DIFFERENCE_THRESHOLD){
                    isNear=true;
                }
            }else{
                timeDif=dateBMilli-dateAMilli;
                if((timeDif/(60*1000))<TIME_DIFFERENCE_THRESHOLD){
                    isNear=true;
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
