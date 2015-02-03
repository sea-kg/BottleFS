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

public class HandlerCreatorStartReindexing implements IHandlerCreator {

   static class HandlerStartReindexing implements HttpHandler {
       public void handle(HttpExchange t) throws IOException {
           String response = "HandlerStartReindexing";
           t.sendResponseHeaders(200, response.length());
           OutputStream os = t.getResponseBody();
           os.write(response.getBytes());
           os.close();
       }
   }

   public String name()
   {
     return "start-reindexing";
   }

   public String info()
   {
     return "TODO";
   }

   public HttpHandler createHttpHandler()
   {
     return new HandlerStartReindexing();
   }
}