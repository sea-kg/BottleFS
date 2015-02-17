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

public class Engine {
		private Properties m_pProps;

		public Engine(Properties pProps) {
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
		
		public void writeXml(File file_xml, Map<String,String> mapData)
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
		
		public String[] getMetadata_textfields() {
			return m_pProps.getProperty("metadata.textfields").split(",");
		}
		
		public String getFilesDirectory() {
			return m_pProps.getProperty("files.directory");
		}
		
		public void initDirs() {
			File dir_files = new File(m_pProps.getProperty("files.directory"));
			dir_files.mkdirs();
		}
		
		public int getPort() {
			return Integer.parseInt(m_pProps.getProperty("port"));
		}
		
		/** Read the given binary file, and return its contents as a byte array.*/ 
		public byte[] read(File file) {
			System.out.println("File size: " + file.length());
			byte[] result = new byte[(int)file.length()];
			try {
				InputStream input = null;
				try {
					int totalBytesRead = 0;
					System.out.println("point 1");
					input = new BufferedInputStream(new FileInputStream(file));
					System.out.println("point 2");
					while(totalBytesRead < result.length){
						System.out.println("point 3");
						int bytesRemaining = result.length - totalBytesRead;
						System.out.println("point 4");
						//input.read() returns -1, 0, or more :
						int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
						if (bytesRead > 0){
							totalBytesRead = totalBytesRead + bytesRead;
						}
						System.out.println("point 5");
					}
					System.out.println("Num bytes read: " + totalBytesRead);
				}
				finally {
					System.out.println("Closing input stream.");
					input.close();
				}
			}
			catch (FileNotFoundException ex) {
				System.out.println("File not found.");
			}
			catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
			return result;
		}
}
