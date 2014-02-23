package com.miz.traktr.fragment;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.miz.traktr.R;
import com.miz.traktr.activity.Main;
import com.miz.traktr.util.Helper;

public class LoginFragment extends Fragment {

	// Views
	private TextView mTitle;
	private EditText mUser, mPass;
	private Button mRegister, mLogin;

	private LoginTask mLoginTask;
	private CreateAccountTask mCreateAccountTask;
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
	public void onResume() {
		super.onResume();

		if (isAdded())
			getActivity().getActionBar().hide();
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

				// The username has to be at least 3 characters and no more than 20 characters in length
				if (!Helper.isValidUsername(mUser.getText().toString())) {
					Toast.makeText(getActivity(), R.string.enter_valid_username, Toast.LENGTH_SHORT).show();
					animateSignInFields(USERNAME);
					return;
				}

				if (mPass.getText().toString().isEmpty()) {
					Toast.makeText(getActivity(), R.string.enter_valid_password, Toast.LENGTH_SHORT).show();
					animateSignInFields(PASSWORD);
					return;
				}

				login();
			}
		});

		mRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showCreateAccountDialog();
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

		if (!Helper.isOnline(getActivity())) {
			Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
			return;
		}

		if (mLoginTask != null)
			mLoginTask.cancel(true);

		mLoginTask = new LoginTask(getActivity(), mUser.getText().toString().trim(), mPass.getText().toString().trim());
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
			mPassword = Helper.SHA1(password);
		}

		@Override
		protected void onPreExecute() {
			setSigningIn(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = Helper.login(mContext, mUsername, mPassword);

			// Check if the task has been cancelled after it finished - if so, don't return the result
			if (isCancelled())
				return false;

			return result;
		}

		@Override
		protected void onPostExecute(Boolean signedIn) {
			// We don't want to use the Activity context if it no longer exists
			if (!isAdded())
				return;

			// Check if the log in succeeded
			if (signedIn) {
				Intent mainIntent = new Intent(getActivity(), Main.class);
				startActivity(mainIntent);
				getActivity().finish();
			} else {
				setSigningIn(false);
				animateSignInFields(BOTH);
				Toast.makeText(mContext, R.string.login_failed, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static final int USERNAME = 0, PASSWORD = 1, BOTH = 2;

	/**
	 * Make a funny side-to-side animation with the text fields if a login fails.
	 * @param type Which field(s) should be animated. Valid parameters are {@link USERNAME}, {@link PASSWORD} and {@link BOTH}.
	 */
	private void animateSignInFields(int type) {
		// Animate the user name field
		ObjectAnimator user = ObjectAnimator.ofFloat(mUser, "translationX", -10f, -5f, 0f, 5f, 10f, 5f, 0f, -5f, -10f, -5f, 0f);
		user.setDuration(300);

		// Animate the password field
		ObjectAnimator pass = ObjectAnimator.ofFloat(mPass, "translationX", -10f, -5f, 0f, 5f, 10f, 5f, 0f, -5f, -10f, -5f, 0f);
		pass.setDuration(300);

		// Start the animations
		switch (type) {
		case USERNAME:
			user.start();
			break;
		case PASSWORD:
			pass.start();
			break;
		default:
			user.start();
			pass.start();
			break;
		}
	}

	private void showCreateAccountDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View createAccountDialogView = inflater.inflate(R.layout.create_account_dialog, null);

		// Set up local variables for the EditText Views
		final EditText username = (EditText) createAccountDialogView.findViewById(R.id.username);
		final EditText password = (EditText) createAccountDialogView.findViewById(R.id.password);
		final EditText email = (EditText) createAccountDialogView.findViewById(R.id.email);
		final EditText confirm = (EditText) createAccountDialogView.findViewById(R.id.confirm_email);

		// Fill out the text fields with the details already entered by the user, if such exist
		username.setText(mUser.getText());
		password.setText(mPass.getText());

		// We want it to use the default typeface but hide the entered password
		password.setTypeface(Typeface.DEFAULT);
		password.setTransformationMethod(new PasswordTransformationMethod());

		// Make it easier for the user by requesting focus in the first empty text field
		if (!username.getText().toString().isEmpty())
			password.requestFocus();

		if (!password.getText().toString().isEmpty())
			email.requestFocus();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setTitle(R.string.register);
		builder.setView(createAccountDialogView)
		// Add action buttons
		.setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				// Check if the user name, password and e-mail are valid
				boolean success = Helper.isValidUsername(username.getText().toString());
				success = success && !password.getText().toString().isEmpty();
				success = success && Helper.isValidEmail(email.getText().toString());
				success = success && email.getText().toString().equals(confirm.getText().toString());

				if (success) {
					mUser.setText(username.getText());
					mPass.setText(password.getText());
					createAccount(username.getText().toString(), password.getText().toString(), email.getText().toString());
				} else {
					Toast.makeText(getActivity(), R.string.check_details, Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	/**
	 * Attempt to create a new account at Trakt.
	 */
	private void createAccount(String username, String password, String email) {
		// We don't want to use the Activity context if it no longer exists
		if (!isAdded())
			return;

		if (!Helper.isOnline(getActivity())) {
			Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
			return;
		}

		if (mCreateAccountTask != null)
			mCreateAccountTask.cancel(true);

		mCreateAccountTask = new CreateAccountTask(getActivity(), username.trim(), password.trim(), email.trim());
		mCreateAccountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

		private Context mContext;
		private String mUsername, mPassword, mEmail;

		public CreateAccountTask(Context context, String username, String password, String email) {
			mContext = context;
			mUsername = username;
			mPassword = Helper.SHA1(password);
			mEmail = email;
		}

		@Override
		protected void onPreExecute() {
			setCreatingAccount(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = Helper.createAccount(mContext, mUsername, mPassword, mEmail);

			// Check if the task has been cancelled after it finished - if so, don't return the result
			if (isCancelled())
				return false;

			return result;
		}

		@Override
		protected void onPostExecute(Boolean signedIn) {
			// We don't want to use the Activity context if it no longer exists
			if (!isAdded())
				return;

			// Check if the log in succeeded
			if (signedIn) {
				Intent mainIntent = new Intent(getActivity(), Main.class);
				startActivity(mainIntent);
				getActivity().finish();
			} else {
				setCreatingAccount(false);
				animateSignInFields(BOTH);
				Toast.makeText(mContext, String.format(getString(R.string.creating_account_failed), mUsername), Toast.LENGTH_LONG).show();
			}
		}
	}

	private void setCreatingAccount(boolean isCreatingAccount) {
		if (isCreatingAccount) {
			mLogin.setEnabled(false);
			mLogin.setText(R.string.creating_account);
			mRegister.setVisibility(View.GONE);
		} else {
			mLogin.setEnabled(true);
			mLogin.setText(R.string.login);	
			mRegister.setVisibility(View.VISIBLE);
		}
	}
}