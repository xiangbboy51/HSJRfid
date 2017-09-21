package com.hsj.hsjrfid;
import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.handle.IMessageNotificationReceivedHandle;
import invengo.javaapi.protocol.IRP1.IntegrateReaderManager;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.Reader;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import android.R.anim;
import android.R.integer;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.hsj.entity.EntityTag;
import com.hsj.entity.MyMessage;
import com.hsj.entity.OrderList;
import com.hsj.entity.TagList;
import com.hsj.sqlite.ServiceManger;
import com.hsj.webservice.WebserviceUtil;
import com.invengo.sample.EPCEntity;
import com.invengo.lib.diagnostics.InvengoLog;

public class MainActivity extends Activity implements
	IMessageNotificationReceivedHandle {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Button connectBtn;
	private Button disconnectBtn;
	private Button readBtn;
	private Button queryBtn;
	private Button stopBtn;
	private Button clearBtn;
	private Reader reader;
	private ListView mEpcListView;
	private EPCEntityAdapter mListAdapter;
	private List<EPCEntity> mEPCEntityList = new ArrayList<EPCEntity>();
	private View mConnectStatusView;	
	private MainActivity mainActivity;
	private EditText mIpText;
	private EditText mPortText;
	private boolean isReading;
	private boolean isConnected;
	private ConnectReaderTask mConnectTask;
	private ActionBar mActionBar;
	private TextView mSum;
	private ServiceManger manager;
	private Button uploadBtn;
	private TextView tvTitle;
	private Spinner mSpinner;
	List<String>  data_list = new ArrayList<String>();
	private String OrderNO=null;
	private Timer timer=new Timer();
	private MediaPlayer media;
	private TextView pandian;
	private int size;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar=getActionBar();
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		
		
		//设置关于actionbar的一些属性值
		 int titleId = Resources.getSystem().getIdentifier("action_bar_title",
		            "id", "android");	
		    tvTitle = (TextView) findViewById(titleId);
		    tvTitle.setTextColor(0xFFFFFFFF);
		    tvTitle.setTextSize(20);
		    tvTitle.setGravity(Gravity.CENTER);
		    mActionBar.setTitle("盘点");	 
		 
		setContentView(R.layout.activity_main);	
		//注册eventbus
		EventBus.getDefault().register(this);
		mainActivity = this;
		
		
		mIpText = (EditText) findViewById(R.id.ipText);
		mPortText = (EditText) findViewById(R.id.portText);
		connectBtn = (Button) findViewById(R.id.connectBtn);
		mConnectStatusView = findViewById(R.id.connect_status);
		mSum=(TextView)findViewById(R.id.sum);
		uploadBtn=(Button)findViewById(R.id.upload);
		mSpinner=(Spinner)findViewById(R.id.spinner);
		pandian=(TextView)findViewById(R.id.pandian);
		//此此处调用webservice拉取数据
		WebserviceUtil.getService2("GetInventoryList");        
		//一进来就要显示进度条
		showProgress(true);
		//构造数据库上传
		manager=new ServiceManger(this);
		
		uploadBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//上传数据库的标签到webservice上去
				new Thread(){
					public void run() {
						//调用webservice 
						
						List<EntityTag> tags=manager.queryEPCEntity(OrderNO);
						String[] tagArray=new String[tags.size()];
						for(int i=0;i<tags.size();i++){
							tagArray[i]=tags.get(i).getEpcData();
						}
						TagList tagList=new TagList();
						if(!TextUtils.isEmpty(OrderNO)){
							tagList.setOrderNO(OrderNO);
							tagList.setTags(tagArray);
							//获取数组之后调用webservice
							//先转化为json字符串
							Gson gson=new Gson();
							String jsonStr=gson.toJson(tagList);
							System.out.println(jsonStr);	
							WebserviceUtil.getService("SaveInventoryData", jsonStr);
							//上传服务器之后删掉对应工单的数据
							manager.delEPCEntity(OrderNO);
							
						}
						
					
					};
					
				}.start();
				
				
			}
		});
		
		
		connectBtn.setOnClickListener(new OnClickListener() {
		
			public void onClick(View v) {
//				if (TextUtils.isEmpty(mIpText.getText())) {
//					showToast("请输入IP地址");
//					return;
//				}
//				if (TextUtils.isEmpty(mPortText.getText())) {
//					showToast("请输入端口");
//					return;
//				}

				String address = mIpText.getText() + "," + mPortText.getText();
				connectReader(address);
			}
		});
		
		disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
		disconnectBtn.setEnabled(true);
		disconnectBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new Thread(new Runnable(){

					@Override
					public void run() {
						if (isConnected) {
							isReading = false;
							reader.send(new PowerOff());
							reader.disConnect();
							Message disconnectMsg = new Message();
							disconnectMsg.what = DISCONNECT;
							cardOperationHandler.sendMessage(disconnectMsg);
						}
					}
					
				}).start();
			}
		});
		readBtn = (Button) findViewById(R.id.readBtn);
		readBtn.setEnabled(false);
		readBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//调用定时器
				
				
				new Thread(new Runnable() {
					public void run() {
						if(isConnected){
							isReading = true;
							ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_6C);
							boolean result = reader.send(readTag);
							Message readMessage = new Message();
							readMessage.what = START_READ;
							readMessage.obj = result;
							cardOperationHandler.sendMessage(readMessage);
						}
					}
				}).start();
			}
		});
		queryBtn = (Button) findViewById(R.id.queryButton);
		queryBtn.setEnabled(true);
		queryBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
			SysQuery_800 msg = new SysQuery_800((byte) 0x21);
				reader.send(msg);
				showToast(new String(msg.getReceivedMessage().getQueryData()));
			}
		});
		stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setEnabled(false);
		stopBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(isConnected){
							isReading = false;
							boolean result = reader.send(new PowerOff());
							Message powerOffMsg = new Message();
							powerOffMsg.what = STOP_READ;
							powerOffMsg.obj = result;
							cardOperationHandler.sendMessage(powerOffMsg);
						}
					}
				}).start();
			}
		});
		clearBtn = (Button) findViewById(R.id.clearButton);
		clearBtn.setEnabled(false);
		clearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEPCEntityList.clear();
				mListAdapter.notifyDataSetChanged();
			}
		});
		mListAdapter = new EPCEntityAdapter(this, R.layout.listview_epc_off_rssi_item, mEPCEntityList);
		mEpcListView = (ListView) findViewById(R.id.dataSet);
		
		mEpcListView.setAdapter(mListAdapter);
	}
	//检查网络状态
	public boolean checkNetState(Context context) {
		ConnectivityManager netManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(null == netManager){
			return false;
		}
		NetworkInfo activeNetworkInfo = netManager.getActiveNetworkInfo();
		if(null == activeNetworkInfo){
			return false;
		}
		return activeNetworkInfo.isConnected();
	}

	private boolean isRun = true;
	private void disconnect() {
		isRun = false;
		connectBtn.setEnabled(true);
		disconnectBtn.setEnabled(false);
		readBtn.setEnabled(false);
		queryBtn.setEnabled(false);
		stopBtn.setEnabled(false);
		clearBtn.setEnabled(false);
	}
   //消息的回调函数
	@Override
	public void messageNotificationReceivedHandle(BaseReader reader,
			IMessageNotification msg) {
		if(isReading){
			if(isConnected){
				if (msg instanceof RXD_TagData) {
					//进入读标签
					//发通知播放声音的handler
					//
					//cardOperationHandler.sendEmptyMessage(PLAY_SOUND);
					
					
						 media.start();
					 
					
					
					RXD_TagData data = (RXD_TagData) msg;
					String epc = convertByteArrayToHexString(data.getReceivedMessage().getEPC());					
					boolean isExists = false;
					//如果集合不为空才进行下面的操作
					if(mEPCEntityList!=null){
						for(int i=0;i<mEPCEntityList.size();i++){
							String old = mEPCEntityList.get(i).getEpcData();
							if(epc.equals(old)){
								isExists = true;
								int oldNumber = mEPCEntityList.get(i).getNumber();
								mEPCEntityList.get(i).setNumber(oldNumber + 1);
								break;
							}
						}
					}
					
					
					if(isExists){
						//do nothing  扫描到相同的标签
					}else{
						EntityTag mTag = new EntityTag();
						EPCEntity newEntity=new EPCEntity();
						newEntity.setNumber(1);
					
						mTag.setNumber(OrderNO);
						if (epc.length()!=8) {
							 media=MediaPlayer.create(MainActivity.this, R.raw.error);
							 media.start();
						}else{
							mTag.setEpcData(epc);
							newEntity.setEpcData(epc);
							mEPCEntityList.add(newEntity);
							cardOperationHandler.sendEmptyMessage(READ_TAG_SUM);	
						}
						
						//先清空数据
						
						//每扫描到一个标签上传到服务器上去
						if(manager.queryEPCbyid(epc)){
							manager.addEPCEntity(mTag);
							size++;
						}
						
						
					}
					
					/*Message dataArrivedMsg = new Message();
					dataArrivedMsg.what = DATA_ARRIVED;
					cardOperationHandler.sendMessage(dataArrivedMsg);*/
				}
			}
		}
	}

	private boolean backDown;
	private long firstTime = 0;

	//设置pda的按键读写能力
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		InvengoLog.i(TAG, "INFO.onKeyDown().");
		if (keyCode == KeyEvent.KEYCODE_BACK && !backDown) {
			backDown = true;
		}
		//设置按钮按一次有效
		else if((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SOFT_RIGHT)
				&& event.getRepeatCount() <= 0
				&& isConnected){
			InvengoLog.i(TAG, "INFO.Start/Stop read tag.");	
			if(isReading == false){
				isReading = true;
				ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_6C);
				boolean result = reader.send(readTag);
				Message readMessage = new Message();
				readMessage.what = START_READ;
				readMessage.obj = result;
				cardOperationHandler.sendMessage(readMessage);
			}else if(isReading == true){
				isReading = false;
				boolean result = reader.send(new PowerOff());
				Message powerOffMsg = new Message();
				powerOffMsg.what = STOP_READ;
				powerOffMsg.obj = result;
				cardOperationHandler.sendMessage(powerOffMsg);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
	//双击退出事件
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && backDown) {
			backDown = false;
			long currentTime = System.currentTimeMillis();
			if (currentTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
				showToast("再按一次 退出程序");
				firstTime = currentTime;// 更新firstTime
				return true;
			} else { // 两次按键小于2秒时，退出应用
				finish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIpText.setText(IntegrateReaderManager.getPortName());
		media=MediaPlayer.create(MainActivity.this, R.raw.success);
		if(checkNetState(this)){
			showToast("有网络");
		}else{
			showToast("无网络");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		if(reader!=null){
			reader.disConnect();
		}
		if (media.isPlaying()) {
			media.stop();
			
			media.release();
			
		}
			
		timer.cancel();
		
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(reader!=null){
			reader.disConnect();
		}
		
		timer.cancel();
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void connectReader(String address) {
		mConnectTask = new ConnectReaderTask();
		mConnectTask.execute(address);
//		new Thread(new ConnectRunnable(address)).start();
	}
	
	private static final int START_READ = 0;
	private static final int STOP_READ = 1;
	private static final int DATA_ARRIVED = 2;
	private static final int DISCONNECT = 3;
	private static final int READ_TAG_SUM=4;
	private static final int PLAY_SOUND=5;
	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case START_READ://开始读卡
				boolean start = (Boolean) msg.obj;
				if (start) {
					showToast("发送读卡指令成功");
					readBtn.setEnabled(false);
					clearBtn.setEnabled(false);
					startListnerListView();//开始进行进行定时器操作
					
				} else {
					showToast("发送读卡指令失败");
				}
				break;
			case STOP_READ://停止读卡
				boolean stop = (Boolean) msg.obj;
				if (stop) {
					showToast("停止读卡成功");
					readBtn.setEnabled(true);
					clearBtn.setEnabled(true);
				} else {
					showToast("停止读卡失败");
				}
				break;
			case DATA_ARRIVED://接收数据
				
			
				EPCEntity entity;
				
				 List<EPCEntity> myDatas=new ArrayList<EPCEntity>();
				  //先清空数据
				  myDatas.clear();
				//动态的跟新数据
				 if(mEPCEntityList.size()>10){	
					 
				  for(int i=mEPCEntityList.size();i>mEPCEntityList.size()-10;i--){
				
					  myDatas.add(mEPCEntityList.get(i-1));
				  }	  
				  mListAdapter.updataListView(myDatas);
				  } 
				 
				 else{				  
				  mListAdapter.updataListView(mEPCEntityList);
				  }
				//如果数据大于10条多余的不显示
				/*if(mEPCEntityList.size()>10){
					mEPCEntityList.get(location)
				}*/
				//((EPCEntityAdapter)mEpcListView.getAdapter()).notifyDataSetChanged();
				//startListnerListView();
				break;
			case DISCONNECT://断开读写器连接
				disconnect();
				isConnected = false;
				showToast("断开连接成功");
				break;
			case READ_TAG_SUM://扫描到的数量
				mSum.setText("扫描的数量:"+mEPCEntityList.size());
				pandian.setText("当前"+OrderNO+"工单盘点数量:"+size);
			case PLAY_SOUND://播放声音
				
			 /*media=MediaPlayer.create(MainActivity.this, R.raw.success);
			 media.start();*/
			default:
				break;
			}
		};
	};
	
	private class ConnectReaderTask extends AsyncTask<String, Void, Boolean>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//showProgress(true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String address = params[0];
			reader = new Reader("Reader1", "RS232", address);
			
			if (reader.connect()) {
				reader.onMessageNotificationReceived.add(mainActivity);
				isConnected = true;
			} else {
				isConnected = false;
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mConnectTask = null;
			showProgress(false);
			if(result){
				connectBtn.setEnabled(false);
				disconnectBtn.setEnabled(true);
				readBtn.setEnabled(true);
				stopBtn.setEnabled(true);
				clearBtn.setEnabled(true);
				showToast("连接成功");
				
			}else{
				connectBtn.setEnabled(true);
				disconnectBtn.setEnabled(false);
				readBtn.setEnabled(false);
				stopBtn.setEnabled(false);
				clearBtn.setEnabled(false);
				showToast("连接失败");
			}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mConnectTask = null;
			showProgress(false);
		}
	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void showProgress(final boolean show){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mConnectStatusView.setVisibility(View.VISIBLE);
			mConnectStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mConnectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});
		} else {
			mConnectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	private class EPCEntityAdapter extends BaseAdapter{
        private List<EPCEntity> datas;
		private int resourceId;
		private Context mContext;
		
		
		public EPCEntityAdapter(Context contex,int resourceId,List<EPCEntity> datas) {
			super();
			this.datas = datas;
			this.resourceId = resourceId;
			this.mContext=contex;
		}

		//更新界面
		public void updataListView(List<EPCEntity> datas){
			EPCEntityAdapter.this.datas=datas;
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EPCEntity entity = getItem(position);
			ViewHolder holder;
			if(null == convertView){
				convertView = LayoutInflater.from(parent.getContext()).inflate(resourceId, null);
				holder = new ViewHolder();
				holder.number = (TextView) convertView.findViewById(R.id.epc_off_rssi_times);
				holder.epcData = (TextView) convertView.findViewById(R.id.epc_off_rssi_data);
				convertView.setTag(holder);
			}else{
				
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.number.setText(String.valueOf(entity.getNumber()));
			holder.epcData.setText(entity.getEpcData());
			
			return convertView;
		}
		
		class ViewHolder{
			TextView number;
			TextView epcData;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.size();
		}

		@Override
		public EPCEntity getItem(int position) {
			// TODO Auto-generated method stub
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	}
	//此注释表示不推荐使用此种方式
	@Deprecated
	private class ConnectRunnable implements Runnable{
		
		private String hostName;
		private int port;
		private Socket client;
		private int timeout = 1000;
		private OutputStream writer;
		private InputStream reader;
		private byte[] inputMsg = new byte[1024];
		public ConnectRunnable(String address) {
			this.hostName = address.substring(0, address.indexOf(':'));
			this.port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
		}
		
		@Override
		public void run() {
			
			isRun = true;
			try {
				client = new Socket();
				InetSocketAddress remoteAddr = new InetSocketAddress(this.hostName, this.port); 
				client.connect(remoteAddr, timeout);
				
				writer = client.getOutputStream();
				reader = client.getInputStream();
				
				byte[] msg = new byte[]{0x00, 0x02, (byte) 0xD2, 0x00, (byte) 0xEC, 0x24};
				writer.write(msg,0, msg.length);
				writer.flush();
				
				Thread.sleep(500);
				
				while(isRun){
					if(client.isConnected()){
						int hasData = reader.read(inputMsg, 0, inputMsg.length);
						if(hasData > 0){
							byte[] temp = new byte[inputMsg.length];
							System.arraycopy(inputMsg, 0, temp, 0, inputMsg.length);
							Log.i(getLocalClassName(), "Data:" + convertByteArrayToHexString(temp));
						}
					}else{
						Log.w(getLocalClassName(), "Socket disconnect!");
						isRun = false;
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				try {
					if (writer != null) {
						writer.close();
					}
					if (reader != null) {
						reader.close();
					}
					if (client != null) {
						client.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Deprecated
	private String convertByteArrayToHexString(byte[] byte_array) {
		String s = "";

		if (byte_array == null)
			return s;

		for (int i = 0; i < byte_array.length; i++) {
			String hex = Integer.toHexString(byte_array[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			s = s + hex;
		}
		return s.toUpperCase().substring(16, 24);
	}
	
//返回按钮	
  @Override
public boolean onOptionsItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	  switch (item.getItemId()) {
	case android.R.id.home://返回按钮
		finish();
		break;

	default:
		break;
	}
	  
	return super.onOptionsItemSelected(item);
	
	
}
  
  @Override
public boolean onCreateOptionsMenu(Menu menu) {
	// TODO Auto-generated method stub
	  MenuInflater inflater=new MenuInflater(this);
	  inflater.inflate(R.menu.main, menu);
	  
	return super.onCreateOptionsMenu(menu);
}
  
  //处理eventbus的事件  看是否上传成功
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onMoonEvent(MyMessage message){
  	Toast.makeText(this, message.getContent().toString(),Toast.LENGTH_SHORT).show();
    if(message.getType()==2){
    
    	String address = mIpText.getText() + "," + mPortText.getText();
		connectReader(address);
    	//拉取工单号
	 //如果是拉取数据
    Gson gson=new Gson();
    OrderList orderList= gson.fromJson(message.getContent(), OrderList.class);
    for(String item:orderList.getLists()){
    	data_list.add(item);  
    }
    
    //适配器
    final ArrayAdapter<String>  arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
    //设置样式
    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    //加载适配器
    mSpinner.setAdapter(arr_adapter);
    mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub

			
			 OrderNO=arr_adapter.getItem(position);
			 size=manager.queryEPCEntity(OrderNO).size();
			
			
			pandian.setText("当前"+OrderNO+"工单盘点数量:"+size);
			System.out.println(arr_adapter.getItem(position));
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			//设置默认值
			
		}
	});
  }
    if(message.getType()==3){
    	showToast("连接超时");
    	showProgress(false);
    }
    
    
  }
//在开始扫描按钮的时候调用中调用定时器
  public void startListnerListView(){
//开启定时器开更新界面

 timer.scheduleAtFixedRate(new TimerTask() {
		
		@Override
		public void run() {
			  // TODO Auto-generated method stub
			  //进行定期的更新listView
			  //如果数量超过10个
			Message dataArrivedMsg = new Message();
			dataArrivedMsg.what = DATA_ARRIVED;
			cardOperationHandler.sendMessage(dataArrivedMsg);
			   
			 
		}}, 0, 100);
   
 
      
  
  }
		 
}