package org.glygen.cfgcuration.util;

import java.util.Arrays;

import org.glygen.cfgcuration.model.tablemaker.GlycanView;
import org.glygen.cfgcuration.model.tablemaker.SequenceFormat;
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
	
	private RestTemplate restTemplate;
	
	private TableMakerAPI() {
	}
	
	public static TableMakerAPI getInstance () {
		if (instance == null)
			instance = new TableMakerAPI();
		return instance;
	}
	
	public void login() {
		String url = apiURL + "api/account/authenticate";	
		
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUsername(userName);
		loginRequest.setPassword(password);
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<LoginRequest>(loginRequest);
		HttpEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
		HttpHeaders header = response.getHeaders();
		this.token = header.getFirst("Authorization");
	}
	
	public void addGlycan(String glycoCT) {
		if (this.token == null) login();
		
		String url = apiURL + "api/glycan/addglycan";
		GlycanView glycan = new GlycanView();
		glycan.setFormat(SequenceFormat.GLYCOCT);
		glycan.setSequence(glycoCT);
		
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", token);
		
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
	
	class LoginRequest {
		public String username;
	    public String password;
		
	    public String getUsername() {
			return username;
		}
	    
	    public String getPassword() {
			return password;
		}
	    
	    public void setUsername(String username) {
			this.username = username;
		}
	    
	    public void setPassword(String password) {
			this.password = password;
		}
	}
}
