package de.turtle.imp.client;

import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import de.turtle.imp.models.Role;
import de.turtle.imp.models.TransferPacket;
import de.turtle.imp.models.User;

/**
 * Static variables are associated with the class, not objects of a class
 */
public class ChatClient {
	// constants
	private static final int SERV_PORT = 55021;
	private static final int AUTH_PORT = 55030;
	private static final String CON_SUCCESS = "CON_EST";
	private static final String CON_FAILURE = "CON_FAIL";
	private static ObjectOutputStream authOut = null;
	private static ObjectInputStream authIn = null;
	private static Console console = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private static User sendUser;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Turtle Usage: java -jar turtle.jar <host> <port>");
			System.exit(1);
		}
		console = System.console();
		if (console == null) {
			System.out.println(getTimestamp() + "No console found.");
			System.exit(1);
		}
		String username = "";
		String password = "";
		System.out.print(getTimestamp() + "Username: ");
		username = console.readLine();
		System.out.print(getTimestamp() + "Password: ");
		char[] pw = console.readPassword();
		for (char c : pw)
			password += c;
		try (Socket authSocket = new Socket(args[0], AUTH_PORT);) {
			// Create a new user object
			sendUser = new User(0, username, new Role());
			/**
			 * Send an authentification request to the authentification server
			 */
			// System.out.println(getTimestamp() + "Sending authentication
			// request to "
			// + authSocket.getInetAddress().getCanonicalHostName());
			TransferPacket helloPacket = new TransferPacket(TransferPacket.AUTH, password, sendUser);
			authOut = new ObjectOutputStream(authSocket.getOutputStream());
			authIn = new ObjectInputStream(authSocket.getInputStream());
			authOut.writeObject(helloPacket);
			/**
			 * Await the authentification response from the server
			 */
			try {
				boolean authResponse = false;
				authResponse = (boolean) authIn.readObject();
				if (authResponse == false) {
					System.out.println(getTimestamp() + "Invalid login credentials.");
					System.exit(1);
				} else {
					System.out.println(getTimestamp() + "Authentification successful.");
					char[] loading = { '\\', '|', '/', '-', '\\', '|', '/', '-' };
					for (int i = 0; i <= 1; i++) {
						for (int j = 0; j < loading.length; j++) {
							System.out.print(getTimestamp() + "Establishing connection... " + loading[j] + "\r");
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					System.out.println();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (NumberFormatException e) {
			System.out.println(getTimestamp() + "ChatClient.main() [NumberFormatException]");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println(getTimestamp() + "ChatClient.main() [UnknownHostException]");
			e.printStackTrace();
		} catch (IOException e) {
			// System.out.println(getTimestamp() + "ChatClient.main()
			// [IOException] 1");
			// e.printStackTrace();
			System.out.println("The authentication server is not reachable. Try again later.");
			System.exit(1);
		}

		/**
		 * Authentication successful
		 */
		try (Socket socket = new Socket(args[0], Integer.parseInt(args[1]));) {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			/**
			 * Send our username
			 */
			out.writeObject(username);
			/**
			 * IOException on the TurtleCage only occurs because no data has
			 * been written yet
			 */

			/**
			 * Test connection with testPacket
			 */
			TransferPacket testCon = new TransferPacket(TransferPacket.TEST, ".", sendUser);
			out.writeObject(testCon);
			TransferPacket testConResp = (TransferPacket) in.readObject();
			if (!testConResp.getMessage().equals(CON_SUCCESS) || testConResp.getMessage().equals(CON_FAILURE)) {
				System.out.println(getTimestamp()
						+ "Connection was not tested successfully. Connection could not be established. Terminating...");
				System.exit(1);
			}
			System.out.println(getTimestamp() + "Connection established.");

			/**
			 * Connection was tested successfully. Send first chat message.
			 */
			Scanner sc = new Scanner(System.in);
			while (true) {
				System.out.print(">");
				String msg = sc.nextLine();
				String msg_subset = "";
				if (msg.length() < 2) {
					msg_subset = msg;
				} else {
					msg_subset = msg.substring(0, 2);
				}
				if (msg_subset.equals("--")) {
					if (msg.equals("--quit")) {
						out.writeObject(
								new TransferPacket(TransferPacket.DISCONNECT, "#", new User(0, username, new Role())));
						break;
					} else if (msg.equals("--showall")) {
						out.writeObject(
								new TransferPacket(TransferPacket.SHOWALL, "#", new User(0, username, new Role())));
						TransferPacket clientNames = (TransferPacket) in.readObject();
						System.out.println("Connected users: " + clientNames.getMessage());
					} else if (msg.equals("--help")) {
						// HELP
						displayHelp();
					} else {
						System.out.println("Unknown command. Type --help for all help.");
					}
				} else {
					out.writeObject(new TransferPacket(TransferPacket.MESSAGE, msg, new User(0, username, new Role())));
					TransferPacket tp = (TransferPacket) in.readObject();
					System.out.println(getTimestamp() + "<" + tp.getSender().getUserName() + "> " + tp.getMessage());
				}
			}
		} catch (IOException e) {
			// System.out.println(getTimestamp() + "ChatClient.main()
			// [IOException] 2");
			// e.printStackTrace();
			System.out.println("The server is not reachable. Please try again later.");
		} catch (ClassNotFoundException e) {
			System.out.println(getTimestamp() + "ChatClient.main() [ClassNotFoundException]");
			e.printStackTrace();
		}
	}

	private static void displayHelp() {
		System.out.println("--quit: Quit");
		System.out.println("--showall: Display all connected users");
	}

	public static boolean login(User req) {
		return true;
	}

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
}
