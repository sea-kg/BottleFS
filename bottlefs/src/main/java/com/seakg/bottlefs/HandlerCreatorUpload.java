package com.seakg.bottlefs;

import org.apache.commons.io.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import org.json.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

public class HandlerCreatorUpload implements IHandlerCreator {

	static class HandlerUpload implements HttpHandler {
		private Properties m_pProps;

		public HandlerUpload(Properties pProps) {
			m_pProps = pProps;
		}
		
		public String MD5(String md5) {
			try {
				java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
				byte[] array = md.digest(md5.getBytes());
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < array.length; ++i) {
					sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
				}
				return sb.toString();
			} catch (java.security.NoSuchAlgorithmException e) {
			}
			return null;
		}

		public String createPath(String md5) {
			String sResult = "";
			for(int i = 0; i < md5.length(); i++) {
				if (i % 4 == 0)
					sResult += "/";
				sResult += md5.charAt(i);
			}
			return sResult;
		}
		
		public void createXml(File file_xml, Map<String,String> mapData)
		{
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
				DocumentBuilder builder = factory.newDocumentBuilder(); 
				DOMImplementation impl = builder.getDOMImplementation(); // более сложный, но и более гибкий способ создания документов 
				Document doc = impl.createDocument(null, // namespaceURI 
												   null, // qualifiedName 
												   null); // doctype 
				Element eDoc = doc.createElement("document");
				doc.appendChild(eDoc);

				for (Map.Entry<String, String> entry : mapData.entrySet())
				{
					Element e = doc.createElement("field");
					e.setAttribute("name", entry.getKey());
					e.setTextContent(entry.getValue());
					eDoc.appendChild(e);
				}
				
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file_xml);
				transformer.transform(source, result);
			} catch (ParserConfigurationException pce) {
				System.out.println("createXml: " + pce.getMessage());
			} catch (TransformerException tfe) {
				System.out.println("createXml: " + tfe.getMessage());
			}
		}
		
		public void handle(HttpExchange t) throws IOException {
			String response = "<pre>HandlerUpload\r\n";
			response = response + t.getHttpContext().getPath() + "\r\n";
			Map<String,Object> params = t.getHttpContext().getAttributes();
			JSONObject json = new JSONObject();
			Map<String,String> mapData = new HashMap<String, String>();
			try {
				/*for (int h = 0; h < m_arrHandlers.size(); h++) {
					json.put( m_arrHandlers.get(h).name(), m_arrHandlers.get(h).info() );
				}*/
				
				if (params.containsKey("file")) {
					String url = params.get("file").toString();
					String md5 = MD5(url);
					mapData.put("id", md5);
					mapData.put("url", url);
					
					File files_d = new File(m_pProps.getProperty("files.directory"));
					// todo parse from property extended fields					
					String[] fields = m_pProps.getProperty("metadata.textfields").split(",");
					for (int i = 0; i < fields.length; i++) {
						String sFieldName = fields[i];
						if (params.containsKey(sFieldName))
							mapData.put(sFieldName, params.get(sFieldName).toString());
						else
							mapData.put(sFieldName, "");
					}

					try {
						URL uriFile = new URL(url);
						File file_d = new File(files_d, createPath(md5));
						file_d.mkdirs();
						File f = new File(file_d, md5);
						FileUtils.copyURLToFile(uriFile, f);

						File file_xml = new File(file_d, md5 + ".xml");
						createXml(file_xml, mapData);
						json.put( "result", "ok" );
						json.put("data", mapData);
					} catch (IOException e) {
						json.put( "error", e.getMessage() );
						System.out.print("error: " + e.getMessage() + "\r\n");
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
					response = json.toString(2);
				}
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
		return "upload";
	}

	public String info()
	{
		return "this handler upload files (bytearray or by http-lnk)";
	}

	public HttpHandler createHttpHandler(Properties pProps)
	{
		return new HandlerUpload(pProps);
	}
}
