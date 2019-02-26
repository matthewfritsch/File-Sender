package FileConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

public class Connector extends Thread{

	private String localIP;
	private String[] myHosts;
	private ServerSocket mySocket;
	private ArrayList<Socket> socketList;
	private Thread t;
	
	public Connector() {
		t = this.currentThread();
		findConnected(findSubnet());
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
		subnet = localIP.substring(1, localIP.lastIndexOf("."));
		return subnet;
	}

	public void findConnected(String sub) {
		System.out.println("Finding connections... please allow 20 seconds to process.");
		ArrayList<String> hosts = new ArrayList<String>();
		int timeout = 20;
		for (int x = 0; x < 255; x++) {
			String host = sub + "." + x;
			try {
				if (InetAddress.getByName(host).isReachable(timeout)) {
					new FindSocketThread(host);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		myHosts = hosts.toArray(new String[hosts.size()]);
		System.out.println("Finding all is complete.");
	}
	
	public void printConnections() {
		for(String str:myHosts) {
			System.out.println(str);
		}
	}
	
	private class FindSocketThread extends Thread{
		@Override
		public void run() {
			System.out.println(this.getName() + " is starting...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(this.getName() + " complete");
		}
		public FindSocketThread(String hostname) {
			this.setName("Connecting with " + hostname);
			this.start();
		}
	}

}
