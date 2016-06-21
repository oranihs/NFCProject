package kr.ac.kumoh.s20110057.nfcproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.HttpClientStack;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
	HttpUtil hu;
	EditText idEt;
	EditText pwEt;
	String id;
	String pw;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}
	public void onLoginBtn(View view){
		idEt = (EditText)findViewById(R.id.login_id);
		pwEt = (EditText)findViewById(R.id.login_pw);
		id=idEt.getText().toString();
		pw=pwEt.getText().toString();
		//new HttpConnectionThread().execute();
		//requestHttpGet(idEt.getText().toString(), pwEt.getText().toString());
		//Intent intent=new Intent(this, CheckActivity.class);
		//startActivity(intent);

		hu = new HttpUtil();
		hu.execute();
	}
	public class HttpUtil extends AsyncTask<String, Void, Void> {
		@Override
		public Void doInBackground(String... params) {
			requestHttpGet(id, pw);
			return null;

		}
	}
	public void requestHttpGet(String id, String pw){
		String sURL="http://172.20.10.3:9999/project/";
		StringBuffer sb = new StringBuffer(sURL);
		sb.append("Login?id=" + id + "&pw=" + pw);

		try {
			URL url = new URL(sb.toString());
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
			httpURLConnection.setRequestProperty("Content-Type", "application/json");
			httpURLConnection.setRequestProperty("Accept", "application/json");
			httpURLConnection.setRequestProperty("Accept","*/*");
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);

			int resCode = httpURLConnection.getResponseCode();

			if(resCode == HttpsURLConnection.HTTP_OK){
				InputStream is = httpURLConnection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				String response;
				byte[] byteBuffer = new byte[1024];
				byte[] byteData = null;
				int nLength = 0;
				while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
					baos.write(byteBuffer, 0, nLength);
				}
				byteData = baos.toByteArray();

				response = new String(byteData);

				JSONObject responseJSON = null;
				try {
					responseJSON = new JSONObject(response);
					String result = (String) responseJSON.get("result");
					if(result.equals("true")){
						//로그인 성공
						Intent intent=new Intent(this, CheckActivity.class);
						intent.putExtra("id", id);
						startActivity(intent);
						finish();
					}
					else if(result.equals("false")){
						//로그인 실패
						Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_LONG);
					}
					else if(result.equals("needsSignUp")){
						//needSigniup


					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
