package CEP.terminalCommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by ruveni on 30/10/15.
 */
public class ExecuteShellComand{
    public static void main(String[] args) {

        ExecuteShellComand obj = new ExecuteShellComand();

        String command = "pwd";
        String absolutePath = obj.executeCommand(command);

        System.out.println("Staring ActiveMQ...");
        String activemqcommand="sh "+absolutePath+"/apache-activemq-5.9.0/bin/activemq start";
        activemqcommand=activemqcommand.replaceAll("\\n","");
        System.out.println(activemqcommand);
        System.out.println(obj.executeCommand(activemqcommand));

        System.out.println();
        System.out.println("Staring CEP...........");
        String cepCommand="sh "+absolutePath+"/wso2cep-3.1.0/bin/wso2server.sh";
        cepCommand=cepCommand.replaceAll("\\n","");
        System.out.println(cepCommand);
        System.out.println(obj.executeCommand(cepCommand));

    }

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }
}
