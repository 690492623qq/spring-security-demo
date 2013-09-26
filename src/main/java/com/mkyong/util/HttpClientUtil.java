package com.mkyong.util;

import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jianyuanyang
 * Date: 13-5-11
 * Time: 下午2:50
 */
public class HttpClientUtil {
	
	public final static String SERVER_ERROR = "-10000";

    private static String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Win32)";

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private static int CONNECT_TIME = 200000 ;

    /**
     * Defines the socket timeout in milliseconds,
     * which is the timeout for waiting for data  or, put differently
     */
    private static int WAIT_DATA_TIME = 200000 ;

    /**
     * 设置 HttpClient 属性 retryHandler,超时时间
     * @return   DefaultHttpClient
     */
    private static DefaultHttpClient getHttpClient(){
        //设置请求超时参数
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIME);
        HttpConnectionParams.setSoTimeout(params, WAIT_DATA_TIME);

        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        //设置重连3次条件
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3,false));

        // 模拟浏览器，解决一些服务器程序只允许浏览器访问的问题
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        httpClient.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, Consts.UTF_8);

        return httpClient ;
    }


    /**
     * 访问https的网站
     * http://www.cnblogs.com/devinzhang/archive/2012/02/28/2371631.html
     * @param httpClient
     */
    private static void enableSSL(DefaultHttpClient httpClient){
        //调用ssl
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { truseAllManager }, null);
            SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme https = new Scheme("https", sf, 443);
            httpClient.getConnectionManager().getSchemeRegistry()
                    .register(https);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重写验证方法，取消检测ssl
     */
    private static TrustManager truseAllManager = new X509TrustManager(){
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    } ;


    /**
     * 获取get 请求返回数据
     * @param getUrl   请求url
     * @return   字符串
     */
    public static String get(String getUrl){
       return httpGetMethod(getUrl);
    }

    /**
     * http post 提交表单
     * @param postUrl  提交url
     * @param nameValuePairList  form数据
     * @return
     */
   public static String post(String postUrl,List<NameValuePair> nameValuePairList){
        return httpPostMethod(postUrl, nameValuePairList,null, false,null);
    }

    /**
     * http post 提交json 数据
     * @param postUrl 提交url
     * @param jsonStr  json 数据格式
     * @return
     */
    public static String postJsonStr(String postUrl,String jsonStr){
        return httpPostMethod(postUrl, null,jsonStr, true,null);
    }

    /**
     * http post 提交form 表单 并且设置 header 参数
     * @param postUrl 请求url
     * @param nameValuePairList form 数据
     * @param headerParams  header 参数
     * @return
     */
    public static String postByHeader(String postUrl,List<NameValuePair> nameValuePairList,Map<String,String> headerParams){
        return httpPostMethod(postUrl, nameValuePairList,null, false,headerParams);
    }


    /**
     * get 方式请求数据
     * @param getUrl  请求url
     * @return 字符串数据
     */
    private static String httpGetMethod(String getUrl){
        DefaultHttpClient defaultHttpClient = getHttpClient();

        //判断是否访问https
        if (getUrl.startsWith("https")){
             enableSSL(defaultHttpClient) ;
        }
        HttpGet httpget = new HttpGet(getUrl);
        return handlerResponse(defaultHttpClient,httpget);

    }
    
    /**
     * 将网络文件下载转换为byte
     * @param url
     * @return
     */
    public static byte [] getByte(String url){  
        try { 
        	DefaultHttpClient httpClient = getHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse ht = httpClient.execute(httpGet);
            
            if(ht.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            	return null;
            
            HttpEntity entity = ht.getEntity();  
            return EntityUtils.toByteArray(entity);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }   
        return null;  
    } 



    /**
     * post 方式请求数据
     * @param postUrl  请求url
     * @param nameValuePairList  参数
     * @param isJsonType  返回数据是否是json格式
     * @return 字符串数据
     */
    private static String httpPostMethod(String postUrl,List<NameValuePair> nameValuePairList,String jsonStr,boolean isJsonType,Map<String,String> headerParams){
        DefaultHttpClient defaultHttpClient = getHttpClient();

        //判断是否访问https
        if (postUrl.startsWith("https")){
            enableSSL(defaultHttpClient) ;
        }

        HttpPost httppost = new HttpPost(postUrl);
          
         //向header 中设置值
        if(null != headerParams && !headerParams.isEmpty()){
        	for(String key : headerParams.keySet()){
            	httppost.setHeader(key, headerParams.get(key)); 
        	}
         }
        //post json 格式数据
        if(isJsonType){
            StringEntity entity = new StringEntity(jsonStr, Consts.UTF_8);
            entity.setContentType("application/json");
            httppost.setEntity(entity);
        }else{//post form 数据
        	  if(null != nameValuePairList){        		  
        		  UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairList, Consts.UTF_8);
        		  httppost.setEntity(entity);
        	   }
        }
        return handlerResponse(defaultHttpClient,httppost);
    }


    /**
     * 处理response 返回数据
     * @param defaultHttpClient  httpClient 对象
     * @param httpRequestBase  请求方式
     * @return 请求响应数据
     */

    private static String handlerResponse(DefaultHttpClient defaultHttpClient,HttpRequestBase httpRequestBase){
        String output = null;
        try {
            HttpResponse response  = defaultHttpClient.execute(httpRequestBase);
            HttpEntity httpEntity = response.getEntity();
            if(null!=httpEntity){
                output = EntityUtils.toString(httpEntity);
            }
        }catch(Exception e){
            e.printStackTrace();
            return SERVER_ERROR ;
        } finally{//释放请求链接
            defaultHttpClient.getConnectionManager().shutdown();
        }
        return output == null ? "":output ;
    }



    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, IOException, KeyStoreException, UnrecoverableKeyException {

        //get
        //String getUrl = "https://api.weibo.com/2/users/show.json" ;

    	/*String getUrl = "http://tao.b5m.com/b5mclist.do" ; 
        String jsonStr1 =  get(getUrl);
        System.out.println("get--json--url--"+jsonStr1);*/

        //post
    	/* String postUrl = "https://api.weibo.com/oauth2/access_token" ;
          List<NameValuePair>  list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("client_id","123"));
        list.add(new BasicNameValuePair("client_secret","123"));
        String jsonStr2 =  post(postUrl, list);
        System.out.println("get--json--url--"+jsonStr2);
        */
        
        String postUrl = "http://tao.b5m.com/b5mglist.do";
        
      /*  {
            "fields": [
                "id",
                "source_url",
                "name",
                "imgurl",
                "source_price",
                "sales_price"
            ],
            "conditions": {
                "isvalid": true,
                "postal": "seller"
            },
            "sortfield": "click",
            "pagesize": 8,
            "pageno": 1
        }*/
        
        Map<String,Object> totalMap = new LinkedHashMap<String,Object>();        
	      
	        
	        Map<String,Object> map = new HashMap<String,Object>();
	        map.put("isvalid", true);
	        map.put("postal", "seller");	        
	         
	       String[] strarray = new String[]{"id","source_url","name","imgurl","source_price","sales_price"};	        
	     
	       
	       totalMap.put("fields", strarray);
	       totalMap.put("conditions", map);
	       totalMap.put("sortfield", "click");
	       totalMap.put("pagesize", 8);
	       totalMap.put("pageno", 1);
	       
	       //System.out.println(JsonUtil.Map2JsonStr(totalMap));
	       
	      // System.out.println(postJsonStr(postUrl,JsonUtil.Map2JsonStr(totalMap)));
         
        
  /*      String postUrl2 ="http://ucenter.stage.bang5mai.com/b5mapi/initInfo.htm";
        
        List<NameValuePair>  list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("appType","4"));
        list.add(new BasicNameValuePair("identifier","25760143477c4738a7fa73d57fe7e6ca"));
        list.add(new BasicNameValuePair("encryCode","eyNRxC7TQ7QlVZ9TAopJL0"));
        list.add(new BasicNameValuePair("email","1234561@qq.com"));
        list.add(new BasicNameValuePair("password","eyNRxC7TQ7QlVZ9TAopJL0"));
        list.add(new BasicNameValuePair("nickName","反对"));
         
        // new NameValuePair
        
        String jsonStr2 =  post(postUrl2, list);
        System.out.println("get--json--url--"+jsonStr2);*/
	       
	       
	       String url = "http://172.16.3.22:8080/taoweb_api/getTradeInfoByUid";
	       Map<String,Object> map2 = new HashMap<String,Object>();
	       map2.put("uid",12);
	       map2.put("pageNo",1);
	       map2.put("pageSize",12);
	       map2.put("sort","record_crt_time");
	       map2.put("sortType","desc");
	       String data = "{uid:12,pageNo:1,pageSize:10,sort=record_crt_time,sortType=desc}";	

	       //String str = postJsonStr(url,JsonUtil.Map2JsonStr(map2));
	       
	       
	   
     
    }



}
