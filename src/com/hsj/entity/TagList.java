package com.hsj.entity;

import java.io.Serializable;
import java.util.List;

import android.R.integer;

public class TagList implements Serializable{
 private String orderNO;

 private String[] tags;

public String getOrderNO() {
	return orderNO;
}

public void setOrderNO(String orderNO) {
	this.orderNO = orderNO;
}

public String[] getTags() {
	return tags;
}

public void setTags(String[] tags) {
	this.tags = tags;
} 
	
}
