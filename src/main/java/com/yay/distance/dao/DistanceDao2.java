package com.yay.distance.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.yay.distance.bean.bo.Attraction;
import com.yay.distance.bean.bo.GeoLocation;

@Component
public class DistanceDao2 {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	private static Logger log = LoggerFactory.getLogger(DistanceDao2.class);
	
	public List<GeoLocation> getAllLocations(){
		List<GeoLocation> attractions = new ArrayList<>();
		String sql = "select distinct reach_id,id,latitude,longitude from apollo2.t_geo_location "+
					"join apollo2.t_reach_xref on geo_location_id=id order by id";
		attractions = jdbcTemplate.query(sql, new RowMapper<GeoLocation>(){

			@Override
			public GeoLocation mapRow(ResultSet rs, int arg1) throws SQLException {
				GeoLocation attraction = new GeoLocation();
				attraction.setId(rs.getInt("id"));
				attraction.setLat(rs.getDouble("latitude"));
				attraction.setLongitude(rs.getDouble("longitude"));
				attraction.getReaches().add(rs.getInt("reach_id"));
				return attraction;
			}
			
		});
		
		List<GeoLocation> nonDuplicateAttractions = new ArrayList<>();
		attractions.forEach(a->{
			if(nonDuplicateAttractions.contains(a)){
				GeoLocation addedAttraction = nonDuplicateAttractions.get(nonDuplicateAttractions.indexOf(a));
				addedAttraction.getReaches().add(a.getReaches().get(0));
			}else{
				nonDuplicateAttractions.add(a);
			}
		});
		log.info("GeoLocations retrieved:"+nonDuplicateAttractions.size());
		return nonDuplicateAttractions;
		
	}
	
	public Map<Integer,String> getTransportTypes(){
		String sql="SELECT id,name FROM apollo2.t_reach";
		List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
		Map<Integer,String> resultMap=new HashMap<>();
		for(Map<String,Object> valueMap:rows) {
			resultMap.put((Integer)valueMap.get("id"), (String)valueMap.get("name"));
		}
		return resultMap;
	}
	
	public int clearDistanceTable(){
		return jdbcTemplate.update("delete from apollo2.t_distance");
	}
	
	public void insertIntoDistance(List<Object[]> dataset){
		String sql="insert into apollo2.t_distance(source_location_id,destination_location_id"
				+ ",reach_id,time_in_min,distance_in_km) values(?,?,?,?,?)";
		
		
		int[] dataTypes= new int[5];
		dataTypes[0] = Types.INTEGER;
		dataTypes[1] = Types.INTEGER;
		dataTypes[2] = Types.INTEGER;
		dataTypes[3] = Types.INTEGER;
		dataTypes[4] = Types.INTEGER;
		
		jdbcTemplate.batchUpdate(sql,dataset , dataTypes);
		
	}
	
	public boolean distanceNotCalculated(Integer source,Integer destination,Integer reach) {
		String sql="select count(*) from apollo2.t_distance where "
				+ "source_location_id=? and destination_location_id=? and reach_id=?";
		int count = jdbcTemplate.queryForObject(
                sql, new Object[] { source,destination,reach }, Integer.class);
		return count==0;
	}

}
