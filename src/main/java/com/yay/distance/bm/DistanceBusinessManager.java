package com.yay.distance.bm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.LatLng;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.yay.distance.bean.bo.Attraction;
import com.yay.distance.bean.bo.GeoLocation;
import com.yay.distance.bean.request.DistanceRequestBean;
import com.yay.distance.bean.result.DistanceResponseBean;
import com.yay.distance.bean.result.Element;
import com.yay.distance.bean.result.Row;
import com.yay.distance.dao.DistanceDao;
import com.yay.distance.dao.DistanceDao2;

@Configuration
@PropertySource("classpath:application.properties")
public class DistanceBusinessManager {

	private static Logger log = LoggerFactory.getLogger(DistanceBusinessManager.class);

	@Autowired
	Environment env;

	@Autowired
	DistanceDao2 distanceDao;

	private String apiKey = null;

	private boolean usingPrimaryKey = true;

	@PostConstruct
	public void setApiKey() {
		apiKey = env.getProperty("google.distance.api.key");
	}

	public DistanceResponseBean getDistance(DistanceRequestBean requestBean) {
		// String apiKey = env.getProperty("google.distance.api.key");
		// log.info("ApiKey:"+apiKey);
		DistanceResponseBean response = new DistanceResponseBean();
		LatLng origin = new LatLng(requestBean.getSource().getLatitude(), requestBean.getSource().getLongitude());
		LatLng dest = new LatLng(requestBean.getDestination().getLatitude(),
				requestBean.getDestination().getLongitude());
		GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).maxRetries(2).build();
		TravelMode selectedTravelMode = TravelMode.DRIVING;
		if (!"Road".equalsIgnoreCase(requestBean.getTravellingBy())) {
			selectedTravelMode = TravelMode.TRANSIT;
		}
		TransitMode selectedTransitMode = null;
		if (selectedTravelMode == TravelMode.TRANSIT) {
			if ("Railway".equalsIgnoreCase(requestBean.getTravellingBy())
					|| "Toy Train".equalsIgnoreCase(requestBean.getTravellingBy())) {
				selectedTransitMode = TransitMode.RAIL;
			} else {
				response.setErrorMessage("Only Road,Railway and Toy Train travelling mode supported.");
			}
		}
		if (response.getErrorMessage() == null) {
			DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(context).origins(origin).destinations(dest)
					.units(Unit.METRIC).mode(selectedTravelMode);
			if (selectedTransitMode != null) {
				req = req.transitModes(selectedTransitMode);
			}
			try {
				DistanceMatrix matrix = req.await();
				convertToResponse(response, matrix);
			} catch (ApiException | InterruptedException | IOException e) {
				if (e.getMessage().contains("You have exceeded your daily request quota")
						&& usingPrimaryKey) {
					log.info("Changing api key as primary key quota exceeded.");
					apiKey = env.getProperty("google.distance.api.key2");
					usingPrimaryKey = false;
					response = getDistance(requestBean);
				} else {
					log.error("Exception from google API", e);
					response.setErrorMessage(e.getMessage());
				}
			}
		} 
		return response;
	}

	private void convertToResponse(DistanceResponseBean response, DistanceMatrix matrix) {
		response.setOrigin_addresses(matrix.originAddresses);
		response.setDestination_addresses(matrix.destinationAddresses);
		List<Row> rows = new ArrayList<Row>();
		for (DistanceMatrixRow matrixRow : matrix.rows) {
			Row row = new Row();
			for (DistanceMatrixElement matrixElm : matrixRow.elements) {
				if (matrixElm.distance != null && matrixElm.duration != null) {
					Element e = new Element();
					e.setDistance(matrixElm.distance.inMeters);
					e.setDuration(matrixElm.duration.inSeconds);
					row.getElements().add(e);
				}
			}
			rows.add(row);
		}
		response.setRows(rows);
	}

	public Map<Integer, String> getTransports() {
		return distanceDao.getTransportTypes();
	}

	@Transactional
	public void refreshDistanceTable() {
		int noOfDeletedRows = distanceDao.clearDistanceTable();
		log.info("# of rows deleted from distance table:" + noOfDeletedRows);
		Map<Integer, String> transportMap = distanceDao.getTransportTypes();
		List<GeoLocation> attractions = distanceDao.getAllLocations();
		for (int i = 0; i < attractions.size(); i++) {
			GeoLocation sourceAttraction = attractions.get(i);
			List<Object[]> dataForSource = new ArrayList<>();
			boolean apiQuotaExceeded = false;
			for (int j = i + 1; j < attractions.size(); j++) {
				GeoLocation destinationAttraction = attractions.get(j);
				DistanceRequestBean reqBean = new DistanceRequestBean();
				reqBean.setSource(sourceAttraction.getLocation());
				reqBean.setDestination(destinationAttraction.getLocation());
				for (Integer reachId : sourceAttraction.getReaches()) {
					if (destinationAttraction.getReaches().contains(reachId)) {
						reqBean.setTravellingBy(transportMap.get(reachId));
						DistanceResponseBean response = getDistance(reqBean);
						if (response.getErrorMessage() == null && response.getRows().size() > 0
								&& response.getRows().get(0).getElements().size() > 0) {
							int time = (int) (response.getRows().get(0).getElements().get(0).getDuration() / 60);
							int km = (int) (response.getRows().get(0).getElements().get(0).getDistance() / 1000);

							Object[] data = new Object[5];
							data[0] = sourceAttraction.getId();
							data[1] = destinationAttraction.getId();
							data[2] = reachId;
							data[3] = time;
							data[4] = km;

							dataForSource.add(data);

							log.info("Distance data will be saved for source:" + sourceAttraction.getId()
									+ " destination:" + destinationAttraction.getId() + " reach:"
									+ transportMap.get(reachId));
						} else {
							log.error("Error in executing distance rest call.");
							log.info("Source:" + sourceAttraction.getId() + " destination:"
									+ destinationAttraction.getId() + " reach:" + transportMap.get(reachId));
							log.info(response.getErrorMessage());
							if (response.getErrorMessage() != null && response.getErrorMessage()
									.contains("You have exceeded your daily request quota")) {
								apiQuotaExceeded = true;
								break;
							}
						}

					}
				}
				if (apiQuotaExceeded) {
					break;
				}

			}
			if (apiQuotaExceeded) {
				break;
			}
			if (dataForSource.size() > 0) {
				distanceDao.insertIntoDistance(dataForSource);
				log.info("Data saved for source:" + sourceAttraction.getId());
			}
		}
	}

}
