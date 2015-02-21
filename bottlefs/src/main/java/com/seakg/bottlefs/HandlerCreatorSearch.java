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
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

public class HandlerCreatorSearch implements IHandlerCreator {

	static class HandlerSearch implements HttpHandler {
		private Engine m_engine;

		public HandlerSearch(Engine engine) {
			m_engine = engine;
		}
		
		public void handle(HttpExchange t) throws IOException {
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			String response = "";
			try {
				JSONObject api = new JSONObject();
				api.put( "method", "search" );
				JSONObject input = new JSONObject();
				input.put("search", "term or term1*");
				Properties search_props = new Properties();
				if (params.containsKey("search")) {
					search_props.setProperty("text", params.get("search").toString());
				}

				String[] fields = m_engine.getMetadata_textfields();
				for (int i = 0; i < fields.length; i++) {
					String sFieldName = fields[i];
					
					input.put(sFieldName, "term* or term*");
					
					if (params.containsKey(sFieldName))
						search_props.setProperty(sFieldName, params.get(sFieldName).toString());
				}

				 
				ArrayList<Properties> result = new ArrayList<Properties>();
				String error = m_engine.search(search_props, result);
				if (error.length() != 0) {
					json.put( "error", error );
				}
				
				JSONArray data = new JSONArray();
				for (int i = 0; i < result.size(); i++) {
					JSONObject doc = new JSONObject();
					Properties props = result.get(i);
					Enumeration e = props.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						doc.put(key, props.getProperty(key));
					}
					data.put(doc);
				}
				json.put( "data", data );

				api.put( "input", input );
				json.put("api", api);
				response = json.toString(2);				
			} catch (JSONException e) {
				// TODO
				System.out.println("Error(1020): " + e.getMessage());
			}

			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public String name()
	{
		return "search";
	}

	public String info()
	{
		return "todo";
	}

	public HttpHandler createHttpHandler(Engine engine)
	{
		return new HandlerSearch(engine);
	}
}
