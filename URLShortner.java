import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class URLShortner implements Runnable {

	// port to listen connection
	static final int PROXY_PORT = 4040;

	// verbose mode
	static final boolean verbose = true;

	protected Thread runningThread = null;

	// fixed thread pool of size 4
	protected ExecutorService threadPool = Executors.newFixedThreadPool(4);

	protected ServerSocket serverSocket;

	public URLShortner() {
	}

	// basically "main" but for Runnables - so start() goes to run() ??
	@Override
	public void run() {

		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}

		// opening server socket
		try {
			this.serverSocket = new ServerSocket(PROXY_PORT);
			if (verbose) {
				System.out.println("Server started.\nListening for connections on port : " + PROXY_PORT + " ...\n");
			}

		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}

		// while running the server try to accept connections
		while (true) {

			if (verbose) {
				System.out.println("Connecton opened. (" + new Date() + ")");
			}
			Socket clientSocket = null;

			try {
				clientSocket = this.serverSocket.accept();
			} catch (Exception e) {
			  System.err.println("Error connecting to Db:" + e);
			}
			/*
			 * new Thread( new handleRunnable(clientSocket) ).start();
			 */
			// threadpool executes the handle class thread, and picks an available thread in
			// the pool
			this.threadPool.execute((new handleRunnable(clientSocket)));
		}
	}

	public static void main(String[] args) {
		URLShortner shorten = new URLShortner();
		new Thread(shorten).start();
	}
}

// the handle method but in a class form as a Runnable
class handleRunnable implements Runnable {

	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	static final String REDIRECT_RECORDED = "redirect_recorded.html";
	static final String REDIRECT = "redirect.html";
	static final String NOT_FOUND = "notfound.html";
  static final int DATABASE_READ_PORT = 3030;
  static final int DATABASE_WRITE_PORT = 5050;
	static final boolean verbose = true;

	private Socket clientSocket;
	private Socket databaseReadSocket;
	private Socket databaseWriteSocket;
	private String[] hosts;

	public handleRunnable(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.hosts = SimpleProxyServer.getHosts();
	}

	@Override
	public void run() {
		// printout to test that all threads are looped through
		long threadId = Thread.currentThread().getId() % 4 + 1;
		if (verbose)
			System.out.println("thread " + threadId + " of 4");

		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream());
			dataOut = new BufferedOutputStream(clientSocket.getOutputStream());

			String input = in.readLine();

			if (verbose) {
				System.out.println("first line: " + input);
			}
			Pattern pput = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
			Matcher mput = pput.matcher(input);
			if (mput.matches()) {
				String shortResource = mput.group(1);
				String longResource = mput.group(2);
				String httpVersion = mput.group(3);

				save(shortResource, longResource);

				File file = new File(WEB_ROOT, REDIRECT_RECORDED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				// read html content to return to client
				byte[] fileData = readFileData(file, fileLength);

				out.println("HTTP/1.1 200 OK");
				out.println("Server: Java HTTP Server/Shortner : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println();
				out.flush();

				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
			} else {
				Pattern pget = Pattern.compile("^(\\S+)\\s+/(\\S+)\\s+(\\S+)$");
				Matcher mget = pget.matcher(input);
				if (mget.matches()) {
					String method = mget.group(1);
					String shortResource = mget.group(2);
					String httpVersion = mget.group(3);

					String longResource = find(shortResource);
					if (longResource != null && !longResource.isEmpty()) {
						File file = new File(WEB_ROOT, REDIRECT);
						int fileLength = (int) file.length();
						String contentMimeType = "text/html";

						// read content to return to client
						byte[] fileData = readFileData(file, fileLength);

						// out.println("HTTP/1.1 301 Moved Permanently");
						out.println("HTTP/1.1 307 Temporary Redirect");
						out.println("Location: " + longResource);
						out.println("Server: Java HTTP Server/Shortner : 1.0");
						out.println("Date: " + new Date());
						out.println("Content-type: " + contentMimeType);
						out.println("Content-length: " + fileLength);
						out.println();
						out.flush();

						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
					} else {
						File file = new File(WEB_ROOT, FILE_NOT_FOUND);
						int fileLength = (int) file.length();
						String content = "text/html";
						byte[] fileData = readFileData(file, fileLength);

						out.println("HTTP/1.1 404 File Not Found");
						out.println("Server: Java HTTP Server/Shortner : 1.0");
						out.println("Date: " + new Date());
						out.println("Content-type: " + content);
						out.println("Content-length: " + fileLength);
						out.println();
						out.flush();

						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
					}
				} else {
				  File file = new File(WEB_ROOT, DEFAULT_FILE);
          int fileLength = (int) file.length();
          String content = "text/html";
          byte[] fileData = readFileData(file, fileLength);

          out.println("HTTP/1.1 404 File Not Found");
          out.println("Server: Java HTTP Server/Shortner : 1.0");
          out.println("Date: " + new Date());
          out.println("Content-type: " + content);
          out.println("Content-length: " + fileLength);
          out.println();
          out.flush();

          dataOut.write(fileData, 0, fileLength);
          dataOut.flush();
				}
			}
		} catch (Exception e) {
			System.err.println("Server error");
		} finally {
			try {
				in.close();
				out.close();
				clientSocket.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
	}

	private synchronized String find(String shortURL) {
		String longURL = null;
		InputStream inp = null;
		BufferedReader brinp = null;
		DataOutputStream out = null;
		boolean useBackup = false;
		try {
		  // Check which db partition to look through
		  try {
		    this.databaseReadSocket = new Socket(this.hosts[getHost(shortURL)], DATABASE_READ_PORT);  
		  } catch (IOException e) {
		    System.out.println("Could not connect to db... Moving to backup");
		    System.err.println("Error searching: " + e);  
		    System.out.println(this.hosts[(getHost(shortURL) +1)%hosts.length]);
		    this.databaseReadSocket = new Socket(this.hosts[(getHost(shortURL) +1)%4], DATABASE_READ_PORT);  
        useBackup = true;
		  }
		  
		  inp = databaseReadSocket.getInputStream();
      brinp = new BufferedReader(new InputStreamReader(inp));
      out = new DataOutputStream(databaseReadSocket.getOutputStream());
			if (verbose) {
				System.out.println("Seaching for shortURL: " + shortURL);
			}
			out.writeBytes(useBackup + "\n");
			out.writeBytes(shortURL + "\n\r");
			out.flush();
			longURL = brinp.readLine();
			
			if (verbose) {
				System.out.println("Returned URL: " + longURL);
			}
		} catch (IOException e) {
			System.err.println("Error searching: " + e);
		} finally {
			try {
				inp.close();
				out.close();
				brinp.close();
				this.databaseReadSocket.close();
			} catch (Exception e) {
				System.err.println("Error closing Find stream : " + e);
			}
		}
		return longURL;
	}

	private synchronized void save(String shortURL, String longURL) {
	  DataOutputStream out = null;
	  boolean useBackup = false;
	  try {
	    if (verbose)
	      System.out.println("Saving "+shortURL+" as " + longURL);
	    try {
	      this.databaseWriteSocket = new Socket(this.hosts[getHost(shortURL)], DATABASE_WRITE_PORT);  
      } catch (IOException e) {
        System.out.println("Could not connect to db... Moving to backup");
        System.err.println("Error searching: " + e);
        this.databaseWriteSocket = new Socket(this.hosts[(getHost(shortURL) +1)%4], DATABASE_WRITE_PORT);  
        useBackup = true;
      }
			
			out = new DataOutputStream(databaseWriteSocket.getOutputStream());
			out.writeBytes(useBackup + "\n");
			out.writeBytes(shortURL + "\t" + longURL + "\n\r");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error saving file: " + e);
		}finally {
      try {
        out.close();
        this.databaseWriteSocket.close();
      } catch (Exception e) {
        System.err.println("Error closing save stream : " + e);
      }
    }
		return;
	}

	private synchronized byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) {
				fileIn.close();
			}
		}

		return fileData;
	}
	
	/**
	 * Given an input return the host that deals with the character
	 * 
	 * @param input
	 * @return
	 */
	private int getHost(String input) {
    if (input.charAt(0) >= 'A' && input.charAt(0) <= 'H' || input.charAt(0) >= 'a' && input.charAt(0) <= 'h') {
      return 0;
    } else if (input.charAt(0) >= 'I' && input.charAt(0) <= 'P' || input.charAt(0) >= 'i' && input.charAt(0) <= 'p') {
      return 1;
    } else if (input.charAt(0) >= 'Q' && input.charAt(0) <= 'Z' || input.charAt(0) >= 'q' && input.charAt(0) <= 'z') {
      return 2;
    } else {
      return 3;
    }
	}
}
