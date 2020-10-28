import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {

    // Store users' configurations - default settings written here
    static Scanner sc = new Scanner(System.in);
    static int location_no=-1, insertion_no=-1, index_no=-1;
    static int multiple_N_tuples = 5;
    static boolean exec_om=false, exec_mixed=false;
    static boolean useServerInfluxDB = false;
    static String data_file_path = "data/TEMPERATURE_DATA.csv";

    // Tests configurations
    static String[] location_types = {"ironmaiden", "ironlady", "pc"};
    static String[] insertion_types = {"one", "multiple", "mixed"};
    static String[] index_types = {"inmem", "tsi1"};

    // Logger names date formatter
    static String logs_path = "logs/";
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "YYYY-MM-dd__HH.mm.ss");

    // Creating the database interactor
    static DatabaseInteractions dbi;

    public static void main(String[] args) throws IOException {

        try {

            // Getting configurations from user
            talkToUser();

            // Instantiate general logger
            Logger general_logger = instantiateLogger("general");
            general_logger.info("Location: " +location_types[location_no]);

            // Loading the credentials to the new influxdb database
            general_logger.info("Instantiating database interactor");
            dbi = new DatabaseInteractions(multiple_N_tuples, data_file_path, useServerInfluxDB);

            // Marking start of tests
            general_logger.info("Executing tests from " +location_types[location_no]);
            general_logger.info("---Start of Tests!---");

            // Iterating through the tests to be done
            for (insertion_no=0; insertion_no<(insertion_types.length); insertion_no++) {

                // Checking if this test is required by the user at the beginning
                if ((insertion_no!=2 && exec_om) || (insertion_no==2 && exec_mixed)) {

                    // Printing out the test configuration and creating logger
                    String test_configuration = ""+(location_no+1)+(insertion_no+1)+(index_no+1);
                    Logger test_logger = instantiateLogger("test_" + test_configuration);
                    test_logger.info("Test #" + test_configuration
                            +": from machine \"" +location_types[location_no]+ "\","
                            +" having \"" +insertion_types[insertion_no]+ "\" insertions at a time"
                            +" and \""+index_types[index_no]+"\" index set.");

                    // Opening a connection to the postgreSQL database
                    test_logger.info("Connecting to the PostgreSQL database...");
                    dbi.createDBConnection();
                    dbi.createDatabase();

                    // Checking whether concurrent queries are running
                    String response = "";
                    if (insertion_no == 2) {
                        while (response.compareTo("y") != 0) {
                            test_logger.info("Asking to start the concurrent queries");
                            System.out.print("Are you at the \"Ready Statement\" on the other script? (y) ");
                            response = sc.nextLine();
                        }
                        test_logger.info("Concurrent queries started");
                    }

                    // ==START OF TEST==
                    System.out.println(test_configuration);
                    dbi.insertTuples(insertion_no, test_logger);

                    // ==END OF TEST==
                    test_logger.info("--End of test #"+test_configuration+"--");

                    // Checking whether concurrent queries are running
                    if (insertion_no == 2) {
                        response = "";
                        while (response.compareTo("y") != 0) {
                            test_logger.info("Asking to stop the concurrent queries");
                            System.out.print("Did you STOP the concurrent queries? (y) ");
                            response = sc.nextLine();
                        }
                        test_logger.info("Concurrent queries stopped");
                    }

                    // Clean database and close connections
                    endOfTest();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbi.closeDBConnection();
        }
    }

    //-----------------------UTILITY----------------------------------------------

    // Interactions with the user to understand his/her preferences
    public static void talkToUser () throws Exception {

        System.out.println("5 questions for you!");
        String response = "";
        String final_message = "";
        boolean correct_answer = false;

        // Understanding where the script is executed
        response = "";
        while (location_no == -1) {
            System.out.print("1. From which machine are you executing this script?"+
                    " (Type \"ironmaiden\", \"ironlady\" or \"pc\"): ");
            response = sc.nextLine();
            location_no = returnStringIndex(location_types, response);
        }
        final_message += "Executing from \""+location_types[location_no]+"\", ";

        // Understanding whether the user wants the sever db or the local db
        response = "";
        correct_answer = false;
        while (!correct_answer) {
            System.out.print("2. Where do you want it to be executed?"
                    +" (Type \"s\" for server database,"
                    +" type \"l\" for local database): ");
            response = sc.nextLine().replace(" ", "");

            // Understanding what the user wants
            if (response.compareTo("l") == 0 || response.compareTo("s") == 0) {
                correct_answer=true;
                if (response.compareTo("l") == 0) {
                    useServerInfluxDB = false;
                }
            }
        }
        final_message += "using the database on \""+((useServerInfluxDB)?"server":"localhost")+"\", ";

        // Understanding what the index configured
        while (index_no == -1) {
            System.out.print("3. What is the index configured right now?"
                    +" (Type \"inmem\" or \"tsi1\"): ");
            response = sc.nextLine().replace(" ", "");
            index_no = returnStringIndex(index_types, response);
        }
        final_message += "with index \""+(index_types[index_no])+"\", ";

        // Understanding what the user wants to be executed
        response = "";
        correct_answer = false;
        while (!correct_answer) {
            System.out.print("4. What do you want to execute?"
                    +" (Type \"1\" for all 3 tests,"
                    +" type \"2\" for One and Multiple tuples only,"
                    +" type \"3\" for Mixed Workload only): ");
            response = sc.nextLine().replace(" ", "");

            // Understanding what the user wants
            if (response.compareTo("1") == 0) {
                exec_om=true;
                exec_mixed=true;
                correct_answer=true;
                final_message += "testing \"all\" configurations, ";
            }
            if (response.compareTo("2") == 0) {
                exec_om=true;
                correct_answer=true;
                final_message += "testing \"all but mixed\" configurations, ";
            }
            if (response.compareTo("3") == 0) {
                exec_mixed=true;
                correct_answer=true;
                final_message += "testing \"only mixed\" configurations, ";
            }
        }

        // Understanding which file to run
        response = "";
        correct_answer = false;
        while (!correct_answer) {
            System.out.print("5. Finally, inside the data folder, what is the " +
                    "data file name? (\"d\" for default): ");
            response = sc.nextLine().replace(" ", "");

            if (response.compareTo("d")==0) {
                response = "TEMPERATURE_ns.csv";
            }

            // Checking if it is a file
            File f = new File("data/"+response);
            if(f.exists() && !f.isDirectory()) {
                data_file_path = "data/"+response;
                correct_answer = true;
            }
        }

        System.out.println(final_message+"getting data from \""+data_file_path+"\".");
        System.out.println("We are ready to start, thank you!");
    }

    // Instantiating the logger for the general information or errors
    public static Logger instantiateLogger (String file_name) throws IOException {

        // Retrieving and formatting current timestamp
        Date date = new Date();
        Timestamp now = new Timestamp(date.getTime());
        String dateAsString = simpleDateFormat.format(now);

        // Setting the name of the folder
        if (file_name.compareTo("general") == 0) {
            file_name += (location_no+1);
            logs_path += dateAsString+"__"+(location_no+1)+"/";
            File file = new File(logs_path);
            boolean bool = file.mkdirs();
        }

        // Instantiating general logger
        String log_complete_path = logs_path + dateAsString + "__" + file_name
                + "__influxdb_data_ingestion.xml";
        Logger logger = Logger.getLogger("InfluxDBDataIngestionGeneralLog_"+file_name);
        logger.setLevel(Level.ALL);

        // Loading properties of log file
        Properties preferences = new Properties();
        try {
            FileInputStream configFile = new FileInputStream("resources/logging.properties");
            preferences.load(configFile);
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException ex) {
            System.out.println("[WARN] Could not load configuration file");
        }

        // Instantiating file handler
        FileHandler gl_fh = new FileHandler(log_complete_path);
        logger.addHandler(gl_fh);

        // Returning the logger
        return logger;
    }

    // Returns the index_no of the specified string in the string array
    public static int returnStringIndex(String[] list, String keyword) {
        for (int i=0; i<list.length; i++) {
            if (list[i].compareTo(keyword) == 0) {
                return i;
            }
        }
        return -1;
    }

    // Cleans the database and closes all the connections to it
    public static void endOfTest() {
        dbi.closeDBConnection();
    }
}
