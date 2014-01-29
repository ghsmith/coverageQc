package coverageqc;

import at.jta.Key;
import at.jta.RegistryErrorException;
import at.jta.Regor;
import coverageqc.data.Amplicon;
import coverageqc.data.Base;
import coverageqc.data.Bin;
import coverageqc.data.GeneExon;
import coverageqc.data.Vcf;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
public class CoverageQc {

    private final static Logger LOGGER = Logger.getLogger(CoverageQc.class.getName()); 
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, JAXBException, TransformerConfigurationException, TransformerException, RegistryErrorException {

        if(args.length == 0) {
            System.out.println("");
            System.out.println("USAGE: java -jar coverageQc.jar VCF-file-name exon-BED-file-name amplicon-BED-file-name");
            System.out.println("");
            System.out.println("If BED file names are not specified, the last ones specified will be used.");
            System.out.println("The BED file name is persisted in the Windows registry at:\n\nComputer\\HKEY_CURRENT_USER\\Software\\CoverageQc REG_SZ exonBedFileName\nComputer\\HKEY_CURRENT_USER\\Software\\CoverageQc REG_SZ ampliconBedFileName\n");
            return;
        }
        
        final File vcfFile = new File(args[0]);

        // Windows registry stuff (this represents a Windows dependency)
        File exonBedFile;
        File ampliconBedFile;
        if(args.length == 1) {
            Regor obj = new Regor();
            Key key = obj.openKey(Regor.HKEY_CURRENT_USER, "Software\\CoverageQc");
            exonBedFile = new File(obj.readValueAsString(key, "exonBedFileName"));
            ampliconBedFile = new File(obj.readValueAsString(key, "ampliconBedFileName"));
        }
        else {
            Regor obj = new Regor();
            Key key = obj.openKey(Regor.HKEY_CURRENT_USER, "Software\\CoverageQc");
            if(key == null) {
                key = obj.createKey(Regor.HKEY_CURRENT_USER, "Software\\CoverageQc");
            }
            obj.saveValue(key, "exonBedFileName", args[1]);
            obj.saveValue(key, "ampliconBedFileName", args[2]);
            exonBedFile = new File(args[1]);
            ampliconBedFile = new File(args[2]);
        }
        
        Reader vcfFileReader = new FileReader(vcfFile);
        BufferedReader vcfBufferedReader = new BufferedReader(vcfFileReader);

        Reader exonBedFileReader = new FileReader(exonBedFile);
        BufferedReader exonBedBufferedReader = new BufferedReader(exonBedFileReader);

        Reader ampliconBedFileReader = new FileReader(ampliconBedFile);
        BufferedReader ampliconBedBufferedReader = new BufferedReader(ampliconBedFileReader);
            
        Vcf vcf = new Vcf();
        vcf.runDate = new Date();
        vcf.fileName = vcfFile.getCanonicalPath();
        vcf.exonBedFileName = exonBedFile.getCanonicalPath();
        vcf.ampliconBedFileName = ampliconBedFile.getCanonicalPath();
        
        // attempt to deduce the amplicon BED, patient BAM, and patient VCF
        // file names for this gVCF file, the assumption is that they are in
        // the same directory as the gVCF file
        {
            File[] files = (new File(vcfFile.getCanonicalFile().getParent())).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return(
                        (
                            pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase())
                            && (pathname.getName().toLowerCase().endsWith(".bam") || (pathname.getName().toLowerCase().endsWith(".vcf")))
                            && (pathname.getName().indexOf("genome") < 0)
                        )
                        || (
                            pathname.getName().equals("amplicons.bed")
                        )
                    );
                }
            });
            for(File file : files) {
                vcf.bedBamVcfFileUrls.add(file.toURI().toURL());
            }
        }

        // read exon BED file
        String exonBedLine;
        while((exonBedLine = exonBedBufferedReader.readLine()) != null) {
            if(!exonBedLine.startsWith("chr")) {
                continue;
            }
            vcf.geneExons.add(GeneExon.populate(exonBedLine));
        }
        LOGGER.info(vcf.geneExons.size() + " regions read from exon BED file");
        exonBedFileReader.close();

        // read amplicon BED file
        String ampliconBedLine;
        while((ampliconBedLine = ampliconBedBufferedReader.readLine()) != null) {
            if(!ampliconBedLine.startsWith("chr")) {
                continue;
            }
            Amplicon amplicon = Amplicon.populate(ampliconBedLine);
            boolean foundGeneExon = false;
            for(GeneExon geneExon : vcf.findGeneExonsForChrRange(amplicon.chr, amplicon.startPos, amplicon.endPos)) {
                foundGeneExon = true;
                geneExon.amplicons.add(amplicon);
                if(amplicon.name.endsWith("_coding")) {
                    geneExon.codingRegion = amplicon;
                }
            }
            if(!foundGeneExon) {
                LOGGER.info("the following amplicon does not correspond to an exon region: " + ampliconBedLine);
            }
        }
        LOGGER.info(vcf.getAmpliconCount() + " regions read from amplicon BED file");
        ampliconBedFileReader.close();

        // read gVCF file
        String vcfLine;
        while((vcfLine = vcfBufferedReader.readLine()) != null) {
            if(vcfLine.startsWith("#")) {
                continue;
            }
            Base base = Base.populate(vcfLine, vcf.bases);
            boolean foundGeneExon = false;
            for(GeneExon geneExon : vcf.findGeneExonsForChrPos(base.chr, base.pos)) {
                foundGeneExon = true;
                geneExon.bases.put(new Long(base.pos), base);
            }
            if(!foundGeneExon) {
                LOGGER.info("the following base does not correspond to an exon region: " + vcfLine);
            }
        }
        LOGGER.info(vcf.getBaseCount() + " bases read from VCF file");
        LOGGER.info(vcf.getReadDepthCount() + " read depths read from VCF file");
        vcfFileReader.close();

        for(GeneExon geneExon : vcf.geneExons) {
            // if a position is absent, create it with read depth 0
            for(long pos = geneExon.startPos; pos <= geneExon.endPos; pos++) {
                if(vcf.bases.get(geneExon.chr + "|" + Long.toString(pos)) == null) {
                    Base base = new Base();
                    base.pos = pos;
                    base.readDepths.add(new Long(0));
                    vcf.bases.put(geneExon.chr + "|" + Long.toString(pos), base);
                }
                if(geneExon.bases.get(new Long(pos)) == null) {
                    geneExon.bases.put(new Long(pos), vcf.bases.get(geneExon.chr + "|" + Long.toString(pos)));
                }
            }
            // perform binning operation
            for(Base base : geneExon.bases.values()) {
                // don't count a base if it is outside of the coding region
                if((base.pos < geneExon.codingRegion.startPos) || (base.pos > geneExon.codingRegion.endPos)) {
                    continue;
                }
                for(Bin bin : geneExon.bins) {
                    if(base.getTotalReadDepth() >= bin.startCount && base.getTotalReadDepth() <= bin.endCount) {
                        bin.count++;
                        bin.pct = Math.round((100d * bin.count) / (Math.min(geneExon.endPos, geneExon.codingRegion.endPos) - Math.max(geneExon.startPos, geneExon.codingRegion.startPos) + 1));
                        break;
                    }
                }
            }
            
            // assign QC value
            if(geneExon.bins.get(0).count > 0 || geneExon.bins.get(1).count > 0) {
                geneExon.qc = "fail";
            }
            else if(geneExon.bins.get(2).count > 0) {
                geneExon.qc = "warn";
            }
            else if(geneExon.bins.get(3).count > 0) {
                geneExon.qc = "pass";
            }
        }
        
        // write to XML
        File xmlTempFile = File.createTempFile("tmp", ".xml");
        OutputStream xmlOutputStream = new FileOutputStream(xmlTempFile);
        JAXBContext jc = JAXBContext.newInstance("coverageqc.data");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        m.marshal(vcf, xmlOutputStream);        
        xmlOutputStream.close();
        LOGGER.info(xmlTempFile.getPath() + " created");

        // transform XML to HTML via XSLT
        Source xmlSource = new StreamSource(new FileInputStream(xmlTempFile.getPath()));
        Source xslSource = new StreamSource(ClassLoader.getSystemResourceAsStream("CoverageReport.xsl"));
        Transformer trans = TransformerFactory.newInstance().newTransformer(xslSource);
        trans.transform(xmlSource, new StreamResult(vcfFile.getPath() + ".coverage_qc.html"));
        LOGGER.info(vcfFile.getPath() + ".coverage_qc.html created");
        
        // show HTML file in default browser
        File htmlFile = new File(vcfFile.getPath() + ".coverage_qc.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
        
    }
    
}