package edu.temple.messagepush;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class API {
static final String APIBaseURL = "http://kamorris.com/temple/gcmdemo/";

    
    /**
     * Make API call with provided data to specified end point.
     * 
     * @param context Context object
     * @param api End point of API call
     * @param values Key/Value pairs for post data
     * @return Server response
     * @throws ClientProtocolException
     * @throws IOException
     */
    private static String makeAPICall(Context context, RequestMethod requestType, String api, JSONObject values) throws ClientProtocolException, IOException {
    	
    	AndroidHttpClient client = AndroidHttpClient.newInstance("Android", context);
        HttpResponse httpResponse;
        
    	if (requestType == RequestMethod.POST){
	    	HttpPost method = new HttpPost(APIBaseURL + api);
	    	method.addHeader("Accept-Encoding", "gzip");
	    	method.setHeader("Content-type", "application/json");
	    	if (values != null)
	    		method.setEntity(new StringEntity(values.toString()));
	        httpResponse = client.execute(method);
    	} else if (requestType == RequestMethod.PUT) {
    		HttpPut method = new HttpPut(APIBaseURL + api);
	    	method.addHeader("Accept-Encoding", "gzip");
	    	method.setHeader("Content-type", "application/json");
	        if (values != null)
	        	method.setEntity(new StringEntity(values.toString()));
	        httpResponse = client.execute(method);
    	} else if (requestType == RequestMethod.DELETE){
    		HttpDelete method = new HttpDelete(APIBaseURL + api);
    		method.addHeader("Accept-Encoding", "gzip");
    		httpResponse = client.execute(method);
    	} else {
    		HttpGet method = new HttpGet(APIBaseURL + api);
    		method.addHeader("Accept-Encoding", "gzip");
    		httpResponse = client.execute(method);
    	}
        
    	String response = extractHttpResponse(httpResponse);

        Log.i("API Call", requestType + ":" + api);
        if (values != null)
            Log.i("API Parameters", values.toString());

        Log.i("API Response", response.toString());
        client.close();
        return response.toString();
    }

    private static String extractHttpResponse(HttpResponse httpResponse) throws IllegalStateException, IOException{
    	InputStream instream = httpResponse.getEntity().getContent();

        Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
        
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            instream = new GZIPInputStream(instream);
        }
        
        BufferedReader r = new BufferedReader(new InputStreamReader(instream));
        
        StringBuilder response = new StringBuilder();
        String line = "";
        
        while ((line = r.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }
    
    public static boolean registerGCM(Context context, int userId, String regId) throws Exception{
    	
        JSONObject regInfo = new JSONObject();
        regInfo.put("user_id", userId);
        regInfo.put("reg_id", regId);
        
        String response = makeAPICall(context,  RequestMethod.POST, "gcm_register.php", regInfo);
        try {
            JSONObject responseObject = new JSONObject(response);
            if (responseObject.getString("status").equalsIgnoreCase("ok")){
                return true;
            }
        } catch (JSONException e) {
            Log.i("JSON Error in: ", response);
            e.printStackTrace();
        }
        return false;
    }
    
    
    private enum RequestMethod {
    	POST, GET, PUT, DELETE
    }

}
