/*--------------------------------------------------------
1. Name / Date:  
   Ralph / 2018-02-03


2. Java version used, if not the official version for the class:
   build 1.8.0_111-b14


3. Command-line compilation instructions:
   > javac *.java 


4. Instructions to run this program:
   Type in: > java MyWebServer
   The server runs on default port 2540.


5. List of files needed for running the program.
   MyWebServer.java


----------------------------------------------------------*/

import java.io.*;	//Import the Java I/O libraries
import java.net.*;	//Import the Java networking libraries
import java.util.Date;

/**
 * This is the Server class.
 */
public class MyWebServer {
	
	//Define booleans for switching server on or off
	public static boolean controlOn = true;
	
	public static void main(String a[]) throws IOException {
		
		//Maximum request numbers in queue from Client browser to Server
		int queueLen = 6; 
		//Default port
	    int port = 2540;
	    
	    //Socket for connection with Client
	    Socket s;
		//Listen to port for client's connection
	    ServerSocket servsock = new ServerSocket(port, queueLen);
	    //Prompt msg on server console once starting server
	    System.out.println("Ralph Zhang's WebServer is running at port 2540.\n");

	    //Create new workers to work on the connection with Client
	    while (controlOn) {
	    	s = servsock.accept();
	    	new Worker(s).start(); 
	    }
	  }
}

/**
 *  Worker class is defined to handle socket with client browser on server side.
 */
class Worker extends Thread {
	
	Socket sock;					//browser-webserver socket
	Worker (Socket s) {sock = s;} 	//Store socket information to instance

	/**
	 * Method of Runnable interface,
	 * handle I/O streams via socket.
	 * Basic routine:
	 * receive request (input stream) from client browser,
	 * analyze the request content,
	 * compose the header and content for response,
	 * send response (output stream) back to client browser.
	 */
	public void run(){
		
		BufferedReader fromClient = null;	//An input stream sent from browser
		OutputStream out = null;			//An output stream sent to browser
		PrintStream toClient = null;		//An output print stream
		
		//Try/catch block to handle I/O exceptions in socket I/O manipulation 
	    try {

	    	fromClient = new BufferedReader
	    			(new InputStreamReader(sock.getInputStream()));
	    	out = new BufferedOutputStream(sock.getOutputStream());
	    	toClient = new PrintStream(out);
	    	
	    	//Read in request's header
	    	String info = fromClient.readLine();
	    	while (true) {
	    		//No info sent to server
		    	if (info == null) break;
		    	else {
		    		
		    		//Print request header in console
		    		System.out.println("Receiving a request from browser.");
		    		System.out.println(info);
		    		while (true) {
	                    String rest = fromClient.readLine();
	                    //Read till end of request
	                    if (rest.trim().length() == 0 || rest == null) {
	                    	System.out.println();
	                        break;
	                    }
	                    System.out.println(rest);
		    		}
		    		
		    		//Check if request in right format
		    		if (!info.startsWith("GET") || !info.endsWith("HTTP/1.1")) {
		    			//Not GET method, return error page 400
		    			String msg = "Server receives a request in wrong format";
		    			errorPage("400", msg, toClient);
		    			break;
		    		} else {
		    			
		    			//Get substring of file path (e.g., /dog.txt in "GET /dog.txt HTTP/1.1")
		    			String path = info.substring(4,info.length()-9).trim();
		    			
		    			//Check if request sent from unwanted or malicious visitors
		                if (path.indexOf("../")!=-1 || path.endsWith("~")) {
		                	//Return error page 403
			    			String msg = "Server forbids access to the requested URL.";
			    			errorPage("403", msg, toClient);
		                	break;
		                } else {
		                	//Compose file path and get the target file
		                    File file = new File(".");
		                    String root = "";
		                    try{
		                    	root = file.getCanonicalPath();
		                    }catch (Throwable e){
		                    	System.out.println(e);
		                    }
		                    String absPath = (root + path).replace("/", "\\");	//Get file path
		                    file = new File(absPath);							//Get file
		                    
		                    //Check if the file is a directory
		                	if (absPath.endsWith("\\") || file.isDirectory()){
		                		//Return a dynamically constructed html page showing the directory
		                		String webContent = idxPageWriter(absPath, path);
	                            String rsp = ("HTTP/1.1 200 OK\r\n" +
	                            		"Content-Length: " + webContent.length() + "\r\n" +
	                            		"Connection: close\r\n" + 
	                            		"Content-Type: text/html\r\n" +
	                            		"Date: " + new Date() + "\r\n" +
	                                    "Server: MyWebServer 1.0\r\n\r\n");
	                            System.out.println("Response sent to browser:");
	                            System.out.println(rsp);
	                            //Send header
	                            toClient.print(rsp);
	                            //Send web page
	                            toClient.print(webContent);
	                            
	                        //Check if browser looks for CGI script   
		                	} else if (path.indexOf("addnums.fake-cgi?")!=-1) {
		                		//Return a dynamically constructed page with the result
		                		String webContent = addNums(path);
	                            String rsp = ("HTTP/1.1 200 OK\r\n" +
	                            		"Content-Length: " + webContent.length() + "\r\n" +
	                            		"Connection: close\r\n" + 
	                            		"Content-Type: text/html\r\n" +
	                            		"Date: " + new Date() + "\r\n" +
	                                    "Server: MyWebServer 1.0\r\n\r\n");
	                            System.out.println("Response sent to browser:");
	                            System.out.println(rsp);
	                            //Send header
	                            toClient.print(rsp);
	                            //Send web page
	                            toClient.print(webContent);
		                		
		                	} else {
		                		try {
			                		//Declare a input stream as container for sending file
		                            InputStream sendFile = new FileInputStream(file);
		                            //Compose the response header
		                            String rsp = ("HTTP/1.1 200 OK\r\n" +
		                            		"Content-Length: " + file.length() + "\r\n" +
		                            		"Connection: close\r\n" +
		                            		"Content-Type: " + getContentType(path) + "\r\n" +
		                            		"Date: " + new Date() + "\r\n" +
		                                    "Server: MyWebServer 1.0\r\n\r\n");
		                            System.out.println("Sending a response to browser:");
		                            System.out.println(rsp);
		                            //Send header
		                            toClient.print(rsp);
		                            //Send raw file
		                            fileWriter(sendFile, out);
		                            
		                		} catch (FileNotFoundException e) {
		                			//Return error page 404
					    			String msg = "The requested URI was not found on server.";
					    			errorPage("404", msg, toClient);
					    			System.out.println(msg);
				                	break;
		                		}
		                	}
		                }
		    		}
		    	}
		    	break;
	    	}
	    	out.flush();
	    } catch (IOException x) {
	    	System.out.println("Connetion has been reset. Start listening again.");
	    }
	}
	
	/**
	 * Take in error information and return an error page.
	 * @param errorCode status code
	 * @param msg information about the error
	 * @param out output stream for sending response to browser
	 */
	private static void errorPage(String errorCode, String msg, PrintStream out) {
		String status = "";
		//Parallel error code with error message
		switch (errorCode) {
			case "400": status = "Bad Request"; break;
			case "401": status = "Unauthorized"; break;
			case "403": status = "Forbidden"; break;
			default: status = "Not Found"; break; 		//Set 404 as default
		}
		//Construct html content
		StringBuffer content = new StringBuffer();
		content.append("<!DOCTYPE html>\r\n" + "<html>\r\n<head>\r\n");
		content.append("<title>" + errorCode + " " + status + "</title>\r\n");
		content.append("</head>\r\n<body>\r\n");
		content.append("<h1>" + errorCode + " " + status + "</h1>\r\n" + msg +"\r\n");
		content.append("<p> Take me back to <a href='/'>root directory</a>.</p>\r\n");
		content.append("</body>\r\n</html>\r\n");
		//Construct response header
		StringBuffer hder = new StringBuffer();
		hder.append("HTTP/1.1 " + errorCode + " " + status + "\r\n");
		hder.append("Content-Length: " + content.length() + "\r\n");
		hder.append("Connection: close\r\n");
		hder.append("Content-Type: text/html\r\n");
		hder.append("Date: " + new Date() + "\r\n");
		hder.append("Server: MyWebServer 1.0\r\n\r\n");
		
		//Write response header in output stream
		out.print(hder.toString());
		
		//Print response header in console
		System.out.println("Sending an ABNORMAL response to browser:");
		System.out.println(hder);
		
		//Write response content in output stream
		out.print(content.toString());
	}
	
	
	/**
	 * Return a dynamically constructed html showing directory content
	 * @param absPath absolute path of directory
	 * @param path	relative path of directory
	 * @return response string
	 */
	private static String idxPageWriter(String absPath, String path) {

        File f = new File(absPath);
        //Collect all files and directories under target directory
        File[] files = f.listFiles();
        
        //Construct directory html
        StringBuffer webStr = new StringBuffer();
        webStr.append("<html>\r\n<head>\r\n");
        webStr.append("<title>Server Directory</title>\r\n");
        webStr.append("</head>\r\n<body>\r\n"); 
        webStr.append("<h1>Index of " + path + "</h1><br>\r\n"); 
        for ( int idx = 0 ; idx < files.length ; idx ++ ) {
    		String fName = files[idx].getName();
    		//Print different file category
        	if (files[idx].isDirectory()) {
        		webStr.append("Directory:");
        	}
          	else if (files[idx].isFile()) {
          		webStr.append("File:");
          	}
            webStr.append("<a href='" + (path.equals("/")?path:(path+"/")) +
                          fName + "'>" + fName + "</a> <br>\r\n");
        }
        webStr.append("</body>\r\n</html>"); 
        String content = webStr.toString();
        return content;
	}
	
	/**
	 * Decompose URL and do the adding of two numbers
	 * @param url URL containing person name and numbers
	 * @return response string
	 */
	private static String addNums(String url){
		//Decompose the URL string: cgi/addnums.fake-cgi?person=Ralph&num1=0&num2=1
		String name = url.substring(url.indexOf("person=") + 7 , url.indexOf("&num1"));
		int n1 = Integer.parseInt(url.substring(url.indexOf("num1=") + 5 , url.indexOf("&num2")));
		int n2 = Integer.parseInt(url.substring(url.indexOf("num2=") + 5));
		
		//Construct html content
		StringBuffer webStr = new StringBuffer();
        webStr.append("<html>\r\n<head>\r\n");
        webStr.append("<title>Addnum result</title>\r\n");
        webStr.append("</head>\r\n<body>\r\n<h1>"); 
        int sum = n1 + n2;
        webStr.append("Dear " + name + ", the sum of " + n1 + " and " + n2 + " is " + sum + "."); 
        webStr.append("</h1><br>\r\n</body>\r\n</html>"); 
        String content = webStr.toString();
        return content;
	}
	
	/**
	 * Utility method for file transfer
	 * @param readInFile file on server to be sent
	 * @param out output stream for sending response
	 */
    private static void fileWriter(InputStream readInFile, OutputStream out) {
        try {
        	//Declare a byte array to store the file buffer
            byte[] fileBuf = new byte[1000];
            //Read file and write into output stream
            while (readInFile.available() > 0) 
                out.write(fileBuf, 0, readInFile.read(fileBuf));
        } catch (IOException e) {
        	System.out.println(e);
        }
    }
	
    /**
     * Utility method to identify category of request file
     * @param url 
     * @return file type string
     */
	private static String getContentType(String url){
		//html type
        if (url.endsWith(".html") || url.endsWith(".htm")) {
            return "text/html";
        //image type
        } else if (url.endsWith(".jpg") || url.endsWith(".jpeg")) {
            return "image/jpeg";
        //All other types as plain text
        } else return "text/plain";
    }
}