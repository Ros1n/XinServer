package httpServer;

import java.io.*;
import java.util.*;
import java.net.*;

class ServerThread extends Thread{
	private Socket client;
	private int i;
	public ServerThread(Socket client, int i) {
		this.client = client;
		this.i = i;
	}
	
	private String getFileName(String s) {
		String f = s.substring(s.indexOf(' ') + 1);
		f = f.substring(0, f.indexOf(' '));
		try {
			if (f.charAt(0) == '/') f = f.substring(1); 
		} catch( StringIndexOutOfBoundsException se){
			se.printStackTrace();
		}
		if (f.equals("")) f = "index.html";
		return f;
	}
	
	private void fileToBrowser(OutputStream out, File f) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			int len = (int)f.length();
			byte buf[] = new byte[len];
			in.readFully(buf);
			out.write(buf, 0, len);
			out.flush();
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void successResponse(String s, File f, PrintWriter out) {
		System.out.println(s + " requested.");
		out.println("HTTP/1.0 200 OK");
		out.print("MIME_version:1.0");
		out.println("Content_Type:text/html");
		int len = (int) f.length();
		out.println("Content_Length:" + len);
		out.println("");
		out.flush();
	}
	
	private void failResponse(PrintWriter out) {
		String notFound = "<html><head><title>Not Found</title></head><body><h1>Error 404-file not found</h1></body></html>";
		out.print("HTTP/1.0 404 Not Found");
		out.println("Content_Type:text/html");
		out.println("Content_Length:" + notFound.length() + 2);
		out.println("");
		out.println(notFound);
		out.flush();
	}
	
	public void run() {
		try (
				OutputStream raw_out = client.getOutputStream();
	            BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream()));
	            PrintWriter out = new PrintWriter(raw_out, true);
	            BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in))				
				){
			System.out.println("Connection " + i + " :connected to " + client.getInetAddress() + " on port " + client.getPort() + "." );
			
			String url = in.readLine();
			if (url.length() > 0) {
				if (url.substring(0,3).equalsIgnoreCase("GET")) {
					String fileName = getFileName(url);
					File file = new File("webapp/" + fileName);
					if (file.exists()) {
						successResponse(fileName, file, out);
						fileToBrowser(raw_out, file);
						raw_out.flush();
					}else {
						failResponse(out);
					}
				}
			}
			
			new Thread() {
				public void run() {
					String message;
					try {
						while((message = in.readLine()) != null){
							System.out.println("Receiving " + message);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			

		} catch( EOFException ie ) {
			
		} catch( IOException ie) {
			ie.printStackTrace();
		}finally {
			//removeConnection(client);
			//System.out.println("Finish connection");
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
	static private int port = 8080;
	static private ServerSocket ss;
	static private Socket client;
	
	
	public static void main(String[] args) throws IOException {
		int i = 0;
		try {
			ss = new ServerSocket(port);
			System.out.println("Listening on port: "+ ss);
			
			for(;;) {
				client = ss.accept();
				System.out.println("Connection from " + client.getLocalPort());
				new ServerThread(client, i).start();
			}		
		}catch(IOException ie) {
			ie.printStackTrace();
		}			
	}
}
