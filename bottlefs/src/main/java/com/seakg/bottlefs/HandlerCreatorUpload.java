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
				input.put("file", "(https|http|ftp)://host/*.*");

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
			
			
			
			byte[] b = response.getBytes(Charset.forName("UTF-8"));
			t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
			t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
			t.getResponseHeaders().set("Content-Length", "" + b.length);
			t.getResponseHeaders().set("Status", "200");
			// t.setStatus(200);
			t.sendResponseHeaders(200, b.length);
			OutputStream os = t.getResponseBody();
			InputStream is = new ByteArrayInputStream(response.getBytes("UTF-8"));
			IOUtils.copy(is,os);
			os.close();
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
