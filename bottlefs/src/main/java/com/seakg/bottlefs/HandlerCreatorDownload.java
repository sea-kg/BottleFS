package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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

public class HandlerCreatorDownload implements IHandlerCreator {

	static class HandlerDownload implements HttpHandler {
		private Engine m_engine;

		public HandlerDownload(Engine engine) {
			m_engine = engine;
		}

		public void handle(HttpExchange t) throws IOException {
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			String response = "";
			try {
				if (params.containsKey("id")) {
					String id = params.get("id").toString();
					File files_d = new File(m_engine.getFilesDirectory());
					File file_d = new File(files_d, m_engine.createPath(id));
					String ext = "";
					if (params.containsKey("other"))
					{
						String other = params.get("other").toString();
						if (other.equals("text"))
							ext = ".text";
						else if (other.equals("xml"))
							ext = ".xml";

						id += ext;
					}
					
					File file = new File(file_d, id);
					if (file.exists() && file.length() > 0) { // todo check is dir

						t.getResponseHeaders().set("Content-Length", "" + file.length());
						if (ext.equals(".text")) {
							t.getResponseHeaders().set("Content-Type","text/plain; charset=utf-8");
							t.getResponseHeaders().set("Content-Disposition", "inline; filename=\"" + id + "\""); // todo filename from xml
						} else if (ext.equals(".xml")) {
							t.getResponseHeaders().set("Content-Type","application/xml; charset=utf-8");
							t.getResponseHeaders().set("Content-Disposition", "inline; filename=\"" + id + "\""); // todo filename from xml
						} else {
							// todo content-type get from xml
							t.getResponseHeaders().set("Content-Type","application/octet-stream");
							t.getResponseHeaders().set("Content-Transfer-Encoding", "binary");
							t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + id + "\""); // todo filename from xml
						}

						t.sendResponseHeaders(200, file.length());
						
						OutputStream os = t.getResponseBody();
						try {
							InputStream is = null;
							try {
								FileInputStream fs = new FileInputStream(file);
								IOUtils.copy(fs,os);
							} finally {
								os.flush();
								os.close();
								is.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					} else {
						json.put("result", "fail" );
						json.put("error", "Not found file");
					}
				} else {
					json.put("result", "fail" );
					json.put("error", "Not found parameter 'id'");
				}

				JSONObject api = new JSONObject();
				api.put( "method", "download" );
				JSONObject input = new JSONObject();
				input.put("id", "md5");
				input.put("other", "(text|xml)");
				api.put( "input:", input );
				json.put("api", api);
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

	public String name()
	{
		return "download";
	}

	public String info()
	{
		return "todo";
	}

	public HttpHandler createHttpHandler(Engine engine)
	{
		return new HandlerDownload(engine);
	}
}
