package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class Server {

    // todo: added array for IBottleFSHandlers
    // private Array<>

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8086), 0);
        // todo: generate all IBottleFSHandlers and add to createContext
        server.createContext("/upload", new com.seakg.bottlefs.HandlerUpload());
        server.createContext("/download", new com.seakg.bottlefs.HandlerDownload());
        server.createContext("/search", new com.seakg.bottlefs.HandlerSearch());
        server.createContext("/start-reindexing", new com.seakg.bottlefs.HandlerStartReindexing());
        server.createContext("/stop-reindexing", new com.seakg.bottlefs.HandlerStopReindexing());
        server.createContext("/rabbit", new com.seakg.bottlefs.HandlerRabbit());
        server.createContext("/help", new HandlerHelp());
        server.createContext("/", new HandlerHelp());

        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class HandlerHelp implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            JSONObject json = new JSONObject();
            String response = "HandlerHelp";
            try {
              // TODO: fill from Array<IBottleFSHandlers>
              json.put( "upload", "Mars" );
              json.put( "download", "NY" );
              json.put( "search", "" );
              json.put( "start-reindexing", "" );
              json.put( "stop-reindexing", "" );
              json.put( "help", "" );
              response = json.toString(2);
            } catch (JSONException e) {
              // TODO
            }
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}