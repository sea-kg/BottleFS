package com.seakg.bottlefs;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Properties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.*;

public class ParameterFilter extends Filter {

    @Override
    public String description() {
        return "Parses the requested URI for parameters";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
			exchange.getHttpContext().getAttributes().clear();
			parseGetParameters(exchange);
			parsePostParameters(exchange);       
			chain.doFilter(exchange);
		} catch(Throwable e) {
			System.out.print("Problem with parse get request (2001):\r\n" + e.getMessage() + "\r\n");
		}
    }

	private void parseGetParameters(HttpExchange exchange) throws UnsupportedEncodingException {
		Map<String,Object> parameters = new HashMap();	
		URI requestedUri = exchange.getRequestURI();
		String query = requestedUri.getRawQuery();
		parseQuery(query, parameters);
		for (Map.Entry<String,Object> entry : parameters.entrySet())
		{
			try {
				exchange.setAttribute(entry.getKey(), entry.getValue().toString());
			} catch(Throwable e) {
				exchange.setAttribute(entry.getKey(), "");
				System.out.print("Problem with parse get request (2000):\r\n" + e.getMessage() + "\r\n");
			}
		}
	}

    private void parsePostParameters(HttpExchange exchange)
        throws IOException {
        /*if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
            @SuppressWarnings("unchecked")
            Map parameters =
                (Map)exchange.getAttribute("parameters");
            InputStreamReader isr =
                new InputStreamReader(exchange.getRequestBody(),"utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);
        }*/
    }

	@SuppressWarnings("unchecked")
	private void parseQuery(String query, Map parameters) throws UnsupportedEncodingException {
		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = "";
				String value = "";
				if (param.length > 0) {
					key = URLDecoder.decode(param[0],
					System.getProperty("file.encoding"));
				}
				if (param.length > 1) {
					value = URLDecoder.decode(param[1],
					System.getProperty("file.encoding"));
				}
				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if(obj instanceof List) {
						List values = (List)obj;
						values.add(value);
					} else if(obj instanceof String) {
						List values = new ArrayList();
						values.add((String)obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}
}
