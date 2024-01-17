package com.cubegen.fp.loc.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.cubegen.common.FileUtils;

/**
 * ClassName : COBLocParser
 * @Description : Line Of Count 코볼 파서
 */
public class COBLocParser extends BaseParser {
	
	ArrayList<String> cobComd = new ArrayList<String>();
	
	public COBLocParser(String lang) {
		
		langID = lang;
// --- (1) COBOL 명령어 리스트 작성 -----
		
		cobComd.add("ADD");
		cobComd.add("CPMPUTE");
		cobComd.add("DIVIDE");
		cobComd.add("MULTIPLY");
		cobComd.add("SUBTRACT");
		cobComd.add("IF");
		//cobComd.add("THEN");
		cobComd.add("ELSE");
		cobComd.add("CONTINUE");
		cobComd.add("NEXT");
		cobComd.add("EXIT");

		cobComd.add("CALL");
		cobComd.add("PERFORM");
		cobComd.add("ALTER");
		cobComd.add("GO");
		cobComd.add("GOBACK");
		cobComd.add("STOP");
		cobComd.add("EVALUATE");

		cobComd.add("EXEC");

		cobComd.add("SEARCH");
		cobComd.add("WHEN");

		cobComd.add("MOVE");
		cobComd.add("INVOKE");
		cobComd.add("ON");
		cobComd.add("MERGE");
		cobComd.add("OUTPUT");
		cobComd.add("DISPLAY");
		cobComd.add("STRING");
		cobComd.add("UNSTRING");
		cobComd.add("ACCEPT");
		cobComd.add("INITIALIZE");
		cobComd.add("INSPECT");
		cobComd.add("SKIP1");
		cobComd.add("SKIP2");
		cobComd.add("SKIP3");
		cobComd.add("EJECT");
		cobComd.add("XML");

		cobComd.add("WRITE");
		cobComd.add("CLOSE");
		cobComd.add("REWRITE");
		cobComd.add("FUNCTION");
		cobComd.add("EJECT");
  }
	
	
	final static boolean isNumber(String str){
		if ((str!=null) && (str.length() > 0) && !str.contains("\"")){
			try {
				Double.parseDouble(str);
				return true;
			} catch (Exception e) {
				return false;
			}
			
		}
		return false;
	}
	
	/**
	 * MethodName : LOCParser
	 * 
	 * @Description : 소스를 비교할 수 있도록 변환.
	 * @param :
	 *            StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(ㅡ변환된 소스)
	 */
	public StringBuffer LOCParser(StringBuffer source) {

		// --- (2) COBOL 소스 READ(INPUT) 선언처리
		StringBuffer tmpsb = new StringBuffer();
		String line = "";
		String line0772 	= ""; 	// COBOL 유효컬럼 데이터 
		String linetrim 	= ""; 	// 소스라인 더블 공백(SPACE) 제거 용
		String token 			= ""; 	// 토큰 데이터 작업용
		int iTmpIdx = 0;
		boolean bEndDot= false;
		
		Scanner scanner = new Scanner(source.toString()).useDelimiter("\n");
		// --- (3) 라인단위 소스 파싱 처리		
		while (scanner.hasNext()) {
			// --- (3.1) 라인 단위 소스입력 처리
			line = scanner.next();
			
			//코드 중간 주석 처리
			if("TANDEM-COBOL".equals(langID)){
				iTmpIdx =line.indexOf("!");
				if (iTmpIdx >0) line = line.substring(0, iTmpIdx);
				
				if((line.trim().length()==0) || (line.charAt(0) == '!')) { //텐덤 코볼 한줄 주석
					continue;
				}
				
				line0772 = line.trim();
			}
			else {
				iTmpIdx =line.indexOf("*>");
				if (iTmpIdx >0) line = line.substring(0, iTmpIdx);
				
				//72컬럼 이후의 8자리 숫자 처리 Sosi문제로 72안쪽으로 들어오는 경우 있음
				if ((line.length()>8) && isNumber(line.substring(line.length()-8))){
					char[] arrChar = line.substring(0, line.length()-8).toCharArray();
					
					for(int i=arrChar.length-1; i>0; i--){				
						if ((arrChar[i]==' ')|| 
							 (arrChar[i]==')')||(arrChar[i]==']')|| 
							 (arrChar[i]=='.')||(arrChar[i]==';')||
							 (arrChar[i]=='"')||(arrChar[i]=='\'')||
							 Character.isLetterOrDigit(arrChar[i])  //영문자,숫자, 한글
							 )
						{						
							break;
						}
						else{
							arrChar[i] = ' ';
						}
					}
					line = String.valueOf(arrChar);
				}
				
				if((line == null)||(line.trim().length() < 7)){ //공백 처리
					continue;
				}
				else if (line.charAt(6) != ' '){ //한줄(7컬럼주석) 주석 처리
					continue;
				}
				
				if(line.length() > 72){
					line0772 = line.substring(7, 72); 
				} else {
					line0772 = line.substring(7, line.length()); 
				}
			}
			
			linetrim = line0772.replaceAll("(( |\\t)( |\\t)+)|(\\t)+", " ").trim();
			if (linetrim.length() == 0) continue;
			
			iTmpIdx = linetrim.indexOf(" ");
			if(iTmpIdx > -1){
				token = linetrim.substring(0, iTmpIdx);
			}else {
				token = linetrim;
			}
								
			if (linetrim.endsWith(".")) {
				tmpsb.append(linetrim+"\n"); // 문
				bEndDot = true;
			}
			else if ("TANDEM-COBOL".equals(langID) && linetrim.startsWith("?")){
				tmpsb.append(linetrim+"\n"); // 문
				bEndDot = true;
			}
			else if((cobComd.contains(token)) || (token.startsWith("END-"))) {
				if (bEndDot) tmpsb.append(linetrim); 
				else         tmpsb.append("\n"+linetrim); 
				bEndDot = false;
			}
			else{
				if (bEndDot) tmpsb.append(linetrim); //키워드 등록이 안된경우 때문 
				else         tmpsb.append(" "+linetrim);
				bEndDot = false;
			}					
		}
		
		scanner.close();
		
		source = tmpsb;
		return source;		
	}

	protected StringBuffer MultiLineCommentReplace(StringBuffer source) {
		return source;
	}

	protected StringBuffer SingleLineCommentReplace(StringBuffer source) {
		return source;
	}

	protected StringBuffer SpaceReplace(StringBuffer source) {
		return source;
	}

	protected StringBuffer MultiSpaceReplace(StringBuffer source) {
		return source;
	}

	protected StringBuffer AddSourceLineFlag(StringBuffer source) {
		return source;
	}

	protected int BracketCheckLineNo(String Line) {
		return 0;
	}

	protected String BracketCheckLineStr(String line) {		
		return "";
	}
	
	
	public static void main(String[] args) {
		COBLocParser pp = new COBLocParser("CICS-COBOL");
		StringBuffer source = FileUtils.FileReader("E:\\MetaPlus\\Resource\\KEB\\ChangeFlow\\57none\\20100806000121\\PDB2D521");
		
		source = pp.LOCParser(source);
		try {
	    FileUtils.FileWriter("c:\\", "aa.txt", source.toString());
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
		if (source.length() > 0){
		
		}
	}	
	
}

End
