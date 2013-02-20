package com.hongle.warmup169ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.gson.Gson;


public class Login extends Activity implements OnClickListener {

    public static final String view = Login.class.getName();

    public AsyncBootLoader postTask = null;
    public String user1, pass1;
    public EditText usernameText, passwordText;
    public TextView result, status;
    public View login, messageText;
    public Button loginButton, adduserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usernameText = (EditText) findViewById(R.id.usernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        result = (TextView) findViewById(R.id.result);
        login = findViewById(R.id.login_form);
        messageText = findViewById(R.id.message);
        status = (TextView) findViewById(R.id.textmessage);
        loginButton = (Button)findViewById(R.id.btnUsername);
        adduserButton = (Button)findViewById(R.id.btnAdd);
        loginButton.setOnClickListener(this);
        adduserButton.setOnClickListener(this);
    }

    //Build from the tutorial from: http://www.mscs.mu.edu/~wmcrae/DB2012/audioLock/src/com/mcrae/audiolock/MasterUnlock.java
   
	@SuppressWarnings("null")
	public void attemptLogin(Boolean register) {
        if (postTask != null) {
            return;
        }
        usernameText.setError(null);
        passwordText.setError(null);
        user1 = usernameText.getText().toString();
        pass1 = passwordText.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (cancel) {
            focusView.requestFocus();
        } else {
            status.setText(R.string.loginCount);
            showProgress(true);
            postTask = new AsyncBootLoader();
            postTask.execute(register);
        }
    }

    public class Request {
        public String user;
        public String password;
    }
    
    public class Response {
        public int errCode;
        public int count;
    }
    /**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        	
        } 
    }

    public Response doPost(String resource, Request req) {
    	String url = "http://stark-dawn-5883.herokuapp.com";
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url + resource);
        Gson tojson = new Gson();
        String jsonRequest = tojson.toJson(req);
        try {
        	try{
        		httpPost.setEntity(new StringEntity(jsonRequest.toString()));
        	}catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader rd = new BufferedReader(new InputStreamReader(content));
               return tojson.fromJson(rd, Response.class);
            } else{
            	return null;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    

    public class AsyncBootLoader extends AsyncTask<Boolean, Void, Response> {
        @Override
        protected Response doInBackground(Boolean... url) {
            assert url.length == 1;	
			String endpoint = url[0] ? "add" : "login";
			Request req = new Request();
			req.user = user1;
			req.password = pass1;
			return doPost("/users/" + endpoint, req);
            
        }

        @Override
        protected void onPostExecute(final Response resp) {
            postTask = null;
            showProgress(false);
            result.setText(R.string.result);
        	int response = resp.errCode;
        	if (response == 1){
            	String output = String.format(getString(R.string.result_success), 
                        user1, resp.count);
                result.setText(output);
            }
            else if(response ==-1){
            	usernameText.setError("Error! Cannot find username/password in database");
                usernameText.requestFocus();
            }
            else if (response == -2){
            	usernameText.setError("Error! Username is taken");
                usernameText.requestFocus();
            }
            else if (response == -3){
            	usernameText.setError("Error! Bad Syntax for Username");
                usernameText.requestFocus();
            }
            else if(response == -4){
            	passwordText.setError("Error! Bad Syntax for Password");
                passwordText.requestFocus();
            }
            else{
            	passwordText.setError("Error!!!! Please check your conntection");
                passwordText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            postTask = null;
            showProgress(false);
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.loginmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == loginButton){
			attemptLogin(false);
		}else if( v == adduserButton){
			attemptLogin(true);
		}
	}
}
