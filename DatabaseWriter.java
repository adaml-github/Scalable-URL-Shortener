import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

/**
 * Project: a1 File: DatabaseReader.java Created by adam on Oct 5, 2018 at
 * 11:08:43 AM
 *
 */

public class DatabaseWriter implements Runnable {
	static final int PORT = 5050;
	private LinkedHashMap<String, String> urlMap;

	DatabaseWriter(LinkedHashMap<String, String> urlMap) {
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
			new DatabaseWriteThread(socket, urlMap).start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		runServer(PORT);
	}
}

class DatabaseWriteThread extends DatabaseThread {

	public DatabaseWriteThread(Socket clientSocket, LinkedHashMap<String, String> urlMap) {
		super(clientSocket, urlMap);
	}

	public synchronized String runAction(Boolean useBackup, String input) {
		FileOutputStream fout = null;
		BufferedReader br = null;
		if (useBackup != this.useBackupToggle) {
		  urlMap.clear();
		}
		this.useBackupToggle = useBackup;
		if (input == null || input.isEmpty()) {
			return "";
		}
		String[] inputList = input.split("\t");
		System.out.println("Caching " + inputList[0] + " as " + inputList[1]);
		urlMap.put(inputList[0], inputList[1]);
		try {
			File db = new File(useBackup ? DATABASE_BACKUP : DATABASE);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(db)));
			StringBuilder sb = new StringBuilder(input);
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append("\n");
				sb.append(line);
			}
			db.delete();
			fout = new FileOutputStream(db);
			fout.write(sb.toString().getBytes());
			fout.flush();
		} catch (IOException e) {
			System.err.println("Error when writing: " + e);
		} finally {
			try {
			  br.close();
			  fout.close();
			} catch (IOException e) {
				System.err.println("Error closing/flushing write: " + e);
			}

		}
		return "";
	}
}
