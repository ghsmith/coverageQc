/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coverageqc.functions;


import coverageqc.data.DoNotCall;
import coverageqc.data.Variant;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Tom
 */
public class MyExcelGenerator{
    
    public HashMap<Integer,Integer> tsvRearrangeConversion=null;
    public HashMap<String,Integer> originalHeadings=null;
    public String[] originalHeadingsArray=null;
    public XSSFWorkbook workbookcopy;
    String headingLine;
    ArrayList<String> sheetNames;
   
    public MyExcelGenerator ()
    {
        //sheetNames=givenSheetNames;
        workbookcopy = new XSSFWorkbook();
       // for(int i=0; i<givenSheetNames.size(); i++)
       // {
       //     workbookcopy.createSheet(sheetNames.get(i));
       // }        
        
    }
    
    public void excelRowCreator(int currentRowNum, String currentSheet, Variant currentVariant, String variantTsvDataLine, ArrayList<DoNotCall> donotcalls) {
      
        XSSFRow currentRow = this.workbookcopy.getSheet(currentSheet).createRow(currentRowNum);
        //list of possible cells to highlight
        Boolean filterCell=false;
        Boolean altVariantFreqCell = false;
        Boolean consequenceCell = false;
        Boolean alleleMinorCell = false;
        Boolean containsDoNotCall = false;
        Boolean intronicCell = false;
        Boolean excludeCell = false;
        //XSSFCellStyle cellStyle = currentRow.getRowStyle();
       // Cell cellInterp = currentRow.createCell(0);
       // XSSFCellStyle cellStyle = (XSSFCellStyle)cellInterp.getCellStyle();
      
        XSSFCellStyle cellStyle = getDefaultCellStyle(currentRow,Color.WHITE);
        
        String textOnInterp="";
          //cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
       // XSSFColor myColor;
         // Tom Addition
                    //if any of these the text on Interp is "Do Not Call"
                    if((currentVariant.consequence.contains("synonymous") && !currentVariant.consequence.contains("nonsynonymous")) || (!currentVariant.NotIntronic) ||
                            currentVariant.altVariantFreq.floatValue()<=5 || currentVariant.typeOfDoNotCall.equals("Don't call, always"))
                    {
                        textOnInterp=textOnInterp+"Do Not Call -";
                       
                       // cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                         cellStyle = getDefaultCellStyle(currentRow,Color.GRAY);
                         containsDoNotCall = true;
                        
                         if (currentVariant.typeOfDoNotCall.equals("Don't call, always")) {
                             
                             if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                             {
                            textOnInterp=textOnInterp+" on lab list of definitive do-not-calls";
                              }else
                              {
                            textOnInterp=textOnInterp+", on lab list of definitive do-not-calls";
                              }
                     
                        }
                         if(currentVariant.consequence.contains("synonymous") && !currentVariant.consequence.contains("nonsynonymous"))
                        {
                            consequenceCell = true;
                                //if the string size 
                                if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                {
                                    textOnInterp=textOnInterp+" synonymous variant";
                                }else
                                {
                                    textOnInterp=textOnInterp+", synonymous variant";
                                }
                        
                      
                        }     
                        if(currentVariant.altVariantFreq.floatValue()<=5)
                        {
                        altVariantFreqCell = true;
                             if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" variant <5%";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", variant <5%";
                                 }
                       
                         }
                        if(currentVariant.NotIntronic == false)
                        {
                        intronicCell = true;
                             if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" intronic variant";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", intronic variant";
                                 }
                       
                         }
                        if(currentVariant.exclude)
                        {
                            excludeCell = true;
                            if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" Excluded gene (ie pseudogene)";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", Excluded gene (ie pseudogene)";
                                 }
                        }
                
                    }
                    //if any of these the text on Interp is "Warning"
                    if((currentVariant.onTheDoNotCallList &&(!currentVariant.typeOfDoNotCall.equals("Don't call, always"))) || currentVariant.alleleFreqGlobalMinor.floatValue()>1 || !(currentVariant.filters.equals("PASS")))
                    {
                        
                        //cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                       // System.out.println(currentVariant.gene);
                        if (!containsDoNotCall)
                        {
                         cellStyle = getDefaultCellStyle(currentRow,Color.YELLOW);
                        }
                            if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+"WARNING -";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", WARNING -";
                                 }
                        
                        
                        
                        if((currentVariant.onTheDoNotCallList &&!(currentVariant.typeOfDoNotCall.equals("Don't call, always"))))
                        {
                     
                            if(currentVariant.typeOfDoNotCall.contains("In same location"))
                            {
                                if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+"in same location as lab list of do-not-calls";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", in same location as lab list of do-not-calls";
                                 }
                            }else
                            {
                                 if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+"on lab list of possible do-not-calls ";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", on lab list of possible do-not-calls - ";
                                 }
                            }
                            
                            
                            
                            }
                    
                        
                        
                       if(currentVariant.alleleFreqGlobalMinor>1)
                       {
                           alleleMinorCell = true;  
                           
                           if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" MAF >1%";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", MAF >1%";
                                 }
                           
                       }
                       
                       if (!(currentVariant.filters.equals("PASS")))
                       {
                           filterCell=true;
                                if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" Quality Filter = " + currentVariant.filters;
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", Quality Filter = " + currentVariant.filters;
                                 }
                           
                           
                       }
                       
                      }//end if statement saying it is on Warning
                       
                    
                    
                    //now creating three columns of interps 
                    for(int x=0; x<=2; x++)
                    {
                        Cell cellInterp = currentRow.createCell(x);
                        cellInterp.setCellStyle(cellStyle);
                        cellInterp.setCellValue(textOnInterp);
                    }
                   
                    
                    //adding TSV dataline to output excel file
                       // row = sheet.createRow(rownum++);
                   // String[] headingsArray = tsvHeadingLine.split("\t");
                       
                      //  HashMap<String, Integer> headings = new HashMap<String, Integer>();
                      //    for(int x = 0; x < headingsArray.length; x++) {
                     //      headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
                     //       }
                          String[] dataArray = variantTsvDataLine.split("\t");
        
                        for(int x = 0; x < dataArray.length; x++) {
                               Cell cell = currentRow.createCell(x+3);
                              if (currentSheet.equals("Illumina"))
                              {
                               if(    (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Filters_")&&filterCell) 
                                       || (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Allele Freq Global Minor_")&&alleleMinorCell)  
                                       )
                               {
                                  
                                   cellStyle = getDefaultCellStyle(currentRow,Color.YELLOW);
                               }else if( (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Consequence_")&&consequenceCell) 
                                       || (altVariantFreqCell && (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Alt Variant Freq_")))
                                        || (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Exonic")&&intronicCell)  || excludeCell
                                       )
                               {
                                    cellStyle = getDefaultCellStyle(currentRow,Color.GRAY);
                               }else
                               {
                                   cellStyle = getDefaultCellStyle(currentRow, Color.WHITE);
                                  
                               }
                                cell.setCellStyle(cellStyle);
                                cell.setCellValue(dataArray[this.tsvRearrangeConversion.get(x)]);
                                
                              }else
                              {
                                  
                                  
                                  if(    (this.originalHeadingsArray[x].contains("Filters")&&filterCell) 
                                       || (this.originalHeadingsArray[x].contains("exac03")&&alleleMinorCell)  
                                       )
                               {
                                  
                                   cellStyle = getDefaultCellStyle(currentRow,Color.YELLOW);
                               }else if( (this.originalHeadingsArray[x].equals("ExonicFunc.refGene")&&consequenceCell) 
                                       || (altVariantFreqCell && (this.originalHeadingsArray[x].contains("Alt Variant Freq"))) 
                                        || (this.originalHeadingsArray[x].equals("Func.refGene")&&intronicCell)  || excludeCell
                                       )
                               {
                                    cellStyle = getDefaultCellStyle(currentRow,Color.GRAY);
                               }else
                               {
                                   cellStyle = getDefaultCellStyle(currentRow, Color.WHITE);
                                  
                               }
                                cell.setCellStyle(cellStyle);
                                cell.setCellValue(dataArray[x]);
                                  
                              }
                                
                             }
                        
       
                    
                    
       // return currentRow;
    }
    
    public void excelHeadingCreator(String specifiedsheet, String headingLine)
    {
        XSSFSheet currentSheet = workbookcopy.createSheet(specifiedsheet);
        XSSFRow currentRow = currentSheet.createRow(0);
        
        XSSFCellStyle cellStyle;
      
       // String[] originalHeadingsArray = headingLine.split("\t");
        this.originalHeadingsArray=headingLine.split("\t");;
      
        HashMap<String, Integer> headings = new HashMap<String, Integer>();
        for(int x = 0; x < originalHeadingsArray.length; x++) {
            if (specifiedsheet.equals("Illumina"))
            {
                headings.put(originalHeadingsArray[x].substring(0, originalHeadingsArray[x].indexOf("_")), x);
            }else
            {
                 headings.put(originalHeadingsArray[x], x);
            }
            
        }
        //will not do rearrangement for annovar in java for now, will try to implement in pandas
        if (specifiedsheet.equals("Illumina"))
        {
        this.setRearrangedHashMap(headings);
        }
        
        this.originalHeadings=headings;
        
        //the headers for the first three columns
        for(int x = 0; x<3; x++)
        {
             Cell cell = currentRow.createCell(x);
           cellStyle = getDefaultCellStyle(currentRow,Color.WHITE);
            if(x==0 || x== 1)
           {
              
               cell.setCellStyle(cellStyle);
               if(x==0)
               {
                    cell.setCellValue("Fellow 1 Interpretation");
                   
               }else
               {
               cell.setCellValue("Fellow 2 Interpretation");
               
               }
               
           }else if(x==2)
           {
               cell.setCellStyle(cellStyle);
               cell.setCellValue("Attending Pathologist Interpretation");
           }
        }
        
        for(int x = 0; x < originalHeadingsArray.length; x++) {
           
            //plus three because have three header columns
           Cell cell = currentRow.createCell(x+3);
           cellStyle = getDefaultCellStyle(currentRow,Color.WHITE);
          // cell.setCellStyle(cellStyle);
           if (specifiedsheet.equals("Illumina"))
           {
               if(this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Gene_")
                       ||this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Variant_")
                       ||this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Chr_"))
                     //  ||originalHeadingsArray[x].contains("Coordinate_")
                    //   ||originalHeadingsArray[x].contains("Type_"))
                     //  ||originalHeadingsArray[x-3].contains("Genotype_")
                      // ||originalHeadingsArray[x-3].contains("Coordinate_")
                     //  ||originalHeadingsArray[x-3].contains("Filters_"))
               {
                   cellStyle.setRotation((short)0);   
               }else
               {  
                cellStyle.setRotation((short)90);      
               }
           }else
           {
               
               cellStyle.setRotation((short)0); 
               //not rotating anymore for the alternative pipeline
              /*  if(this.originalHeadingsArray[x].contains("Gene.refGene"))
                     //  ||originalHeadingsArray[x].contains("Coordinate_")
                    //   ||originalHeadingsArray[x].contains("Type_"))
                     //  ||originalHeadingsArray[x-3].contains("Genotype_")
                      // ||originalHeadingsArray[x-3].contains("Coordinate_")
                     //  ||originalHeadingsArray[x-3].contains("Filters_"))
               {
                   cellStyle.setRotation((short)0);   
               }else
               {  
                //not    
                cellStyle.setRotation((short)90);      
               }*/
               
               
                
           }
            
              cell.setCellStyle(cellStyle);
              //cell.setCellValue(originalHeadingsArray[x-3]);
             // this.tsvRearrangeConversion.get(x)
              if (specifiedsheet.equals("Illumina"))
              {
              cell.setCellValue(this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)]);
              }else
              {
              cell.setCellValue(this.originalHeadingsArray[x]);    
              }
               //cell.setCellValue(headinsArray[headingsConversion.get(x-3)]);
           
           
           
        }//end for loop
        
        
        //return currentRow;
    }
    
    
    
    
    public void excelFormator(String sheetName, File variantTsvFile) throws IOException
    {
       // String[] headingsArray = tsvHeadingLine.split("\t");
       // HashMap<String, Integer> headings = new HashMap<String, Integer>();
       // for(int x = 0; x < headingsArray.length; x++) {
       //     headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
       // }
        XSSFSheet currentSheet = this.workbookcopy.getSheet(sheetName);
       
        XSSFPrintSetup printSetup = (XSSFPrintSetup)currentSheet.getPrintSetup();
        
             File xslxTempFile = new File(variantTsvFile.getCanonicalPath() + ".coverage_qc.xlsx");
                        currentSheet.getHeader().setLeft(xslxTempFile.getName());
                        currentSheet.getHeader().setRight("DO NOT DISCARD!!!  Keep with patient folder.");
                        //in Dr. Carter's VBA was set at points 18 which is .25 inches
                        currentSheet.setMargin(Sheet.RightMargin, .25);
                        currentSheet.setMargin(Sheet.LeftMargin, .25);
                      
                        printSetup.setOrientation(PrintOrientation.LANDSCAPE);
                        
                        
                        //NOTE: setFitWidth doesn't work for columns, ie can't setFitToPageColumns, this 
                        //is the best workaround I can do, it will only looked cramped for those with a lot of calls
                        printSetup.setFitWidth((short)1);
                        printSetup.setFitHeight((short)3);
                       currentSheet.setRepeatingRows(CellRangeAddress.valueOf("1"));
                        //not making the rows hidden anymore for the alternative pipeline so I am not setting fit to page
                        //currentSheet.setFitToPage(true);
                        //making it by default not print the fellow's interp
                        currentSheet.getWorkbook().setPrintArea(0, 2, 20, 0, currentSheet.getLastRowNum());
                       
                        
                        try
                        {
                            for(int x=0; x<currentSheet.getRow(0).getPhysicalNumberOfCells();x++)
                       {
                           
                           currentSheet.autoSizeColumn(x);
                          
                           if(sheetName.equals("ILLUMINA"))
                           {
                           if (x>33)
                           {    
                           currentSheet.setColumnHidden(x, true);
                           }
                           }else
                           {
                               //want to show column AZ = 26+26=52, AY = 51
                               if (x>25 &&!(x>=39 && x<=48))
                                 {    
                                   currentSheet.setColumnHidden(x, true);
                                 }
                           }
                          
                       }
                            
                        }catch (IndexOutOfBoundsException e)
                        {
                            System.err.println("IndexOutOfBoundsException: " + e.getMessage());
                            System.err.println("Cannot autofit columns, unknown how to fix this problem at this time, perhaps need updated poi verison");
                        }
//                       
                        currentSheet.setColumnWidth(0, 10000);
                        currentSheet.setColumnWidth(1, 10000);
                        currentSheet.setColumnWidth(2, 10000);
                     
                      
    
                
                 
            
                   
    }
    
    //If Color.White is specified there is no do not calls or warnings
    //If Color.Yellow is specified there is no do not calls but there are warnings
    //If Color.Gray is specifified there are do not calls and there may or may not be warnings
    private  XSSFCellStyle getDefaultCellStyle(XSSFRow currentRow, Color specifiedColor)
    {
       
        XSSFCellStyle cellStyle = currentRow.getSheet().getWorkbook().createCellStyle();
        
        cellStyle.setWrapText(true);
        cellStyle.setBorderBottom(cellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(cellStyle.BORDER_THIN);
        cellStyle.setBorderRight(cellStyle.BORDER_THIN);
        cellStyle.setBorderTop(cellStyle.BORDER_THIN);
        XSSFFont myFont = currentRow.getSheet().getWorkbook().createFont();
        myFont.setFontHeight(9);
        cellStyle.setFont(myFont);
        cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(new XSSFColor(specifiedColor));
       return cellStyle;
    }
    
    //place holder, for now not rearranging
    //TODO
    //may not eventually implement as I can do the arrangement using pandas python library
     public void setRearrangedHashMapAnnovar(HashMap<String, Integer> originalHeadings)
    {
      
        
        
    }
    
    public void setRearrangedHashMap(HashMap<String, Integer> originalHeadings)
    {
        //illumina TSV columns end in BQ (26 *2) + 17; hence the hashmap should be 69 in length, TSV ends in BQ
        //HashMap<Integer,Integer> tsvRearrangeConversion;
        tsvRearrangeConversion = new HashMap<Integer,Integer>();
        int x=0;
        tsvRearrangeConversion.put(x++,originalHeadings.get("Gene"));//A
        tsvRearrangeConversion.put(x++,originalHeadings.get("Variant"));//B
        tsvRearrangeConversion.put(x++,originalHeadings.get("Chr"));//C
        tsvRearrangeConversion.put(x++,originalHeadings.get("cDNA Position"));//AC
        tsvRearrangeConversion.put(x++,originalHeadings.get("CDS Position"));//AD
        tsvRearrangeConversion.put(x++,originalHeadings.get("Protein Position"));//AE
        tsvRearrangeConversion.put(x++,originalHeadings.get("Coordinate"));//D
        tsvRearrangeConversion.put(x++,originalHeadings.get("Type"));//E
        tsvRearrangeConversion.put(x++,originalHeadings.get("Genotype"));//F
        tsvRearrangeConversion.put(x++,originalHeadings.get("Exonic"));//G
        tsvRearrangeConversion.put(x++,originalHeadings.get("Filters"));//H
        tsvRearrangeConversion.put(x++,originalHeadings.get("Quality"));//I
        tsvRearrangeConversion.put(x++,originalHeadings.get("GQX"));//J
        tsvRearrangeConversion.put(x++,originalHeadings.get("Alt Variant Freq"));//k
        tsvRearrangeConversion.put(x++,originalHeadings.get("Read Depth"));//L
        tsvRearrangeConversion.put(x++,originalHeadings.get("Alt Read Depth"));//M
        tsvRearrangeConversion.put(x++,originalHeadings.get("Consequence"));//N
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Global Minor"));//O
        tsvRearrangeConversion.put(x++,originalHeadings.get("Sift"));//P
        tsvRearrangeConversion.put(x++,originalHeadings.get("PolyPhen"));//Q
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC ID"));//R
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Primary Site"));//S
        tsvRearrangeConversion.put(x++,originalHeadings.get("ENSP"));//T
        tsvRearrangeConversion.put(x++,originalHeadings.get("HGVSc"));//U
        tsvRearrangeConversion.put(x++,originalHeadings.get("HGVSp"));//V
        tsvRearrangeConversion.put(x++,originalHeadings.get("dbSNP ID"));//W
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Accession"));//X
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Ref"));//Y
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Alleles"));//Z
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Allele Type"));//AA
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Significance"));//AB
        tsvRearrangeConversion.put(x++,originalHeadings.get("Classification"));//AF
        tsvRearrangeConversion.put(x++,originalHeadings.get("Inherited From"));//AG
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allelic Depths"));//AH
        tsvRearrangeConversion.put(x++,originalHeadings.get("Custom Annotation"));//AI
        tsvRearrangeConversion.put(x++,originalHeadings.get("Custom Gene Annotation"));//AJ
        tsvRearrangeConversion.put(x++,originalHeadings.get("Num Transcripts"));//AK
        tsvRearrangeConversion.put(x++,originalHeadings.get("Transcript"));//AL
        tsvRearrangeConversion.put(x++,originalHeadings.get("Amino Acids"));//AM
        tsvRearrangeConversion.put(x++,originalHeadings.get("Codons"));//AN
        tsvRearrangeConversion.put(x++,originalHeadings.get("HGNC"));//AO
        tsvRearrangeConversion.put(x++,originalHeadings.get("Transcript HGNC"));//AP
        tsvRearrangeConversion.put(x++,originalHeadings.get("Canonical"));//AQ
        tsvRearrangeConversion.put(x++,originalHeadings.get("Ancestral Allele"));//AR
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq"));//AS
        tsvRearrangeConversion.put(x++,originalHeadings.get("Global Minor Allele"));//AT
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Amr"));//AU
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Asn"));//AV
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Af"));//AW
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Eur"));//AX
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Evs"));//AY
        tsvRearrangeConversion.put(x++,originalHeadings.get("EVS Coverage"));//AZ
        tsvRearrangeConversion.put(x++,originalHeadings.get("EVS Samples"));//BA
        tsvRearrangeConversion.put(x++,originalHeadings.get("Conserved Sequence"));//BB
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Wildtype"));//BC
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Allele"));//BD
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Gene"));//BE
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Histology"));//BF
        tsvRearrangeConversion.put(x++,originalHeadings.get("Alternate Alleles"));//BG
        tsvRearrangeConversion.put(x++,originalHeadings.get("Google Scholar"));//BH
        tsvRearrangeConversion.put(x++,originalHeadings.get("PubMed"));//BI
        tsvRearrangeConversion.put(x++,originalHeadings.get("UCSC Browser"));//BJ
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar RS"));//BK
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Disease Name"));//BL
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar MedGen"));//BM
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar OMIM"));//BN
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Orphanet"));//BO
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar GeneReviews"));//BP
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar SnoMedCt ID"));//BQ
        
        
    }
    
    public XSSFWorkbook getWorkbook()
    {
        return this.workbookcopy;
    }
    
}
