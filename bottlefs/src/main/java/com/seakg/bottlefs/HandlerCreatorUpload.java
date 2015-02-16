package com.seakg.bottlefs;

import org.apache.commons.io.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.ParserConfigurationException;
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
		
		public void createXml(File file_xml, String url, String md5)
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
				Element eId = doc.createElement("field");
				eId.setAttribute("name", "id");
				eId.setTextContent(md5);
				eDoc.appendChild(eId);
				
				Element eUrl = doc.createElement("field");
				eUrl.setAttribute("name", "url");
				eUrl.setTextContent(url);
				eDoc.appendChild(eUrl);
				
				/*Element eUrl = doc.createElement("field");
				eUrl.setAttribute("name", "url");
				eUrl.setTextContent(url);
				eDoc.appendChild(eUrl);*/
				
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
			Map<String,Object> map = t.getHttpContext().getAttributes();

			/*
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				response = response + entry.getKey() + ": " + entry.getValue().toString() + "\r\n";				
			}*/
			
			if (map.containsKey("file")) {
				String url = map.get("file").toString();
				String md5 = MD5(url);
				File files_d = new File(m_pProps.getProperty("files.directory"));
				response += "File: " + url + "\r\n";

				try {
					URL uriFile = new URL(url);
					File file_d = new File(files_d, createPath(md5));
					file_d.mkdirs();
					File f = new File(file_d, md5);
					FileUtils.copyURLToFile(uriFile, f);
					
					File file_xml = new File(file_d, md5 + ".xml");
					createXml(file_xml, url, md5);

				} catch (IOException e) {
					response += "Error: " + e.getMessage() + "\r\n";
					System.out.print("Error: " + e.getMessage() + "\r\n");
					// return;
				}
				
			}

			
			response += "<form action='?' method='GET'><input type='text' name='file'/><input type='submit'/></form>";
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
