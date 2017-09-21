package com.invengo.sample;

import java.io.Serializable;

/**
 * @author Administrator
 *
 */
public class EPCEntity  {

	private int number;
	private String epcData;
	
	public EPCEntity() {
		
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getEpcData() {
		return epcData;
	}

	public void setEpcData(String epcData) {
		this.epcData = epcData;
	}
}
