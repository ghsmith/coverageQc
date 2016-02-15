package coverageqc.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
@XmlRootElement
public class Vcf {

    @XmlAttribute
    final static String BUILD = "20160204";
    @XmlAttribute
    final static String VERSION = "1.6";
    
    @XmlAttribute
    public String fileName;
    @XmlAttribute
    public String variantsExcludedFromCoverageQC;
    @XmlAttribute
    public String doNotCallFileName;
    @XmlAttribute
    public String filesToUseFileName;
    @XmlAttribute
    public String genesToExcludeFileName;
    @XmlAttribute
    public String alignersFileName;
    @XmlAttribute
    public String exonBedFileName;
    @XmlAttribute
    public String ampliconBedFileName;
    @XmlAttribute
    public String variantTsvFileName;
    @XmlAttribute
    public String annovarFiles;
    @XmlAttribute
    public Integer variantTsvFileLineCount;
    @XmlAttribute
    public Date runDate;
    @XmlElementWrapper(name = "geneExons")
    @XmlElement(name = "geneExon")
    public TreeSet<GeneExon> geneExons = new TreeSet<GeneExon>();
    //making as an ArrayList with each array list specific for an aligner
    @XmlAttribute
    //public ArrayList<ArrayList<URL>> bedBamVCFFileUrlsList = new ArrayList<ArrayList<URL>>();
    public ArrayList<URL> bedBamVcfFileUrls = new ArrayList<URL>(); // these are used to construct an IGV link
    @XmlAttribute
    public String usedAligner;
    
    @XmlTransient
    public TreeMap<String, Base> bases = new TreeMap<String, Base>(); // key is chr|pos (e.g., "chr9|320001")

    /**
     * 
     * @return 
     */
    @XmlAttribute
    public long getBaseCount() {
        return bases.size();
    }

    /**
     * 
     * @return 
     */
    @XmlAttribute
    public long getFilteredAnnotatedVariantCount() {
        long filteredAnnotatedVariantCount = 0;
        for(GeneExon geneExon : geneExons) {
            filteredAnnotatedVariantCount += geneExon.variants.size();
        }
        return filteredAnnotatedVariantCount;
    }
    
    /**
     * 
     * @return 
     */
    @XmlAttribute
    public long getAmpliconCount() {
        TreeSet<Amplicon> amplicons = new TreeSet<Amplicon>();
        for(GeneExon geneExon : geneExons) {
            for(Amplicon amplicon : geneExon.amplicons) {
                amplicons.add(amplicon);
            }
        }
        return amplicons.size();
    }
    
    /**
     * 
     * @return 
     */
    @XmlAttribute
    public long getReadDepthCount() {
        long readDepthCount = 0;
        for(Base base : bases.values()) {
            readDepthCount += base.readDepths.size();
        }
        return readDepthCount;
    }

    /**
     * 
     * @param chr
     * @param pos
     * @return The set of exons that contain the coordinate.
     */
    public TreeSet<GeneExon> findGeneExonsForChrPos(String chr, long pos) {
        TreeSet<GeneExon> matchedGeneExons = new TreeSet<GeneExon>();
        for(GeneExon geneExon : geneExons) {
            if(geneExon.chr.equals(chr) && geneExon.startPos <= pos && geneExon.endPos >= pos) {
                matchedGeneExons.add(geneExon);
            }
        }
        return matchedGeneExons;
    }
    
 
    
    
    
    
    
    /**
     * 
     * @param chr
     * @param startPos
     * @param endPos
     * @return The set of exons that contain any part of the region.
     */
    public TreeSet<GeneExon> findGeneExonsForChrRange(String chr, long startPos, long endPos) {
        TreeSet<GeneExon> matchedGeneExons = new TreeSet<GeneExon>();
        for(GeneExon geneExon : geneExons) {
            if(geneExon.chr.equals(chr) && (geneExon.startPos <= endPos && geneExon.endPos >= startPos)) {
                matchedGeneExons.add(geneExon);
            }
        }
        return matchedGeneExons;
    }

    /**
     * @todo Hacking UNC notation if drive is "S:" ("S:" = "//euh/ehc").
     * @return A comma separated string of file URLs suitable consumption by
     * IGV.
     */
    @XmlAttribute
    public String getBedBamVcfFileUrlsAsString() {
        StringBuilder fileUrlsAsString = new StringBuilder();
        for(URL fileUrl : bedBamVcfFileUrls) {
            if(fileUrl.toString().substring(6).startsWith("S:")) {
                fileUrlsAsString.append((fileUrlsAsString.length() > 0  ? "," : "") + "file:////euh/ehc" + fileUrl.toString().substring(8));
            }
            else {
                fileUrlsAsString.append((fileUrlsAsString.length() > 0  ? "," : "") + "file:///" + fileUrl.toString().substring(6));
            }
        }
        return fileUrlsAsString.toString();
    }
    
}
