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

@Component
public class DistanceDao {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	private static Logger log = LoggerFactory.getLogger(DistanceDao.class);
	
	public List<Attraction> getAllAttractions(){
		List<Attraction> attractions = new ArrayList<>();
		String sql = "select distinct reach_id,id,latitude,longitude from apollo.t_attractions "+
					"join apollo.t_attraction_reach on attraction_id=id order by id";
		attractions = jdbcTemplate.query(sql, new RowMapper<Attraction>(){

			@Override
			public Attraction mapRow(ResultSet rs, int arg1) throws SQLException {
				Attraction attraction = new Attraction();
				attraction.setId(rs.getInt("id"));
				attraction.setLat(rs.getDouble("latitude"));
				attraction.setLongitude(rs.getDouble("longitude"));
				attraction.getReaches().add(rs.getInt("reach_id"));
				return attraction;
			}
			
		});
		
		List<Attraction> nonDuplicateAttractions = new ArrayList<>();
		attractions.forEach(a->{
			if(nonDuplicateAttractions.contains(a)){
				Attraction addedAttraction = nonDuplicateAttractions.get(nonDuplicateAttractions.indexOf(a));
				addedAttraction.getReaches().add(a.getReaches().get(0));
			}else{
				nonDuplicateAttractions.add(a);
			}
		});
		log.info("Attractions retrieved:"+nonDuplicateAttractions.size());
		return nonDuplicateAttractions;
		
	}
	
	public Map<Integer,String> getTransportTypes(){
		String sql="SELECT id,name FROM apollo.t_reach";
		List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
		Map<Integer,String> resultMap=new HashMap<>();
		for(Map<String,Object> valueMap:rows) {
			resultMap.put((Integer)valueMap.get("id"), (String)valueMap.get("name"));
		}
		return resultMap;
	}
	
	public int clearDistanceTable(){
		return jdbcTemplate.update("delete from apollo.t_distance");
	}
	
	public void insertIntoDistance(List<Object[]> dataset){
		String sql="insert into apollo.t_distance(attraction_id_src,attraction_id_dst,reach_id,time_in_min,distance_in_km) values(?,?,?,?,?)";
		
		
		int[] dataTypes= new int[5];
		dataTypes[0] = Types.INTEGER;
		dataTypes[1] = Types.INTEGER;
		dataTypes[2] = Types.INTEGER;
		dataTypes[3] = Types.INTEGER;
		dataTypes[4] = Types.INTEGER;
		
		jdbcTemplate.batchUpdate(sql,dataset , dataTypes);
		
	}

}
