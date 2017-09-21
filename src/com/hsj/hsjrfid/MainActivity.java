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
		
		
		//���ù���actionbar��һЩ����ֵ
		 int titleId = Resources.getSystem().getIdentifier("action_bar_title",
		            "id", "android");	
		    tvTitle = (TextView) findViewById(titleId);
		    tvTitle.setTextColor(0xFFFFFFFF);
		    tvTitle.setTextSize(20);
		    tvTitle.setGravity(Gravity.CENTER);
		    mActionBar.setTitle("�̵�");	 
		 
		setContentView(R.layout.activity_main);	
		//ע��eventbus
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
		//�˴˴�����webservice��ȡ����
		WebserviceUtil.getService2("GetInventoryList");        
		//һ������Ҫ��ʾ������
		showProgress(true);
		//�������ݿ��ϴ�
		manager=new ServiceManger(this);
		
		uploadBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//�ϴ����ݿ�ı�ǩ��webservice��ȥ
				new Thread(){
					public void run() {
						//����webservice 
						
						List<EntityTag> tags=manager.queryEPCEntity(OrderNO);
						String[] tagArray=new String[tags.size()];
						for(int i=0;i<tags.size();i++){
							tagArray[i]=tags.get(i).getEpcData();
						}
						TagList tagList=new TagList();
						if(!TextUtils.isEmpty(OrderNO)){
							tagList.setOrderNO(OrderNO);
							tagList.setTags(tagArray);
							//��ȡ����֮�����webservice
							//��ת��Ϊjson�ַ���
							Gson gson=new Gson();
							String jsonStr=gson.toJson(tagList);
							System.out.println(jsonStr);	
							WebserviceUtil.getService("SaveInventoryData", jsonStr);
							//�ϴ�������֮��ɾ����Ӧ����������
							manager.delEPCEntity(OrderNO);
							
						}
						
					
					};
					
				}.start();
				
				
			}
		});
		
		
		connectBtn.setOnClickListener(new OnClickListener() {
		
			public void onClick(View v) {
//				if (TextUtils.isEmpty(mIpText.getText())) {
//					showToast("������IP��ַ");
//					return;
//				}
//				if (TextUtils.isEmpty(mPortText.getText())) {
//					showToast("������˿�");
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
				//���ö�ʱ��
				
				
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
	//�������״̬
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
   //��Ϣ�Ļص�����
	@Override
	public void messageNotificationReceivedHandle(BaseReader reader,
			IMessageNotification msg) {
		if(isReading){
			if(isConnected){
				if (msg instanceof RXD_TagData) {
					//�������ǩ
					//��֪ͨ����������handler
					//
					//cardOperationHandler.sendEmptyMessage(PLAY_SOUND);
					
					
						 media.start();
					 
					
					
					RXD_TagData data = (RXD_TagData) msg;
					String epc = convertByteArrayToHexString(data.getReceivedMessage().getEPC());					
					boolean isExists = false;
					//������ϲ�Ϊ�ղŽ�������Ĳ���
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
						//do nothing  ɨ�赽��ͬ�ı�ǩ
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
						
						//���������
						
						//ÿɨ�赽һ����ǩ�ϴ�����������ȥ
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

	//����pda�İ�����д����
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		InvengoLog.i(TAG, "INFO.onKeyDown().");
		if (keyCode == KeyEvent.KEYCODE_BACK && !backDown) {
			backDown = true;
		}
		//���ð�ť��һ����Ч
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
    
	//˫���˳��¼�
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && backDown) {
			backDown = false;
			long currentTime = System.currentTimeMillis();
			if (currentTime - firstTime > 2000) { // ������ΰ���ʱ��������2�룬���˳�
				showToast("�ٰ�һ�� �˳�����");
				firstTime = currentTime;// ����firstTime
				return true;
			} else { // ���ΰ���С��2��ʱ���˳�Ӧ��
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
			showToast("������");
		}else{
			showToast("������");
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
			case START_READ://��ʼ����
				boolean start = (Boolean) msg.obj;
				if (start) {
					showToast("���Ͷ���ָ��ɹ�");
					readBtn.setEnabled(false);
					clearBtn.setEnabled(false);
					startListnerListView();//��ʼ���н��ж�ʱ������
					
				} else {
					showToast("���Ͷ���ָ��ʧ��");
				}
				break;
			case STOP_READ://ֹͣ����
				boolean stop = (Boolean) msg.obj;
				if (stop) {
					showToast("ֹͣ�����ɹ�");
					readBtn.setEnabled(true);
					clearBtn.setEnabled(true);
				} else {
					showToast("ֹͣ����ʧ��");
				}
				break;
			case DATA_ARRIVED://��������
				
			
				EPCEntity entity;
				
				 List<EPCEntity> myDatas=new ArrayList<EPCEntity>();
				  //���������
				  myDatas.clear();
				//��̬�ĸ�������
				 if(mEPCEntityList.size()>10){	
					 
				  for(int i=mEPCEntityList.size();i>mEPCEntityList.size()-10;i--){
				
					  myDatas.add(mEPCEntityList.get(i-1));
				  }	  
				  mListAdapter.updataListView(myDatas);
				  } 
				 
				 else{				  
				  mListAdapter.updataListView(mEPCEntityList);
				  }
				//������ݴ���10������Ĳ���ʾ
				/*if(mEPCEntityList.size()>10){
					mEPCEntityList.get(location)
				}*/
				//((EPCEntityAdapter)mEpcListView.getAdapter()).notifyDataSetChanged();
				//startListnerListView();
				break;
			case DISCONNECT://�Ͽ���д������
				disconnect();
				isConnected = false;
				showToast("�Ͽ����ӳɹ�");
				break;
			case READ_TAG_SUM://ɨ�赽������
				mSum.setText("ɨ�������:"+mEPCEntityList.size());
				pandian.setText("��ǰ"+OrderNO+"�����̵�����:"+size);
			case PLAY_SOUND://��������
				
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
				showToast("���ӳɹ�");
				
			}else{
				connectBtn.setEnabled(true);
				disconnectBtn.setEnabled(false);
				readBtn.setEnabled(false);
				stopBtn.setEnabled(false);
				clearBtn.setEnabled(false);
				showToast("����ʧ��");
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

		//���½���
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
	//��ע�ͱ�ʾ���Ƽ�ʹ�ô��ַ�ʽ
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
	
//���ذ�ť	
  @Override
public boolean onOptionsItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	  switch (item.getItemId()) {
	case android.R.id.home://���ذ�ť
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
  
  //����eventbus���¼�  ���Ƿ��ϴ��ɹ�
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onMoonEvent(MyMessage message){
  	Toast.makeText(this, message.getContent().toString(),Toast.LENGTH_SHORT).show();
    if(message.getType()==2){
    
    	String address = mIpText.getText() + "," + mPortText.getText();
		connectReader(address);
    	//��ȡ������
	 //�������ȡ����
    Gson gson=new Gson();
    OrderList orderList= gson.fromJson(message.getContent(), OrderList.class);
    for(String item:orderList.getLists()){
    	data_list.add(item);  
    }
    
    //������
    final ArrayAdapter<String>  arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
    //������ʽ
    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    //����������
    mSpinner.setAdapter(arr_adapter);
    mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub

			
			 OrderNO=arr_adapter.getItem(position);
			 size=manager.queryEPCEntity(OrderNO).size();
			
			
			pandian.setText("��ǰ"+OrderNO+"�����̵�����:"+size);
			System.out.println(arr_adapter.getItem(position));
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			//����Ĭ��ֵ
			
		}
	});
  }
    if(message.getType()==3){
    	showToast("���ӳ�ʱ");
    	showProgress(false);
    }
    
    
  }
//�ڿ�ʼɨ�谴ť��ʱ������е��ö�ʱ��
  public void startListnerListView(){
//������ʱ�������½���

 timer.scheduleAtFixedRate(new TimerTask() {
		
		@Override
		public void run() {
			  // TODO Auto-generated method stub
			  //���ж��ڵĸ���listView
			  //�����������10��
			Message dataArrivedMsg = new Message();
			dataArrivedMsg.what = DATA_ARRIVED;
			cardOperationHandler.sendMessage(dataArrivedMsg);
			   
			 
		}}, 0, 100);
   
 
      
  
  }
		 
}