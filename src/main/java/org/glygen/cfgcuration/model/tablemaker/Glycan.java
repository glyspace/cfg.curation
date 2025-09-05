package org.glygen.cfgcuration.model.tablemaker;

public class Glycan {
	
	Long glycanId;
    String glytoucanID;
    
    /**
     * @return the glytoucanID
     */
    public String getGlytoucanID() {
        return glytoucanID;
    }
    /**
     * @param glytoucanID the glytoucanID to set
     */
    public void setGlytoucanID(String glytoucanID) {
        this.glytoucanID = glytoucanID;
    }
	public Long getGlycanId() {
		return glycanId;
	}
	public void setGlycanId(Long glycanId) {
		this.glycanId = glycanId;
	}
    
}
