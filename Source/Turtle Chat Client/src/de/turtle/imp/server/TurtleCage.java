package de.turtle.imp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import de.turtle.imp.client.ChatClient;
import de.turtle.imp.models.Role;
import de.turtle.imp.models.TransferPacket;
import de.turtle.imp.models.User;

public class TurtleCage {
	private static Console console;
	private static final int PORT = 55021;
	private static final String CON_SUCCESS = "CON_EST";
	private static final String CON_FAILURE = "CON_FAIL";
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private static ArrayList<ClientThread> clients;
	private int clientIds = 1;

	public TurtleCage() {
		this.clients = new ArrayList<ClientThread>();
	}

	private void start() throws ClassNotFoundException {
		System.out.println(getTimestamp() + "Server is up and running. Waiting for clients to connect.");
		boolean running = true;
		// Initialize the server socket in a try-with-resources statement, so it
		// will
		// be closed on an occuring exception or natural program exit
		// This prevents a port bind after the server was closed
		try (ServerSocket serverSocket = new ServerSocket(PORT);) {

			while (running) {
				Socket socket = serverSocket.accept();
				// System.out.println(getTimestamp() + "Connection received from
				// " + socket.getLocalAddress());
				ClientThread ct = new ClientThread(socket);
				clients.add(ct);
				ct.start();
			}
			// after infinite loop is broken out off, everything will be
			// properly
			// closed
			try {
				// close server socket
				serverSocket.close();
				for (ClientThread c : clients) {
					// close client socket input stream
					c.in.close();
					// close client socket output stream
					c.out.close();
				}
			} catch (Exception e) {
				System.out.println(getTimestamp() + "Error at closing server and clients.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// private synchronized void broadcastMessage(String username, String
	// message) {
	// String time = sdf.format(new Date());
	// for (ClientThread c : clients) {
	//
	// }
	// }

	public static void main(String[] args) {
		console = System.console();
		if (console == null) {
			System.out.println(getTimestamp() + "Could not instantiate a console.");
		}
		System.out.println(getTimestamp() + "Starting server.");
		TurtleCage server = new TurtleCage();
		try {
			server.start();
		} catch (Exception e) {
			System.out.println(getTimestamp() + "Failed to start server at port " + PORT);
			e.printStackTrace();
		}
		System.out.println(getTimestamp() + "Server stopped the service.");
	}

	// ClientThread class
	class ClientThread extends Thread {
		private Socket socket;
		private int id;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private String username;
		private User userAgent;

		public ClientThread(Socket socket) {
			this.socket = socket;
			this.id = clientIds++;
			try {
				this.in = new ObjectInputStream(socket.getInputStream());
				this.out = new ObjectOutputStream(socket.getOutputStream());

			} catch (Exception e) {
				System.out.println(TurtleCage.getTimestamp() + "Exception occured in ClientThread.");
				e.printStackTrace();
			}
			try {
				// 1. Message: username
				// Following Messages: Messages
				// The first thing the client sends is his username
				username = (String) in.readObject();
				System.out.println(TurtleCage.getTimestamp() + username + " has connected.");
			} catch (ClassNotFoundException e) {
				System.out.println(
						TurtleCage.getTimestamp() + "Exception at ClientThread.readObject() [ClassNotFoundException]");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println(TurtleCage.getTimestamp() + "Exception at ClientThread.readObject() [IOException]");
				e.printStackTrace();
			}
			userAgent = new User(id, username, new Role());
		}

		public void run() {
			/**
			 * Receive the testPacket
			 * 
			 * TYPE CODE 0 AUTH 1 MESSAGE 2 DISCONNECT 3 SHOWALL 4 TEST
			 */
			System.out.println("Current Thread ID is: " + Thread.currentThread().getId());
			try {
				TransferPacket testCon = (TransferPacket) in.readObject();
				if (testCon.getType() == TransferPacket.TEST) {
					testCon.setMessage(CON_SUCCESS);
					out.writeObject(testCon);
					// out.writeObject(CON_SUCCESS);
				}
			} catch (ClassNotFoundException e1) {
				System.out.println(getTimestamp() + "TurtleCage.ClientThread.run() [ClassNotFoundException]");
				e1.printStackTrace();
			} catch (IOException e1) {
				System.out.println(getTimestamp() + "TurtleCage.ClientThread.run() [IOException]");
				e1.printStackTrace();
			}
			boolean running = true;
			while (running) {
				TransferPacket tp = null;
				try {
					tp = (TransferPacket) in.readObject();
					String msg = tp.getMessage();
					if (tp.getType() == TransferPacket.MESSAGE) {
						System.out.println(getTimestamp() + tp.getSender().getUserName() + ": " + tp.getMessage());
						TurtleCage.broadcastMessage(tp);
					} else if (tp.getType() == TransferPacket.DISCONNECT) {
						/**
						 * Remove UserThread from HashMap
						 */
						System.out.println(username + " has disconnected.");
						disconnectUser();
						running = false;
					} else if (tp.getType() == TransferPacket.SHOWALL) {
						String clientNames = "";
						for (int i = 0; i < clients.size(); i++) {
							clientNames += clients.get(i).username + ", ";
						}
						out.writeObject(new TransferPacket(TransferPacket.SHOWALL, clientNames,
								new User(0, username, new Role())));
					}
				} catch (SocketException e) {
					System.out.println("Catched a Socket Exception at listening for incoming messages");
					System.out.println("Disconnecting user " + username);
					disconnectUser();
					// e.printStackTrace();
					return;
				} catch (EOFException e) {
					System.out.println("EOFException occured. Could not read from the client socket.");
					System.out.println("Disconnecting user " + username);
					disconnectUser();
					// e.printStackTrace();
					return;
				} catch (IOException e) {
					System.out.println(TurtleCage.getTimestamp() + "Exception at ClientThread::run()");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					System.out.println(TurtleCage.getTimestamp() + "Exception at ClientThread::run()");
					e.printStackTrace();
				}
			}
		}

		private void disconnectUser() {
			for (int i = 0; i < clients.size(); i++) {
				ClientThread c = clients.get(i);
				if (c.username.equals(this.username)) {
					clients.remove(i);
					return;
				}
			}
		}
	}
	// End ClientThread class

	/**
	 * Remark: Methods that are not static can be called in any other methods of
	 * this class. However non-static methods can not be called in the main
	 * method, because non static methods must be called in an instance of the
	 * object. Static methods are auxiliary methods which are used to simplify
	 * the logic.
	 */
	private static String getTimestamp() {
		return "[" + sdf.format(new Date()) + "] ";
	}

	public static synchronized void broadcastMessage(TransferPacket tp) {
		for (ClientThread c : clients) {
			try {
				c.out.writeObject(tp);
			} catch (IOException e) {
				System.out.println("Could not broadcast message \"" + tp.getMessage() + "\" from user "
						+ tp.getSender().getUserName());
				e.printStackTrace();
			}
		}
	}
}
