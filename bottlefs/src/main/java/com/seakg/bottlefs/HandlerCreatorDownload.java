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
					File file = new File(file_d, id);
					if (file.exists() && file.length() > 0) { // todo check is dir
						t.getResponseHeaders().set("Content-Type","application/octet-stream");
						t.getResponseHeaders().set("Content-Transfer-Encoding", "binary");
						t.getResponseHeaders().set("Content-Length", "" + file.length());
						t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + id + "\""); // todo extension and filename from xml
						t.sendResponseHeaders(200, file.length());

						System.out.println("point 0 " + id);
						
						OutputStream os = t.getResponseBody();
						try {
							InputStream is = null;
							try {
								System.out.println("point 1 " + file.getAbsolutePath());
								FileInputStream fs = new FileInputStream(file);
								System.out.println("point 1.1 ");
								// is = new BufferedInputStream(fs);
								System.out.println("point 2 ");
								IOUtils.copy(fs,os);
								System.out.println("point 3 ");
							} finally {
								System.out.println("point 4 ");
								os.flush();
								os.close();
								is.close();
								System.out.println("point 5 ");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}/* catch (FileNotFoundException ex) {
							json.put("result", "fail" );
							json.put("error", "Could not read file");
						}*/
						
						System.out.println("return");
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
