package de.turtle.imp.auth_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
/*
 * To Do:
 * - Differentiate whether user is registered and entered a wrong password or
 * 		if the user is not registered
 * */
import de.turtle.imp.models.*;

public class AuthentificationServer {

	private static final int AUTH_PORT = 55030;
	private static HashMap<String, String> usersMap = new HashMap<String, String>();
	private static BufferedReader fileIn = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public AuthentificationServer() {

	}

	private void start() {
		// ServerSocket serverSocket;
		// read the user list to authenticate users
		try {
			fileIn = new BufferedReader(new FileReader("users.txt"));
		} catch (FileNotFoundException e2) {
			System.out.println("TurtleCage.start() at FileReader users.txt");
			e2.printStackTrace();
		}
		// Get line count to fill the hash map
		// Number of users = number of lines
		int lines = getLines(fileIn);
		try {
			loadUsers(lines);
		} catch (IOException e2) {
			System.out.println("TurtleCage.start()");
			e2.printStackTrace();
		}
		// Check if the message type is a authentification message
		// AUTH = 0
		// else create a new ClientThread object
		try (ServerSocket serverSocket = new ServerSocket(AUTH_PORT);) {
			boolean running = true;
			while (running) {
				Socket socket = serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				TransferPacket tp = (TransferPacket) in.readObject();
				if (tp.getType() == TransferPacket.AUTH) {
					String userToAuthenticate = tp.getSender().getUserName();
					String userToAuthenticatePassword = tp.getMessage();
					String matchUser = usersMap.get(userToAuthenticate);
					boolean isAuth = false;
					if (matchUser == null || !matchUser.equals(userToAuthenticatePassword)) {
						out.writeObject(isAuth);
						System.out.println(getTimestamp() + "Denied access to user " + userToAuthenticate
								+ ". Invalid credentials provided.");
						// Break out of the loop, so no ClientThread will be
						// created
						continue;
					} else {
						System.out.println(getTimestamp() + "Successfully authenticated " + userToAuthenticate
								+ ". Access granted.");
						isAuth = true;
						out.writeObject(isAuth);
					}
				}

			}
		} catch (Exception e) {
			System.out.println(getTimestamp() + "AuthentificationServer.start() at answer with auth response.");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println(getTimestamp() + "Starting server on port " + AUTH_PORT);
		AuthentificationServer authServer = new AuthentificationServer();
		authServer.start();
	}

	// Load users from the users list into the hash map, so the users can be
	// authenticated
	private static void loadUsers(int users) throws IOException {
		String username = "";
		String password = "";
		String line = "";
		String[] splitter;
		File usersFile = new File("users.txt");
		try (BufferedReader fin = new BufferedReader(new FileReader(usersFile));) {
			while ((line = fin.readLine()) != null) {
				splitter = line.split(",");
				username = splitter[0];
				password = splitter[1];
				System.out.println(getTimestamp() + "User found: " + username + " ****");
				usersMap.put(username, password);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Count the lines of the user list file
	private static int getLines(BufferedReader fileIn) {
		int lines = 0;
		LineNumberReader lnr = new LineNumberReader(fileIn);
		try {
			while (lnr.readLine() != null) {
				lines++;
			}
		} catch (IOException e) {
			System.out.println("AuthentificationServer.getLines()");
			e.printStackTrace();
		} finally {
			try {
				lnr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lines;
	}

	// Remark: Methods that are not static can be called in any other methods of
	// this class. However non-static methods
	// can not be called in the main method, because non static methods must be
	// called in an instance of the object.
	// Static methods are auxiliary methods which are used to simplify the
	// logic.
	private static String getTimestamp() {
		return "[" + sdf.format(new Date()) + "] ";
	}
}
