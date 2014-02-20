package com.miz.traktr.fragment;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.miz.traktr.R;

public class LoginFragment extends Fragment {

	// Views
	private TextView mTitle;
	private EditText mUser, mPass;
	private Button mRegister, mLogin;

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

	private void login() {

	}
}