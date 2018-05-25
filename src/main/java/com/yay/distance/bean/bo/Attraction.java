package com.yay.distance.bean.bo;

import java.util.ArrayList;
import java.util.List;

import com.yay.distance.bean.request.LocationBean;

public class Attraction {
	private int id;
	private double lat;
	private double longitude;
	private List<Integer> reaches = new ArrayList<Integer>();
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public List<Integer> getReaches() {
		return reaches;
	}
	public void setReaches(List<Integer> reaches) {
		this.reaches = reaches;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Attraction)){
			return false;
		}else{
			return id == ((Attraction)obj).getId();
		}
	}
	
	public LocationBean getLocation(){
		LocationBean location = new LocationBean();
		location.setLatitude(lat);
		location.setLongitude(longitude);
		return location;
	}
	

}
