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

		public String parseFormat(String p) {
			String format = "unknown";
			if (p.equals("text")) {
				format = "text";
			} else if (p.equals("binary")) {
				format = "binary";
			} else if (p.equals("metadata")) {
				format = "metadata";
			}
			return format;
		}

		public void handle(HttpExchange t) throws IOException {
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			String response = "";
			try {
				String format = params.containsKey("format") ? parseFormat(params.get("format").toString()) : "unknown";

				if (params.containsKey("id") && !format.equals("unknown")) {
					String id = params.get("id").toString();
					File files_d = new File(m_engine.getFilesDirectory());
					File file_d = new File(files_d, m_engine.createPath(id));
					
					String disposition = "";
					String contentType = "";
					String contentTransferEncodifng = "";

					File f_metadata = new File(file_d, id + ".metadata");
					if (f_metadata.exists()) {
						System.out.println("f_metadata: exists");
						Properties props = new Properties();
						try {
							FileInputStream is = new FileInputStream(f_metadata);
							props.loadFromXML(is);
							is.close();
						} catch(IOException e) {
							System.out.println("Error(1010): read form properties, " + e.getMessage());
						}
						System.out.println("f_metadata: exists2");
						if (props.containsKey("tika_Content-Type")) {
							contentType = props.getProperty("tika_Content-Type");
						}
						System.out.println("f_metadata: exists3");
						if (props.containsKey("url")) {
							String filename = URLEncoder.encode(props.getProperty("url"), "UTF-8");
							disposition = "attachment; filename=\"" + filename + "\"";
						}
					}
					// System.out.println("contentType: " + contentType);

					File file = null;
					
					if (format.equals("metadata")) {
						file = new File(file_d, id + ".metadata");
						contentType = "application/xml; charset=utf-8";
						disposition = "inline; filename=\"" + id + ".xml\"";
					} else if (format.equals("text")) {
						file = new File(file_d, id + ".text");
						contentType = "text/plain; charset=utf-8";
						disposition = "inline; filename=\"" + id + ".text\"";
					} else if (format.equals("binary")) {
						file = new File(file_d, id + ".binary");
						if (contentType.equals(""))
						  contentType = "application/octet-stream";
						if (disposition.equals(""))
							disposition = "attachment; filename=\"" + id + ".binary\"";
						contentTransferEncodifng = "binary";
					}
					
					if (file.exists() && file.length() > 0) { // todo check is dir

						t.getResponseHeaders().set("Content-Length", "" + file.length());
						
						t.getResponseHeaders().set("Content-Type",contentType);
						t.getResponseHeaders().set("Content-Disposition", disposition); // todo filename from xml
						
						if (!contentTransferEncodifng.equals(""))
							t.getResponseHeaders().set("Content-Transfer-Encoding", contentTransferEncodifng);

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
					json.put("error", "Not found parameter 'id' or 'format' or format is unknown");
				}

				JSONObject api = new JSONObject();
				api.put( "method", "download" );
				JSONObject input = new JSONObject();
				input.put("id", "md5");
				input.put("format", "(binary|text|metadata)");
				api.put( "input", input );
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
