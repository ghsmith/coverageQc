package coverageqc;
import java.net.URL;
import coverageqc.data.Amplicon;
import coverageqc.data.Base;
import coverageqc.data.Bin;
import coverageqc.data.DoNotCall;
import coverageqc.data.GeneExon;
import coverageqc.data.Variant;
import coverageqc.data.Vcf;
//import coverageqc.functions.MyExcelGenerator;
import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
//import org.apache.poi.openxml4j.opc.OPCPackage;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.PrintOrientation;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
////import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.apache.poi.xssf.eventusermodel.XSSFReader;
//import org.apache.poi.xssf.model.SharedStringsTable;
//import org.apache.poi.xssf.usermodel.XSSFCellStyle;
//import org.apache.poi.xssf.usermodel.XSSFColor;
//import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//#import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import htsjdk.samtools.*;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.vcf.*;
import htsjdk.variant.variantcontext.*;
import java.io.File;



//end Tom addition

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 * @coauthor schneiderthomas@gmail.com
 */
public class CoverageQc {

    private final static Logger LOGGER = Logger.getLogger(CoverageQc.class.getName()); 
    
    // Tom Addition
	//public String[][] doNotCallList = null;
         public static ArrayList<DoNotCall> donotcalls = new ArrayList<DoNotCall>();

	// End Tom Addition
        
    /**
     * @param args the command line arguments//Tom Addition is the
	 *            OpenXML4JException and InvalidFormatException
     */
    public static void main(String[] args) throws OpenXML4JException,
			InvalidFormatException, UnsupportedEncodingException, FileNotFoundException, IOException, JAXBException, TransformerConfigurationException, TransformerException, Exception {

        if(args.length == 0) {
            System.out.println("");
            //Tom Addition adding in argument doNotCall List
            System.out.println("USAGE: java -jar coverageQc.jar VCF-file-name exon-BED-file amplicon-BED-file list-of-aligners-used-CSV-file types-of-Files-To-Collect-CSV-file genes-excluding-CSV-file doNotCall-xlsx-file(optional)");
            System.out.println("");
            //Tom Addition extended comment
             System.out.println("If BED and file names are not specified, the system will attempt to use the\n\"exons_ensembl.bed\", \"amplicons.bed\" , \"aligners.csv\", \"filestolookfor.csv\", and \"genes_excluding.csv\" files located in the same directory\nas this JAR (or exe) file.  If excel file name is not specified will look under exe file directory");
            return;
        }
        
        File jarFile = new File(CoverageQc.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String jarFileDir = URLDecoder.decode(jarFile.getParent(), "UTF-8");

        final File vcfFile = new File(args[0]);

        File exonBedFile;
        File ampliconBedFile;
        File alignersFile;
        File filesToUseFile;
        File genesToExcludeFile;
        //Tom addition
        File doNotCallFile = null;
                 ///
        if(args.length == 1) {
            // look for the BED files in the JAR file directory with names of
            // the form:
            //      xxx.YYYYMMDD.exons.bed
            //      xxx.YYYYMMDD.amplicons.bed
            //
            // ultimately use the ones that sort alphabetically highest
            File[] exonFiles = (new File(jarFileDir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("exons.bed")); }
            });
            File[] ampliconFiles = (new File(URLDecoder.decode(jarFile.getParent(), "UTF-8"))).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("amplicons.bed")); }
            });
            File[] alignerFiles = (new File(URLDecoder.decode(jarFile.getParent(), "UTF-8"))).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("aligners.csv")); }
            });
            File[] genesToExcludeFiles = (new File(URLDecoder.decode(jarFile.getParent(), "UTF-8"))).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("genes_excluding.csv")); }
            });
            File[] filesUsingFiles = (new File(URLDecoder.decode(jarFile.getParent(), "UTF-8"))).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("filestolookfor.csv")); }
            });
            if(exonFiles.length == 0 || ampliconFiles.length == 0) {
                System.out.println("ERROR: Could not find exons.bed and/or amplicons.bed file(s) in " + URLDecoder.decode(jarFile.getParent(), "UTF-8"));
                return;
            }
            Arrays.sort(exonFiles, Collections.reverseOrder());
            exonBedFile = exonFiles[0];
            Arrays.sort(ampliconFiles, Collections.reverseOrder());
            ampliconBedFile = ampliconFiles[0];
            Arrays.sort(alignerFiles, Collections.reverseOrder());
            alignersFile = alignerFiles[0];
            Arrays.sort(filesUsingFiles, Collections.reverseOrder());
            filesToUseFile = filesUsingFiles[0];
            Arrays.sort(filesUsingFiles, Collections.reverseOrder());
            genesToExcludeFile = genesToExcludeFiles[0];
            
             //Tom Addition
                        //assuming it is always in the jarfiledirectory
                       
                        File[] doNotCallFiles = (new File(jarFileDir))
					.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return (pathname.getName().endsWith("list.xlsx"));
						}
					});
                          
                        if (doNotCallFiles.length != 0)
                        {
                        Arrays.sort(doNotCallFiles, Collections.reverseOrder());
			doNotCallFile = doNotCallFiles[0];
                        }else
                        {
                            //there is no file in jarfile directory so null will be used
                        }
                       
                            
                        ///end Tom Addition
            
            
        }
        else {
            exonBedFile = new File(args[1]);
            ampliconBedFile = new File(args[2]);
            alignersFile = new File(args[3]);
            filesToUseFile = new File(args[4]);
            genesToExcludeFile = new File(args[5]);
             //Tom addition
                        if (args.length>=7)
                        {
                        doNotCallFile = new File(args[6]);
                        }
                          
             ////
        }
        
        //excluding some genes in the vcf from being displayed in the CoverageQC html file (note they will still be put in the created xlsx file)
        
        Reader genesToExcludeReader = new FileReader(genesToExcludeFile);
        BufferedReader genesToExcludeBufferedReader = new BufferedReader(genesToExcludeReader);
        String genesToExcludeLine;
        String variantExcludedString = "Exclude Genes: ";
        ArrayList<String> genesToExcludeList = new ArrayList<String>();
             
               while((genesToExcludeLine = genesToExcludeBufferedReader.readLine()) != null)
                {
                    genesToExcludeList.add(genesToExcludeLine);
                    variantExcludedString=variantExcludedString + genesToExcludeLine + ",";
                }
        
        variantExcludedString=variantExcludedString + "Exclude: intronic variants, and variants with pool bias(if Annovar file)";
        //will extract the aligner used in the genome vcf file
        //will use aligner file to see what aligner used
        
        Reader alignerReader = new FileReader(alignersFile);
        BufferedReader alignerBufferedReader = new BufferedReader(alignerReader);
        String alignerLine;
        ArrayList<String> alignersList = new ArrayList<String>();
             
               while((alignerLine = alignerBufferedReader.readLine()) != null)
                {
                    alignersList.add(alignerLine);
                    
                }
               
        Iterator<String> AlignerIterator = alignersList.iterator();
        String vcfFileName=vcfFile.getCanonicalPath();
        String vcfFileName2=vcfFile.getAbsolutePath();
        //System.out.println("DEBUGGING ONLY, THE NAME OF THE VCFILENAME IS:  " + vcfFileName);
        //System.out.println("DEBUGGING ONLY, THE NAME OF THE ABSOLUTE PATH IS:  " + vcfFileName2);
        Boolean alignerFound=false;
        String usedAligner="";
        while(AlignerIterator.hasNext())
        {
            String currentAligner=AlignerIterator.next();
            if (vcfFileName.contains(currentAligner))
            {
                alignerFound=true;
                usedAligner=currentAligner;
            }
            
        }
        
        if (!alignerFound)
        {
            //need to exit as the name of the vcf file does not contain a used aligner, which means parsing of the genome vcf failed
           // System.out.println("ERROR: Can't find in the name of the genome vcf the aligner used. Parsing error, check name of file and alignerlist.csv");
            //        return;
            //assuming it is an illumina file
            usedAligner="AmpliconDS";
        }

   
    
         
/*        
           // TOM ADDITION
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Will populate doNotCall, reading XSLX file	
        //DoNotCallConverter(doNotCallFile);
        if (doNotCallFile!=null)
        {
            
        InputStream inp = new FileInputStream(doNotCallFile);
        // Get the workbook instance for XLS file
	XSSFWorkbook workbook = new XSSFWorkbook(inp);
        
         Iterator<XSSFSheet> sheetIterator = workbook.iterator();
         int typeOfCall = 1;
	int rowIndex;
        //note the first three sheets are illumina do not calls
        //the second three sheets are annovar do not calls
        while (sheetIterator.hasNext())
                {
                    XSSFSheet sheet = sheetIterator.next();
                    Iterator<Row> rowIterator = sheet.iterator();
                    Row headerRow = null;
                    while (rowIterator.hasNext()) {
			
			Row row = rowIterator.next();
                        
			rowIndex = row.getRowNum();
                       // System.out.println("The row index is " +rowIndex);
			if (rowIndex == 0) {	
                            headerRow=row;
				continue;
			}else if(row.getCell(0)==null)
                        {
                            // if it is null then nothing is there
                            continue;
                        }
                        else if (row.getCell(0).getCellType() == 3) {
				// if the cell type is 3 that means it is a blank field
				continue;
			} 
			DoNotCall donotcall = DoNotCall.populate(headerRow, row, typeOfCall);
                        donotcalls.add(donotcall);
		}//end while rowiterator           
                    typeOfCall++; 
                    //system.out.println()
                }//end while sheetiterator     
        }//end if doNotCallFile is null
*/        
        
       
		
                
            // END TOM ADDITION//////////////////////////////////////////////////////////////////////////
                
        
        
        //Tom addition using file reader from htsjdk instead instead
       // SamInputResource.index(vcfFile);
        VCFFileReader this_vcf = new VCFFileReader(vcfFile,false);
       
        //System.out.println(vcfFile);
        //Reader vcfFileReader = new FileReader(vcfFile);
        //BufferedReader vcfBufferedReader = new BufferedReader(vcfFileReader);

        Reader exonBedFileReader = new FileReader(exonBedFile);
        BufferedReader exonBedBufferedReader = new BufferedReader(exonBedFileReader);

        Reader ampliconBedFileReader = new FileReader(ampliconBedFile);
        BufferedReader ampliconBedBufferedReader = new BufferedReader(ampliconBedFileReader);
        
        

        // is there an Illumina variant file to fold into the report?
        File variantTsvFile = null;
        Integer variantTsvFileLineCount = null;
        Reader variantTsvFileReader = null;
        BufferedReader variantTsvBufferedReader = null;
        {
            File[] files = (new File(vcfFile.getCanonicalFile().getParent())).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return(
                        (
                            pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && pathname.getName().toLowerCase().endsWith(".tsv")
                        )
                    );
                }
            });
            if(files.length == 1) {
                variantTsvFile = files[0];
                variantTsvFileReader = new FileReader(variantTsvFile);
                LineNumberReader lnr = new LineNumberReader(variantTsvFileReader);
                while(lnr.skip(Long.MAX_VALUE) > 0) {}
                variantTsvFileLineCount = lnr.getLineNumber();
                variantTsvFileReader.close();
                variantTsvFileReader = new FileReader(variantTsvFile);
                variantTsvBufferedReader = new BufferedReader(variantTsvFileReader);
            }
        }
        
         // is there an Annovar variant file to fold into the report?
        ArrayList<File> annovarTSVFiles= null;
        File current_variantTSVFileAnnovar=null;
        Reader variantTsvFileReaderAnnovar = null;
        BufferedReader variantTsvBufferedReaderAnnovar = null;
        {
            //have to change geoff's old code because it won't allow local variavles
            File[] filestemp = (new File (vcfFile.getCanonicalFile().getParent())).listFiles();
            String file_to_look_for = usedAligner + ".allvariantcallers.final.hg19_multianno.txt";
            for(int i =0; i<filestemp.length; i++)
            {
                if(filestemp[i].getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && filestemp[i].getName().toLowerCase().endsWith(file_to_look_for))
                {
                    annovarTSVFiles = new ArrayList<File>();
                    annovarTSVFiles.add(filestemp[i]);
                }
                
            }
            
            
        }
        //System.out.println(annovarTSVFiles);
        
        
        
        
        //TODO: editing a vcf so that it can contain multiple vcf
        Vcf vcf = new Vcf();
        
        
        //TODO: IMPLEMENT ABILITY TO DISPLAY CONTENTS OF MULTIPLE ALIGNERS, FOR NOW, just using the aligner used in the current genome vcf

        
        
        
        
        
        
        vcf.runDate = new Date();
        vcf.fileName = vcfFile.getCanonicalPath();
        vcf.exonBedFileName = exonBedFile.getCanonicalPath();
        //Tom addition
        //so the call file will be displayed in the report
        if (doNotCallFile!=null)
        {
            vcf.doNotCallFileName = doNotCallFile.getCanonicalPath();
        }else
        {
         vcf.doNotCallFileName = "NO DO NOT CALL FILE USED!";   
        }
        vcf.ampliconBedFileName = ampliconBedFile.getCanonicalPath();
        vcf.variantTsvFileName = (variantTsvFile != null ? variantTsvFile.getCanonicalPath() : null);
        vcf.variantTsvFileLineCount = variantTsvFileLineCount;
        vcf.usedAligner=usedAligner;
        vcf.alignersFileName = alignersFile.getCanonicalPath();
        vcf.filesToUseFileName = filesToUseFile.getCanonicalPath();
        vcf.genesToExcludeFileName = genesToExcludeFile.getCanonicalPath();
        String annovarFilesString="";
        if (annovarTSVFiles != null)
        {
            for(int i=0; i<annovarTSVFiles.size(); i++)
            {
                if(i==annovarTSVFiles.size()-1)
                {
                    annovarFilesString=annovarFilesString+annovarTSVFiles.get(i);
                }else
                {
                    annovarFilesString=annovarFilesString+annovarTSVFiles.get(i) + ", ";
                }
            }
        }
        vcf.annovarFiles = annovarFilesString;
        
        // attempt to deduce the amplicon BED, patient BAM, and patient VCF
        // file names for this gVCF file, the assumption is that they are in
        // the same directory as the gVCF file
      
        Reader FilesToUseReader = new FileReader(filesToUseFile);
        BufferedReader FilesToUseBufferedReader = new BufferedReader(FilesToUseReader);
        String FilesToUseLine;
        ArrayList<String> FilesToUseList = new ArrayList<String>();
                
                while((FilesToUseLine = FilesToUseBufferedReader.readLine()) != null)
                {
                    FilesToUseList.add(FilesToUseLine);
                    
                }        
                
        {
            //Iterator<String> AlignerIterator = alignersList.iterator();
            Iterator<String> FilesToUseIterator = FilesToUseList.iterator();
            String currentFileToUse;
          //  String currentAligner;
          //  while (AlignerIterator.hasNext())
          //  {
          //      ArrayList<URL> bedBamVcfFileUrls = new ArrayList<URL>();
          //      currentAligner=AlignerIterator.next();
            while (FilesToUseIterator.hasNext())
            {
                
                currentFileToUse=FilesToUseIterator.next();
                //NOW most of the files will have the word aligner in them ie .A.aligner.final.bam; the word aligner needs to be replaced by string contain in currentAligner
                currentFileToUse=currentFileToUse.replace("aligner", usedAligner);
                //gets the list of all the files
                File[] filestemp = (new File (vcfFile.getCanonicalFile().getParent())).listFiles();
                List<File> currentFilesUsingList = new ArrayList<File>();
                for (int i = 0; i<filestemp.length; i++)
                {
                   //System.out.println(currentFileToUse);
                    if (filestemp[i].getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && filestemp[i].getName().toLowerCase().endsWith(currentFileToUse.toLowerCase()))
                    {
                        //System.out.println(currentFileToUse);
                        currentFilesUsingList.add(filestemp[i]);
                                
                    }else if (filestemp[i].getName().toLowerCase().endsWith(currentFileToUse.toLowerCase())
                            && filestemp[i].getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + "_"))
                    {
                        currentFilesUsingList.add(filestemp[i]);
                    }
                        
                }
                //can't use Geoff's old code because can't use the variable currentString in inner class
            
           
            for(File file : currentFilesUsingList) {
            vcf.bedBamVcfFileUrls.add(file.toURI().toURL());
        }
            
            
            } //while (FilesToUseIterator.hasNext())
            
            vcf.bedBamVcfFileUrls.add(ampliconBedFile.toURI().toURL());
            //vcf.bedBamVCFFileUrlsList.add(bedBamVcfFileUrls);
         // }//while (AlignerIterator.hasNext())
        };

        // read exon BED file
        String exonBedLine;
        while((exonBedLine = exonBedBufferedReader.readLine()) != null) {
            if(!exonBedLine.startsWith("chr")) {
                continue;
            }
            //System.out.println(exonBedLine);        
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
        //TOM additon, using htsjdk library for variants
        //String vcfLine;
        VariantContext vcfLine;
        CloseableIterator<VariantContext> variant_iterator = this_vcf.iterator();
        //the variantcontext file starts with each variant line
        while(variant_iterator.hasNext())
        {
            vcfLine = variant_iterator.next();
            Base base = Base.populate(vcfLine, vcf.bases);
            boolean foundGeneExon = false;
            for(GeneExon geneExon : vcf.findGeneExonsForChrPos(base.chr, base.pos)) {
                foundGeneExon = true;
                geneExon.bases.put(new Long(base.pos), base);
            }
            if(!foundGeneExon) {
                LOGGER.info("the following base does not correspond to an exon region: " + vcfLine.getContig() + " " + vcfLine.getStart());
            }
            
            
        }
       
        LOGGER.info(vcf.getBaseCount() + " bases read from VCF file");
        LOGGER.info(vcf.getReadDepthCount() + " read depths read from VCF file");
        this_vcf.close();

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


/*        
        // read variant file
        //ArrayList<String> textFilesList = new ArrayList<String>();
        MyExcelGenerator excelgenerator= new MyExcelGenerator();
        //textFilesList.add("ILLUMINA");
         // read variant file
         
        if(variantTsvFile != null) {
            String variantTsvDataLine;
            String variantTsvHeadingLine = variantTsvBufferedReader.readLine();
            
            //MyExcelGenerator excelgenerator= new MyExcelGenerator(textFilesList);
            
            //Tom addition 
        //making excel copy of tsv
        //XSSFWorkbook workbookcopy = new XSSFWorkbook();
      //  XSSFSheet sheet = workbookcopy.createSheet("TSV copy");
       // XSSFCellStyle cellStyle = (XSSFCellStyle)workbookcopy.createCellStyle();
        //cellStyle.setWrapText(true);
       // XSSFRow row = sheet.createRow(0);
        
         excelgenerator.excelHeadingCreator("ILLUMINA", variantTsvHeadingLine);
        
        int rownum =1;
        
        //end Tom addition
            
            while((variantTsvDataLine = variantTsvBufferedReader.readLine()) != null) {
                // Tom addition adding in variable doNotCallList
                Variant variant = Variant.populate(variantTsvHeadingLine, variantTsvDataLine, donotcalls, genesToExcludeList);
               
               // XSSFRow row = sheet.createRow(rownum++);
                excelgenerator.excelRowCreator(rownum++, "ILLUMINA", variant, variantTsvDataLine, donotcalls);
                
                 //end Tom addition
                
                boolean foundGeneExon = false;
                for(GeneExon geneExon : vcf.findGeneExonsForChrPos("chr" + String.valueOf(variant.chr), variant.coordinate)) {
                    foundGeneExon = true;
                    
                    
                    geneExon.variants.add(variant);
                    Base currentbase = vcf.bases.get("chr" + String.valueOf(variant.chr) + "|" + Long.toString(variant.coordinate));
                    currentbase.variant = variant.variant;
                    currentbase.variantText = "";
                    currentbase.variantText += variant.variant + " (reads: " + variant.readDepth + ">" + variant.altReadDepth + ")";
                    vcf.bases.put(currentbase.chr + "|" + Long.toString(currentbase.pos), currentbase);
                    
                    
                     if (variant.onTheDoNotCallList) {
                        
                        if(variant.typeOfDoNotCall.equals("Don't call, always"))
                        {
                        geneExon.containsDoNotCallAlways = true;
                        geneExon.donotcallVariantsAlways.add(variant);
                        }
                        }
                    
        
                }
                if(!foundGeneExon && (!variant.exclude)) {
                    //LOGGER.info("the following variant does not correspond to an exon region: " + variantTsvDataLine);
                    System.out.println("The variant gene name is: " + variant.gene);
                    System.out.println("Is the variant excluded? " + variant.exclude);
                    System.out.println("ERROR: The following variant does not correspond to an exon region:\n" + variantTsvDataLine);
                    return;
                }
            }
           
              //Adding page setup parameters per Dr. Carter, and column hiding options
            excelgenerator.excelFormator("ILLUMINA", variantTsvFile);
          
            // end Tom addition
            
            
            
            variantTsvFileReader.close();
        }
        
        //NOW READ ANNOVAR VARIANTS WHICH COULD BE MULTIPLE AS MULTIPLE PIPELINES MAY BE IMPLEMENTED'
        //ASSUMPTION: The headings should all be the same for each pipeline, will fail if this is NOT 
        //the case
        
        
        
        if(annovarTSVFiles != null) {
            int rownum =1;
            String oldvariantTSVHeadingLineAnnovar = null;
            //debugging, only doing vardict
            for (int i = 0; i<annovarTSVFiles.size(); i++)
            {
                current_variantTSVFileAnnovar = annovarTSVFiles.get(i);
                //System.out.println(annovarTSVFiles.get(i));
                variantTsvFileReaderAnnovar = new FileReader(current_variantTSVFileAnnovar);
                variantTsvBufferedReaderAnnovar = new BufferedReader(variantTsvFileReaderAnnovar);
                 String variantTsvDataLineAnnovar=null;
                 String variantTsvHeadingLineAnnovar = variantTsvBufferedReaderAnnovar.readLine();
                 if (i==0)
                 {
                     oldvariantTSVHeadingLineAnnovar=variantTsvHeadingLineAnnovar;
                     excelgenerator.excelHeadingCreator("ANNOVAR", variantTsvHeadingLineAnnovar);
                 }else
                 {
                     if(!oldvariantTSVHeadingLineAnnovar.equals(variantTsvHeadingLineAnnovar))
                     {
                    System.out.println("ERROR: THE HEADINGS OF THE ANNOVAR FILES DO NOT MATCH!!! CANNOT PROCEED AS THIS IS A MAJOR"
                            + "ASSUMPTION OF THIS PROGRAM!!!");
                    return;
                     }
                 }
                 
                while ((variantTsvDataLineAnnovar = variantTsvBufferedReaderAnnovar.readLine()) != null) {

                    Variant variant = Variant.populateAnnovar(variantTsvHeadingLineAnnovar, variantTsvDataLineAnnovar, donotcalls, genesToExcludeList);
                    excelgenerator.excelRowCreator(rownum++, "ANNOVAR", variant, variantTsvDataLineAnnovar, donotcalls);

                    boolean foundGeneExon = false;
                    for (GeneExon geneExon : vcf.findGeneExonsForChrPos("chr" + String.valueOf(variant.chr), variant.coordinate)) {
                        foundGeneExon = true;
                        //if(variant.hgvscMap != null) {
                        //variant.hgvsc = variant.hgvscMap.get(geneExon.refSeqAccNo);
                         //  }
                        // if(variant.hgvspMap != null) {
                        //    variant.hgvsp = variant.hgvspMap.get(geneExon.refSeqAccNo);
                        //  }
                        //Geoff in the original pipeline had a check to get rid of frequencies lower than 3%, got rid off as variant 
                         //callers should handle this preprocessing
                         
                         //coverageQC report will not highlight intronic variants as well as variants with probe bias in the html file
                        
                         if ((variant.NotIntronic) && !variant.filters.contains("PB"))
                         {
                               geneExon.variants.add(variant);
                               Base currentbase= vcf.bases.get("chr" + String.valueOf(variant.chr) + "|" + Long.toString(variant.coordinate));
                               currentbase.variant=variant.variant;
                               currentbase.variantText="";
                               currentbase.variantText+=variant.variant + " (reads: " + variant.readDepth + ">" + variant.altReadDepth + ")";
                               vcf.bases.put(currentbase.chr + "|" + Long.toString(currentbase.pos), currentbase);
                               
                                  
                               
                         }

                        if (variant.onTheDoNotCallList) {

                            if (variant.typeOfDoNotCall.equals("Don't call, always")) {
                                geneExon.containsDoNotCallAlways = true;
                                geneExon.donotcallVariantsAlways.add(variant);
                            }
                        }

                    }
                    if (!foundGeneExon) {
                        //LOGGER.info("the following variant does not correspond to an exon region: " + variantTsvDataLine);
                        
                        //will only output if this is an exonor a splic site variant since it should fail then, non-splicing intronic variants not reported
                        if (variant.NotIntronic && (!variant.exclude))
                                {
                             System.out.println("The variant gene name is: " + variant.gene);
                             System.out.println("Is the variant excluded? " + variant.exclude);       
                             System.out.println("ERROR: The following exonic/splicing/UTR variant does not correspond to an exon region:\n" + variantTsvDataLineAnnovar);
                             return;
                             
                                }
                                
                        
                    }

                }
                 
                variantTsvFileReaderAnnovar.close();
                
            }
            
            
            //Adding page setup parameters per Dr. Carter, and column hiding options
            //just need the location of the annovarTSV
            //TODO: OPTIMIZE THIS, KIND OF DUMB HOW DOING THIS, JUST NEED TO PASS A STRING
            excelgenerator.excelFormator("ANNOVAR", annovarTSVFiles.get(0));
            
            
            
            
            
            
        }
        
        vcf.variantsExcludedFromCoverageQC=variantExcludedString;

        // write to XML
        //File xmlTempFile = File.createTempFile("tmp", ".xml");
        File xmlTempFile = new File(vcfFile.getCanonicalPath() + ".coverage_qc.xml");
        OutputStream xmlOutputStream = new FileOutputStream(xmlTempFile);
        JAXBContext jc = JAXBContext.newInstance("coverageqc.data");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        m.marshal(vcf, xmlOutputStream);        
        xmlOutputStream.close();
        LOGGER.info(xmlTempFile.getCanonicalPath() + " created");

        //Tom addition
        //write to xlsx
        if(variantTsvFile != null || annovarTSVFiles != null)
        {
         
         File xslxTempFile;
         if (variantTsvFile != null)
         {
         xslxTempFile = new File(variantTsvFile.getCanonicalPath() + ".coverage_qc.xlsx");
         }else
         {
         xslxTempFile = new File(annovarTSVFiles.get(0).getCanonicalPath() + ".coverage_qc.xlsx");    
         }
         OutputStream xslxOutputStream = new FileOutputStream(xslxTempFile);
         excelgenerator.workbookcopy.write(xslxOutputStream);
         xslxOutputStream.close();
         LOGGER.info(xslxTempFile.getCanonicalPath() + " created");

        }
        //end Tom addition

        // transform XML to HTML via XSLT
        Source xmlSource = new StreamSource(new FileInputStream(xmlTempFile.getCanonicalPath()));
        Source xslSource;
       if((new File(jarFileDir + "/coverageQc.xsl")).exists()) {
           xslSource = new StreamSource(new FileInputStream(jarFileDir + "/coverageQc.xsl"));
        }
       else {
           xslSource = new StreamSource(ClassLoader.getSystemResourceAsStream("coverageQc.xsl"));
           }
          Transformer trans = TransformerFactory.newInstance().newTransformer(xslSource);
          trans.transform(xmlSource, new StreamResult(vcfFile.getCanonicalPath() + ".coverage_qc.html"));
          LOGGER.info(vcfFile.getCanonicalPath() + ".coverage_qc.html created");
       
         // show HTML file in default browser
        File htmlFile = new File(vcfFile.getPath() + ".coverage_qc.html");
        Desktop.getDesktop().browse(htmlFile.toURI());

*/

        StringBuffer sbOut = new StringBuffer();
        for(GeneExon geneExon : vcf.geneExons) {
            if(geneExon.bins.get(0).pct +  geneExon.bins.get(1).pct > 0) {
                sbOut.append(String.format("gene/locus %s/%s:%d-%d pct-of-locus-failing-QC: %d\\n", geneExon.name.replaceFirst("ex.*$", ""), geneExon.chr, geneExon.codingRegion.startPos, geneExon.codingRegion.endPos, geneExon.bins.get(0).pct +  geneExon.bins.get(1).pct));
            }
        }
        if(sbOut.length() > 0) {
            System.out.print("Portions of the following captured regions were not sequenced sufficiently for clinical interpretation (at least one base in the sequenced portion of the coding region was read less than 500 times):\\n");
            System.out.print(sbOut);
        }
        else {
            System.out.print("All portions of the captured regions were sequenced sufficiently for clinical interpretation (all bases in the sequenced portion of the coding region were read more than 500 times).\\n");
        }
        
    }
   
   
}
