package com.seakg.bottlefs;

import org.apache.commons.io.*;
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

public class HandlerCreatorIndex implements IHandlerCreator {

   static class HandlerIndex implements HttpHandler {
       public void handle(HttpExchange t) throws IOException {
			String response = "";
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			Map<String,String> mapData = new HashMap<String, String>();
			Properties props = new Properties();
			
			/*try {
				
				JSONObject api = new JSONObject();
				api.put( "method", "index" );
				JSONObject input = new JSONObject();
				api.put( "input", input );
				json.put("api", api);
				
				if (params.containsKey("file")) {
					String url = params.get("file").toString();
					props.setProperty("url", url);
					mapData = m_engine.toIndex(props);
					json.put("data", mapData);
					json.put( "result", "ok" );
				} else {
					json.put("result", "fail" );
					json.put("error", "Not found parameter 'file'");
				}
				response = json.toString(2);				
				
			} catch (JSONException e) {
				// TODO
			}
*/
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			InputStream is = new ByteArrayInputStream(response.getBytes("UTF-8"));
			IOUtils.copy(is,os);
			os.close();
       }
   }

   public String name()
   {
     return "index";
   }

   public String info()
   {
     return "TODO";
   }

   public HttpHandler createHttpHandler(Engine engine)
   {
     return new HandlerIndex();
   }
}
