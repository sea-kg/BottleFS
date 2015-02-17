package com.seakg.bottlefs;

import org.apache.commons.io.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import org.apache.tika.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.*;
import org.apache.tika.sax.*;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.apache.tika.parser.pdf.PDFParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HandlerCreatorUpload implements IHandlerCreator {

	static class HandlerUpload implements HttpHandler {
		private Engine m_engine;

		public HandlerUpload(Engine engine) {
			m_engine = engine;
		}

		public void handle(HttpExchange t) throws IOException {
			String response = "";
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			Map<String,String> mapData = new HashMap<String, String>();
			
			try {
				String[] fields = m_engine.getMetadata_textfields();
				for (int i = 0; i < fields.length; i++) {
					String sFieldName = fields[i];
					if (params.containsKey(sFieldName))
						mapData.put(sFieldName, params.get(sFieldName).toString());
					else
						mapData.put(sFieldName, "");
				}
				
				JSONObject api = new JSONObject();
				api.put( "method", "upload" );
				JSONObject input = new JSONObject();
				input.put("file", "(https|http|ftp)://host/*.*");

				for (int i = 0; i < fields.length; i++) {
					input.put(fields[i], "text");
				}

				api.put( "input:", input );
				json.put("api", api);
					
				/*for (int h = 0; h < m_arrHandlers.size(); h++) {
					json.put( m_arrHandlers.get(h).name(), m_arrHandlers.get(h).info() );
				}*/
				
				if (params.containsKey("file")) {
					String url = params.get("file").toString();
					String md5 = m_engine.MD5(url);
					mapData.put("id", md5);
					mapData.put("url", url);
					
					File files_d = new File(m_engine.getFilesDirectory());
					// todo parse from property extended fields					

					try {
						URL uriFile = new URL(url);
						File file_d = new File(files_d, m_engine.createPath(md5));
						file_d.mkdirs();
						File f = new File(file_d, md5);
						FileUtils.copyURLToFile(uriFile, f);
						mapData.put("length", "" + f.length());
						if (f.exists() && f.length() > 0) {
							InputStream stream = new FileInputStream(f);
							try {
								Parser parser = new AutoDetectParser();
								ContentHandler textHandler = new BodyContentHandler(Integer.MAX_VALUE);
								Metadata metadata = new Metadata();
								ParseContext context = new ParseContext();

								parser.parse(stream,textHandler,metadata,context);
								String title = metadata.get(Metadata.TITLE);
								if (title != null)
									mapData.put("title", title);
								String text = textHandler.toString();
								// System.out.println("Body: " + text);
								
								File file_text = new File(file_d, md5 + ".text");
								FileUtils.writeStringToFile(file_text, text);
								
								/*Tika tika = new Tika();
								mapData.put("content-type", tika.detect(stream).toString());*/
							} catch (  Exception e) {
								json.put( "error_tika", e.getMessage() );
							} finally {
								stream.close();
							}
							
						}
						
						File file_xml = new File(file_d, md5 + ".xml");
						m_engine.writeXml(file_xml, mapData);
						json.put("data", mapData);
						json.put( "result", "ok" );
					} catch (IOException e) {
						json.put( "error", e.getMessage() );
						json.put( "result", "fail" );
						System.out.print("error: " + e.getMessage() + "\r\n");
					}
				} else {
					json.put("result", "fail" );
					json.put("error", "Not found parameter 'file'");
				}
				response = json.toString(2);				
				
			} catch (JSONException e) {
				// TODO
			}

			t.sendResponseHeaders(200, response.length());
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
