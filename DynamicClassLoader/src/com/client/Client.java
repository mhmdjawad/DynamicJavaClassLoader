package com.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	private Socket socket;
	private BufferedReader inputStream;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	public Client(Socket s) throws IOException {
		socket = s;
		inputStream = new BufferedReader(new InputStreamReader(System.in));
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		printWriter = new PrintWriter(socket.getOutputStream(),true);
	}
	public void HandleReuests() throws IOException {
		String request = "";
		System.out.println("Enter your option \n a- send file to server\n b- use uploaded file methods");
		while(!request.equals("exit")) {
			System.out.print("ENTER OPTION ---->>>>  ");
			request = inputStream.readLine();
			System.out.print("Request in progress -> " + request + "\n");
			switch(request) {
				case "a" : 
					sendFileOverSocket();
					break;
				case "b":
					callMethodOverSocket();
					break;
				case "exit" : System.out.println("Bye"); break;	
				default : System.out.println("Request not available\n"); break;
			}
			
		}
	}
	private void callMethodOverSocket() throws IOException {
		printWriter.println("callMethodOverSocket");
		System.out.println("enter your input, use the folloing format MethodName;parm1;parm2;...");
		String request = inputStream.readLine();
		printWriter.println(request);
		System.out.println("Request : " + request);
		System.out.println("From SERVER : " + bufferedReader.readLine());
	}
	private void sendFileOverSocket() throws IOException {
		printWriter.println("a");//send to server 1
		String response = bufferedReader.readLine();
		if(! response.equals("OK For Sending File")) {
			System.out.println("Server Sent : " + response );
			return;
		}
		System.out.println("Enter the directory path of file to send, example D:/UPDIR/Calculator.java");
		String FILE_PATH = inputStream.readLine();
		byte [] bytes = simulateFileByteArr(FILE_PATH);
		//byte [] bytes = simulateFileByteArr("D:/UPDIR/Calculator.java");
		//System.out.println("length of bytes " + bytes.length);
		printWriter.println(bytes.length+"");
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		out.write(bytes);
		out.flush();
		response = bufferedReader.readLine();
		if(response.equals("Sent File  Recieved")) {
			System.out.println("File Sent and saved, Please enter class name as in file, example Calculator");
			printWriter.println(inputStream.readLine());
			//printWriter.println("Calculator");
		}
		System.out.println(bufferedReader.readLine());
	}
	public static byte[] simulateFileByteArr(String FILE_PATH) {
		File file = new File(FILE_PATH);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte fileByte[] = new byte[(int)file.length()];
			fis.read(fileByte);
			fis.close();
			return fileByte;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	public static void main(String [] args) {
		try {
			Client c1 = new Client( new Socket("localhost",8080));
			c1.HandleReuests();
		}
		catch(Exception e) {
		}
	}
}
