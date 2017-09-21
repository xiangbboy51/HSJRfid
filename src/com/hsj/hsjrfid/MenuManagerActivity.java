package com.hsj.hsjrfid;
import java.util.ArrayList;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.DbManager.DbUpgradeListener;
import org.xutils.x;
import org.xutils.common.Callback.Cancelable;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import com.hsj.entity.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


@ContentView(R.layout.activity_menu_manager)
public class MenuManagerActivity extends SlidingFragmentActivity{
   
	/*@ViewInject(R.id.check)
	private Button check;
	@ViewInject(R.id.grounding)
    private Button grounding;
	@ViewInject(R.id.undercarriage)
    private Button undercarriage;*/
	@ViewInject(R.id.gridView)
	private GridView gridView;
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //使用xutils注解方式加载
        x.view().inject(this);
        //设置侧边栏
        setBehindContentView(R.layout.left_menu);
        
       SlidingMenu menu=getSlidingMenu();
       
       menu.setMode(SlidingMenu.LEFT);
       
       menu.setShadowWidth(3);
       
       menu.setShadowDrawable(null);
       
       menu.setBehindOffset(300);
       
       menu.setFadeDegree(0.15f);
        
       menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
       
       menu.setBehindScrollScale(0);
        
        //数据库模板
   /*  new DbManager.DaoConfig().setDbName("aa.db")
                              .setDbVersion(1)
                              .setDbUpgradeListener(new DbUpgradeListener() {
								
								@Override
								public void onUpgrade(DbManager arg0, int arg1, int arg2) {
									// TODO Auto-generated method stub
									
								}
			
							
							});*/
       
       
       gridView.setAdapter(new MyAdapter(this));
       
       gridView.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position,
				long id) {
			// TODO Auto-generated method stub
			//设置动画
			 //属性动画类,绕着Y轴旋转180度
	        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "rotationY",
	                0, 360);
	        objectAnimator.setDuration(1200);
	       
	        objectAnimator.setInterpolator(new AccelerateInterpolator());
	        objectAnimator.addListener(new Animator.AnimatorListener() {
	            @Override
	            public void onAnimationStart(Animator animation) {
	            }
	            @Override
	            public void onAnimationEnd(Animator animation) {
	           	 switch (position) {
	 			case 0:
	 				 Intent intent=new Intent(MenuManagerActivity.this,MainActivity.class);
	 	                startActivity(intent);
	 				break;
	 			case 1:
	 				System.out.println("qqqq");
	 				break;
	 			default:
	 				break;
	 			}
	            }

	            @Override
	            public void onAnimationCancel(Animator animation) {
	            }
	            @Override
	            public void onAnimationRepeat(Animator animation) {

	            }
	        });
	        objectAnimator.start();
			
		
			
		}
	});
       
       
       
       
    }
   /* @Event(value={R.id.check,R.id.grounding,R.id.undercarriage},type=View.OnClickListener.class) 
    private void MyClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.check:
                intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.grounding:
               
                break;
            case R.id.undercarriage:
               
                break;
        }
       

    }*/
     
    class MyAdapter extends BaseAdapter{
       private Context context;
       private List<MenuItem> items=new ArrayList<MenuItem>();
       //图片数组 
       private Integer[] imgs = { 
               R.drawable.pandian_select, R.drawable.ruku_select, R.drawable.chuku_select,  
               R.drawable.shangjia, R.drawable.camera, R.drawable.dayin_select,  
               R.drawable.chanpin_select
       }; 
       private String[] titles={
    		   "盘点","入库","出库",
    		   "上架","拍照","打印",
    		   "产品"
       };
       
      
       
		public MyAdapter(Context context) {
	    
		this.context = context;
		MenuItem item;
		 for(int i=0;i<imgs.length;i++){
			 item=new MenuItem();
			 item.setImg(imgs[i]);
			 item.setTitle(titles[i]);
			 items.add(item);
	       }
		
	}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imgs.length;
		}

		@Override
		public MenuItem getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Myholder holder;
			 if (convertView == null) { 
              convertView=LayoutInflater.from(parent.getContext())
            		  .inflate(R.layout.girdview_item, null);
              holder = new Myholder();
              holder.img=(ImageView) convertView.findViewById(R.id.gridview_img_item);
              holder.title=(TextView) convertView.findViewById(R.id.girdView_titel_item);
              convertView.setTag(holder);
             }  
             else { 
               holder=(Myholder)convertView.getTag();
             }
			 holder.img.setImageResource(getItem(position).getImg());
			 holder.title.setText(getItem(position).getTitle());		 
			return convertView; 
             
		}
    	
		class Myholder{
			ImageView img;
			TextView title;
		}
    
    	
    	
    }
    
}
