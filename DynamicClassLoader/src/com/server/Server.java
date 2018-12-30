package com.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		System.out.println("Server Started");
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(8080);
			while(true) {
				try {
					Socket client = serverSocket.accept();
					(new ServerThread(client)).start();
				}
				catch(Exception e) {
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
