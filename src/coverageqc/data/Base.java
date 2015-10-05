package coverageqc.data;

import java.util.HashSet;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import htsjdk.samtools.*;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.vcf.*;
import htsjdk.variant.variantcontext.*;
import htsjdk.variant.variantcontext.CommonInfo;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
public class Base implements Comparable<Object> {
    
    @XmlAttribute
    public String chr;
    @XmlAttribute
    public long pos;
    @XmlTransient
    public HashSet<Long> readDepths = new HashSet<Long>();
    @XmlAttribute
    public String variant; // e.g., "A>G"
    @XmlAttribute
    public String variantText; // e.g., "A>G (804>34 reads)"
    @XmlAttribute
    public String quality;
    @XmlAttribute
    public String filter;
    
    @java.lang.Override
    public int compareTo(Object o) {
        return (new Long(this.pos)).compareTo(new Long(((Base)o).pos));
    }

    /**
     * @todo This is currently a "max" operation; this relates to how (I think)
     * indels are represented in the gVCF file, with multiple entries for the
     * same position.
     * @return The read depth that will be used for this position.
     */
    @XmlAttribute
    public long getTotalReadDepth() {
        long totalReadDepth = 0;
        for(Long readDepth : readDepths) {
            if(readDepth.longValue() > totalReadDepth) {
                totalReadDepth = readDepth.longValue();
            }
        }
        return totalReadDepth;
    }

    /**
     * 
     * @param vcfLine
     * @param bases
     * @return 
     */
    public static Base populate(VariantContext vcfLine, TreeMap<String, Base> bases) {
       // String[] fields = vcfLine.split("\t");
        String chr = vcfLine.getContig();
        long pos = vcfLine.getStart();
        GenotypesContext genotype_field = vcfLine.getGenotypes();
        //getting the first genotype, should at least have this
        Genotype this_gc_1= genotype_field.get(0);
        CommonInfo info_field = vcfLine.getCommonInfo();
        List<String> sampleNames = vcfLine.getSampleNamesOrderedByName();
        int numberOfSamples = sampleNames.size();
        //long pos = Long.parseLong(fields[1]) - 0; // VCF is base 1, BED is base 0, I am using base 1
        // special handling for read depth:
        // [1] truncate "DP=" prefix
        // [2] maintaining set of read depths in Base class, since the same
        //     position can appear multiple time in the genomic VCF file;
        //     at this point I am taking the unique read depths for each
        //     position and maxing it - this might be risky
        
        //Genotype current_genotype = genotype_field.get(0);
        //Getting the read depth
        String variant = null;
        long readDepth=0;
        //first checking if the info field has read depth, this is the case for the illumina genome vcf files
        //note: for the mpileup file the INFO DP is the raw depth which you don't want, want the DP in the genotype field
        //illumina doesn't have in the genotype field so if not present in genotype know it is an illumina file and will take from there
        //if not illumina, want to take from genotype field
        if (info_field.hasAttribute("DP") && !(this_gc_1.hasDP()))
          {
                readDepth=info_field.getAttributeAsInt("DP", 0);
          }
        else
        {
            Iterator<Genotype> genotype_iterator = genotype_field.iterator();
            while(genotype_iterator.hasNext())
            {
                Genotype current_geno=genotype_iterator.next();
                readDepth=readDepth + current_geno.getDP();
            }
            
        }
        
        //will put in when going through annotated files
//        Allele reference_allele = vcfLine.getReference();
//
//        List<Allele> alternate_list = vcfLine.getAlternateAlleles();
//        String alternate_allele = "";
//        
//        if (alternate_list.size() > 0) {
//            for (int i = 0; i < alternate_list.size(); i++) {
//                alternate_allele = alternate_allele + alternate_list.get(i);
//                if (i + 1 != alternate_list.size()) {
//                    alternate_allele = alternate_allele + ",";
//                }
//            }
//
//            variant = reference_allele.getBaseString() + ">" + alternate_allele;
//        }


        
        
        
       
        Base base = bases.get(chr + "|" + Long.toString(pos));
        
        if(base == null) {
            base = new Base();
            base.chr = chr;
            base.pos = pos;
            bases.put(chr + "|" + Long.toString(pos), base);
        }
        base.readDepths.add(new Long(readDepth));
        
        
        //now will do later
//        if(variant != null) {
//            base.variant = (base.variant == null ? "" : base.variant + ", ") + variant;
//            int quality = (int)vcfLine.getPhredScaledQual();
//            //int quality =  Math.round(Float.parseFloat(fields[5]));
//            Set<String> filters = vcfLine.getFilters();
//            Iterator<String> filters_iterator = filters.iterator();
//            String filter = "";
//            while(filters_iterator.hasNext())
//            {
//                filter = filter + filters_iterator.next();
//            }
//            
//            int refReads=0;
//            int altReads=0;
//            Iterator<Genotype> genotype_iterator = genotype_field.iterator();
//            //TODO: varscan has AD as only one number, will need versus unified gentyper and illumina that has
//            //AD as [refread,altread]
//            //have to iterate because the unifiedgenotyper genome vcf will be multisample
//             System.out.println(variant);
//            while (genotype_iterator.hasNext()) {
//                Genotype current_geno = genotype_iterator.next();
//                if (current_geno.hasAD()) {
//                    if (current_geno.getAD().length > 1) {
//                        refReads = refReads + current_geno.getAD()[0];
//                        altReads = refReads + current_geno.getAD()[1];
//                    } else {
//                        altReads = altReads - current_geno.getAD()[0];
//                        //yes maybe not the greatest coding because the calculation on first loop is pointless, but it does its job nonetheless with multisample
//                        refReads = (int) readDepth - altReads;
//                    }
//                }
//            }
//            
//            
//            base.variantText =
//                (base.variantText == null ? pos + ": " : base.variantText + ", ")
//                + ""
//                + variant
//                + " ("
//                + "reads: " + refReads + ">" + altReads
//                + ", filter: " + filter
//                + ", qual: " + quality
//                + ")";
//        }
        return base;
    }
    
}
