package com.seakg.bottlefs;

import org.apache.commons.io.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HandlerCreatorUpload implements IHandlerCreator {

	static class HandlerUpload implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			String response = "<pre>HandlerUpload\r\n";
			response = response + t.getHttpContext().getPath() + "\r\n";
			Map<String,Object> map = t.getHttpContext().getAttributes();

			/*
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				response = response + entry.getKey() + ": " + entry.getValue().toString() + "\r\n";				
			}*/
			
			if (map.containsKey("file")) {
				response += "File: " + map.get("file").toString() + "\r\n";
				try {
					URL uriFile = new URL(map.get("file").toString());
					File f = new File("tmp");
					FileUtils.copyURLToFile(uriFile, f);
				} catch (IOException e) {
					response += "Error: " + e.getMessage() + "\r\n";
					System.out.print("Error: " + e.getMessage() + "\r\n");
					// return;
				}
				
			}

			
			response += "<form action='?' method='GET'><input type='text' name='file'/><input type='submit'/></form>";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
			
			
		}
	}

   public String name()
   {
     return "upload";
   }

   public String info()
   {
     return "this handler upload files (bytearray or by http-lnk)";
   }

   public HttpHandler createHttpHandler()
   {
     return new HandlerUpload();
   }
}
