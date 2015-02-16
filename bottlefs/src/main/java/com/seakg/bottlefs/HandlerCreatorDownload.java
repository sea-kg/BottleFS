package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class HandlerCreatorDownload implements IHandlerCreator {

   static class HandlerDownload implements HttpHandler {
       public void handle(HttpExchange t) throws IOException {
           String response = "<pre>HandlerDownload\r\n";
           response = response + t.getHttpContext().getPath() + "\r\n";

           Map<String,Object> map = t.getHttpContext().getAttributes();
           for (Map.Entry<String, Object> entry : map.entrySet())
           {
              // response = response + entry.getKey() + ": " + entry.getValue().toString() + "\r\n";
           }
           response += "<form action='?' method='GET'><input type='text' name='file-link'/><input type='submit'/></form>";
           t.sendResponseHeaders(200, response.length());
           OutputStream os = t.getResponseBody();
           os.write(response.getBytes());
           os.close();
       }
   }

   public String name()
   {
     return "download";
   }

   public String info()
   {
     return "todo";
   }

   public HttpHandler createHttpHandler(Properties pProps)
   {
     return new HandlerDownload();
   }
}
