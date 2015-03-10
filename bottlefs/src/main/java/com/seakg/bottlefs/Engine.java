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
import java.util.*;
import java.nio.charset.Charset;

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

import org.apache.tika.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.*;
import org.apache.tika.sax.*;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.apache.tika.parser.pdf.PDFParser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.*;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

// import org.apache.lucene.queryparser.*;
import org.apache.lucene.queryparser.classic.*;

import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;
import org.apache.lucene.util.Version.*;

public class Engine implements Runnable {
		private Properties m_pProps;
		private Thread t;
		private Stack<String> m_requests = new Stack<String>();
		private String processingId = "";
		
		private final Object mutex = new Object();

		public Engine(Properties pProps) {
			m_pProps = pProps;
		}
		
		public URL parseUrl(String s) throws Exception {
			 URL u = new URL(s);
			 return new URI(
					u.getProtocol(), 
					u.getAuthority(), 
					u.getPath(),
					u.getQuery(), 
					u.getRef()).
					toURL();
		}
		
		public void run() {
			System.out.println("Running " +  m_pProps.getProperty("name"));
			
			try {
				while(true) {
					String id = null;
					synchronized(mutex) {
						if (m_requests.size() > 0) {
							id = m_requests.pop();
							processingId = id;
						} else {
							processingId = "";
						}
					}
					
					if (id == null) {
						// System.out.println("Thread: " + m_pProps.getProperty("name") + ", sleep 2 sec");
						Thread.sleep(2000); // sleep 2 second
					} else {
						System.out.println("Thread: " + m_pProps.getProperty("name") + ", processing: " + id);

						// load data from peroperties
						File files_d = new File(this.getFilesDirectory());
						File file_d = new File(files_d, this.createPath(id));
						file_d.mkdirs();
						File f = new File(file_d, id + ".binary");
						File f_metadata = new File(file_d, id + ".metadata");
						if (!f_metadata.exists()) {
							System.err.println("Error(1004): metadata are not found, " + id);
							continue;
						}
						Properties props = new Properties();
						try {
							FileInputStream is = new FileInputStream(f_metadata);
							props.loadFromXML(is);
							is.close();
						} catch(IOException e) {
							System.err.println("Error(1001): read form properties, " + e.getMessage());
							continue;
						}

						// set status 'to_downloading...'
						props.setProperty("bottlefs_status", "to_downloading");

						// save props
						try {
							FileOutputStream os = new FileOutputStream(f_metadata);
							props.storeToXML(os, "to_downloading");
							os.close();
						} catch (IOException e) {
							System.err.println("Error(1002): to_downloading, " + e.getMessage());
							continue;
						}

						// downloading file
						try {
							String url = props.getProperty("url");
							URL urlFile = parseUrl(url);
							FileUtils.copyURLToFile(urlFile, f);
							props.setProperty("bottlefs_status", "to_parse");
							props.setProperty("length", "" + f.length());
						} catch (Exception e) {
							System.err.println("Error(1003): downloading, " + e.getMessage());
							props.setProperty("bottlefs_status", "error_downloading");
							props.setProperty("bottlefs_error", e.getMessage());
						}
						
						
						// save props
						try {
							FileOutputStream os = new FileOutputStream(f_metadata);
							props.storeToXML(os, "to_parse");
							os.close();
						} catch (IOException e) {
							System.err.println("Error(1005): to_downloading, " + e.getMessage());
							continue;
						}

						if (!f.exists()) {
							continue;
						}
						
						// get text
						if (f.length() > 0) {
							try {
								InputStream stream = new FileInputStream(f);
								Parser parser = new AutoDetectParser();
								ContentHandler textHandler = new BodyContentHandler(Integer.MAX_VALUE);
								Metadata metadata = new Metadata();
								ParseContext context = new ParseContext();

								parser.parse(stream,textHandler,metadata,context);
		
								String[] md = metadata.names(); 
								
								for (int i = 0; i < md.length; i++) {
									String val = metadata.get(md[i]);
									if (val != null)
										props.setProperty("tika_" + md[i], val);								
								}

								String text = textHandler.toString();
								// System.out.println("Body: " + text);
								
								File file_text = new File(file_d, id + ".text");
								FileUtils.writeStringToFile(file_text, text);
								
								/*Tika tika = new Tika();
								mapData.put("content-type", tika.detect(stream).toString());*/
							} catch (  Exception e) {
								System.err.println("Error(1007): parsing, " + e.getMessage());
								props.setProperty("bottlefs_error", e.getMessage());
							}
						}
						
						// save props
						try {
							FileOutputStream os = new FileOutputStream(f_metadata);
							props.storeToXML(os, "to_indexing");
							os.close();
						} catch (IOException e) {
							System.err.println("Error(1008): to_downloading, " + e.getMessage());
							continue;
						}
						
						// indexing
						// lucene 5.0.0
						// http://lucene.apache.org/core/5_0_0/core/index.html

						try {
							Analyzer analyzer = new StandardAnalyzer();
							
							File index_d = new File(this.getIndexDirectory());
							Directory directory = FSDirectory.open(index_d.toPath());
							IndexWriterConfig config = new IndexWriterConfig(analyzer);
							IndexWriter indexWriter = new IndexWriter(directory, config);
							
							// QueryParser parser = new QueryParser("id", analyzer);
							// indexWriter.deleteDocuments(parser.parse(id));
							indexWriter.deleteDocuments(new Term("id", id));

							Document document = new Document();
							Enumeration e = props.propertyNames();
							while (e.hasMoreElements()) {
								String key = (String) e.nextElement();
								document.add(new Field(key, props.getProperty(key), TextField.TYPE_STORED));
							}

							File file_text = new File(file_d, id + ".text");
							String text = FileUtils.readFileToString(file_text, "UTF-8");
							document.add(new Field("text", text, TextField.TYPE_NOT_STORED));
							indexWriter.addDocument(document);
							indexWriter.close();
						} catch(IOException e) {
							System.err.println("Error(1011): to_indexing, " + e.getMessage());
							continue;
						}; /* catch(ParseException e) {
							System.err.println("Error(1023): parse, " + e.getMessage());
							continue;
						}*/
						
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Thread " +  m_pProps.getProperty("name") + " interrupted.");
			}
			System.out.println("Thread " +  m_pProps.getProperty("name") + " exiting.");
		}
		
		public void start()
		{
			System.out.println("Starting " +  m_pProps.getProperty("name") );
			if (t == null)
			{
				t = new Thread (this, m_pProps.getProperty("name"));
				t.start ();
			}
		}
		
		public Map<String,String> toIndex(Properties props) {
			Map<String,String> mapData = new HashMap<String, String>();

			String[] fields = this.getMetadata_textfields();
			for (int i = 0; i < fields.length; i++) {
				String sFieldName = fields[i];
				if (props.containsKey(sFieldName))
					mapData.put(sFieldName, props.getProperty(sFieldName));
				else
					mapData.put(sFieldName, " ");
			}

			String url = props.getProperty("url");
			String id = this.MD5(url);
			mapData.put("id", id);
			props.setProperty("id", id);
			props.setProperty("bottlefs_status", "to_index");
			
			File files_d = new File(this.getFilesDirectory());
			File file_d = new File(files_d, this.createPath(id));
			file_d.mkdirs();
			File f_metadata = new File(file_d, id + ".metadata");

			synchronized(mutex) {
				if (!m_requests.contains(id) && !processingId.equals(id)) {
					try {
						FileOutputStream os = new FileOutputStream(f_metadata);
						props.storeToXML(os, "to_index");
						os.close();
					} catch (IOException e) {
						System.err.println("Error(1000): to_index, " + e.getMessage());
					}
					m_requests.push(id);
				} else 
					System.out.println("Alredy exists: " + id);
			}
			return mapData;
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
		
		public String[] getMetadata_textfields() {
			return m_pProps.getProperty("metadata.textfields").split(",");
		}
		
		public String getFilesDirectory() {
			return m_pProps.getProperty("files.directory");
		}
		
		public String getIndexDirectory() {
			return m_pProps.getProperty("index.directory");
		}
		
		public void initDirs() {
			File dir_files = new File(m_pProps.getProperty("files.directory"));
			dir_files.mkdirs();

			File dir_index = new File(m_pProps.getProperty("index.directory"));
			dir_index.mkdirs();
		}
		
		public Properties docToProps(Document hitDoc) {
			Properties props = new Properties();
			props.setProperty("id", hitDoc.get("id"));
			props.setProperty("url", hitDoc.get("url"));
			props.setProperty("length", hitDoc.get("length"));
			
			String[] view_fields = this.getMetadata_textfields();
			for (int fi = 0; fi < view_fields.length; fi++) {
				String sFieldName = view_fields[fi];
				String value = hitDoc.get(sFieldName);
				if (value != null)
					props.setProperty(sFieldName, value);
			}
			return props;
		}
		
		public String search(Properties search_props, ArrayList<Properties> result) {
			String error = "";
			try {
				Analyzer analyzer = new StandardAnalyzer();
				File index_d = new File(this.getIndexDirectory());
				Directory directory = FSDirectory.open(index_d.toPath());

				// Now search the index:
				DirectoryReader ireader = DirectoryReader.open(directory);

				// search_props
				ArrayList<String> queries = new ArrayList();
				ArrayList<String> fields = new ArrayList();
				ArrayList<BooleanClause.Occur> flags = new ArrayList();

				Enumeration e = search_props.propertyNames();
				while (e.hasMoreElements()) {

					String key = (String) e.nextElement();
					String query = (String) search_props.getProperty(key);
					if (query.trim().length() != 0) {
						fields.add(key);
						queries.add(search_props.getProperty(key));
						/*if (key.equals("text"))
							flags.add(BooleanClause.Occur.SHOULD);
						else*/
							flags.add(BooleanClause.Occur.MUST);
					}
				}
				
				if (fields.size() != 0) {
					IndexSearcher isearcher = new IndexSearcher(ireader);	
					Query query = MultiFieldQueryParser.parse(
						queries.toArray(new String[queries.size()]),
						fields.toArray(new String[fields.size()]),
						flags.toArray(new BooleanClause.Occur[flags.size()]),
						analyzer
					);

					ScoreDoc[] hits = isearcher.search(query, null, 50).scoreDocs;
					// Iterate through the results:
					for (int i = 0; i < hits.length; i++) {
						Document hitDoc = isearcher.doc(hits[i].doc);
						result.add(docToProps(hitDoc));
					}
				} else { // if query is empty
					// IndexReader reader = new IndexReader();
					String[] view_fields = this.getMetadata_textfields();
					for (int i=0; i<ireader.maxDoc(); i++) {
						/*if (ireader.isDeleted(i))
							continue;*/
						Document hitDoc = ireader.document(i);
						result.add(docToProps(hitDoc));
						if (i >= 50) 
							break;
					}			
				}
				ireader.close();
				directory.close();
			} catch (IOException e) {
				error = "Error(1012): search, " + e.getMessage();
				System.err.println(error);
			} catch (ParseException e) {
				error = "Error(1013): parse, " + e.getMessage();
				System.err.println(error);
			}
			return error;
		}

		public int getPort() {
			return Integer.parseInt(m_pProps.getProperty("port"));
		}
		
		public void sendResponse() {
			
		}
}
