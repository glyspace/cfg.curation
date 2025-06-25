package org.glygen.cfgcuration.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.glygen.cfgcuration.model.Publication;
import org.json.JSONArray;
import org.json.JSONObject;

public class CrossRefAPI {
	
    String apiUrl = "https://api.crossref.org/works?query.title=";

    public List<Publication> getPublicationByTitle(String title) throws Exception {
    	List<Publication> results = new ArrayList<>();
    	title = title.replaceAll("\n", " ");
    	String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
    	String url = apiUrl + encodedTitle;
    	HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        
        JSONObject obj = new JSONObject(json);
        JSONArray items = obj.getJSONObject("message").getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
        	Publication pub = new Publication();
        	JSONObject item = items.getJSONObject(i);
            String doi = item.getString("DOI");
            pub.setDoiId(doi);
            
            if (item.has("short-container-title")) {
	            String journalName = item.getJSONArray("short-container-title").getString(0);
	            pub.setJournalName(journalName);
            }
            
            if (item.has("issued")) {
	            JSONObject is = item.getJSONObject("issued");
	            if (is.has("date-parts")) {
	            	JSONArray dateParts = is.getJSONArray("date-parts");
	            	if (dateParts.length() > 0) {
	            		JSONArray date =  dateParts.getJSONArray(0);
	            		if (date != null && date.length() > 0) {
	            			try {
	            				int year = date.getInt(0);
	            				pub.setYear(year+"");
	            			} catch (Exception e) {
	            				// ignore
	            			}
	            		}
	            	}	
	            }
	            
            }
            
            if (item.has("volume")) {
            	String volume = item.getString("volume");
            	pub.setVolume(volume);
            }
            
            if (item.has("author")) {
	            JSONArray authors = item.getJSONArray("author");
	            String authorString = "";
	            for (int j = 0; j < authors.length(); j++) {
	            	if (authors.getJSONObject(j).has("given")) {
		                String fullName = authors.getJSONObject(j).getString("family") + " " + authors.getJSONObject(j).getString("given");
		                authorString += fullName;
		                if (j < authors.length() - 1) authorString += "; ";
	            	}
	            }
	            pub.setAuthor(authorString);
            }
            
            if (item.has("page")) {
            	String page = item.getString("page");
            	pub.setPageRange(page);
            }
            
            JSONArray titles = item.getJSONArray("title");
            String titleFromItem = titles.getString(0);
            if (titleFromItem.toLowerCase().contains(title.toLowerCase())) {    // special case where they put extra stuff to the beginning of actual title
            	pub.setTitle(title);
            }
            else pub.setTitle(titleFromItem);
            
            results.add(pub);
        }
        
        return results;
    }
    
    public static void main(String[] args) {
    	try {
    		Publication input = new Publication();
    		input.setTitle("Enzymes involved in mammalian oligosaccharide biosynthesis");
    		input.setAuthor("Natsuka S; Lowe JB");
    		input.setJournalName("Curr Opin Struct Biol");
    		input.setYear("1994");
    		input.setVolume("4");
    		input.setPageRange("683-691");
    		List<Publication> results = new CrossRefAPI().getPublicationByTitle("Enzymes in Organic Synthesis: Application to the Problems of Carbohydrate Recognition (Part 2)");
    		for (Publication pub: results) {
    			if (pub.equals(input)) {
					input.setPmid(pub.getPmid());
					input.setDoiId(pub.getDoiId());
					System.out.println ("Found a match " + pub.getDoiId());
				}
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
	}
}
