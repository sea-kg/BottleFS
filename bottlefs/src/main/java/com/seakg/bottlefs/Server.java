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
		m_arrHandlers.add(new HandlerCreatorIndex());

		for (int i = 0; i < m_arrProps.size(); i++) {
			Engine engine = new Engine(m_arrProps.get(i));
			engine.initDirs();
			engine.start();
			
			// init server
			System.out.println("Start server on " + engine.getPort());
			HttpServer server = HttpServer.create(new InetSocketAddress(engine.getPort()), 0);
			for (int h = 0; h < m_arrHandlers.size(); h++) {
				IHandlerCreator creator = m_arrHandlers.get(h);
				HttpContext context = server.createContext("/" + creator.name(), creator.createHttpHandler(engine));
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
