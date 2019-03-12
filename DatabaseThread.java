import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedHashMap;

public class DatabaseThread extends Thread{
  protected Socket socket;
  protected LinkedHashMap<String, String> urlMap;
  static final String DATABASE = "/virtual/username/database.txt";
  static final String DATABASE_BACKUP = "/virtual/username/databaseBackup.txt";
  boolean useBackupToggle;
  public DatabaseThread(Socket clientSocket, LinkedHashMap<String, String> urlMap) {
      this.socket = clientSocket;
      this.urlMap = urlMap;
      this.useBackupToggle = false;
  }
  
  /**
   * 
   * @param input
   * @return
   */
  public String runAction(Boolean useBackup, String input) {
    return "";
  }

  /* 
   * Take in any URLShortner request and run the given DB action
   */
  public void run() {
      InputStream inp = null;
      BufferedReader brinp = null;
      DataOutputStream out = null;
      try {
          inp = socket.getInputStream();
          brinp = new BufferedReader(new InputStreamReader(inp));
          out = new DataOutputStream(socket.getOutputStream());
          Boolean useBackup = Boolean.parseBoolean(brinp.readLine());
          System.out.println("Database: " + useBackup);
          String request = brinp.readLine();
          System.out.println("Request: " + request);
          String result = runAction(useBackup, request);
          System.out.println("Result: " + result);
          if (result != null) {
            out.writeBytes(result+"\n\r");  
            out.flush();
          }
      } catch (IOException e) {
          System.err.println("Error reading and finding file: " +e);
      } finally {
        try {
          inp.close();
          out.close();
          brinp.close();
        } catch (Exception e) {
          System.err.println("Error closing stream : " + e.getMessage());
        } 
      }
  }
}