package FileConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

public class Connector{

	private final static int TIMEOUT = 20;

	private String[] myHosts;
	private String localIP;
	private ServerSocket mySocket;
	private ArrayList<Socket> socketList;
	private Thread receiving, sending;
	
	public Connector() {
		receiving = new ReceiveSocketThread();
		sending = new SendSocketThread(findSubnet());
	}

	private String findSubnet() {
		String subnet;
		try {
			for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				for (InetAddress in : Collections.list(ni.getInetAddresses())) {
					if (in.isSiteLocalAddress()) {
						localIP = in.toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		localIP = localIP.substring(1);
		subnet = localIP.substring(0, localIP.lastIndexOf("."));
		return subnet;
	}
	
	public void printConnections() {
		for(String str:myHosts) {
			System.out.println(str);
		}
	}

	private class SendSocketThread extends Thread{
		private String subnet = "";
		@Override
		public void run(){
			System.out.println("Finding connections... please allow 20 seconds to process.");
			ArrayList<String> hosts = new ArrayList<String>();
			for (int x = 0; x < 255; x++) {
				String host = subnet + "." + x;
				try {
					if (InetAddress.getByName(host).isReachable(TIMEOUT) && !host.equals(localIP)) {
						System.out.println("Found connection with "+host);
						new FindSocketThread(host);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			myHosts = hosts.toArray(new String[hosts.size()]);
			System.out.println("Finding all is complete.");
		}
		public SendSocketThread(String sub){
			subnet = sub;
			this.start();
		}
	}
	
	private class FindSocketThread extends Thread{
		private String host;
		@Override
		public void run() {
			//System.out.println(this.getName() + " is starting...");
			try {
				//Thread.sleep(2000);
				Socket s = new Socket();
				s.connect(new InetSocketAddress(host, 7654), TIMEOUT);
			} catch (IOException e) {
				System.err.println("Failed to create connection to "+host);
				//e.printStackTrace();
			}
			//System.out.println(this.getName() + " complete");
		}
		public FindSocketThread(String hostname) {
			this.setName("Connecting with " + hostname);
			host = hostname;
			this.start();
		}
	}

	private class ReceiveSocketThread extends Thread{
		@Override
		public void run(){
			System.out.println("Server is waiting for connection...");
			try{
				Socket receiver;
				mySocket = new ServerSocket(7654);
				receiver = mySocket.accept();
				if(!mySocket.isClosed()){
					//System.out.println("You were connected to!");
					if(sending.isAlive()){
						sending.interrupt();
					}
				}
			}
			catch(IOException e){
				System.err.println("Failed to create server");
				//e.printStackTrace();
			}
		}
		public ReceiveSocketThread(){
			this.setName("Attempting to receive connections");
			this.start();
		}
	}
	
	
}
