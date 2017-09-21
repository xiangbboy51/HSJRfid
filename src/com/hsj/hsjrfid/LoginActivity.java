package com.hsj.hsjrfid;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.hsj.webservice.WebserviceUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity  {
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private TextView mLinkPwd;
    private EditText mPhone;
    private EditText mPwd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		getActionBar().hide();
		//初始化控件
		initView();	
	
		
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//调用webservice
		/*WebserviceUtil.getService("TranslatorString","google");*/
		
		
		
	
		//点击登录按钮
		mLoginBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//判断账号和密码是否为空
				if(validateLogin()){
				//跳转页面
					if (mPhone.getText().toString().trim().equals("admin")
							&&mPwd.getText().toString().trim().equals("123456")) {
						Intent intent=new Intent(LoginActivity.this,MenuManagerActivity.class);
						startActivity(intent);
						finish();
					}else{
						showDialog("密码账号输入错误!");	
					}
					
				}else{
				//弹出一个提示框
				showDialog("密码账号不能为空!");	
				}
				
				
			}
		});
		
		
		
		
		
	}
	
	
	/**
	 * 初始化控件
	 */
	private void initView() {
		mLoginBtn=(Button)findViewById(R.id.login_btn);
		mRegisterBtn=(Button)findViewById(R.id.register_btn);
		mLinkPwd=(TextView)findViewById(R.id.linkPwd);
		mPhone=(EditText)findViewById(R.id.login_phone);
		mPwd=(EditText)findViewById(R.id.login_password);
		
	}
	
	/**
	 * 登录的账号密码跳转的验证
	 */
	private boolean validateLogin(){
		if(TextUtils.isEmpty(mPhone.getText().toString())||
				TextUtils.isEmpty(mPwd.getText().toString())){
			return false;
		}
		return true;
	}
	/**
	 * 显示一个提示框
	 */
    private void showDialog(String text){
    	LayoutInflater layoutInflater=LayoutInflater.from(this);
    	View view=layoutInflater.inflate(R.layout.message, null);
    	TextView textView=(TextView)(view.findViewById(R.id.showDialog));
    	textView.setText(text);
    	Dialog dialog=new AlertDialog.Builder(this)
    	.setTitle("hsjPDA")
    	.setIcon(R.drawable.background)
    	.setView(view)
    	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).create();
        
    	dialog.show();
    }
  
    
    
   
    
}
