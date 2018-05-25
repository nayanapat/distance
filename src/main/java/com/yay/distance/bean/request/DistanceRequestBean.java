package com.yay.distance.bean.request;

public class DistanceRequestBean {
	private LocationBean source;
	private LocationBean destination;
	private String travellingBy;
	public String getTravellingBy() {
		return travellingBy;
	}
	public void setTravellingBy(String travellingBy) {
		this.travellingBy = travellingBy;
	}
	public LocationBean getSource() {
		return source;
	}
	public void setSource(LocationBean source) {
		this.source = source;
	}
	public LocationBean getDestination() {
		return destination;
	}
	public void setDestination(LocationBean destination) {
		this.destination = destination;
	}
	

}
