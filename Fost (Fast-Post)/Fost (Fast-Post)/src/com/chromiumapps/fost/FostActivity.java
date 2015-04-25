package com.chromiumapps.fost;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class FostActivity extends FragmentActivity {
	/** Called when the activity is first created. */

	/*
	 * public boolean Facebook = false; public boolean Twitter = false; public
	 * boolean Googleplus = false; public boolean Blogger = false;
	 */

	private MainFragment mainFragment;
	SharedPreferences pref;

	private static String CONSUMER_KEY = "Omyx5MAWwWDIpSlTlmZEOgbfe";
	private static String CONSUMER_SECRET = "ipe9GCCZvTUoGbcBAoVBtU3jbLz5UNkTchQBSjTbJ8bTQRLTdI";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = getPreferences(0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString("CONSUMER_KEY", CONSUMER_KEY);
		edit.putString("CONSUMER_SECRET", CONSUMER_SECRET);
		edit.commit();

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mainFragment).commit();
			//s = new ShareBarActivity();
			//getSupportFragmentManager().beginTransaction()
					//.add(android.R.id.content, s).commit();
			

		} else {
			// Or set the fragment from restored state info
			/*mainFragment = (MainFragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);*/
			/*linkedInFragment = (LinkedIn_Fragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);
			*/
		}
		setContentView(R.layout.main);
		// buttonHandler();

	}

	/*
	 * public void buttonHandler(){ Button facebookbutton = (Button)
	 * findViewById(R.id.facebookbutton); facebookbutton.setOnClickListener(new
	 * View.OnClickListener() {
	 * 
	 * public void onClick(View v) { // TODO Auto-generated method stub if
	 * (Facebook){ Facebook=false; }
	 * 
	 * 
	 * else{ Facebook=true; }
	 * 
	 * } });
	 * 
	 * Button twitterbutton = (Button) findViewById(R.id.Twitterbutton);
	 * twitterbutton.setOnClickListener(new View.OnClickListener() {
	 * 
	 * public void onClick(View v) { // TODO Auto-generated method stub if
	 * (Twitter){ Twitter=false; } else{ Twitter=true; }
	 * 
	 * 
	 * } });
	 * 
	 * Button bloggerbutton = (Button) findViewById(R.id.Bloggerbutton);
	 * bloggerbutton.setOnClickListener(new View.OnClickListener() {
	 * 
	 * public void onClick(View v) { // TODO Auto-generated method stub if
	 * (Blogger){ Blogger=false; } else{ Blogger=true; }
	 * 
	 * } });
	 * 
	 * Button Googleplusbutton = (Button) findViewById(R.id.Googleplusbutton);
	 * Googleplusbutton.setOnClickListener(new View.OnClickListener() {
	 * 
	 * public void onClick(View v) { // TODO Auto-generated method stub if
	 * (Googleplus){ Googleplus=false; } else{ Googleplus=true; }
	 * 
	 * } });
	 * 
	 * }
	 */

}