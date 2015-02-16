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

public class HandlerCreatorStopReindexing implements IHandlerCreator {

   static class HandlerStopReindexing implements HttpHandler {
       public void handle(HttpExchange t) throws IOException {
           String response = "HandlerStopReindexing";
           t.sendResponseHeaders(200, response.length());
           OutputStream os = t.getResponseBody();
           os.write(response.getBytes());
           os.close();
       }
   }

   public String name()
   {
     return "stop-reindexing";
   }

   public String info()
   {
     return "todo";
   }

   public HttpHandler createHttpHandler(Properties pProps)
   {
     return new HandlerStopReindexing();
   }
}