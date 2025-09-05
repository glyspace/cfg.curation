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

public class ElsevierAPIUtil {
	
	String apiKey;
	
	public ElsevierAPIUtil(String apiKey) {
		this.apiKey = apiKey;
	}
	
	String apiUrl = "https://api.elsevier.com/content/search/sciencedirect?query=";

    public List<Publication> getPublicationByTitle(String title) throws Exception {
    	List<Publication> results = new ArrayList<>();
    	title = title.replaceAll("\n", " ");
    	title = title.replace("(", "");
    	title = title.replace(")", "");
    	String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
    	encodedTitle = encodedTitle.replace("+", "%20");
    	String url = apiUrl + encodedTitle;
    	if (apiKey != null) {
    		url += "&apiKey=" + apiKey;
    	}
    	HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        
        JSONObject obj = new JSONObject(json);
        if (!obj.has("search-results") || !obj.getJSONObject("search-results").has("entry")) {
        	return results;
        }
        JSONArray items = obj.getJSONObject("search-results").getJSONArray("entry");

        for (int i = 0; i < items.length(); i++) {
        	Publication pub = new Publication();
        	JSONObject item = items.getJSONObject(i);
        	String doi = null;
        	if (item.has("dc:identifier")) {
        		doi = item.getString("dc:identifier");
        	}
            if (doi != null && doi.startsWith("DOI:")) {
            	pub.setDoiId(doi.substring(doi.indexOf(":")+1));
            } else {
            	if (item.has("prism:doi")) {
            		doi = item.getString("prism:doi");
            		pub.setDoiId(doi);
            	}
            }
            
            if (item.has("prism:publicationName")) {
	            String journalName = item.getString("prism:publicationName");
	            if (journalName.equalsIgnoreCase("Carbohydrate Research")) {
	            	pub.setJournalName("Carbohydrate Res");
	            } else if (journalName.equalsIgnoreCase("European Journal of Biochemistry")) {
	            	pub.setJournalName("Eur J Biochem");
	            } else if (journalName.equalsIgnoreCase("Bioorganicheskaia khimiia")) {
	            	pub.setJournalName("Bioorg Khim");
	            }
	            pub.setJournalName(journalName);
            }
            
            if (item.has("dc:title")) {
	            String t = item.getString("dc:title");
	            //remove italic/bold etc. tags from the title
	            t = t.replaceAll("<[^>]+>", "");
	            if (t.toLowerCase().contains(title.toLowerCase())) {    // special case where they put extra stuff to the beginning of actual title
	            	pub.setTitle(title);
	            }
	            else pub.setTitle(t);
            }
            
            String start = "";
            String end = "";
            if (item.has("prism:startingPage")) {
            	start = item.getString("prism:startingPage");
            }
            
            if (item.has("prism:endingPage")) {
            	end = item.getString("prism:endingPage");
            }
            
            if (start.isEmpty() && end.isEmpty()) {
            	pub.setPageRange(null);
            } else {
            	pub.setPageRange(start + "-" + end); 
            }
            
            if (item.has("prism:volume")) {
            	String volume = item.getString("prism:volume");
            	pub.setVolume(volume);
            }
            
            if (item.has("prism:coverDate")) {
            	String val = item.getString("prism:coverDate");
            	pub.setYear(val.substring(0, val.indexOf("-")));
            }
            
            if (item.has("authors") && !item.isNull("authors")) {
	            JSONObject authors = item.getJSONObject("authors");
	            if (authors.has("author")) {
		            Object authorObject = authors.get("author");
		            if (authorObject instanceof JSONArray) {
		            	JSONArray authorList = authors.getJSONArray("author");
			            String authorString = "";
			            for (int j = 0; j < authorList.length(); j++) {
			            	String fullName = ((JSONObject)authorList.get(j)).getString("$");
			            	String[] names = fullName.split(" ");
			            	String given = "";
			            	for (int k=0; k < names.length-1; k++) {
			            		if (!names[k].isEmpty()) given += names[k].charAt(0);
			            	}
			            	authorString += names[names.length-1] + " " + given.trim();
			                if (j < authorList.length() - 1) authorString += "; ";
			            }
			            pub.setAuthor(authorString);
		            } else {
		            	String authorString = "";
		            	String fullName = authors.getString("author");
		            	String[] names = fullName.split(" ");
		            	String given = "";
		            	for (int k=0; k < names.length-1; k++) {
		            		if (!names[k].isEmpty()) given += names[k].charAt(0);
		            	}
		            	authorString += names[names.length-1] + " " + given.trim();
		            	pub.setAuthor(authorString);
		            }
	            }
	            
            }
            
            results.add(pub);
        }
        
        return results;
    }
    
    public static void main(String[] args) {
    	try {
    		Publication input = new Publication();
    		input.setTitle("Primary structure of the oligosaccharide moiety of hemocyanin from the scorpion Androctonus australis");
    		input.setAuthor("Debeire P, Montreuil J");
    		input.setJournalName("Carbohydr Res");
    		input.setYear("1986");
    		input.setVolume("151");
    		input.setPageRange("305-310");
    		List<Publication> results = new ElsevierAPIUtil("7f59af901d2d86f78a1fd60c1bf9426a").getPublicationByTitle("Primary structure of the oligosaccharide moiety of hemocyanin from the scorpion Androctonus australis");
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
