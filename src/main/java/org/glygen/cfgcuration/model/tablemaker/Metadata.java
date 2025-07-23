package org.glygen.cfgcuration.model.tablemaker;

public class Metadata {
    Long metadataId;
    Datatype type;
    String value;
    String valueUri;
 	String valueId;
    
    /**
     * @return the id
     */
    public Long getMetadataId() {
        return metadataId;
    }
    /**
     * @param datatypeId the id to set
     */
    public void setMetadataId(Long id) {
        this.metadataId = id;
    }
    /**
     * @return the type
     */
    public Datatype getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(Datatype type) {
        this.type = type;
    }
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
   
    public String getValueUri() {
		return valueUri;
	}
	public void setValueUri(String valueUri) {
		this.valueUri = valueUri;
	}
	
	public String getValueId() {
		return valueId;
	}
	public void setValueId(String valueId) {
		this.valueId = valueId;
	}
}
