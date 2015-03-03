package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;

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
					String p = params.get("search").toString().trim();
					if (p.length() > 0) {
						
						String[] arr = p.split(" ");
						ArrayList<String> list = new ArrayList<String>();
						for(int i = 0; i < arr.length; i++) {
							String sN = arr[i].trim();
							if (sN.length() > 0) {
								sN = sN.replaceAll("\\\\", Matcher.quoteReplacement(""));
								sN = sN.replaceAll("\"", Matcher.quoteReplacement(""));
								sN = sN.replaceAll("\\+", Matcher.quoteReplacement(""));
								sN = sN.replaceAll("and", Matcher.quoteReplacement(""));
								sN = sN.replaceAll("or", Matcher.quoteReplacement(""));
								sN = sN + "*";
								list.add(sN);
								
							}
						}
						String sN = StringUtils.join(list.toArray()," and ");
						System.out.println(sN);
						search_props.setProperty("text", sN);
					} else
						search_props.setProperty("text", "");
				}

				String[] fields = m_engine.getMetadata_textfields();
				for (int i = 0; i < fields.length; i++) {
					String sFieldName = fields[i];

					input.put(sFieldName, "term1* or +term2* or -term3*");
					
					if (params.containsKey(sFieldName))
					{
						System.out.println(sFieldName + "=" + params.get(sFieldName).toString());
						search_props.setProperty(sFieldName, params.get(sFieldName).toString());
					}
				}

				ArrayList<Properties> result = new ArrayList<Properties>();
				String error = m_engine.search(search_props, result);
				if (error.length() != 0) {
					json.put( "error", error );
				}
				
				JSONArray data = new JSONArray();
				for (int i = 0; i < result.size(); i++) {
					// System.out.println("" + i);
					JSONObject doc = new JSONObject();
					Properties props = result.get(i);
					Enumeration e = props.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String val = (String) props.getProperty(key);
						// System.out.println("" + key);
						// System.out.println("" + val);
						doc.put(key, val);
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

			// System.out.println(data.toString(2));
			byte[] b = response.getBytes(Charset.forName("UTF-8"));
			t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
			t.getResponseHeaders().set("Content-Length", "" + b.length);
			t.sendResponseHeaders(200, b.length);
			OutputStream os = t.getResponseBody();
			os.write(b);
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
