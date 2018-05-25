package com.yay.distance.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yay.distance.bean.request.DistanceRequestBean;
import com.yay.distance.bean.result.DistanceResponseBean;
import com.yay.distance.bm.DistanceBusinessManager;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/distance")
@Api(tags={"distance"})
public class DistanceController {
	@Autowired
	DistanceBusinessManager distanceBusinessManager;
	private static Logger log = LoggerFactory.getLogger(DistanceController.class);
	
	@RequestMapping(value="getDistance",method=RequestMethod.POST,
			produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value="Returns distance and time needed to travel between two coordinates",notes="Returns distance in km")
	public DistanceResponseBean getDistance(@RequestBody(required = true) DistanceRequestBean requestBean){
		long start = System.currentTimeMillis();
		DistanceResponseBean response = distanceBusinessManager.getDistance(requestBean);
		long diff = System.currentTimeMillis() - start;
		log.info("Time taken for getDistance call:"+(diff/1000)+"s "+(diff % 1000)+"ms");
		return response;
	}
	
	@RequestMapping(value="getTransports",method=RequestMethod.GET,
			produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value="Returns transport mediums",notes="Returns transport mediums")
	public Map<Integer,String> getTransports(){
		long start = System.currentTimeMillis();
		Map<Integer,String> result=distanceBusinessManager.getTransports();
		long diff = System.currentTimeMillis() - start;
		log.info("Time taken for getTransports call:"+(diff/1000)+"s "+(diff % 1000)+"ms");
		return result;
	}
	
	@RequestMapping(value="populateDistanceTable",method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value="Populates distance table",notes="Deletes all rows from distance table and populate the table with latest attractions available.")
	public void populateDistanceTable(){
		long start = System.currentTimeMillis();
		distanceBusinessManager.refreshDistanceTable();
		long diff = System.currentTimeMillis() - start;
		log.info("Time taken for populateDistanceTable call:"+(diff/1000)+"s "+(diff % 1000)+"ms");
	}

}
