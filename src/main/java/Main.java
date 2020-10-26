import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// https://www.baeldung.com/java-influxdb
public class Main {

    static final String serverURL = "http://ironmaiden.inf.unibz.it:8086";
    static final String localURL = "";

    static final String username = "root";
    static final String password = "root";

    static final String dbName = "test_db";

    static InfluxDB influxDB = null;


    public static void main(String[] args) throws IOException {

        // Connecting to the DB
        influxDB = InfluxDBFactory.connect(serverURL, username, password);

        // Pinging the DB
        Pong response = influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error pinging server");
            return;
        }

        // Creating database
        influxDB.deleteDatabase(dbName);
        influxDB.createDatabase(dbName);

        // Create and set a retention policy
        influxDB.createRetentionPolicy("defaultPolicy", dbName, "30d", 1, true);
        influxDB.setRetentionPolicy("defaultPolicy");

        // Inserting data
        insertOnePoint();
        insertTwoPoints();

        // Enable Batch?
        // influxDB.enableBatch(5, 100000000, TimeUnit.MILLISECONDS);
        // influxDB.disableBatch();

        // Method Concluded
        influxDB.close();
        System.out.println("Finished.");
    }

    // Inserting multiple points at a time
    public static void insertTwoPoints() {
        System.out.println("Insert two points method");
        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("name", "point_21")
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();

        Point point2 = Point.measurement("memory")
                .time(System.currentTimeMillis() - 100, TimeUnit.MILLISECONDS)
                .addField("name", "point_22")
                .addField("free", 4743696L)
                .addField("used", 1016096L)
                .addField("buffer", 1008467L)
                .build();

        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);
    }

    // Inserting one point
    public static void insertOnePoint () {
        System.out.println("Insert one point method");

        Point point = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("name", "point_11")
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();
        influxDB.write(dbName, "defaultPolicy", point);
    }
}
