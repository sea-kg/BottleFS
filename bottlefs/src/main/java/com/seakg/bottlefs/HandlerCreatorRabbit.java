package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HandlerCreatorRabbit implements IHandlerCreator {

   static class HandlerRabbit implements HttpHandler {
       public void handle(HttpExchange t) throws IOException {
           String response = "HandlerRabbit";
           t.sendResponseHeaders(200, response.length());
           OutputStream os = t.getResponseBody();
           os.write(response.getBytes());
           os.close();
       }
   }

   public String name()
   {
     return "rabbit";
   }

   public String info()
   {
     return "todo";
   }

   public HttpHandler createHttpHandler(Properties pProps)
   {
     return new HandlerRabbit();
   }
}