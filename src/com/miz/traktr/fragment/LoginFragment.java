package com.miz.traktr.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.miz.traktr.R;
import com.miz.traktr.activity.Main;
import com.miz.traktr.util.Helper;

public class LoginFragment extends Fragment {

	// Views
	private TextView mTitle;
	private EditText mUser, mPass;
	private Button mRegister, mLogin;

	private LoginTask mLoginTask;
	private Typeface mTypeface;

	// Empty constructor as per the API documentation
	public LoginFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

		setRetainInstance(true);

		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), "RobotoCondensed-Regular.ttf");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login, container, false);
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		mTitle = (TextView) v.findViewById(R.id.title);
		mTitle.setTypeface(mTypeface);

		mUser = (EditText) v.findViewById(R.id.username);
		mPass = (EditText) v.findViewById(R.id.password);

		// We want it to use the default typeface but hide the entered password
		mPass.setTypeface(Typeface.DEFAULT);
		mPass.setTransformationMethod(new PasswordTransformationMethod());

		mLogin = (Button) v.findViewById(R.id.login);
		mRegister = (Button) v.findViewById(R.id.register);

		mLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				login();
			}
		});
	}

	/**
	 * Attempt to log in on Trakt.
	 */
	private void login() {
		// We don't want to use the Activity context if it no longer exists
		if (!isAdded())
			return;

		if (mLoginTask != null)
			mLoginTask.cancel(true);

		mLoginTask = new LoginTask(getActivity(), mUser.getText().toString().trim(), mUser.getText().toString().trim());
		mLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void setSigningIn(boolean isSigningIn) {
		if (isSigningIn) {
			mLogin.setEnabled(false);
			mLogin.setText(R.string.signing_in);
			mRegister.setVisibility(View.GONE);
		} else {
			mLogin.setEnabled(true);
			mLogin.setText(R.string.login);	
			mRegister.setVisibility(View.VISIBLE);
		}
	}

	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		private Context mContext;
		private String mUsername, mPassword;

		public LoginTask(Context context, String username, String password) {
			mContext = context;
			mUsername = username;
			mPassword = password;
		}

		@Override
		protected void onPreExecute() {
			setSigningIn(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = Helper.login(mContext, mUsername, mPassword);

			if (isCancelled())
				return false;

			return result;
		}

		@Override
		protected void onPostExecute(Boolean signedIn) {
			// We don't want to use the Activity context if it no longer exists
			if (!isAdded())
				return;
			
			if (signedIn) {
				Intent mainIntent = new Intent(getActivity(), Main.class);
				startActivity(mainIntent);
			} else {
				setSigningIn(false);
			}
		}
	}
}