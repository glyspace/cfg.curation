package org.glygen.cfgcuration.util;

import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.glygen.cfgcuration.model.tablemaker.CollectionView;
import org.glygen.cfgcuration.model.tablemaker.DatasetInputView;
import org.glygen.cfgcuration.model.tablemaker.GlycanView;
import org.glygen.cfgcuration.model.tablemaker.LoginRequest;
import org.glygen.cfgcuration.model.tablemaker.SequenceFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class TableMakerAPI {
	
	String apiURL;
	String userName;
	String password;
	
	String token;
	
	static TableMakerAPI instance;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	private TableMakerAPI() {
	}
	
	public static TableMakerAPI getInstance () {
		if (instance == null)
			instance = new TableMakerAPI();
		return instance;
	}
	
	public void login() {
		String url = apiURL + "api/account/authenticate";	
		

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUsername(userName);
		loginRequest.setPassword(password);
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<LoginRequest>(loginRequest, headers);
		HttpEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
		String resp = response.getBody();
		JSONObject obj = new JSONObject(resp);
		JSONObject data = obj.getJSONObject("data");
		this.token = data.getString("token");
	}
	
	public String addGlycan(String sequence) {
		if (this.token == null) login();
		
		String url = apiURL + "api/data/addglycan";
		GlycanView glycan = new GlycanView();
		glycan.setFormat(SequenceFormat.LINEARCODE);
		glycan.setSequence(sequence);
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<GlycanView> requestEntity = new HttpEntity<GlycanView>(glycan, headers);
		ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
		JSONObject obj = new JSONObject(response.getBody());
		JSONObject data = obj.getJSONObject("data");
		
		return data.getString("glytoucanID");
	}
	
	public Long retrieveGlycanByGlytoucanId (String glytoucanId) {
		if (this.token == null) login();
		
		String url = apiURL + "api/data/getglycans";
		JSONArray filterArray = new JSONArray();
		JSONObject filter = new JSONObject();
		filter.put("id","glytoucanID");
		filter.put("value", glytoucanId);
		filterArray.put(filter);

		url +="?start=0&size=1&filters=" + URLEncoder.encode(filterArray.toString(), StandardCharsets.UTF_8);
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
		ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
		JSONObject obj = new JSONObject(response.getBody());
		JSONObject data = obj.getJSONObject("data");
		JSONArray results = data.getJSONArray("objects");
		if (results.length() > 0) {
			JSONObject result = results.getJSONObject(0);
			return result.getLong("glycanId");
		}
		return null;
	}
	
	public String addCollection (CollectionView collection) {
		if (this.token == null) login();
		
		String url = apiURL + "api/data/addcollection";
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<CollectionView> requestEntity = new HttpEntity<>(collection, headers);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			JSONObject obj = new JSONObject(response.getBody());
			JSONObject data = obj.getJSONObject("data");
			
			return data.getString("collectionId");
		} catch (HttpClientErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			JSONObject obj = new JSONObject(errorMessage);
			if (obj.has("message")) {
				String message = obj.getString("message");
				if (message != null) {
					if (message.contains("already")) {
						// duplicate
						// find the existing collection
						return getCollection (collection.getName());
					}
				}
			}
			return null;
		}
	}
	
	public String publishDataset(DatasetInputView dataset) {
		if (this.token == null) login();
		
		String url = apiURL + "api/dataset/publishdataset";
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<DatasetInputView> requestEntity = new HttpEntity<>(dataset, headers);
		ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
		JSONObject obj = new JSONObject(response.getBody());
		JSONObject data = obj.getJSONObject("data");
		
		return data.getString("datasetIdentifier");
	}

	public String getApiURL() {
		return apiURL;
	}

	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String addGlycanGlycoCT(String glycoCT) {
		if (this.token == null) login();
		
		String url = apiURL + "api/data/addglycan";
		GlycanView glycan = new GlycanView();
		glycan.setFormat(SequenceFormat.GLYCOCT);
		glycan.setSequence(glycoCT);
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<GlycanView> requestEntity = new HttpEntity<GlycanView>(glycan, headers);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			JSONObject obj = new JSONObject(response.getBody());
			JSONObject data = obj.getJSONObject("data");
			
			return data.getString("glytoucanID");
		} catch (HttpClientErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			JSONObject obj = new JSONObject(errorMessage);
			if (obj.has("message")) {
				String message = obj.getString("message");
				if (message != null) {
					if (message.contains("already")) {
						// duplicate
						// find the existing glycan
						return getGlycan (glycoCT);
					}
				}
			}
			return null;
		}
	}

	private String getGlycan(String glycoCT) {
		if (this.token == null) login();
		
		String url = apiURL + "api/data/getglycan";
		GlycanView glycan = new GlycanView();
		glycan.setFormat(SequenceFormat.GLYCOCT);
		glycan.setSequence(glycoCT);
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<GlycanView> requestEntity = new HttpEntity<GlycanView>(glycan, headers);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			JSONObject obj = new JSONObject(response.getBody());
			JSONObject data = obj.getJSONObject("data");
			
			return data.getString("glytoucanID");
		} catch (HttpClientErrorException e) {
			return null;
		}
	}
	
	private String getCollection(String name) {
		if (this.token == null) login();
		
		String url = apiURL + "api/data/getcollectionbyname?name=" + name;
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", "Bearer " + token);
		
		HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
			JSONObject obj = new JSONObject(response.getBody());
			JSONObject data = obj.getJSONObject("data");
			
			return data.getString("collectionId");
		} catch (HttpClientErrorException e) {
			return null;
		}
	}
}
