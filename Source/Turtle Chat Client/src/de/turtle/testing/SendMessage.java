package de.turtle.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendMessage {
	public static void main(String[] args) throws IOException {
		PrintWriter out = null;
		BufferedReader stdIn = null;
		try {
			Socket socket = new Socket("localhost", 55021);
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			stdIn = new BufferedReader(new InputStreamReader(System.in));

			String s = "";
			while ((s = stdIn.readLine()) != null) {
				out.println(s);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
			if (stdIn != null)
				stdIn.close();
		}

	}
}
