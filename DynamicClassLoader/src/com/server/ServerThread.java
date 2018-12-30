package com.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ServerThread extends Thread {
	private Socket clientSocket;
	private String timeStart;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private Object obj;
	private Class<?> compiledClass;
	public static void main(String [] args) {
		try {
			System.out.println("Server Started");
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
	public ServerThread(Socket client) throws IOException {
		timeStart = (new java.util.Date()).toString();
		clientSocket = client;
		System.out.println("New Client Connected " + timeStart );
		bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		printWriter = new PrintWriter(clientSocket.getOutputStream(),true);
	}
	public void run() {
		System.out.println("server thread is running");
		String response = "";
		try
		{
			while(!response.equals("exit")) {
				try {
					response = bufferedReader.readLine();
					//System.out.println("client sent "+ response);
					switch(response) {
						case "a": handleSentFile();break;
						case "callMethodOverSocket":
							runMethodOverSocket();
							break;
						default :
							System.out.println("Method not found");
							printWriter.println("SERVER->CLIENT : option ( " + response +" ) not available" );
					}
				} catch (IOException e) {
					System.out.println("Session Closed");
					//e.printStackTrace();
					//clientSocket.close();
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void handleSentFile() throws IOException {
		printWriter.println("OK For Sending File");
		DataInputStream in = new DataInputStream( new BufferedInputStream(clientSocket.getInputStream()) );
		int byteCount = Integer.parseInt(bufferedReader.readLine());
		byte [] inputBytes = new byte[byteCount];
		in.read(inputBytes);
		printWriter.println("Sent File  Recieved");
		String FileName = bufferedReader.readLine();
		File JavaFile = new File("ClientJavaFile/"+FileName+".java");
		if (JavaFile.getParentFile().exists() || JavaFile.getParentFile().mkdirs()) {
			try (FileOutputStream stream = new FileOutputStream("ClientJavaFile/"+FileName+".java")) {
			    stream.write(inputBytes);
			}
		}
		try {
			obj = compileFile(JavaFile , FileName);
			if(obj != null) {
				printWriter.println("file compiled");
			}
			else {
				printWriter.println("file not compiled");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	private void runMethodOverSocket() throws IOException {
		try {
			String request = bufferedReader.readLine();
			StringTokenizer st = new StringTokenizer(request,";");
			String methodName = st.nextToken();
			Object InvokeResult = null;
			Method [] methods =	compiledClass.getDeclaredMethods();
			for(Method m : methods) {
				if(m.getName().equals(methodName)) {
					Map<Integer,Class<?>> mapParameterClass = new HashMap<Integer,Class<?>>();
					int i = 0;
					for(Class<?> cls : m.getParameterTypes()) {
						mapParameterClass.put(i++, cls);
					}
					List<Object> objects = new ArrayList<Object>();
					i = 0;
					while(st.hasMoreTokens()) {
						Class<?> cls = mapParameterClass.get(i++);
						Object o = CastObject(st.nextToken(),cls);
						objects.add(o);
					}
					InvokeResult = m.invoke(obj,objects.toArray());
					break;
				}
			}
			printWriter.println("Result from invoking " + InvokeResult);
		}
		catch( Exception e) {
			e.printStackTrace();
			printWriter.println("Error occured " + e.getMessage());
		}
	}
	public Object compileFile(File JavaFile , String className) throws Exception {
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();	
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		List<String> optionList = new ArrayList<String>();
        optionList.add("-classpath");
        optionList.add(System.getProperty("java.class.path") + ";dist/InlineCompiler.jar");
        Iterable<? extends JavaFileObject> compilationUnit
        = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(JavaFile));
        JavaCompiler.CompilationTask task = compiler.getTask(
            null, 
            fileManager, 
            diagnostics, 
            optionList, 
            null, 
            compilationUnit);
        if (task.call()) {
             URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
             Class<?> loadedClass = classLoader.loadClass("ClientJavaFile." + className);
             Object obj = loadedClass.newInstance();
             classLoader.close();
             fileManager.close();
             compiledClass = loadedClass;
             return obj;
        }
        else {
        	for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.format("Error on line %d in %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getSource().toUri());
            }
        }
        fileManager.close();
        return null;
	}
	private Object CastObject(Object o,Class<?> cls) {
		Object ob = o;
		String className = cls.getName();
		//System.out.println(className);
		switch(className) {
		case "double" : return Double.parseDouble(o.toString());
		case "int" : return Integer.parseInt(o.toString());
		default : return ob;
		}
	}
}
