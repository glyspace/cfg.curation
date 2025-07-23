package org.glygen.cfgcuration.model.tablemaker;

import java.util.List;

public class Datatype {
    Long datatypeId;
    String uri;
    String name;
    String description;
    String example;
    String wikiUrl;
    Namespace namespace;
    Boolean multiple = false;
    List<String> allowedValues;
    
    /**
     * @return the id
     */
    public Long getDatatypeId() {
        return datatypeId;
    }
    /**
     * @param datatypeId the id to set
     */
    public void setDatatypeId(Long id) {
        this.datatypeId = id;
    }
    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the namespace
     */
    public Namespace getNamespace() {
        return namespace;
    }
    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

	public Boolean getMultiple() {
		return multiple;
	}
	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}
	
	public List<String> getAllowedValues() {
		return allowedValues;
	}
	public void setAllowedValues(List<String> allowedValues) {
		this.allowedValues = allowedValues;
	}
	
	public String getExample() {
		return example;
	}
	public void setExample(String example) {
		this.example = example;
	}
	
	public String getWikiUrl() {
		return wikiUrl;
	}
	public void setWikiUrl(String wikiUrl) {
		this.wikiUrl = wikiUrl;
	}

}
