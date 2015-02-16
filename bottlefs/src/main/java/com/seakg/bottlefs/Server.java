package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

public class Server {

    static ArrayList<Properties> m_arrProps = new ArrayList();
    static ArrayList<IHandlerCreator> m_arrHandlers = new ArrayList();

    public static void main(String[] args) throws Exception {

      File configd = new File(args[0]);
      if (!configd.exists()) {
        throw new Exception("Error: Folder does not exists.");
      }

      File[] listOfFiles = configd.listFiles();

      for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile()) {
          System.out.println("Reading config file " + listOfFiles[i].getName());
          try {
            InputStream is = new FileInputStream(listOfFiles[i]);
            Properties props = new Properties();
            props.load(is);
            is.close();
            m_arrProps.add(props);
          } catch ( Exception e ) {
            throw new Exception("Error: Could not reading properties");
          }
        }
      }

      // init handlers
      m_arrHandlers.add(new HandlerCreatorUpload());
      m_arrHandlers.add(new HandlerCreatorDownload());
      m_arrHandlers.add(new HandlerCreatorSearch());
      m_arrHandlers.add(new HandlerCreatorStartReindexing());
      m_arrHandlers.add(new HandlerCreatorStopReindexing());
      m_arrHandlers.add(new HandlerCreatorRabbit());

      for (int i = 0; i < m_arrProps.size(); i++) {
        int port = Integer.parseInt(m_arrProps.get(i).getProperty("port"));
		// init directories
		File dir_files = new File(m_arrProps.get(i).getProperty("files.directory"));
		dir_files.mkdirs();
		

        // init server
        System.out.println("Start server on " + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        for (int h = 0; h < m_arrHandlers.size(); h++) {
          HttpContext context = server.createContext("/" + m_arrHandlers.get(h).name(), m_arrHandlers.get(h).createHttpHandler(m_arrProps.get(i)));
          context.getFilters().add(new ParameterFilter());
        }
        server.createContext("/help", new HandlerHelp());
        server.setExecutor(null); // creates a default executor
        server.start();
      }        
    }

    static class HandlerHelp implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            JSONObject json = new JSONObject();
            String response = "HandlerHelp";
            try {
              json.put( "help", "this" );

              for (int h = 0; h < m_arrHandlers.size(); h++) {
                json.put( m_arrHandlers.get(h).name(), m_arrHandlers.get(h).info() );
              }

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