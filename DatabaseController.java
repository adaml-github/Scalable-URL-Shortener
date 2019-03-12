import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Project: a1
 * File: DatabaseController.java
 * Created by adam on Oct 8, 2018 at 2:44:17 PM
 *
 */

/**
 * @author adam
 *
 */
public class DatabaseController {
  public static void main(String args[]) {
    LinkedHashMap<String, String> urlMap = new LinkedHashMap<String, String>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
          return size() > 16;
      }
    };
    DatabaseWriter dbw = new DatabaseWriter(urlMap);
    DatabaseReader dbr = new DatabaseReader(urlMap);
    Thread t1=new Thread(dbw);
    Thread t2=new Thread(dbr);
    t1.start();
    t2.start();
  }
}
