package CEP.AutomateWRF;

import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

public class RunScript {
    int iExitValue;
    String sCommandString;

    public void runScript(String command){
        sCommandString = command;
        CommandLine oCmdLine = CommandLine.parse(sCommandString);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        try {
            iExitValue = oDefaultExecutor.execute(oCmdLine);
        } catch (ExecuteException e) {
            // TODO Auto-generated catch block
            System.err.println("Execution failed.");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("permission denied.");
            e.printStackTrace();
        }
    }

//    public static void main(String args[]){
//        RunScript runScript = new RunScript();
//        runScript.runScript("sh /home/ruveni/IdeaProjects/DataAgent/src/main/java/org/mora/cep/AutomateWRF/autoauto.sh");
//    }
}