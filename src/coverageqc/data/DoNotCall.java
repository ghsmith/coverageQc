/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coverageqc.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author Tom
 * DoNotCall is a like a subtype OF VARIANT ///in future should make it an offical subclass by using "extends" feature
 */
@XmlRootElement
public class DoNotCall {
    //key inheritance
    //public String hgvscComplete;
    @XmlAttribute
    public String ensp;
    @XmlAttribute
    public String hgvsc;
   // @XmlAttribute
   // public String hgvsp;
    @XmlAttribute
    public String callType;
     @XmlAttribute
    public String transcript;
      @XmlAttribute
    public Long coordinate;
      @XmlAttribute
    public Long coordinate_end;
      @XmlAttribute
    public String AAChange;
    @XmlAttribute
    public Integer chr;
    @XmlAttribute
    public String annotator;
    @XmlAttribute
    public String pipeline;
    
       
   // @XmlAttribute
    public static DoNotCall populate(Row xslxHeadingRow, Row xslxDataRow, Integer calltype)
    {
        DoNotCall donotcall = new DoNotCall();
        int columnNumber;
	int cellIndex;
        String[] headerArray;
	HashMap<String, Integer> headings = new HashMap<String, Integer>();
                
        columnNumber = xslxHeadingRow.getLastCellNum();
	headerArray = new String[columnNumber];
        
        Iterator<Cell> cellIterator = xslxHeadingRow.cellIterator();
        while (cellIterator.hasNext())
        {
            Cell cell = cellIterator.next();
					cellIndex = cell.getColumnIndex();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN:
						headerArray[cellIndex] = Boolean.toString(cell
								.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						headerArray[cellIndex] = Double.toString(cell
								.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_STRING:
						headerArray[cellIndex] = cell.getStringCellValue();
						break;
					default:
						headerArray[cellIndex] = "";
					}
            
        }//end while celliterator
  
        for (int x = 0; x < headerArray.length; x++) {
                
                if(calltype <4)
                {
		headings.put(headerArray[x].substring(0,headerArray[x].indexOf("_")), x);
                }else
                {
                headings.put(headerArray[x], x);
                }
				}

        if(calltype<=3)
        //if it is less than or equal to three then it is is an illumina do not call, else it is a annovar variant    
        {
            donotcall.annotator="Illumina";
            donotcall.pipeline="Illumina";
            //String[] dataArray = xslxDataLine.split("\t");
        if(xslxDataRow.getCell(headings.get("HGVSc"))!=null)
        {
        donotcall.hgvsc = xslxDataRow.getCell(headings.get("HGVSc").intValue()).getStringCellValue();
           
        }
        //donotcall.hgvsp = xslxDataRow.getCell(headings.get("HGVSp").intValue()).getStringCellValue();
        if(xslxDataRow.getCell(headings.get("ENSP"))!=null)
        {
        donotcall.ensp = xslxDataRow.getCell(headings.get("ENSP").intValue()).getStringCellValue();
        }
        if(xslxDataRow.getCell(headings.get("Transcript"))!=null)
        {
        donotcall.transcript = xslxDataRow.getCell(headings.get("Transcript").intValue()).getStringCellValue();
        }else
        {
            System.out.println("Transcript_27 column entry is negative!  This is essential to do not call! Do not call list needs to be fixed!  Crashing to prevent abnormal behavior!");
                    System.exit(1);
        }
        donotcall.coordinate = (long)xslxDataRow.getCell(headings.get("Coordinate").intValue()).getNumericCellValue();
        
        }else
        {
            donotcall.annotator="Annovar";
            if(xslxDataRow.getCell(headings.get("AAChange.refGene"))!=null)
            {
             donotcall.AAChange = xslxDataRow.getCell(headings.get("AAChange.refGene").intValue()).getStringCellValue();
           
            }
            donotcall.coordinate = (long)xslxDataRow.getCell(headings.get("Start").intValue()).getNumericCellValue();
            donotcall.pipeline = xslxDataRow.getCell(headings.get("Pipeline").intValue()).getStringCellValue();
            donotcall.coordinate_end = (long)xslxDataRow.getCell(headings.get("End").intValue()).getNumericCellValue();
            String tmpchr = xslxDataRow.getCell(headings.get("Chr").intValue()).getStringCellValue();
            donotcall.chr=Integer.valueOf(tmpchr.substring(3));
           

            
        }
    
        
        
        if(calltype==1||calltype==4)
        {
            donotcall.callType="Don't call, always";
        }else if (calltype==2)
        {
            donotcall.callType="If percentage low, don't call";
            
        }else if (calltype==5)
        {
            donotcall.callType="If percentage low or strand bias don't call";
        }
        else if (calltype==3||calltype==6)
        {
            donotcall.callType= "On lab list, Unknown significance";
        }
       
        return donotcall;
    }
    
 
    
    
    
}
