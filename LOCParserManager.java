	package com.cubegen.fp.loc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cubegen.common.FTPControl;
import com.cubegen.common.FileUtils;
import com.cubegen.common.GTOneDecrypt;
import com.cubegen.common.StringUtils;
import com.cubegen.fp.loc.Parser.BaseParser;
import com.cubegen.fp.loc.Parser.COBLocParser;
import com.cubegen.fp.loc.Parser.GeneralParser;
import com.cubegen.fp.loc.Parser.JavaLocParser;
import com.cubegen.fp.loc.Parser.ProCLocParser;
import com.cubegen.fp.loc.Parser.TALLocParser;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Delta.TYPE;

public class LOCParserManager {

	protected static final Logger logger =  LogManager.getLogger("fpstudio");
	HashMap<String, BaseParser> ParserList = new HashMap<String, BaseParser>(); 
	FTPControl FTP = null;
	GTOneDecrypt decrypt = new GTOneDecrypt();
	Properties prop = new Properties();
	
	public LOCParserManager() throws FileNotFoundException, IOException {
		ParserList.put("GENERAL", new GeneralParser());
		ParserList.put("JAVA", new JavaLocParser());
		ParserList.put("COBOL", new COBLocParser("COBOL"));
		ParserList.put("CICS-COBOL", new COBLocParser("CICS-COBOL"));
		ParserList.put("TANDEM-COBOL", new COBLocParser("TANDEM-COBOL"));
		ParserList.put("PRO*C", new ProCLocParser());
		ParserList.put("TAL", new TALLocParser());

		//InputStream in = LOCParserManager.class.getClassLoader().getResourceAsStream("ftpconnect.properties");
		InputStream in = new FileInputStream("ftpconnect.properties");
		prop.load(in);
		//FTP.login();
  }
	
	public void finalize() {
		if (FTP!=null) FTP.logout();   
	}

	private void PblDump(String file, File fPath) throws IOException, InterruptedException {

		String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(); 
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/")+1);
		//logger.info("EXE 경로:"+decodedPath);
		Process prc = Runtime.getRuntime().exec(decodedPath+"PblDump.exe -es \""+file+"\" *.*", null,fPath);
		
		//InputStream readLine를 하지 않으면 버퍼가꽉차 무한대기가됨
		InputStream is = prc.getInputStream();                 
		BufferedReader br = new BufferedReader(new InputStreamReader(is));                 
		while (true)                 
		{                         
			String s = br.readLine();                         
			if (s == null) break;                         
			//System.out.println(s);                 
		}                 
  }
	
	private void CFDownloadFile(String server, String localpath,String localfile, String codetype) {
		String temp = localpath+localfile+".temp";
		FTP.get(server, temp);
		if ("MVS".equals(codetype)){
			decrypt.SrcDecrypt(temp, localpath+localfile, true);
		} else {						
			decrypt.SrcDecrypt(temp, localpath+localfile, false);
		}
		File f = new File(localpath+localfile);
		
		logger.info("FileSize:"+localfile+"-"+f.length());
		
  }
	
	/**
	 * 
	 * @param filePath 파일 전체 경로
	 * @param fileName 확장자포함 파일명
	 * @param cVerID 현재버전
	 * @param LangID 소스코드 언어
	 * @return
	 */
  public LocResult Run(String filePath, String fileName, String cVerID, String LangID, String ResrcID, 
  			String ProjectID, String CodeType, String ParentResrcID, boolean IsBaseLine) {
  	
  	boolean bChangeFlow = filePath.contains("ChangeFlow\\");
  	boolean bChangeFlowCMS = false;
  	String sCFPath="ChangeFlow\\";
  	String sCFRemotPath="/cms/CF/Repository/Upload/CFDevRepository/";
		
  	if (filePath.contains("ChangeFlow_CMS\\")) {
				bChangeFlow = true;
				bChangeFlowCMS = true;
				sCFPath="ChangeFlow_CMS\\";
				sCFRemotPath ="/wscf_fs/CF/Repository/Upload/CFDevRepository/";
  	}
  		
  	
  	if (FTP == null){
  		logger.info("FTP 생성");
  		FTP = new FTPControl(prop.getProperty("cf.server"), 21, prop.getProperty("cf.user"), prop.getProperty("cf.password"));
  		FTP.login();
  	}
  	
  	if (bChangeFlowCMS && (!prop.getProperty("wcf.server").equals(FTP.getServer()))){
  		logger.info("FTP변경 ChangeFlowCMS");
  		FTP.logout();
  		
  		FTP = new FTPControl(prop.getProperty("wcf.server"), 21, prop.getProperty("wcf.user"), prop.getProperty("wcf.password"));;
  		FTP.login();
  	}
  	else if ((!bChangeFlowCMS) && bChangeFlow && (!prop.getProperty("cf.server").equals(FTP.getServer()))){
  		logger.info("FTP변경 ChangeFlow");
  		FTP.logout();
  		
  		FTP = new FTPControl(prop.getProperty("cf.server"), 21, prop.getProperty("cf.user"), prop.getProperty("cf.password"));
  		FTP.login();
  	} 	
  	
  	
  	
  	LocResult result = new LocResult();
  	String runSetp="";
  	
  	
  	try {
  		runSetp = "01.시작";
			BaseParser parser = ParserList.get(LangID.toUpperCase());
			
			if (parser == null){	
				parser = ParserList.get("GENERAL");
			}
			
			StringBuffer revisedFile = null;
			StringBuffer originalFile =null;
			StringBuffer sbDiffResult= new StringBuffer();
			
			ArrayList<String> original = new ArrayList<String>();
			ArrayList<String> revised =  new ArrayList<String>();
			

			String OldFilePath = filePath.replaceAll("(?i)\\\\Resource\\\\", "\\\\ResourceLOC\\\\");
			String OldFile=ResrcID+'_'+fileName; //파일명에 대소문자가 구분되어 형상관리 1개이상의 자원이 등록되어 있음
			
			boolean bPbl = fileName.toLowerCase().endsWith(".pbl");
			boolean bCMS = filePath.toLowerCase().contains("\\cms\\");
			
			if ("1.0".equals(cVerID)) {
				FileUtils.DeleteFile(OldFilePath+OldFile);
			}
			else if ((cVerID==null) ||(cVerID=="")){
				cVerID = "1.0";
			}
		
			String oldVer =  String.valueOf(Double.parseDouble(cVerID)-1);
			if (!IsBaseLine){
				if (bChangeFlow){
					StringBuffer tempBuff= null;
					
					
					
					String resPath = filePath.substring(0, filePath.indexOf(sCFPath)+sCFPath.length())+"Temp\\";
					String serPath = filePath.substring(filePath.indexOf(sCFPath)+sCFPath.length());
									
					if (ResrcID.startsWith("1P")){
						resPath = resPath+ParentResrcID+"\\";
						
						if (!"1.0".equals(cVerID)) {
							runSetp = "03.OLD SRC LOCParser";
							tempBuff = FileUtils.FileReader(resPath+oldVer+"\\"+fileName);
							try {
								StringBuffer oldBuffer = parser.LOCParser(tempBuff);
								FileUtils.FileWriter(OldFilePath,OldFile, oldBuffer.toString());
              } catch (Exception e) {
              	throw new RuntimeException("OLD SRC LOCParser 오류", e);
              }
						}
						
						runSetp = "03.New SRC LOCParser";					
						tempBuff = FileUtils.FileReader(resPath+cVerID+"\\"+fileName);
						try{
							revisedFile = parser.LOCParser(tempBuff);
						} catch (Exception e) {
							throw new RuntimeException("New SRC LOCParser 오류", e);
	          }
					}
					else {
						try{
							serPath = serPath.substring(serPath.indexOf("\\")+1).replace("\\", "/");
							runSetp = "02.FTP ChangeDir";
							FTP.changeDir(sCFRemotPath+serPath);
							
							if (bPbl){
								resPath = resPath+ResrcID+"\\";
								FileUtils.DeleteDirectory(resPath);
								FileUtils.CreatePath(resPath);
								
								File fPath;
								
								if (!"1.0".equals(cVerID)) {
									CFDownloadFile(fileName+"."+oldVer, resPath,"OldSrc.pbl", CodeType);
									fPath = FileUtils.CreatePath(resPath+oldVer+"\\");
									PblDump(resPath+"OldSrc.pbl", fPath);
								}
								
								CFDownloadFile(fileName+"."+cVerID, resPath,"NewSrc.pbl", CodeType);
								fPath = FileUtils.CreatePath(resPath+cVerID+"\\");
								PblDump(resPath+"NewSrc.pbl", fPath);
								
								return result;
							}
							else{
								
								FileUtils.CreatePath(resPath);
								FileUtils.DeleteFile(resPath+"OldSrc.txt");
								FileUtils.DeleteFile(resPath+"NewSrc.txt");
								
								if (!"1.0".equals(cVerID)) {
									CFDownloadFile(fileName+"."+oldVer, resPath,"OldSrc.txt", CodeType);
									/*
									FTP.get(fileName+"."+oldVer, resPath+"OldTemp.txt");
									if ("MVS".equals(CodeType)){
										decrypt.SrcDecrypt(resPath+"OldTemp.txt", resPath+"OldSrc.txt", true);
									} else {						
										decrypt.SrcDecrypt(resPath+"OldTemp.txt", resPath+"OldSrc.txt", false);
									}*/
									runSetp = "03.OLD SRC LOCParser";
									tempBuff = FileUtils.FileReader(resPath+"OldSrc.txt");
									try{
										StringBuffer oldBuffer = parser.LOCParser(tempBuff);
										FileUtils.FileWriter(OldFilePath,OldFile, oldBuffer.toString());
									} catch (Exception e) {
										throw new RuntimeException("OLD SRC LOCParser 오류", e);
		              }
									
								}
								
								CFDownloadFile(fileName+"."+cVerID, resPath,"NewSrc.txt", CodeType);
								/*
								FTP.get(fileName+"."+cVerID, resPath+"NewTemp.txt");
								if ("MVS".equals(CodeType)){
									decrypt.SrcDecrypt(resPath+"NewTemp.txt", resPath+"NewSrc.txt", true);
								} else {					
									decrypt.SrcDecrypt(resPath+"NewTemp.txt", resPath+"NewSrc.txt", false);
								}
								*/
								runSetp = "03.New SRC LOCParser";
								
								tempBuff = FileUtils.FileReader(resPath+"NewSrc.txt");
								if (tempBuff.length()==0) {
									result.setError("revisedFile Size 0byte -"+fileName+"."+cVerID);
									return result;
								}
								try{
									revisedFile = parser.LOCParser(tempBuff);
								} catch (Exception e) {
									throw new RuntimeException("New SRC LOCParser 오류", e);
	              }
							}
			      } catch (Exception e) {
			      	logger.error(e.getMessage(), e);
			      	result.setError(e.getMessage()+"\n ChangeFlow 파일 다운로드 오류\n"+filePath+fileName);
			      	return result;
			      }
					}
				}
			}
			
			if ((!bChangeFlow) || (IsBaseLine)){
				runSetp = "02.SrcFile Load";
				
				StringBuffer SrcFile = FileUtils.FileReader(filePath+fileName);
				
				
				if (parser != null){			
					runSetp = "03.LOC Parsing";
					try{
						revisedFile = parser.LOCParser(SrcFile);
					} catch (Exception e) {
	        	result.setError(e.getMessage()+"\n LOC Parsing 오류\n"+filePath+fileName);
	        	return result;
	        }
				}
				else{
					throw new RuntimeException("not Find LocParser");
				}
			}
			
			runSetp = "04.Line Source 생성";
			//FileUtils.FileWriter(filePath,fileName+"1", revisedFile.toString());
			
			//revised =FileUtils.fileToLines(filePath+fileName+"1");
			
			if (revisedFile.length()==0){
				result.setError("revisedFile Size 0byte -"+fileName+"."+cVerID);
				return result;
			}
			
			Scanner scanner = new Scanner(revisedFile.toString()).useDelimiter("\n");						
			while(scanner.hasNext()){
				revised.add(scanner.next());
			}
			scanner.close();
			
			
			if (!IsBaseLine){
				runSetp = "05.OLD File Load";
				originalFile = FileUtils.FileReader(OldFilePath+OldFile); //임시용
				original = FileUtils.fileToLines(OldFilePath+OldFile);
			}
			
			result.setNewLineCount(revised.size());
			result.setOldLineCount(original.size());
			
			//FileUtils.FileWriter(TempSrcPath, fileName, SrcFile.toString());
	
			
			if (original.size()==0){
				result.setAddLines(revised.size());
			}
			else{			
				//FileUtils.FileWriter(TempLoCPath, OldFile, original.toString());
				
				runSetp = "06.File Diff";
				logger.info("Diff Start => original:"+original.size() +", revised:"+ revised.size());
				Patch patch = DiffUtils.diff(original, revised);
				logger.info("Diff End");
				int addsLoc=0;
				int chgsLoc=0;
				int delsLoc=0;
				
				
				runSetp = "07.File Diff Counting";
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
				
				result.setAddLines(addsLoc);
				result.setChgLines(chgsLoc);
				result.setDelLines(delsLoc);
			}


			
			String TempPath = filePath.replaceAll("(?i)\\\\Resource\\\\", "\\\\ResProject\\\\"+ProjectID+"\\\\");
			
			if (original.size()>0){
				FileUtils.FileWriter(TempPath,fileName+"."+oldVer, originalFile.toString());
				FileUtils.FileWriter(TempPath,fileName+".Diff", sbDiffResult.toString());
			}
			
			FileUtils.FileWriter(TempPath,fileName+"."+cVerID, revisedFile.toString());
		

			
			runSetp = "08. OLD File Write";
			FileUtils.FileWriter(OldFilePath,OldFile, revisedFile.toString());
			
			runSetp = "100.LOC완료";
			
    } catch (Exception e) {
    	result.setError(e.getMessage()+"\n"+runSetp);
    	logger.error(e.getMessage(), e);
    } finally {	
    	logger.info(runSetp);
      logger.info(result.toString());
  	}    
		return result;
  }
  
  public  void ftpTest() {
		if (FTP != null) FTP.logout();
		logger.info("wcf FTP Connect test");
		logger.info("wcf.server:"+ prop.getProperty("wcf.server"));
		logger.info("wcf.user:"+ prop.getProperty("wcf.user"));
		logger.info("wcf.password:"+ prop.getProperty("wcf.password"));
		FTP = new FTPControl(prop.getProperty("wcf.server"), 21, prop.getProperty("wcf.user"), prop.getProperty("wcf.password"));;
		FTP.login();
		
		FTP.logout();
		logger.info("cf FTP Connect test");		
		logger.info("cf.server:"+ prop.getProperty("cf.server"));
		logger.info("cf.user:"+ prop.getProperty("cf.user"));
		logger.info("cf.password:"+ prop.getProperty("cf.password"));
		FTP = new FTPControl(prop.getProperty("cf.server"), 21, prop.getProperty("cf.user"), prop.getProperty("cf.password"));
		FTP.login();
		FTP.logout(); 	
  }
  
  public static void main( String[] args) throws FileNotFoundException, IOException{
  	LOCParserManager lpm = new LOCParserManager();
  	lpm.ftpTest();
  }
  
}
