package coverageqc.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class GeneExon implements Comparable<Object> {
    
    //Tom Addition
    @XmlAttribute
    public Boolean containsDoNotCallAlways;
   // @XmlAttribute
   // public Boolean onlycontainsDoNotCall;
   // @XmlAttribute
    //public String typeOfDoNotCall;
    //Tom Addition
    @XmlAttribute
    public String chr;
    @XmlAttribute
    public long startPos;
    @XmlAttribute
    public long endPos;
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String qc; // "pass", "warn", "fail"
    @XmlAttribute
    public String ensemblGeneId; // parsed from exon BED custom field #5 (capture group 1): /.*\\|Ensembl-IDs: ([A-Z0-9\\.]*):([A-Z0-9\\.]*):([A-Z0-9\\.]*)\\|.*\\|.*/
    @XmlAttribute
    public String ensemblTranscriptId; // parsed from exon BED custom field #5 (capture group 2): /.*\\|Ensembl-IDs: ([A-Z0-9\\.]*):([A-Z0-9\\.]*):([A-Z0-9\\.]*)\\|.*\\|.*/
    @XmlAttribute
    public String ensemblExonId; // parsed from exon BED custom field #5 (capture group 3): /.*\\| /.*\\|Ensembl-IDs: ([A-Z0-9\\.]*):([A-Z0-9\\.]*):([A-Z0-9\\.]*)\\|.*\\|.*/
    @XmlAttribute
    public String ensemblExonNumber; // parsed from exon BED custom field #5: /.*\\|.*\\|Ensembl-exon-number: (.*)\\|.*/
    @XmlAttribute
    public String vendorGeneExonName; // parsed from exon BED custom field #5: /vendor-gene-exon-name: (.*)\\|.*\\|.*\\|.*/
    @XmlAttribute
    public float pctOfExon; // parsed from exon BED custom field #5: /.*\\|.*\\|.*\\|pct-of-exon: (.*)/
    @XmlAttribute
    public String refSeqAccNo;
    @XmlTransient
    public TreeMap<Long, Base> bases = new TreeMap<Long, Base>();
    @XmlElementWrapper(name = "bins")
    @XmlElement(name = "bin")
    public ArrayList<Bin> bins = new ArrayList<Bin>();
    @XmlElementWrapper(name = "amplicons")
    @XmlElement(name = "amplicon")
    public ArrayList<Amplicon> amplicons = new ArrayList<Amplicon>();
    public Amplicon codingRegion; // regions in the amplicon BED file that have a name with a "_coding" suffix
    @XmlElementWrapper(name = "variants")
    @XmlElement(name = "variant")
    public ArrayList<Variant> variants = new ArrayList<Variant>();
    //Tom Addition
    @XmlElementWrapper(name = "donotcallvariants")
    @XmlElement(name = "donotcallvariant")
    public ArrayList<Variant> donotcallVariantsAlways = new ArrayList<Variant>();

    /**
     * 
     */
    public GeneExon() {
        bins.add(new Bin(0, 99, "0-99"));
        bins.add(new Bin(100, 499, "100-499"));
        bins.add(new Bin(500, 999, "500-999"));
        bins.add(new Bin(1000, 9999999, "&ge;1000"));
    }
    
    public String nameForCompare;
    public int exonNumberForCompare;
    public String suffixForCompare;
    @java.lang.Override
    public int compareTo(Object o) {
        if(this.nameForCompare.compareTo(((GeneExon)o).nameForCompare) != 0) {
            return this.nameForCompare.compareTo(((GeneExon)o).nameForCompare);
        }
        else if((new Integer(this.exonNumberForCompare)).compareTo(new Integer(((GeneExon)o).exonNumberForCompare)) != 0) {
            return (new Integer(this.exonNumberForCompare)).compareTo(new Integer(((GeneExon)o).exonNumberForCompare));
        }
        else if(this.suffixForCompare != null) {
            return this.suffixForCompare.compareTo(((GeneExon)o).suffixForCompare);
        }
        return 0;
    }

    /**
     * 
     * @return List of bases, primarily for JAXB XML generation.
     */
    @XmlElementWrapper(name = "bases")
    @XmlElement(name = "base")
    public Collection<Base> getBasesAsList() {
        return bases.values();
    }
    
    /**
     * 
     * @return True if a variant was called in this exon.
     */
    @XmlAttribute
    public boolean getVariantCalled() {
        for(Base base : bases.values()) {
            if(base.variant != null) {
                return true;
            }
        }
        if(variants.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @return True if a variant was annotated in this exon.
     */
    @XmlAttribute
    public boolean getVariantAnnotated() {
        if(variants.size() > 0) {
            return true;
        }
        return false;
    }
    
    
    //Tom Addition
    /**
     * 
     * @return True if variantlist is the same size as donotcalllist, hence it only contains do not calls; also must be annotated to be true
     */
     @XmlAttribute
    public boolean getOnlyContainsDoNotCallAlways() {
        if(variants.size() == donotcallVariantsAlways.size()&&variants.size()>0) {
          //  onlycontainsDoNotCall = true;
            return true;
        }else
        {
        //onlycontainsDoNotCall = false;
        return false;
        }
        //return false;
    }
    
    

    /**
     * 
     * @param bedLine
     * @return 
     */
    public static GeneExon populate(String bedLine) {
        String[] fields = bedLine.split("\t");
        GeneExon geneExon = new GeneExon();
        geneExon.chr = fields[0];
        geneExon.startPos = Long.parseLong(fields[1]) + 1;
        //Tom Addition changed to +1 instead of +0 so will contain very last endPos (errors when variant is on the end position 
        geneExon.endPos = Long.parseLong(fields[2]) + 0;
        geneExon.name = fields[3];
        {
            Pattern pattern = Pattern.compile("vendor-gene-exon-name: (.*)\\|.*\\|.*\\|.*\\|.*");
            Matcher matcher = pattern.matcher(fields[5]);
            if(matcher.find()) {
                geneExon.vendorGeneExonName = matcher.group(1);
            }
        }
        {
            Pattern pattern = Pattern.compile(".*\\|Ensembl-IDs: ([A-Z0-9\\.]*):([A-Z0-9\\.]*):([A-Z0-9\\.]*)\\|");
            Matcher matcher = pattern.matcher(fields[5]);
            matcher.find();
            geneExon.ensemblGeneId = matcher.group(1);
            geneExon.ensemblTranscriptId = matcher.group(2);
            geneExon.ensemblExonId = matcher.group(3);
        }
        {
            Pattern pattern = Pattern.compile(".*\\|.*\\|Ensembl-exon-number: (.*)\\|.*\\|.*");
            Matcher matcher = pattern.matcher(fields[5]);
            if(matcher.find()) {
                geneExon.ensemblExonNumber = matcher.group(1);
            }
        }
        {
            Pattern pattern = Pattern.compile(".*\\|.*\\|.*\\|pct-of-exon: (.*)\\|.*");
            Matcher matcher = pattern.matcher(fields[5]);
            if(matcher.find()) {
                geneExon.pctOfExon = Math.round(Float.parseFloat(matcher.group(1)));
            }
        }
        // late addition of RefSeq accession no.
        {
            Pattern pattern = Pattern.compile(".*\\|.*\\|.*\\|.*\\|RefSeq-acc-no: (.*)");
            Matcher matcher = pattern.matcher(fields[5]);
            if(matcher.find()) {
                geneExon.refSeqAccNo = matcher.group(1);
            }
        }
        {
            Pattern pattern = Pattern.compile("([A-Za-z0-9]*)ex([0-9]*)(.*)?");
            Matcher matcher = pattern.matcher(geneExon.name);
            if(matcher.find()) {
                geneExon.nameForCompare = matcher.group(1) + "ex";
                geneExon.exonNumberForCompare = Integer.parseInt(matcher.group(2));
                geneExon.suffixForCompare = matcher.group(3);
            }
        }
        return geneExon;
    }
    
}
