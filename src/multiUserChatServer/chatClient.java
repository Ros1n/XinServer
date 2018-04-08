package multiUserChatServer;

import java.io.*;
import java.net.*;

class ClientThread extends Thread{
	PrintWriter out;
	private Socket s;
	String serverOutput;
	public ClientThread(PrintWriter out, Socket s) {
		this.out = out;
		this.s = s;
	}
	
	public void run() {
		try(
	            BufferedReader in = new BufferedReader( new InputStreamReader(s.getInputStream()));
				) {
			while ( (serverOutput = in.readLine())!= null ) {
				System.out.println("response: " + serverOutput);			
			}
		} catch(IOException ie) {
			System.out.print(ie.getMessage());
			ie.printStackTrace();
		}finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Client stop writing.");
		}

	}
}

public class chatClient {
	static private Socket s;
	static private String host = "";
	static private int port;
	private static String clientName;
	private void startClient(int port, String clientName) throws UnknownHostException, IOException {
		s = new Socket(host, port);
		try(
	            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	            BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in))				
				) {
			System.out.println("Connected to the " + s);
			String userInput;	
			new ClientThread(out, s).start();

			while (( userInput = stdIn.readLine() ) != null) {
				out.println("From client " + clientName + " : " + userInput);
			}
		}catch(UnknownHostException ie) {
			System.err.println("Don't know about host " + host);
			System.exit(1);
		}catch(IOException ie ) {
			System.err.println("Couldn't get I/O for the connection to " + host);
			System.exit(1);
		}		
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: Java client <host name> <port number>");
			System.exit(1);
		}
		port = Integer.parseInt(args[0]);
		clientName = args[1];
		new chatClient().startClient(port, clientName);
	}

}
