package com.seakg.bottlefs;

import org.apache.commons.io.*;
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
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

import org.apache.commons.lang3.StringUtils;

public class HandlerCreatorUpload implements IHandlerCreator {

	static class HandlerUpload implements HttpHandler {
		private Engine m_engine;

		public HandlerUpload(Engine engine) {
			m_engine = engine;
		}

		public void handle(HttpExchange t) throws IOException {
			String response = "";
			t.getResponseHeaders().clear();
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			Map<String,String> mapData = new HashMap<String, String>();
			Properties props = new Properties();

			try {
				JSONObject api = new JSONObject();
				api.put( "method", "upload" );
				JSONObject input = new JSONObject();
				input.put("url", "(https|http|ftp)://host/*.*");

				String[] fields = m_engine.getMetadata_textfields();
				for (int i = 0; i < fields.length; i++) {
					String sFieldName = fields[i];
					input.put(sFieldName, "text");

					if (params.containsKey(sFieldName))
						props.setProperty(sFieldName, params.get(sFieldName).toString());
					else
						props.setProperty(sFieldName, "");
				}

				api.put( "input", input );
				json.put("api", api);
				
				if (params.containsKey("url")) {
					String url = params.get("url").toString();				
					if (!m_engine.allowIndexingLocalFiles()) {
						try {
							URL u = new URL(url);
							if (u.getProtocol().equals("file")) {
								m_engine.sendResponseError(t, 1003, "Not allow 'file://'");
								return;
							}
						} catch (Exception e) {
							m_engine.sendResponseError(t, 1001, e.getMessage());
							return;
						}
						
					}
					props.setProperty("url", url);
					mapData = m_engine.toIndex(props);
					json.put("data", mapData);
					json.put( "result", "ok" );
				} else {
					m_engine.sendResponseError(t, 1002, "Not found parameter 'url'");
					return;					
				}
				m_engine.sendResponse(t, json);
			} catch (JSONException e) {
				// TODO
			}
		}
	}

	public String name()
	{
		return "upload";
	}

	public String info()
	{
		return "this handler upload files (bytearray or by http-lnk)";
	}

	public HttpHandler createHttpHandler(Engine engine)
	{
		return new HandlerUpload(engine);
	}
}
