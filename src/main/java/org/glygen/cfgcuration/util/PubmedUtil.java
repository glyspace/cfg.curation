package org.glygen.cfgcuration.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glygen.cfgcuration.model.Publication;
import org.glygen.cfgcuration.model.Species;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PubmedUtil {
	
	String apiKey;
	
	public PubmedUtil(String apiKey) {
		this.apiKey = apiKey;
	}
	
	static String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmode=json&term=";
	static String fetchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=";
	
	static String ncbiTaxUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&retmode=json&term=";
	static String ncbiFetchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&retmode=xml&id=";
	
	String doiUrl = "https://doi.org/";
	
	public List<Publication> getPublicationByTitle (String title) throws IOException {
		List<Publication> results = new ArrayList<>();
		
		title = title.replaceAll("\n", " ");
		title = title.replace(".", "");
		String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);

		String apiUrl = url + encodedTitle + "&field=Title";
		if (apiKey != null) {
			apiUrl += "&api_key=" + apiKey;
		}
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
	        HttpGet request = new HttpGet(apiUrl);
	        HttpResponse response = httpClient.execute(request);
	        if (response.getStatusLine().getStatusCode() > 300) {
	        	throw new IOException ("Error getting the search results from PubMed: " + response.getStatusLine().getReasonPhrase());
	        }
	        String json = EntityUtils.toString(response.getEntity());

	        JSONObject obj = new JSONObject(json);
	        JSONObject searchResult = obj.getJSONObject("esearchresult");
	        if (searchResult != null && searchResult.has("idlist")) {
		        JSONArray idList = searchResult.getJSONArray("idlist");
		        if (idList.length() > 0) {
		        	for (int i=0; i < idList.length(); i++) {
		        		String pmid = idList.getString(i);
		        		Publication pub = getPublicatonByPMID(pmid);
		        		results.add(pub);
		        		
		        	}
		        } 	
	        }
		} 
		return results;
	}
	
	public Publication getPublicatonByPMID (String pmid) throws IOException {
		String apiUrl = fetchUrl + pmid;
		if (apiKey != null) {
			apiUrl += "&api_key=" + apiKey;
		}
		try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            Publication pub = new Publication();
            pub.setPmid(pmid);

            NodeList articleList = doc.getElementsByTagName("PubmedArticle");
            for (int i = 0; i < articleList.getLength(); i++) {
                Element article = (Element) articleList.item(i);
                
                NodeList articleIdList = doc.getElementsByTagName("ArticleId");
                for (int j=0; j < articleIdList.getLength(); j++) {
                	Element articleId = (Element) articleIdList.item(j);
                	if ("doi".equals(articleId.getAttribute("IdType"))) {
                		String doi = articleId.getTextContent();
                		pub.setDoiId(doi);
                		break;
                	}
                }

                // Title
                String title = article.getElementsByTagName("ArticleTitle").item(0).getTextContent();
                if (title.endsWith(".")) {
                	title = title.substring(0, title.length()-1);
                }
                pub.setTitle(title);
                
                // Journal
                String journal = article.getElementsByTagName("ISOAbbreviation").item(0).getTextContent();
                if (journal != null) 
                	pub.setJournalName(journal);
                

                // Publication Date
                Element pubDate = (Element) article.getElementsByTagName("PubDate").item(0);
                Element year = (Element) pubDate.getElementsByTagName("Year").item(0);
                if (year != null) {
                	pub.setYear(year.getTextContent());
                }
                
                // Inside your existing loop over PubmedArticle
                Element journalIssue = (Element) article.getElementsByTagName("JournalIssue").item(0);
                String volume = getTagValue(journalIssue, "Volume");
                if (volume != null) {
                	pub.setVolume(volume);
                }
                
                Element pagination = (Element) article.getElementsByTagName("Pagination").item(0);
                if (pagination != null) {
                	String start = getTagValue(pagination, "StartPage");
                	String end = getTagValue(pagination, "EndPage");
                	if (start != null && end != null) {
                		pub.setPageRange(start+"-"+ end);
                	}	
                }
                
                // Authors
                NodeList authors = article.getElementsByTagName("Author");
                String authorList = "";
                for (int j = 0; j < authors.getLength(); j++) {
                    Element author = (Element) authors.item(j);
                    String lastName = getTagValue(author, "LastName");
                    String foreName = getTagValue(author, "Initials");
                    if (lastName != null && foreName != null) {
                        authorList += replaceUmlaut(lastName) + " " + replaceUmlaut(foreName) + "; ";
                    }
                }
                if (!authorList.isEmpty() && authorList.trim().length() > 1)
                	authorList = authorList.trim().substring(0, authorList.trim().length()-1);
                pub.setAuthor(authorList);
            }
            return pub;

        } catch (Exception e) {
        	throw new IOException (e);
        }
	}
	
    private static String getTagValue(Element parent, String tagName) {
	    NodeList list = parent.getElementsByTagName(tagName);
	    if (list.getLength() > 0) {
	        return list.item(0).getTextContent();
	    }
	    return null;
	}
    
    public Species findCommonAncestor (String id1, String id2) throws IOException {
    	String apiUrl = ncbiFetchUrl + id1;
		if (apiKey != null) {
			apiUrl += "&api_key=" + apiKey;
		}
		
		List<Species> hierarchy1 = new ArrayList<>();
		List<Species> hierarchy2 = new ArrayList<>();
		try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            
            NodeList lineageList = doc.getElementsByTagName("LineageEx");
            if (lineageList.getLength() > 0) {
                Element lineage = (Element) lineageList.item(0);
                NodeList taxonList = lineage.getElementsByTagName("Taxon");
                
                for (int i=0; i < taxonList.getLength(); i++) {
                	Element taxon = (Element) taxonList.item(i);
                	NodeList idList = taxon.getElementsByTagName("TaxId");
                	NodeList nameList = taxon.getElementsByTagName("ScientificName");
                	NodeList rankList = taxon.getElementsByTagName("Rank");
                	Species species = new Species();
                	species.setName(nameList.item(0).getTextContent());
                	species.setRank(rankList.item(0).getTextContent());
                	species.setId(idList.item(0).getTextContent());
                	hierarchy1.add(species);
                }
            }
            
            try {
		        Thread.sleep(100); // wait 100 milliseconds between requests
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt(); // restore interrupted status
		    }
            
            apiUrl = ncbiFetchUrl + id2;
    		if (apiKey != null) {
    			apiUrl += "&api_key=" + apiKey;
    		}
            url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            inputStream = conn.getInputStream();

            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            doc = builder.parse(inputStream);
            
            
            lineageList = doc.getElementsByTagName("LineageEx");
            if (lineageList.getLength() > 0) {
                Element lineage = (Element) lineageList.item(0);
                NodeList taxonList = lineage.getElementsByTagName("Taxon");
                
                for (int i=0; i < taxonList.getLength(); i++) {
                	Element taxon = (Element) taxonList.item(i);
                	NodeList idList = taxon.getElementsByTagName("TaxId");
                	NodeList nameList = taxon.getElementsByTagName("ScientificName");
                	NodeList rankList = taxon.getElementsByTagName("Rank");
                	Species species = new Species();
                	species.setName(nameList.item(0).getTextContent());
                	species.setRank(rankList.item(0).getTextContent());
                	species.setId(idList.item(0).getTextContent());
                	hierarchy2.add(species);
                }
            }
            
            // check if id1 exists in hierarchy2 or if id2 exists in hierarchy1
            for (Species s: hierarchy2) {
            	if (s.getId().equals(id1)) {
            		return s;
            	}
            }
            
            for (Species s: hierarchy1) {
            	if (s.getId().equals(id2)) {
            		return s;
            	}
            }
            
            // if not in each other's hierarchy, find the first common one
            for (int i = hierarchy1.size() -1; i >= 0; i--) {
            	Species sp1 = hierarchy1.get(i);
            	for (Species sp2: hierarchy2) {
                	if (sp1.getId().equals(sp2.getId())) {
                		return sp1;
                	}
                }
            }
            
            // if not in each other's hierarchy, find the first common one
            for (int i = hierarchy2.size() -1; i >= 0; i--) {
            	Species sp1 = hierarchy2.get(i);
            	for (Species sp2: hierarchy1) {
                	if (sp1.getId().equals(sp2.getId())) {
                		return sp1;
                	}
                }
            }
            
            return null;
		} catch (Exception e) {
        	throw new IOException (e);
        }
    }
    
    public boolean checkIfSameHierarchy  (String id1, String id2) throws IOException {
    	String apiUrl = ncbiFetchUrl + id1;
		if (apiKey != null) {
			apiUrl += "&api_key=" + apiKey;
		}
		
		List<String> hierarchy1 = new ArrayList<>();
		List<String> hierarchy2 = new ArrayList<>();
		try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            
            NodeList lineageList = doc.getElementsByTagName("LineageEx");
            if (lineageList.getLength() > 0) {
                Element lineage = (Element) lineageList.item(0);
                NodeList taxIdList = lineage.getElementsByTagName("TaxId");
                
                for (int i=0; i < taxIdList.getLength(); i++) {
                	Element taxId = (Element) taxIdList.item(i);
                	hierarchy1.add(taxId.getTextContent());
                }
            }
            
            try {
		        Thread.sleep(100); // wait 100 milliseconds between requests
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt(); // restore interrupted status
		    }
            
            apiUrl = ncbiFetchUrl + id2;
    		if (apiKey != null) {
    			apiUrl += "&api_key=" + apiKey;
    		}
            url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            inputStream = conn.getInputStream();

            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            doc = builder.parse(inputStream);
            
            
            lineageList = doc.getElementsByTagName("LineageEx");
            if (lineageList.getLength() > 0) {
                Element lineage = (Element) lineageList.item(0);
                NodeList taxIdList = lineage.getElementsByTagName("TaxId");
                
                for (int i=0; i < taxIdList.getLength(); i++) {
                	Element taxId = (Element) taxIdList.item(i);
                	hierarchy2.add(taxId.getTextContent());
                }
            }
            
            // check if id1 exists in hierarchy2 or if id2 exists in hierarchy1
            for (String id: hierarchy2) {
            	if (id.equals(id1)) {
            		return true;
            	}
            }
            
            for (String id: hierarchy1) {
            	if (id.equals(id2)) {
            		return true;
            	}
            }
            
            
            
            return false;
            
		} catch (Exception e) {
        	throw new IOException (e);
        }
    	
    }
    
    public Publication getPublicationByDOI (String doi) throws IOException, Exception {
		String pubUrl = doiUrl + doi;

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
	        HttpGet request = new HttpGet(pubUrl);
	        request.setHeader("Accept", "application/vnd.citationstyles.csl+json");
	        HttpResponse response = httpClient.execute(request);
	        if (response.getStatusLine().getStatusCode() > 300) {
	        	throw new IOException ("Error getting the publication with DOI: " + response.getStatusLine().getReasonPhrase());
	        }
	        String json = EntityUtils.toString(response.getEntity());
	        return createFromJson (json);
        }
	}
	
	private Publication createFromJson(String json) {
		Publication pub = new Publication();
		final JSONObject obj = new JSONObject(json);
		if (obj.has("page")) {
			String pageRange = obj.getString("page");
			if (pageRange != null) {
				pub.setPageRange(pageRange);
			}
		}
		if (obj.has("DOI")) pub.setDoiId(obj.getString("DOI"));
		if (obj.has("title")) pub.setTitle(obj.getString("title"));
		if (obj.has("volume")) pub.setVolume(obj.getString("volume"));
		if (obj.has("container-title")) pub.setJournalName(obj.getString("container-title"));
		if (obj.has("created")) {
			JSONObject dateObject = obj.getJSONObject("created");
			if (dateObject != null) {
				String dateTime = dateObject.getString("date-time");
				if (dateTime != null && dateTime.length() > 4) {
					try {
						pub.setYear(dateTime.substring(0, 4));
					} catch (NumberFormatException e) {
						//ignore
					}
				}
			}
		}
		if (obj.has("author")) {
			JSONArray authors = obj.getJSONArray("author");
			String authorsString = "";
			for (int i=0; i < authors.length(); i++) {
				JSONObject author = authors.getJSONObject(i);
				authorsString += author.getString("family") + author.getString("given").charAt(0) + ", ";
			}
			pub.setAuthor(authorsString.substring(0, authorsString.lastIndexOf(",")));
		}
		return pub;
	}
    
    public Species getSpeciesByID (String id) throws IOException {
		String apiUrl = ncbiFetchUrl + id;
		if (apiKey != null) {
			apiUrl += "&api_key=" + apiKey;
		}
		try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            Species species = new Species();
            species.setId(id);
            
            NodeList articleList = doc.getElementsByTagName("Taxon");
            if (articleList.getLength() > 0) {
                Element article = (Element) articleList.item(0);
                
                // Scientific Name
                String name = article.getElementsByTagName("ScientificName").item(0).getTextContent();
                species.setName(name);
                
                // Rank
                String rank = article.getElementsByTagName("Rank").item(0).getTextContent();
                species.setRank(rank);
                return species;
            }
            
            return null;
		} catch (Exception e) {
        	throw new IOException (e);
        }
    }
    
    public List<Species> getSpecies (String name) throws IOException {
		List<Species> results = new ArrayList<>();
		
		name = name.replaceAll("\n", " ");
		
		String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);

		String apiUrl = ncbiTaxUrl + encodedName;
		if (apiKey != null) {
			apiUrl += "&api_key=" + apiKey;
		}
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
	        HttpGet request = new HttpGet(apiUrl);
	        HttpResponse response = httpClient.execute(request);
	        if (response.getStatusLine().getStatusCode() > 300) {
	        	throw new IOException ("Error getting the search results from PubMed: " + response.getStatusLine().getReasonPhrase());
	        }
	        String json = EntityUtils.toString(response.getEntity());

	        JSONObject obj = new JSONObject(json);
	        JSONArray idList = obj.getJSONObject("esearchresult").getJSONArray("idlist");
	        if (idList.length() > 0) {
	        	for (int i=0; i < idList.length(); i++) {
	        		String taxonomyId = idList.getString(i);
	        		Species species = getSpeciesByID(taxonomyId);
	        		if (species != null) {
	        			results.add(species);
	        			species.setMatchedName(name);
	        		}
	        	}
	        } 	        
		} 
		
		return results;
	}
    
    public static String replaceUmlaut(String input) {
   	 
        // replace all lower Umlauts
        String output = input.replace("ü", "ue")
                             .replace("ö", "oe")
                             .replace("ä", "ae")
                             .replace("ß", "ss");
    
        // first replace all capital Umlauts in a non-capitalized context (e.g. Übung)
        output = output.replaceAll("Ü(?=[a-zäöüß ])", "Ue")
                       .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                       .replaceAll("Ä(?=[a-zäöüß ])", "Ae");
    
        // now replace all the other capital Umlauts
        output = output.replace("Ü", "UE")
                       .replace("Ö", "OE")
                       .replace("Ä", "AE");
    
        return output;
    }
    
    public static String ignoreUmlaut(String input) {
   	 
        // replace all lower Umlauts
        String output = input.replace("ü", "u")
                             .replace("ö", "o")
                             .replace("ä", "a")
                             .replace("ß", "b")
                             .replace("ó", "o")
                             .replace("é", "e")
                             .replace("á", "a");
    
        // first replace all capital Umlauts in a non-capitalized context (e.g. Übung)
        output = output.replaceAll("Ü(?=[a-zäöüß ])", "U")
                       .replaceAll("Ö(?=[a-zäöüß ])", "O")
                       .replaceAll("Ä(?=[a-zäöüß ])", "A");
    
        // now replace all the other capital Umlauts
        output = output.replace("Ü", "U")
                       .replace("Ö", "O")
                       .replace("Ä", "A");
    
        return output;
    }
    
    public static void main(String[] args) {
    	try {
		/*	List<Publication> results = new PubmedUtil(null).getPublicationByTitle("Triterpenoid glycosides from Stauntonia hexaphylla");
			for (Publication result: results) {
				System.out.println (result.getTitle() + "\n" + result.getAuthor() + "\n" + result.getJournal());
			}
			
			try {
		        Thread.sleep(500); // wait 100 milliseconds between requests
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt(); // restore interrupted status
		    }*/
			
			Boolean result = new PubmedUtil(null).checkIfSameHierarchy("499", "500");
			System.out.println ("They are in the same hierarchy? " + result);
			
			try {
		        Thread.sleep(500); // wait 100 milliseconds between requests
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt(); // restore interrupted status
		    }
			
			Species species = new PubmedUtil(null).findCommonAncestor("4498", "50455");
			if (species != null)
				System.out.println ("Common parent: " + species.getName() + " id: "+ species.getId() + " rank: " + species.getRank());
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
