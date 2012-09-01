package se.serp.LiULunch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;

class ReadContentException extends Exception {
	private static final long serialVersionUID = 2087061907834635859L;
	
	String text;
	
	public ReadContentException(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return this.text;
	}
}

class GetContentResult {
	public String content;
	public String failReason;
	
	public GetContentResult(boolean success, String s) {
		if (success) {
			this.content = s;
			this.failReason = null;
		}
		else {
			this.content = null;
			this.failReason = s;
		}
	}
}

class ContentGetter extends AsyncTask<String, Void, GetContentResult> {
	// Kodningsproblem?
	// testa https://github.com/raek/utf8-with-fallback
	public static String getContent(String url) throws ReadContentException {
		HttpGet httpGet = new HttpGet(url);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 5000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpResponse response;
		try
		{
			response = httpClient.execute(httpGet);
		}
		catch (ClientProtocolException e)
		{
			throw new ReadContentException("HTTP-fel.");
		}
		catch (IOException e)
		{
			throw new ReadContentException("Det gick inte att ansluta till meny-servern.");
		}

		// slurp
		BufferedReader r;
		try
		{
			r = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		}
		catch (IOException e)
		{
			throw new ReadContentException("\"if the stream could not be created\"");
		}
		catch (IllegalStateException e)
		{
			throw new ReadContentException("\"if this entity is not repeatable and the stream has already been obtained previously\"");
		}
		StringBuilder total = new StringBuilder();
		String line;
		try
		{
			while ((line = r.readLine()) != null) {
			    total.append(line);
			}
		}
		catch (IOException e)
		{
			throw new ReadContentException("\"if this reader is closed or some other I/O error occurs.\"");
		}
		return total.toString();
	}

	protected GetContentResult doInBackground(String... params) {
		String url = params[0];
		try
		{
			String content = getContent(url);
			return new GetContentResult(true, content);
		}
		catch(ReadContentException e)
		{
			return new GetContentResult(false, "Fel vid hämtning: " + e.toString());
		}
	}
}
