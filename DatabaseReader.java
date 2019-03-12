/**
 * Project: a1
 * File: DatabaseReader.java
 * Created by adam on Oct 5, 2018 at 9:44:53 AM
 */
import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
/**
 * 
 *
 */
public class DatabaseReader implements Runnable {
  static final int PORT = 3030;
  private LinkedHashMap<String, String> urlMap;
  
  DatabaseReader(LinkedHashMap<String, String> urlMap){
    this.urlMap = urlMap;
  }
 
  
  public void runServer(int port) {
    ServerSocket serverSocket = null;
    Socket socket = null;

    try {
        serverSocket = new ServerSocket(port);
    } catch (IOException e) {
        e.printStackTrace();
    }
    while (true) {
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("I/O error: " + e);
        }
        // new thread for a client
        new DatabaseReaderThread(socket, urlMap).start();
    }    
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    runServer(PORT);
  }
}

class DatabaseReaderThread extends DatabaseThread{
	public DatabaseReaderThread(Socket socket,LinkedHashMap<String, String> urlMap) {
		super(socket, urlMap);
	}

	public synchronized String runAction(Boolean useBackup,String input) {
	  String longURL = null;
	  if (useBackup != this.useBackupToggle) {
      urlMap.clear();
    }
    this.useBackupToggle = useBackup;
    if (input == null || input.isEmpty()) {
      return "";
    }
	//Check cache before connecting to any DB
    if ((longURL = urlMap.get(input)) != null) {
      return longURL;
    }
	  FileReader fileReader = null;
	  BufferedReader bufferedReader = null;
	  try {
	    File file = new File(useBackup ? DATABASE_BACKUP : DATABASE);
	    fileReader = new FileReader(file);
	    bufferedReader = new BufferedReader(fileReader);
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
	      String [] map = line.split("\t");
	      if(map[0].equals(input)){
	        longURL = map[1];
	        break;
	      }
	    }
	  } catch (IOException e) {
	    System.err.println("Error when reading: "+e);
	  } finally {
	    try {
        fileReader.close();
        bufferedReader.close();
      } catch (IOException e) {
        System.err.println("Error closing Read stream:"+e);
      }
	  }
	  urlMap.put(input, longURL == null ? "" : longURL);
	  return longURL;
	}
}

