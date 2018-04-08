package com.xin.server;

import java.io.*;
import java.util.*;
import java.net.*;

class ServerThread extends Thread{
	Socket client;
	HttpServer server;
	public ServerThread(HttpServer server, Socket s) {
		this.client = s;
		this.server = server;
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
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

public class HttpServer {
	static private int port;
	static private ServerSocket ss;
	static private Socket s;
	
	public void startServer(int port) throws IOException {
		ss = new ServerSocket(port);
		System.out.println("Listening on port: "+ ss);			

		
		while(true) {
			s = ss.accept();
			System.out.println("Connection from " + s);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			new ServerThread(this, s).start();
		}		
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: Java chatServer <port number>");
			System.exit(1);
		}
		port = Integer.parseInt(args[0]);
		new HttpServer().startServer(port);;
	}

}
