package WRF;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunScript {
    private int iExitValue;
    private String sCommandString;

    //namelist.wps parameters to be changed
    private static String inputWPSPath="/home/ruveni/WRF/AutomatedScripts/WPS/namelist.wps";
    private static String startDate="'2015-03-25_00:00:00'";
    private static String endDate="'2015-03-26_00:00:00'";
    private static int maxDom=1;
    private static long intervalSeconds=21600;
    private static String e_we="75";
    private static String e_sn="70";
    private static String geo_data_path="/home/ruveni/WRF/AutomatedScripts/WPS/geogrid/Data/geog_minimum";

    //namelist.input parameters to be changed
    private static String namelistWRFPath="/home/ruveni/WRF/AutomatedScripts/WRFV3/test/em_real/namelist.input";
    //int runDays,int runHours, String startYear, String startMonth, String startDay, String startHour, String endYear, String endMonth, String endDay, String endHour, long intervalSeconds, int maxDom, int e_we, int e_sn, int num_metgrid_levels, int num_metgrid_soil_levels) {
    private static int runDays=0;
    private static int runHours=24;
    private static String startYear="2015";
    private static String startMonth="03";
    private static String startDay="25";
    private static String startHour="00";
    private static String endYear="2015";
    private static String endMonth="03";
    private static String endtDay="26";
    private static String endHour="00";
    private static int num_metgrid_levels=40;
    private static int num_metgrid_soil_levels=4;

    public void runScript(String command) {
        sCommandString = command;
        CommandLine oCmdLine = CommandLine.parse(sCommandString);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        try {
            iExitValue = oDefaultExecutor.execute(oCmdLine);
        } catch (ExecuteException e) {

            System.err.println("Execution failed.");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("permission denied.");
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        RunScript runScript = new RunScript();

        //initially change the namelist.wps and namelist.input files according to the parameters set
        runScript.changeNamelistWPS(inputWPSPath,startDate,endDate,maxDom,intervalSeconds,e_we,e_sn,geo_data_path);
        runScript.changeNamelipsInput(namelistWRFPath,runDays,runHours,startYear,startMonth,startDay,startHour,endYear,endMonth,endtDay,endHour,intervalSeconds,maxDom,e_we,e_sn,num_metgrid_levels,num_metgrid_soil_levels);

        //run the WRF using the shell scripts
        runScript.runScript("sh /home/ruveni/IdeaProjects/DataAgent/src/main/java/org/mora/cep/AutomateWRF/autoauto.sh");
    }

    //changes the namelist.wps property file according to the parameters set
    public void changeNamelistWPS(String filePath, String startDate, String endDate, int maxDom, long intervalSeconds, String e_we, String e_sn, String geog_data_path) {
        try {
            FileWriter fw=new FileWriter(filePath);
            Stream<String> namelistContent = Files.lines(Paths.get("/home/ruveni/IdeaProjects/DataAgent/src/main/java/org/mora/cep/AutomateWRF/namelist.wps"));
            String[] namelistArray = namelistContent.collect(Collectors.toList()).toArray(new String[0]);
            //changes the parameters
            for (int i = 0; i < namelistArray.length; i++) {
                if (namelistArray[i].contains("start_date")) {
                    namelistArray[i] = " start_date = " + startDate + ",";
                } else if (namelistArray[i].contains("end_date")) {
                    namelistArray[i] = " end_date = " + endDate + ",";
                } else if (namelistArray[i].contains("max_dom")) {
                    namelistArray[i] = " max_dom = "+maxDom+",";
                } else if (namelistArray[i].contains("interval_seconds")) {
                    namelistArray[i] = " interval_seconds = "+intervalSeconds+",";
                } else if (namelistArray[i].contains("e_we")) {
                    namelistArray[i] = " e_we              =  "+e_we+",";
                } else if (namelistArray[i].contains("e_sn")) {
                    namelistArray[i] = " e_sn              =  "+e_sn+",";
                } else if (namelistArray[i].contains("geog_data_path")) {
                    namelistArray[i] = " geog_data_path = '"+geog_data_path+"',";
                }
                //writes the data to the file
                fw.write(namelistArray[i] + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //changes the namelist.input file according to the parameters set
    public void changeNamelipsInput(String filePath, int runDays,int runHours, String startYear, String startMonth, String startDay, String startHour, String endYear, String endMonth, String endDay, String endHour, long intervalSeconds, int maxDom, String e_we, String e_sn, int num_metgrid_levels, int num_metgrid_soil_levels) {
        try {
            FileWriter fw=new FileWriter(filePath);
            Stream<String> namelistContent = Files.lines(Paths.get("/home/ruveni/IdeaProjects/DataAgent/src/main/java/org/mora/cep/AutomateWRF/namelist.input"));
            String[] namelistArray = namelistContent.collect(Collectors.toList()).toArray(new String[0]);
            //changes the parameters
            for (int i = 0; i < namelistArray.length; i++) {
                if (namelistArray[i].contains("max_dom")) {
                    namelistArray[i] = "max_dom = "+maxDom+",";
                } else if (namelistArray[i].contains("interval_seconds")) {
                    namelistArray[i] = "interval_seconds = "+intervalSeconds+",";
                } else if (namelistArray[i].contains("e_we")) {
                    namelistArray[i] = "e_we              =  "+e_we+",";
                } else if (namelistArray[i].contains("e_sn")) {
                    namelistArray[i] = "e_sn              =  "+e_sn+",";
                } else if(namelistArray[i].contains("run_days")){
                    namelistArray[i] = "run_days                            = "+runDays+",";
                }else if(namelistArray[i].contains("run_hours")){
                    namelistArray[i] = "run_hours                            = "+runHours+",";
                }else if(namelistArray[i].contains("start_year")){
                    namelistArray[i] = "start_year                            = "+startYear+",";
                }else if(namelistArray[i].contains("start_month")){
                    namelistArray[i] = "start_month                            = "+startMonth+",";
                }else if(namelistArray[i].contains("start_day")){
                    namelistArray[i] = "start_day                            = "+startDay+",";
                }else if(namelistArray[i].contains("start_hour")){
                    namelistArray[i] = "start_hour                            = "+startHour+",";
                }else if(namelistArray[i].contains("end_year")){
                    namelistArray[i] = "end_year                            = "+endYear+",";
                }else if(namelistArray[i].contains("end_month")){
                    namelistArray[i] = "end_month                            = "+endMonth+",";
                }else if(namelistArray[i].contains("end_day")){
                    namelistArray[i] = "end_day                            = "+endDay+",";
                }else if(namelistArray[i].contains("end_hour")){
                    namelistArray[i] = "end_hour                            = "+endHour+",";
                }else if(namelistArray[i].contains("num_metgrid_levels")){
                    namelistArray[i] = "num_metgrid_levels                            = "+num_metgrid_levels+",";
                }else if(namelistArray[i].contains("num_metgrid_soil_levels")){
                    namelistArray[i] = "num_metgrid_soil_levels                            = "+num_metgrid_soil_levels+",";
                }
                //writes the data to the file
                fw.write(namelistArray[i] + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}