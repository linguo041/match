package com.roy.football.match.httpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.gson.reflect.TypeToken;
import com.roy.football.match.logging.ErrorType;
import com.roy.football.match.logging.MatchLogger;
import com.roy.football.match.util.GsonConverter;
import com.roy.football.match.util.StringUtil;

public class HttpRequestService {
	public final static String POST_METHOD = "POST";
	public final static String GET_METHOD = "GET";

	private final static String RSP_STRING = "\"data\":\"\"";
	private final static String RSP_OBJECT = "\"data\":{}";
	private final static String RSP_ARRAY = "\"data\":[]";
	
	private final static String CONTENT_TYPE_KEY = "Content-Type";
//	private final static String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private final static String DEFAULT_CONTENT_TYPE = "application/json";
	private final static int CONNECT_TIMEOUT = 20000; // 20s
	private final static int READ_TIMEOUT = 20000; // 20s

	private static String _proxyHost = "qa-proxy.qa.ebay.com";
//	private static String _proxyHost = "httpproxy.vip.ebay.com";
	private final static int PROXY_PORT = 80;
	private final static int MAX_RETRY_ATTEMPT = 3;
	
	private static MatchLogger logger = MatchLogger.getInstance(HttpRequestService.class);
	
	private static HttpRequestService instance = null;
	
	public synchronized static HttpRequestService getInstance() {
		if (instance == null) {
			instance = new HttpRequestService();
		}

		return instance;
	}
	
	public String doHttpRequest(String requestUrl, String requestMethod,
			String content, Map<String, String> headers) throws HttpRequestException {
		HttpRequestException throwout = null;
		for (int count = 0; count < MAX_RETRY_ATTEMPT; count++) {
			try {
				return doHttpRequest(requestUrl, requestMethod, content, headers, false);
			} catch (HttpRequestInterruptedException e) {
				// TODO Auto-generated catch block
				throwout = e;
				sleep(300);
			} catch (HttpRequestException e) {
				// TODO Auto-generated catch block
				throwout = e;
				sleep(300);
			}
		}
		
		throw throwout;
	}
	
	private static void sleep (int n) {
		try {
			Thread.sleep(n);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String doHttpRequest(String requestUrl, String requestMethod,
			String content, Map<String, String> headers, boolean useProxy) throws HttpRequestInterruptedException, HttpRequestException {
		URL url = null;
		HttpURLConnection conn;

		try {
			url = new URL(requestUrl);
			
			if (useProxy) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_proxyHost, PROXY_PORT));
				conn = (HttpURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			
			conn.setRequestMethod(requestMethod);
			
			conn.setDoInput(true);
			conn.setDoOutput(true); // no content
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
		} catch (MalformedURLException e) {
			throw new HttpRequestException(ErrorType.UnableToParseURL, e, requestUrl);
		} catch (ProtocolException e) {
			throw new HttpRequestException(ErrorType.InvalidRequestMethod, e, requestMethod);
		} catch(IOException e) {
			throw new HttpRequestException(ErrorType.UrlUnableToConnect, e, requestUrl);
		}

		if (headers != null) {
			boolean hasContentType = false;
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				String key = entry.getKey();
				if  (CONTENT_TYPE_KEY.equalsIgnoreCase(key)) {
					hasContentType = true;
				}
				conn.addRequestProperty(key, entry.getValue());
			}
			if (!hasContentType) {
				conn.addRequestProperty(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
			}
		}

		try {
			conn.connect();
		} catch (InterruptedIOException e) {
			throw new HttpRequestInterruptedException(e, requestUrl);
		} catch (IOException e2) {
			throw new HttpRequestException(ErrorType.UrlUnableToConnect, e2, requestUrl);
		}

		if (!StringUtil.isEmpty(content)) {
			OutputStream outputStream = null;
			try {
				outputStream = conn.getOutputStream();
				outputStream.write(content.getBytes("UTF-8"));
				outputStream.flush();

			} catch (IOException e) {
				throw new HttpRequestException(ErrorType.UnableWriteData, e);
			} finally {
				try {
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException e) {
					// ignore
					logger.warn("Unable to close the connection.", e);
				}
			}
		}

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				String responseMsg = conn.getResponseMessage();
				String errorMsg = String
						.format("Server [%s] responses error with error code: %d, and error message: %s",
								requestUrl, responseCode, responseMsg);
//				logger.warn();
				throw new HttpRequestException(ErrorType.ServiceAbnormal, errorMsg);
			}
		} catch (IOException e1) {
			// ignore
			logger.warn("Unable to get response code.", e1);
		}
		
		String contentEncoding = conn.getContentEncoding();
		boolean gzip = "gzip".equalsIgnoreCase(contentEncoding);

		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			InputStream ins = gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
			String line = null;
			
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (InterruptedIOException e) {
			throw new HttpRequestInterruptedException(e, requestUrl);
		} catch (IOException e) {
			throw new HttpRequestException(ErrorType.UnableReadData, e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// ignore
				logger.warn("Unable to close the connection.", e);
			}
		}

		return sb.toString();
	}

	public <T> T getResponseData(String json,
			Class<T> clazz) {
	    return getResponseData(json, clazz, null);
	}

	public <T> T getResponseData(String json,
            Class<T> clazz, String converterName) {
	    if (!StringUtil.isEmpty(json)) {
	        if (StringUtil.isEmpty(converterName)) {
	            return GsonConverter.convertJSonToObjectUseNormal(json, clazz);
	        } else {
	            return GsonConverter.useCustomizedGson(converterName).convertJSonToObject(json, clazz);
	        }
        }
        return null;
	}

	public <T> HttpResponseData<T> getResponseData(String json,
            TypeToken<HttpResponseData<T>> type) {
	    return getResponseData(json, type, null);
	}

	public <T> HttpResponseData<T> getResponseData(String json,
			TypeToken<HttpResponseData<T>> type, String converterName) {
		if (!StringUtil.isEmpty(json)) {
			ParameterizedType genericType = (ParameterizedType)type.getType();
			Type [] types = genericType.getActualTypeArguments();
			
			Class<?> genericClass = null;
			if (types[0] instanceof Class) {
				genericClass = (Class<?>)types[0];
			} else if (types[0] instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType)types[0];
				genericClass = (Class<?>)pType.getRawType();
			} else {
				try {
					String className = types[0].toString();
					className = className.substring(className.indexOf(' ') + 1);
					className.replaceAll("<.*>", "");
					genericClass = Class.forName(className);
				} catch (ClassNotFoundException e) {
					logger.error("The generic class hasn't been loaded", e);
				}
			}

			if (!String.class.isAssignableFrom(genericClass) && !Number.class.isAssignableFrom(genericClass)) {
				if (Collection.class.isAssignableFrom(genericClass)) {
					json = json.replaceFirst(RSP_STRING, RSP_ARRAY);
				} else {
					json = json.replaceFirst(RSP_STRING, RSP_OBJECT);
				}
			}
			
			if (StringUtil.isEmpty(converterName)) {
			    return GsonConverter.convertJSonToObjectUseNormal(json, type);
			} else {
			    return GsonConverter.useCustomizedGson(converterName).convertJSonToObject(json, type);
			}
		}
		return null;
	}
}
