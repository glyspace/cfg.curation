package org.glygen.cfgcuration.util;

import java.util.Arrays;

import org.glygen.cfgcuration.model.tablemaker.GlycanView;
import org.glygen.cfgcuration.model.tablemaker.LoginRequest;
import org.glygen.cfgcuration.model.tablemaker.SequenceFormat;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
	
	public void addGlycan(String sequence) {
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
}
