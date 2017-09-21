package com.hsj.webservice;

import java.io.IOException;

import javax.security.auth.PrivateCredentialPermission;

import org.greenrobot.eventbus.EventBus;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.hsj.entity.MyMessage;

import android.os.Handler;
import android.os.Message;
import android.text.GetChars;

public class WebserviceUtil {
	
/*  //命名空间
  final static String SERVICE_NS="http://WebXml.com.cn/";
  //wsdl地址
  final static String SERVICE_URL="http://fy.webxml.com.cn/webservices/EnglishChinese.asmx";*/
  
  final static String SERVICE_NS="http://tempuri.org/";
  final static String SERVICE_URL="http://192.168.1.2:9002/ServiceInfo.asmx";
  //通过调用webservice获取数据
  public static void getService(String methodName,String...  params){
	  final HttpTransportSE ht=new HttpTransportSE(SERVICE_URL,8000);
	  //ht.debug=false;
	 
	  final SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER12);
	  
	  SoapObject soapObject=new SoapObject(SERVICE_NS, methodName);
	 /* for(String value:params){
		  soapObject.addProperty("wordKey",value);
	  }  */
	  if(params!=null){
	  soapObject.addProperty("injson",params[0]);
	  }
	  envelope.dotNet = true;  
	  envelope.setOutputSoapObject(soapObject);
	  //envelope.dotNet = false;
	   
	  System.out.println("进入方法");
	  new Thread(){
		 public void run() {
			 System.out.println("开启线程。。。");
			try {
				//soapAction
				/*ht.call("http://WebXml.com.cn/TranslatorString", envelope);*/
				ht.call("http://tempuri.org/SaveInventoryData", envelope);
				if(envelope.getResponse()!=null){
					SoapObject result=(SoapObject)envelope.bodyIn;					
					
				 System.out.println(result.getProperty(0).toString());
				// SoapObject soap=(SoapObject)result.getProperty(0);
				 
				StringBuffer sbBuffer=new StringBuffer();
				sbBuffer.append(result.getProperty(0).toString());
		       /* for(int i=0;i<soap.getPropertyCount();i++){
		        	sbBuffer.append(soap.getProperty(i).toString());
		        	System.out.println(soap.getProperty(i).toString());
		        }	*/
		       //使用eventbus进行事件分发
				MyMessage myMessage=new MyMessage();
				myMessage.setType(1);
				myMessage.setContent(sbBuffer.toString());
		        EventBus.getDefault().post(myMessage);
		       
				}
			
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
		 };
		  
	  }.start();
	  
  }
  
  //通过调用webservice获取数据
  public static void getService2(String methodName){
	  final HttpTransportSE ht=new HttpTransportSE(SERVICE_URL,8000);
	  
	  //ht.debug=false;
	  final long initTime=System.currentTimeMillis();
	  final SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER12);
	  
	  SoapObject soapObject=new SoapObject(SERVICE_NS, methodName);
	 /* for(String value:params){
		  soapObject.addProperty("wordKey",value);
	  }  */
	 
	  envelope.dotNet = true;  
	  envelope.setOutputSoapObject(soapObject);
	  //envelope.dotNet = false;
	   
	  System.out.println("进入方法");
	  new Thread(){
		 public void run() {
			 System.out.println("开启线程。。。");
			try {
				//soapAction
				/*ht.call("http://WebXml.com.cn/TranslatorString", envelope);*/
				ht.call("http://tempuri.org/SaveInventoryData", envelope);
			   
				if(envelope.getResponse()!=null){
					SoapObject result=(SoapObject)envelope.bodyIn;					
					
				 System.out.println(result.getProperty(0).toString());
				// SoapObject soap=(SoapObject)result.getProperty(0);
				 
				StringBuffer sbBuffer=new StringBuffer();
				sbBuffer.append(result.getProperty(0).toString());
		       /* for(int i=0;i<soap.getPropertyCount();i++){
		        	sbBuffer.append(soap.getProperty(i).toString());
		        	System.out.println(soap.getProperty(i).toString());
		        }	*/
		       //使用eventbus进行事件分发
				MyMessage myMessage=new MyMessage();
				myMessage.setType(2);
				myMessage.setContent(sbBuffer.toString());
		        EventBus.getDefault().post(myMessage);
				}
			
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
					MyMessage myMessage=new MyMessage();
					myMessage.setType(3);
					myMessage.setContent("连接超时!");
			        EventBus.getDefault().post(myMessage);
							
			}
			 
		 };
		  
	  }.start();
	  
  }
 
}
