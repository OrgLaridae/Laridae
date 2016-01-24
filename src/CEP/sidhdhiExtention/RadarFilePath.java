package CEP.sidhdhiExtention;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;


/**
 * Created by ruveni on 16/11/15.
 */

@SiddhiExtension(namespace = "file", function = "getPath")
public class RadarFilePath extends FunctionExecutor{
    Attribute.Type returnType;

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        for (Attribute.Type attributeType : types) {
            if (attributeType == Attribute.Type.STRING) {
                returnType = attributeType;
                break;
            } else {
                throw new QueryCreationException("file cannot have parameters with types String or Bool");
            }
        }
    }

    @Override
    protected Object process(Object o) {
        String file="";
        if (o instanceof Object) {
            String data = o.toString();
            String[] zValues = data.split(",");
            file=zValues[0];
        }
        return file;
    }

    public void destroy() {
    }

    public Attribute.Type getReturnType() {
        return returnType;
    }
}
