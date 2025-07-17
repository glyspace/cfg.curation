package org.glygen.cfgcuration.util;

import org.grits.toolbox.glycanarray.om.parser.cfg.CFGMasterListParser;

public class SequenceUtils {
    
    public static String parseIUPACSequence (String sequence) {   
        CFGMasterListParser parser = new CFGMasterListParser();
        String searchSequence = parser.translateSequence(cleanupSequence(sequence.trim()));     
        return searchSequence;
    }
    
    public static String getSequence(String a_sequence) {
        int t_index = a_sequence.lastIndexOf("-");
        String sequence = a_sequence.substring(0, t_index).trim();
        return cleanupSequence(sequence);
    }
    
    public static String cleanupSequence (String a_sequence) {
        String sequence = a_sequence.trim();
        sequence = sequence.replaceAll(" ", "");
        sequence = sequence.replaceAll("\u00A0", "");
        if (sequence.endsWith("1") || sequence.endsWith("2")) {
            sequence = sequence.substring(0, sequence.length()-1);
        }
        return sequence;
    }
    
    public static String getLinker(String a_sequence) {
        int t_index = a_sequence.lastIndexOf("-");
        String linker = a_sequence.substring(t_index+1).trim();
                
        linker = linker.replaceAll("\u00A0", "");
        return linker;
    }

}
