package coverageqc.data;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

//Tom addition
import java.util.ArrayList;
//
/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
@XmlRootElement
public class Variant {
    
    @XmlAttribute
    public String gene;
    @XmlAttribute
    public String variant;
    @XmlAttribute
    public Integer chr;
    @XmlAttribute
    public Long coordinate;
    @XmlAttribute
    public String type;
    @XmlAttribute
    public String genotype;
    @XmlAttribute
    public Float altVariantFreq;
    @XmlAttribute
    public Long readDepth;
    @XmlAttribute
    public Long altReadDepth;
    @XmlAttribute
    public String consequence;
    @XmlAttribute
    public String cosmicId;
    @XmlAttribute
    public String hgvsc;
    @XmlAttribute
    public String hgvsp;
    @XmlAttribute
    public String dbSnpIdPrefix;
    @XmlAttribute
    public String dbSnpIdSuffix;
    @XmlAttribute
    public String filters;
    @XmlAttribute
    public Float alleleFreqGlobalMinor;
    //TOM ADDITION
    @XmlAttribute
    public String geneMutation;
    @XmlAttribute
    //adding the complete version of the HGVSc since this will make it easy to check if variant is in DoNotCall list
    public String hgvscComplete;
    @XmlAttribute
     public String hgvspComplete;
    @XmlAttribute
     public String ensp;
    @XmlAttribute
    public Boolean onTheDoNotCallList;
    @XmlAttribute
    public String typeOfDoNotCall;
    @XmlAttribute
    public String transcript;
    
    @XmlAttribute
    public String refSeqAccNoHgvsc;
    @XmlAttribute
    public String refSeqAccNoTranscript;
 
    //end of Tom Addition
            //Tom Addition I am adding in the input of a String[][]
    public static Variant populate(String tsvHeadingLine, String tsvDataLine, ArrayList<DoNotCall> donotcalls) {
        Variant variant = new Variant();
        String[] headingsArray = tsvHeadingLine.split("\t");
        HashMap<String, Integer> headings = new HashMap<String, Integer>();
        for(int x = 0; x < headingsArray.length; x++) {
            headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
        }
        String[] dataArray = tsvDataLine.split("\t");
        variant.gene = dataArray[headings.get("Gene").intValue()];
        variant.variant = dataArray[headings.get("Variant").intValue()];
        variant.chr = Integer.valueOf(dataArray[headings.get("Chr").intValue()] != null && !dataArray[headings.get("Chr").intValue()].isEmpty() ? dataArray[headings.get("Chr").intValue()] : null);
        // note: subtracting zero (0)
        variant.coordinate = Long.valueOf(dataArray[headings.get("Coordinate").intValue()] != null && !dataArray[headings.get("Coordinate").intValue()].isEmpty() ? dataArray[headings.get("Coordinate").intValue()] : null) - 0;
        variant.type = dataArray[headings.get("Type").intValue()];
        variant.genotype = dataArray[headings.get("Genotype").intValue()];
        variant.altVariantFreq = Float.valueOf(dataArray[headings.get("Alt Variant Freq").intValue()] != null && !dataArray[headings.get("Alt Variant Freq").intValue()].isEmpty() ? dataArray[headings.get("Alt Variant Freq").intValue()] : null);
        variant.readDepth = Long.valueOf(dataArray[headings.get("Read Depth").intValue()] != null && !dataArray[headings.get("Read Depth").intValue()].isEmpty() ? dataArray[headings.get("Read Depth").intValue()] : null);
        variant.altReadDepth = Long.valueOf(dataArray[headings.get("Alt Read Depth").intValue()] != null && !dataArray[headings.get("Alt Read Depth").intValue()].isEmpty() ? dataArray[headings.get("Alt Read Depth").intValue()] : null);
        variant.consequence = dataArray[headings.get("Consequence").intValue()];
        variant.cosmicId = dataArray[headings.get("COSMIC ID").intValue()];
        variant.filters = dataArray[headings.get("Filters").intValue()];
         //TOM ADDITION
        //note this gets Transcript_27 instead of Transcript HGNC_25 because the way substring works it gets string to left of first underscore and in Transcript HGNC_25 case this is Transcript HGNC
        variant.transcript = dataArray[headings.get("Transcript").intValue()];
         if(dataArray[headings.get("HGVSc")]!= null)
        {
        variant.hgvscComplete = dataArray[headings.get("HGVSc").intValue()];
        }
         if(dataArray[headings.get("HGVSp")]!= null)
        {
        variant.hgvspComplete = dataArray[headings.get("HGVSp").intValue()];
        }
        if(dataArray[headings.get("ENSP")]!= null)
        {
        variant.ensp = dataArray[headings.get("ENSP").intValue()];
        }
        if (donotcalls!=null)
        {
        variant = CheckIfOnDoNOTCallList(variant,donotcalls);
        }else
        {
            variant.onTheDoNotCallList=false;
	    variant.typeOfDoNotCall = "Not on lab list/Potentially Valid";
        }
        //end of TOM addition
        // note: parsing out RefSeq IDs
        if(dataArray[headings.get("HGVSc")] != null) {
           Pattern pattern = Pattern.compile("^(.*):(.*)$");
            Matcher matcher = pattern.matcher(dataArray[headings.get("HGVSc")]);
            if(matcher.find()) {
                variant.refSeqAccNoHgvsc = matcher.group(1);
                variant.hgvsc = matcher.group(2);
            }
            else {
                //variant.hgvsc = dataArray[headings.get("HGVSc")];
                System.out.println("invalid HGVSc format");
                System.exit(1);
            }
        }
        variant.refSeqAccNoTranscript = dataArray[headings.get("Transcript")];
        if(variant.refSeqAccNoTranscript == null
            || variant.refSeqAccNoTranscript.length() == 0
            || variant.refSeqAccNoHgvsc == null
            || variant.refSeqAccNoHgvsc.length() == 0
            || !variant.refSeqAccNoTranscript.equals(variant.refSeqAccNoHgvsc)) {
            System.out.println("RefSeq accession number mismatch between Transcript and HGVSc");
            System.exit(1);
        }
        // note: parsing out RefSeq IDs
        if(dataArray[headings.get("HGVSp")] != null) {
            Pattern pattern = Pattern.compile("^.*:(.*)$");
            Matcher matcher = pattern.matcher(dataArray[headings.get("HGVSp")]);
            if(matcher.find()) {
                variant.hgvsp = matcher.group(1);
            }
            else {
                variant.hgvsp    = dataArray[headings.get("HGVSp")];
            }
        }
        if(dataArray[headings.get("dbSNP ID")] != null) {
            Pattern pattern = Pattern.compile("^([A-Za-z]*)([0-9]*)$");
            Matcher matcher = pattern.matcher(dataArray[headings.get("dbSNP ID")]);
            if(matcher.find()) {
                variant.dbSnpIdPrefix = matcher.group(1);
                variant.dbSnpIdSuffix = matcher.group(2);
            }
            else {
                variant.dbSnpIdPrefix = dataArray[headings.get("dbSNP ID")];
            }
        }
        variant.alleleFreqGlobalMinor = Float.valueOf(dataArray[headings.get("Allele Freq Global Minor").intValue()] != null && !dataArray[headings.get("Allele Freq Global Minor").intValue()].isEmpty() ? dataArray[headings.get("Allele Freq Global Minor").intValue()] : null);
         //TOM ADDITION
            variant.geneMutation = variant.gene; 
            variant.geneMutation += " ";
            variant.geneMutation += variant.hgvsc;
    
      ////end of TOM ADDITION
        
        
        
        return variant;
    }
    
    //Tom Addition///////////////////////////////////////////////////////////////////////////////
	private static Variant CheckIfOnDoNOTCallList(Variant variant2,
			ArrayList<DoNotCall> donotcalls) {
		// TODO Auto-generated method stub
		variant2.onTheDoNotCallList=false;
		variant2.typeOfDoNotCall = "Not on lab list /Potentially Valid";
                boolean currentlyCanDefinativelyCompare;
                comparisonloop: for(int i=0; i<donotcalls.size(); i++)
		{
			
			//System.out.println(i);
                        DoNotCall currentdonotcall = donotcalls.get(i);
                        String donotcallcomparison=null;
                        String variantcomparison=null;
                        String donotcallcomparison_transcript=currentdonotcall.transcript;
                        String variantcomparison_transcript=variant2.transcript; 
                        Long donotcallcomparison_coordinate=currentdonotcall.coordinate;
                        Long variantcomparison_coordinate=variant2.coordinate; 
                       
                        //Currently only checking Transcript_27 and if empty in tsv or do not call will crash
                        if(currentdonotcall.hgvsc!=null && variant2.hgvscComplete!=null)
                        {
                            currentlyCanDefinativelyCompare = true;
                            donotcallcomparison=currentdonotcall.hgvsc;
                            variantcomparison=variant2.hgvscComplete; 
                        }else
                        {
                            currentlyCanDefinativelyCompare=false;
                        }
//                        else if(currentdonotcall.hgvsc!=null && variant2.hgvscComplete!=null)
//                        {
//                            
//                            donotcallcomparison=currentdonotcall.hgvsc;
//                            variantcomparison=variant2.hgvscComplete; 
//                        }else if(currentdonotcall.ensp!=null && variant2.ensp!=null)
//                        {
//                            donotcallcomparison=currentdonotcall.ensp;
//                            variantcomparison=variant2.ensp; 
//                        }
//                        else
//                        {
//                        System.out.println("ERROR: the current donotcall can't be compared!  Crashing to prevent abnormal behavior!");
//                        System.exit(1);
//                        }
                        
			if(donotcallcomparison_transcript.equals(variantcomparison_transcript) && variantcomparison_coordinate.equals(donotcallcomparison_coordinate))
			{
				variant2.onTheDoNotCallList=true;
                                //adding a fourth call type, meaning if it is the exact location of a do not call but does not match by hgvsccomple then a separate warning
                                if (currentlyCanDefinativelyCompare)
                                { 
                                    if(donotcallcomparison.equals(variantcomparison))
                                    {
				         variant2.typeOfDoNotCall=currentdonotcall.callType;
                                         //stop looking
                                         break comparisonloop;
                                    }else
                                    {
                                        variant2.typeOfDoNotCall="In same location as do not call variant.  However mutation is different";
                                        //still look because maybe better matching variant is available, hence don't break loop
                                    }
                                }else
                                {
                                    variant2.typeOfDoNotCall="In same location as do not call variant. However can't compare if same mutation.";
                                     //still look because maybe better matching variant is available, hence don't break loop
                                }
			}
			
		}
		return variant2;
		
	}
	//Tom ADDITION////////////////////////////////////////////////////////////////////////////////////////////////
  
    
    
}
