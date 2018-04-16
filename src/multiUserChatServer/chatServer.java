package multiUserChatServer;

import java.util.*;
import java.io.*;
import java.net.*;

class ServerThread extends Thread{
	Socket client;
	chatServer server;
	Hashtable<Socket, PrintWriter> hashtable;
	public ServerThread(chatServer server, Hashtable<Socket, PrintWriter> hashtable, Socket s) {
		this.client = s;
		this.server = server;
		this.hashtable = hashtable;
	}
	
	public void run() {
		try (
	            BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream()));
	            BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in))				
				){
			
			String message;
			while((message = in.readLine()) != null){
				System.out.println("Receiving " + message);
			}
		} catch( EOFException ie ) {
			
		} catch( IOException ie) {
			ie.printStackTrace();
		}finally {
			removeConnection(client);
			System.out.println("Finish connection");
		}
	}
	
	public void removeConnection(Socket st) {
		hashtable.remove(st);
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

public class chatServer {
	static int port = 80;
	private ServerSocket ss;
	private Socket s;
	private PrintWriter out;
	private Hashtable<Socket, PrintWriter> acceptSocket = new Hashtable<Socket, PrintWriter>();
	private void startServer(int port) throws IOException {
		ss = new ServerSocket(port);
		System.out.println("Listening on port: "+ ss);			

		new Thread() {
			public void run() {
				String userInput;
				try(
				        BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));
						) {
					while ( (userInput = stdIn.readLine()) != null ) {
						for (Socket st: acceptSocket.keySet()) {
							out = acceptSocket.get(st);
							out.println("From server: " + userInput);
						}
					}
				} catch(IOException ie) {
					System.out.print(ie.getMessage());
					ie.printStackTrace();
				}finally {
					System.out.println("Server stop writing.");
				}

			}
		}.start();
		
		while(true) {
			s = ss.accept();
			System.out.println("Connection from " + s);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			acceptSocket.put(s, out);
			new ServerThread(this, acceptSocket, s).start();
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: Java chatServer <port number>");
			System.exit(1);
		}
		port = Integer.parseInt(args[0]);
		new chatServer().startServer(port);;
	}

}
