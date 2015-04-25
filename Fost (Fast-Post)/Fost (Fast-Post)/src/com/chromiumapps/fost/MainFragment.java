package com.chromiumapps.fost;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.chromiumapps.fost.R;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chromiumapps.fost.LinkedinDialog.OnVerifyListener;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.support.v4.app.Fragment;
public class MainFragment extends Fragment {
	public Button shareButton;

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private static final String TAG = "MainFragment";
	private Session.StatusCallback callback = new Session.StatusCallback() {

		public void call(Session session, SessionState state,
				Exception exception) {
			// TODO Auto-generated method stub
			onSessionStateChange(session, state, exception);
		}
	};

	private UiLifecycleHelper uiHelper;
	EditText message;
	String m;

	Button login, signout;
	Twitter twitter;
	RequestToken requestToken = null;
	AccessToken accessToken;
	String oauth_url, oauth_verifier, profile_url;
	Dialog auth_dialog;
	WebView web;
	SharedPreferences pref;
	ProgressDialog progress;
	Bitmap bitmap;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.main, container, false);
		LoginButton authbutton = (LoginButton) view
				.findViewById(R.id.loginbutton);
		authbutton.setFragment(this);
		login = (Button) view.findViewById(R.id.login);
		pref = getActivity().getPreferences(0);
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(pref.getString("CONSUMER_KEY", ""),
				pref.getString("CONSUMER_SECRET", ""));

		login.setOnClickListener(new LoginProcess());
		signout = (Button) view.findViewById(R.id.signout);
		signout.setOnClickListener(new SignOut());
		shareButton = (Button) view.findViewById(R.id.shareButton);
		linkedInLogin = (Button) view.findViewById(R.id.linkedInLogin);
		linkedInLogout = (Button) view.findViewById(R.id.linkedInLogout);
		linkedInLogout.setOnClickListener(new LinkedInSignOut());
		linkedInLogin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				linkedInlinkedInLogin();
			}
		});
		shareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				message = (EditText) view.findViewById(R.id.message);
				m = message.getText().toString();
				publishStory();
				new PostTweet().execute();
				message.setText("");
				linkedInPost();
			}
		});
		if (savedInstanceState != null) {
			pendingPublishReauthorization = savedInstanceState.getBoolean(
					PENDING_PUBLISH_KEY, false);
		}
		/////////////LinkedIn////////////
		if( Build.VERSION.SDK_INT >= 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy); 
		}
		//share = (Button) findViewById(R.id.share);
		//et = (EditText) findViewById(R.id.et_share);
		

		
		

			
			
				

		return view;
	}

	private class SignOut implements OnClickListener {// Twitter

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			SharedPreferences.Editor edit = pref.edit();
			edit.putString("ACCESS_TOKEN", "");
			edit.putString("ACCESS_TOKEN_SECRET", "");
			edit.putString("CALLBACK_URL", "api.twitter.com/oauth/authorize?force_login=true");
     		edit.apply();
			edit.commit();	
			
			//Intent intent = new Intent(getActivity(), FostActivity.class);
			//startActivity(intent);


			CookieSyncManager.createInstance(getActivity());
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeSessionCookie();
			edit.remove(OAuth.OAUTH_TOKEN);
			twitter.setOAuthAccessToken(null);
			//twitter= new TwitterFactory().getInstance();
		    //TwitterFactory().getInstance().shutdown();
			signout.setVisibility(View.GONE);
			login.setVisibility(View.VISIBLE);

			Toast.makeText(getActivity(), "Signout Succesful!",
					Toast.LENGTH_SHORT).show();

		}
	}

	private class PostTweet extends AsyncTask<String, String, String> {// Twitter
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(getActivity());
			progress.setMessage("Processing...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(true);
			progress.show();
			m = message.getText().toString();
		}

		@Override
		protected String doInBackground(String... args) {

			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
			builder.setOAuthConsumerSecret(pref
					.getString("CONSUMER_SECRET", ""));

			AccessToken accessToken = new AccessToken(pref.getString(
					"ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET",
					""));
			Twitter twitter = new TwitterFactory(builder.build())
					.getInstance(accessToken);

			try {
				twitter4j.Status response = twitter.updateStatus(m);
				return response.toString();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String res) {// Twitter
			if (res != null) {
				progress.dismiss();
				Toast.makeText(getActivity(), "Post Succesful!",
						Toast.LENGTH_SHORT).show();
			} else {
				progress.dismiss();
				Toast.makeText(getActivity(), "Error while tweeting !",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class LoginProcess implements OnClickListener {// Twitter
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			new TokenGet().execute();
			
		}
	}

	private class TokenGet extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {

			try {
				requestToken = twitter.getOAuthRequestToken();
				oauth_url = requestToken.getAuthorizationURL();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return oauth_url;
		}

		@Override
		protected void onPostExecute(String oauth_url) {
			if (oauth_url != null) {
				Log.e("URL", oauth_url);
				auth_dialog = new Dialog(getActivity());
				auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				auth_dialog.setContentView(R.layout.auth_dialog);
				web = (WebView) auth_dialog.findViewById(R.id.webv);
				web.getSettings().setJavaScriptEnabled(true);
				web.loadUrl(oauth_url);
				web.setWebViewClient(new WebViewClient() {
					boolean authComplete = false;

					@Override
					public void onPageStarted(WebView view, String url,
							Bitmap favicon) {
						super.onPageStarted(view, url, favicon);

					}

					@Override
					public void onPageFinished(WebView view, String url) {

						super.onPageFinished(view, url);
						if (url.contains("oauth_verifier")
								&& authComplete == false) {
							authComplete = true;
							Log.e("Url", url);
							Uri uri = Uri.parse(url);
							oauth_verifier = uri
									.getQueryParameter("oauth_verifier");

							auth_dialog.dismiss();
							login.setVisibility(View.GONE);
							signout.setVisibility(View.VISIBLE);
							new AccessTokenGet().execute();
						} else if (url.contains("denied")) {
							auth_dialog.dismiss();
							Toast.makeText(getActivity(),
									"Sorry !, Permission Denied",
									Toast.LENGTH_SHORT).show();

						}
					}
				});
				auth_dialog.show();
				auth_dialog.setCancelable(true);

			} else {

				Toast.makeText(getActivity(),
						"Sorry !, Network Error or Invalid Credentials",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class AccessTokenGet extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {// Twitter
			super.onPreExecute();
			progress = new ProgressDialog(getActivity());
			progress.setMessage("Fetching Data ...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(true);
			progress.show();

		}

		@Override
		protected Boolean doInBackground(String... args) {// Twitter

			try {

				accessToken = twitter.getOAuthAccessToken(requestToken,
						oauth_verifier);
				SharedPreferences.Editor edit = pref.edit();
				edit.putString("ACCESS_TOKEN", accessToken.getToken());
				edit.putString("ACCESS_TOKEN_SECRET",
						accessToken.getTokenSecret());
				edit.commit();

			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean response) {// Twitter
			if (response) {
				progress.hide();
				Toast.makeText(getActivity().getApplicationContext(),
						"Login Succesful!", Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
		uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception e) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in..");
			shareButton.setVisibility(View.VISIBLE);
			if (pendingPublishReauthorization
					&& state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				pendingPublishReauthorization = false;
				publishStory();
			}
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged off....");
			shareButton.setVisibility(View.INVISIBLE);
		}
	}

	public void publishStory() {
		Session session = Session.getActiveSession();

		if (session != null) {

			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					String postId = null;
					try {
						postId = graphResponse.getString("id");
					} catch (JSONException e) {
						Log.i(TAG, "JSON error " + e.getMessage());
					}
					FacebookRequestError error = response.getError();
					if (error != null) {
						Toast.makeText(getActivity().getApplicationContext(),
								error.getErrorMessage(), Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(getActivity().getApplicationContext(),
								"Post Sucessful!", Toast.LENGTH_LONG).show();
					}
				}
			};

			Request request = Request.newStatusUpdateRequest(session, m,
					callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}

	private boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
	
	Button linkedInLogin;
	Button linkedInLogout;
	Button share;
	EditText et;
	TextView name;
	ImageView photo;
	public static final String OAUTH_CALLBACK_HOST = "https://twitter.com/InsaneZain10";

	final LinkedInOAuthService oAuthService = LinkedInOAuthServiceFactory
            .getInstance().createLinkedInOAuthService(
                    Config.LINKEDIN_CONSUMER_KEY,Config.LINKEDIN_CONSUMER_SECRET, Config.scopeParams);
	final LinkedInApiClientFactory factory = LinkedInApiClientFactory
			.newInstance(Config.LINKEDIN_CONSUMER_KEY,
					Config.LINKEDIN_CONSUMER_SECRET);
	LinkedInRequestToken liToken;
	LinkedInApiClient client;
	LinkedInAccessToken accessTokenLinkedIn = null;

	

	private void linkedInlinkedInLogin() {
		ProgressDialog progressDialog = new ProgressDialog(
				getActivity());

		LinkedinDialog d = new LinkedinDialog(getActivity()
				,progressDialog);
		d.show();

		// set call back listener to get oauth_verifier value
		d.setVerifierListener(new OnVerifyListener() {
			
			public void onVerify(String verifier) {
				try {
					Log.i("LinkedinSample", "verifier: " + verifier);

					accessTokenLinkedIn = LinkedinDialog.oAuthService
							.getOAuthAccessToken(LinkedinDialog.liToken,
									verifier);
					LinkedinDialog.factory.createLinkedInApiClient(accessTokenLinkedIn);
					client = factory.createLinkedInApiClient(accessTokenLinkedIn);
					Log.i("LinkedinSample",
							"ln_access_token: " + accessTokenLinkedIn.getToken());
					Log.i("LinkedinSample",
							"ln_access_token: " + accessTokenLinkedIn.getTokenSecret());
					Person p = client.getProfileForCurrentUser();
					linkedInLogout.setVisibility(View.VISIBLE);
					linkedInLogin.setVisibility(View.GONE);

				} catch (Exception e) {
					Log.i("LinkedinSample", "error to get verifier");
					e.printStackTrace();
				}
			}
		});
		progressDialog.setMessage("Loading...");
		progressDialog.setCancelable(true);
		progressDialog.show();
		
	}
	public void linkedInPost(){
		if (null != m && !m.equalsIgnoreCase("")) {
			OAuthConsumer consumer = new CommonsHttpOAuthConsumer(Config.LINKEDIN_CONSUMER_KEY, Config.LINKEDIN_CONSUMER_SECRET);
		    consumer.setTokenWithSecret(accessTokenLinkedIn.getToken(), accessTokenLinkedIn.getTokenSecret());
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("https://api.linkedin.com/v1/people/~/shares");
			try {
				consumer.sign(post);
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // here need the consumer for sign in for post the share
			post.setHeader("content-type", "text/XML");
			String myEntity = "<share><comment>"+ m +"</comment><visibility><code>anyone</code></visibility></share>";
			try {
				post.setEntity(new StringEntity(myEntity));
				org.apache.http.HttpResponse response = httpclient.execute(post);
				Toast.makeText(getActivity(),
						"Shared sucessfully", Toast.LENGTH_SHORT).show();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			Toast.makeText(getActivity(),
					"Please enter the text to share",
					Toast.LENGTH_SHORT).show();
		}
		
		/*String share = et.getText().toString();
		if (null != share && !share.equalsIgnoreCase("")) {
			client = factory.createLinkedInApiClient(accessToken);
			client.postNetworkUpdate(share);
			et.setText("");
			Toast.makeText(LinkedInSampleActivity.this,
					"Shared sucessfully", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(LinkedInSampleActivity.this,
					"Please enter the text to share",
					Toast.LENGTH_SHORT).show();
		}*/
	}
	
	private class LinkedInSignOut implements OnClickListener {

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			accessTokenLinkedIn.setToken("");
			accessTokenLinkedIn.setTokenSecret("");			
			//Intent intent = new Intent(getActivity(), FostActivity.class);
			//startActivity(intent);
			linkedInLogout.setVisibility(View.GONE);
			linkedInLogin.setVisibility(View.VISIBLE);

			Toast.makeText(getActivity(), "Signout Succesful!",
					Toast.LENGTH_SHORT).show();

		}
	}
	///////////////////////////
	

}
