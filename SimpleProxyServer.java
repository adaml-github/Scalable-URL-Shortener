import java.io.*;
import java.net.*;
import java.util.Arrays;

public class SimpleProxyServer {
  static final boolean verbose = true;
  public static void main(String[] args) throws IOException {
    try {
      int remoteport = 4040;
      int localport = 2020;
      String[] hosts = getHosts();
      // Print a start-up message
      if (verbose)
      System.out.println("Starting proxy :" + remoteport
          + " on port " + localport);
      // And start running the server
      runServer(hosts, remoteport, localport); // never returns
    } catch (Exception e) {
      System.err.println(e);
    }
  }
  
  public static String[] getHosts(){
		String stringHosts = "";
		try {
			File file = new File("hosts");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			
			while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				stringHosts += line + ",";
			}
			fileReader.close();
		} catch (IOException e) {
			
		} 
		return stringHosts.split(",");
	}

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public static void runServer(String[] hosts, int remoteport, int localport)
      throws IOException {
    // Create a ServerSocket to listen for connections with
    ServerSocket ss = new ServerSocket(localport);
    if (verbose) {
      System.out.println(Arrays.toString(hosts));
    }
    int i = 0;
    final byte[] request = new byte[1024];
    byte[] reply = new byte[4096];
    
    while (true) {
      Socket client = null, urlShortnerServer = null;
      try {
        // Wait for a connection on the local port
        client = ss.accept();

        final InputStream streamFromClient = client.getInputStream();
        final OutputStream streamToClient = client.getOutputStream();
        // Make a connection to the real server.
        // If we cannot connect to the server, send an error to the
        // client, disconnect, and continue waiting for connections.
        while (true) {
	        try {
	          i++;
	          //Connect to URLShortner        	
	          urlShortnerServer = new Socket(hosts[i%hosts.length], remoteport);
	          if (verbose)
	          System.out.println("Using host: " + hosts[i%hosts.length]);
	          break;
	        } catch (IOException e) {
	        }
        }
        // Get server streams.
        InputStream streamFromServer = urlShortnerServer.getInputStream();
        OutputStream streamToServer = urlShortnerServer.getOutputStream();

        // a thread to read the client's requests and pass them
        // to the server. A separate thread for asynchronous.
        Thread t = new Thread() {
          @Override
          public void run() {
            int bytesRead;
            try {
              while ((bytesRead = streamFromClient.read(request)) != -1) {
                streamToServer.write(request, 0, bytesRead);
                streamToServer.flush();
              }
            } catch (IOException e) {
            }

            // the client closed the connection to us, so close our
            // connection to the server.
            try {
              streamToServer.close();
            } catch (IOException e) {
            }
          }
        };

        // Start the client-to-server request thread running
        t.start();

        // Read the server's responses
        // and pass them back to the client.
        int bytesRead;
        try {
          while ((bytesRead = streamFromServer.read(reply)) != -1) {
            streamToClient.write(reply, 0, bytesRead);
            streamToClient.flush();
          }
        } catch (IOException e) {
        }

        // The server closed its connection to us, so we close our
        // connection to our client.
        streamToClient.close();
      } catch (IOException e) {
        System.err.println(e);
      } finally {
        try {
          if (urlShortnerServer != null) {
            urlShortnerServer.close();
          }
          if (client != null) {
            client.close();
          }
        } catch (IOException e) {
        }
      }
    }
  }
}
