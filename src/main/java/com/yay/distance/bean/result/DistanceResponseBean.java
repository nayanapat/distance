package com.yay.distance.bean.result;

import java.util.List;

public class DistanceResponseBean {
	private String[] destination_addresses;
	private String[] origin_addresses;
	private List<Row> rows;
	private String errorMessage;
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String[] getDestination_addresses() {
		return destination_addresses;
	}
	public void setDestination_addresses(String[] destination_addresses) {
		this.destination_addresses = destination_addresses;
	}
	public String[] getOrigin_addresses() {
		return origin_addresses;
	}
	public void setOrigin_addresses(String[] origin_addresses) {
		this.origin_addresses = origin_addresses;
	}
	public List<Row> getRows() {
		return rows;
	}
	public void setRows(List<Row> rows) {
		this.rows = rows;
	}
	

}
