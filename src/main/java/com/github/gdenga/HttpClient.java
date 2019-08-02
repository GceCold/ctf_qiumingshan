package com.github.gdenga;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * @author: gdenga
 * @date: 2019/8/2 9:21
 * @content:
 */
public class HttpClient {
    public static final int MAX_TOTAL_CONNECTIONS = 400;

    public static final int MAX_ROUTE_CONNECTIONS = 100;

    public static final int CONNECT_TIMEOUT = 10000;

    public static final int SOCKET_TIMEOUT = 20000;

    public static final long CONN_MANAGER_TIMEOUT = 10000;

    public static HttpParams parentParams;

    public static PoolingClientConnectionManager cm;

    private static final HttpHost DEFAULT_TARGETHOST = new HttpHost("123.206.87.240", 8002);
    public static HttpRequestRetryHandler httpRequestRetryHandler;

    static {

        SchemeRegistry schemeRegistry = new SchemeRegistry();

        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        cm = new PoolingClientConnectionManager(schemeRegistry);

        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);

        cm.setMaxPerRoute(new HttpRoute(DEFAULT_TARGETHOST), 20);

        parentParams = new BasicHttpParams();

        parentParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        parentParams.setParameter(ClientPNames.DEFAULT_HOST, DEFAULT_TARGETHOST);

        parentParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        parentParams.setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);

        parentParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);

        parentParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);

        parentParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

        parentParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);

        //设置头信息,模拟浏览器
        Collection collection = new ArrayList();

        collection.add(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)"));

        collection.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));

        collection.add(new BasicHeader("Accept-Language", "zh-cn,zh,en-US,en;q=0.5"));

        collection.add(new BasicHeader("Accept-Charset", "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7"));

        collection.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));

        parentParams.setParameter(ClientPNames.DEFAULT_HEADERS, collection);

        //请求重试处理
        httpRequestRetryHandler = new HttpRequestRetryHandler() {

            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= 3) {
                    // 如果超过最大重试次数，那么就不要继续了
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {
                    // 不要重试SSL握手异常
                    return false;
                }
                HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // 如果请求被认为是幂等的，那么就重试
                    return true;
                }
                return false;
            }


        };
    }
    private static String readHtmlContentFromEntity(HttpEntity httpEntity) throws ParseException, IOException {
        String html = "";
        Header header = httpEntity.getContentEncoding();
        {
            InputStream in = httpEntity.getContent();
            if (header != null && "gzip".equals(header.getValue())) {
                html = unZip(in);
            } else {
                html = readInStreamToString(in);
            }
            if (in != null) {
                in.close();
            }
        }
        return html;
    }
    private static String unZip(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(in);
            byte[] _byte = new byte[1024];
            int len = 0;
            while ((len = gis.read(_byte)) != -1) {
                baos.write(_byte, 0, len);
            }
            String unzipString = new String(baos.toByteArray(), "utf-8");
            return unzipString;
        } finally {
            if (gis != null) {
                gis.close();
            }
            if (baos != null) {
                baos.close();
            }
            if(in!=null)
            {
                in.close();
            }
        }
    }

    private static String readInStreamToString(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        String line;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        while ((line = bufferedReader.readLine()) != null) {
            str.append(line);
            str.append("\r\n");
        }
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        if(in!=null) {
            in.close();
        }
        return str.toString();
    }

    public static String post(DefaultHttpClient httpClient, String url, List<NameValuePair> nameValuePairList) throws UnsupportedEncodingException {
        HttpPost httpGet = new HttpPost (url);
        String result = "";
        httpGet.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, DEFAULT_TARGETHOST);
        StringEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
        httpGet.setEntity(entity);
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                httpGet.abort();
                return result;
            }
            result = readHtmlContentFromEntity(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            httpGet.abort();
            return e.toString();
        } catch (IOException ex) {
            httpGet.abort();
            return ex.toString();
        } finally {
            httpGet.releaseConnection();
        }
        return result;
    }

    public static List<NameValuePair> getParams(Object[] params, Object[] values){
        boolean flag = params.length>0 && values.length>0 &&  params.length == values.length;
        if (flag){
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for(int i =0; i<params.length; i++){
                nameValuePairList.add(new BasicNameValuePair(params[i].toString(),values[i].toString()));
            }
            return nameValuePairList;
        }else{
            System.out.println("请求参数为空且参数长度不一致");
        }
        return null;
    }
}
