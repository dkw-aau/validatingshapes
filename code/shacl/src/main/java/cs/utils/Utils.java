package cs.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class contains some random utility functions required throughout the project
 */
public class Utils {
    
    private static long secondsTotal;
    private static long minutesTotal;
    
    public static void getCurrentTimeStamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        dtf.format(now);
    }
    
    public static boolean isValidIRI(String iri) {
        return iri.indexOf(':') > 0;
    }
    

    
    public static void logTime(String method, long seconds, long minutes) {
        secondsTotal += seconds;
        minutesTotal += minutes;
        System.out.println("Time Elapsed Time " + method + " " + seconds + " sec , " + minutes + " min");
        System.out.println("Total Parsing Time " + secondsTotal + " sec , " + minutesTotal + " min");
    }
}
