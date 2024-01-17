package com.cubegen.fp.loc;

import java.awt.List;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

import com.cubegen.common.CharsetToolkit;
import com.cubegen.common.FTPControl;
import com.cubegen.common.FileUtils;
import com.cubegen.common.SmartEncodingInputStream;
import com.cubegen.common.StringUtils;
import com.cubegen.fp.loc.Parser.BaseParser;
import com.cubegen.fp.loc.Parser.GeneralParser;
import com.cubegen.fp.loc.Parser.JavaLocParser;

import difflib.Delta;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Delta.TYPE;

public class test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main( String[] args) throws InterruptedException, IOException
	{	
		
    try { 
    	
    	FTPControl FTP = new FTPControl("17.180.203.30", 21, "fpadmin", "fp12345");
    	System.out.println("1");
    	FTP.CheckConnect(true);
    	System.out.println("2");
    	Thread.sleep(1000*60*25);
    	System.out.println("3");
    	FTP.CheckConnect(true);
    	System.out.println("4");
    	FTP.CheckConnect(true);
    	FTP.CheckConnect(true);
    	
    	File a = new File("c:\\aa.txt");
    	if (a.exists()) {
    		a.setWritable(true);
    			
    	}
    	StringBuffer SrcFile = FileUtils.FileReader("D:\\Work\\샘플 소스\\파일형식\\UTF-16LE.txt");
    	FileUtils.FileWriter("c:\\","aa.txt", "aa");
    	FileOutputStream fos = new FileOutputStream("c:\\aa.txt");    	
    	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    	osw.write(SrcFile.toString());
    	osw.close();
    	fos.close();
    	
      // Open the file that is the first 
      // command line parameter 
      //FileInputStream fstream = new FileInputStream("D:\\Work\\샘플 소스\\파일형식\\UTF-16LE.txt");
    	 SrcFile = FileUtils.FileReader("D:\\Work\\샘플 소스\\파일형식\\UTF-16LE.txt");
    	FileInputStream fstream = new FileInputStream("D:\\Work\\샘플 소스\\파일형식\\UTF-16LE.txt");
      // Get the object of DataInputStream 
      DataInputStream in = new DataInputStream(fstream); 
      BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-16LE")); 
      String strLine; 
      // Read File Line By Line 
      while ((strLine = br.readLine()) != null) { 
          // Write to the file 
      	System.out.println(strLine); 
      } 
      // Close the input stream 
      in.close(); 
  } catch (Exception e) {// Catch exception if any 
      System.err.println("Error: " + e.getMessage()); 
  } 

  System.out.println("done."); 
  
		
		
		File file = new File("D:\\Work\\샘플 소스\\파일형식\\UTF-16LE.txt");

		Charset guessedCharset = CharsetToolkit.guessEncoding(file, 4096);
		System.err.println("Charset found: " + guessedCharset.displayName());

		FileInputStream fis = new FileInputStream(file);
		//InputStreamReader isr = new InputStreamReader(fis, guessedCharset);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-16LE");
		BufferedReader br = new BufferedReader(isr);

		String line;
		while ((line = br.readLine()) != null)
		{
			System.out.println(line);
			break;
		}

		
		/*
		StringBuffer source = new StringBuffer();
		source = FileUtils.FileReader("E:\\MetaPlus\\util\\Resource\\KEB\\463\\20100817000266\\com\\keb\\stpa\\ejb\\fee\\CommissionBean.java.2.0");
		BaseParser pp = new JavaLocParser();
		StringBuffer newsrc = pp.LOCParser(source);
		FileUtils.FileWriter("c:\\", "aaa.srd", newsrc.toString());
		ArrayList<String> revised =  new ArrayList<String>(); 
		Scanner scanner = new Scanner(newsrc.toString()).useDelimiter("\n");						
		while(scanner.hasNext()){
			revised.add(scanner.next());
		}
		scanner.close();
		 
		 if (newsrc.length() > 0 ){
			 
		 }
		 */
	
				
		//FileUtils.FileWriter(TempLoCPath, OldFile, original.toString());
		
		ArrayList<String> original = new ArrayList<String>();
		ArrayList<String> revised =  new ArrayList<String>();
		/*
		original = FileUtils.fileToLines("D:\\Work\\샘플 소스\\파일형식\\UTF_8.txt");
		original = FileUtils.fileToLines("D:\\Work\\샘플 소스\\파일형식\\UTF-16LE.txt");
		original = FileUtils.fileToLines("D:\\Work\\샘플 소스\\파일형식\\PC1G010FL_unicode.cs");
		original = FileUtils.fileToLines("D:\\Work\\샘플 소스\\파일형식\\cPLkUpPltConfig.cs");
		*/
		
		original = FileUtils.fileToLines("E:\\MetaPlus\\ResProject\\M1202-1826\\KEB\\ChangeFlow\\461\\20100817000262\\appl\\tffe03\\d_tmffe001_ed.srd.3.0");
		revised = FileUtils.fileToLines("E:\\MetaPlus\\ResProject\\M1202-1826\\KEB\\ChangeFlow\\461\\20100817000262\\appl\\tffe03\\d_tmffe001_ed.srd.4.0");
		
		Patch patch = DiffUtils.diff(original, revised);
		
		int addsLoc=0;
		int chgsLoc=0;
		int delsLoc=0;
		StringBuffer sbDiffResult= new StringBuffer();
		
		String stype ="";
		for (Delta delta: patch.getDeltas()) {
			
			
			if (delta.getType() == TYPE.INSERT) {
				addsLoc += delta.getRevised().size();
			} else if(delta.getType() == TYPE.CHANGE) {
				if(delta.getRevised().size() == delta.getOriginal().size()){
					chgsLoc += delta.getRevised().size();
				}else if(delta.getRevised().size() > delta.getOriginal().size()){
					addsLoc += (delta.getRevised().size() - delta.getOriginal().size());
					chgsLoc += delta.getOriginal().size();
				}else if(delta.getRevised().size() < delta.getOriginal().size()){
					delsLoc += (delta.getOriginal().size() - delta.getRevised().size());
					chgsLoc += delta.getRevised().size();
				}						
			} else if(delta.getType() == TYPE.DELETE) {
				delsLoc += delta.getOriginal().size();
			}
			
			
			sbDiffResult.append("Type    : "+delta.getType().toString()+"\n");
			sbDiffResult.append("Original: [position: " + delta.getOriginal().getPosition() + ", size: " + delta.getOriginal().size() +"]\n"+ 
					StringUtils.ListToString(delta.getOriginal().getLines(), "\n")+"\n");
			sbDiffResult.append("Revised : [position: " + delta.getRevised().getPosition() + ", size: " + delta.getRevised().size() +"]\n"+ 
					StringUtils.ListToString(delta.getRevised().getLines(), "\n")+"\n");
			sbDiffResult.append("==================================================="+"\n\n");
		}
		FileUtils.FileWriter("c:\\", "DiffResult", sbDiffResult.toString());
		if (addsLoc>0){
			System.out.println(addsLoc);
		}
	}

}
