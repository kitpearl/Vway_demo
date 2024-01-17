package com.cubegen.fp.loc.Parser;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cubegen.common.StringUtils;

public class TALLocParser extends BaseParser {
	int inputline;     // 입력 소스 라인수
	int blankline;     // 공백 라인수
	int commentline;   // 주석라인수
	int sourceline;    // 문 라인수

	/**
	 * MethodName : LOCParser
	 * 
	 * @Description : 소스를 비교할 수 있도록 변환.
	 * @param : StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(변환된 소스)
	 */
	public StringBuffer LOCParser(StringBuffer source) {

		// --- (1) TAL 구분자 리스트 작성 (미사용) -----
		ArrayList<String> talComd = new ArrayList<String>();

		talComd.add("wlform");
		talComd.add("call");
		talComd.add("return");
		talComd.add("do");
		

		// --- (2) TAL 소스 READ(INPUT) 선언처리
		StringBuffer tmpsb = new StringBuffer();
		Scanner scanner = new Scanner(source.toString()).useDelimiter("\n");

		// source = MultiLineCommentReplace(source);
		// source = SingleLineCommentReplace(source);
		// source = SpaceReplace(source);
		// source = AddSourceLineFlag(source);

		// --- (3) 라인단위 소스 파싱 처리
		String line = "";
		char   lineendkbn = ' ';
		int    intab, 
		       inkbn, inkbn1, inkbn2, inkbn3, inkbn4, inkbn5 = 0;
		char   data_state = 'E';   // 데이터구조 선언 
		int    ifkbn      = 0;    
		char   if_state   = 'E';   // if 문 구조   
		char   statemun   = 'E';   // 문 종료 체크용 
		
		while (scanner.hasNext()) {                       // 소스 데이터가 있는 반복처리 

			line = scanner.next();                       // 소스라인 단위 입력
			inputline++;                                 // 입력 소스 라인수 계산
			// System.out.println("입력 소스 = " + line);  // OK

			int charsu = 0;                              // ! 문자 갯수
			char cflag = ' ';                            // 주석개시 'C'
			String templine = "";                        // 주석 제거 후 라인 소스

			// --- (3.1) 주석데이터 제거 (!, --)
			for (int i = 0; i < line.length(); i++) {
				
				//--- (3.1.1) (!) 주석 제거 용
				if (line.charAt(i) == '!') {
					if (cflag == 'C') {
						cflag = ' ';
					} else {
						cflag = 'C';
					}
					continue;
				}
				
				//--- (3.1.2) (--) 주석제거 용
				if (i+1 < line.length()){
				    if ((line.charAt(i)== '-') && (line.charAt(i+1)== '-')){
					cflag = 'C';
				    }
				}    
				
				//--- (3.1.3) 주석 이외 데이터 설정 
				if (cflag != 'C') {
					templine += line.charAt(i);
				}

				//--- (3.1.4) 주석 내의 문자수 체크 
				if ((i < 71) && (line.charAt(i) != ' ')) {
					charsu++;
				}
			}
					
			templine = templine.trim();                            // 좌우 공백제거 라인 소스
			//System.out.println("주석제거라인소스=" + templine);  // OK

			// --- (3.3) 주석 및 공백라인 계산
			if (templine.length() == 0) { // 공백 라인 체크
				if (charsu > 0) {
					commentline++;                                  // 주석라인 계산
				} else {
					blankline++;                                    // 공백 라인 수 계산
				}
				continue;
			}

			// --- (3.4) token 생성 및 문 소스 더블 공백 제거
			templine = templine.replaceAll("\t", " ");
			String token = "";                                     // 토큰 데이터 작업용
			String[] tokenarray = new String[50];                  // 라인단위 토큰용 배열
			int it = 0;
			String linetrim = "";
			linetrim += templine.charAt(0);
			token    += templine.charAt(0);
			for (int i = 1; i < templine.length(); i++) {

				// --- (3.4.1) 문 소스 더블 공백 제거
				if ((i>1) && (templine.charAt(i-1)   == ' ') &&
					(templine.charAt(i)     == ' ')) {
					continue;
				} else if (templine.charAt(i) != ' '){
                    	linetrim += templine.charAt(i);  //
                    	token    += templine.charAt(i);  //
				}

				// --- (3.4.2) token 처리
				if ((token.length() > 0) && (templine.charAt(i) == ' ')) {
				    linetrim += templine.charAt(i); 
					//tokenarray[it] = token;
					token = "";
					it++;
				}
			}
	
			// ---(3.4.3) 최종 token 설정 ( 토큰은 미사용 )
			if (token.length() > 0) {
				//tokenarray[it] = token;
				token = "";
			}
			linetrim += ' ';
			
			linetrim = linetrim.toLowerCase();    // 소스(대문자) --> 소스(소문자) 변경
			//System.out.println("공백제거라인소스=" + templine);  // OK
			
			
			
			// --- (3.5) 문 종료 체크   및   문 소스 라인 계산
			//System.out.println("파싱대상데이터 =" + linetrim);
			
			while (linetrim.length() > 0) {
								
				int isincl = linetrim.indexOf("?");
				if (isincl != -1) {
					tmpsb.append(linetrim + "\n");                
					sourceline++;                                 
					linetrim = "";
					break;
				}
				
				// --- (3.5.1) 데이터 구조체 설정  체크
				inkbn1 = linetrim.indexOf("define");
				inkbn2 = linetrim.indexOf("literal");
				inkbn3 = linetrim.indexOf("string");
				inkbn4 = linetrim.indexOf("int");
				inkbn5 = linetrim.indexOf("fixed");
				if ((inkbn1 != -1) || (inkbn2 != -1) ||
					(inkbn3 != -1) || (inkbn4 != -1) || 
					(inkbn5 != -1)) {
					data_state = 'Y';                  
				}
				
				//--- (3.5.2) else if 체크 		
				int iselseif = linetrim.indexOf("else if");
				if (iselseif != -1){
					if (statemun == 'N') {
						tmpsb.append("\n");	
						statemun = 'E';
					}
					tmpsb.append(linetrim.substring(iselseif, iselseif+5) + "\n" );  // else문 출력
					sourceline++;
					statemun = 'E';
					linetrim = StringUtils.SubString(linetrim, iselseif+5);            
                    linetrim = linetrim.trim();
                    continue;
				}
								
				//--- (3.5.3) if then 처리  
				int isifkbn = linetrim.indexOf("if");
				if (isifkbn != -1) {
					if_state = 'S';
				}
				
				if (isifkbn != -1){				
					int isifthen = linetrim.indexOf("then");
					if (isifthen != -1) {
						if (statemun == 'N') {
							tmpsb.append("\n");	
							statemun = 'E';
						}
						tmpsb.append(linetrim.substring(ifkbn, isifthen-1) + "\n");   // if문 출력
						sourceline++;
						tmpsb.append(linetrim.substring(isifthen, isifthen+4) + "\n" );  // then문 출력
						sourceline++;
						statemun = 'E';
						if (linetrim.length() > isifthen+4){
							linetrim = StringUtils.SubString(linetrim, isifthen+5);            
							linetrim = linetrim.trim();
							continue;
						} else {
							linetrim = "";
							continue;
						}
                     }
				}
				
				
				//--- (3.5.4) then 체크처리   
				int isthen = linetrim.indexOf("then");
				if (isthen != -1) {
			        if (isthen > 0 ){
			        	tmpsb.append(linetrim.substring(0, isthen-1) + "\n");    // if 문 출력
			        	sourceline++;
			        	tmpsb.append(linetrim.substring(isthen, isthen+4) + "\n" );  // then문 출력
						sourceline++;
						statemun = 'E';
						if (linetrim.length() > isthen+4){
							linetrim = StringUtils.SubString(linetrim, isthen+5);            
							linetrim = linetrim.trim();
							continue;
						} else {
							linetrim = "";
							continue;
						}
			        	
			        } else { 
			        	tmpsb.append(linetrim.substring(isthen, isthen+4) + "\n" );  // then문 출력
						sourceline++;
						statemun = 'E';
						if (linetrim.length() > isthen+4){
							linetrim = StringUtils.SubString(linetrim, isthen+5);            
							linetrim = linetrim.trim();
							continue;
						} else {
							linetrim = "";
							continue;
						}
			        }
				}	
					
				//--- (3.5.5) else 체크처리   
				int iselse = linetrim.indexOf("else");
	            if (iselse != -1) {
	            	if (iselse > 0) { 
	            		if (statemun == 'N') {
							tmpsb.append("\n");	
							statemun = 'E';
						}
	            		tmpsb.append(linetrim.substring(0, iselse-1) + "\n");        // else문 출력
						sourceline++;
						tmpsb.append(linetrim.substring(iselse, iselse+4) + "\n" );  // else 문 출력
						sourceline++;
						statemun = 'E';
						linetrim = StringUtils.SubString(linetrim, iselse+5);            
                        linetrim = linetrim.trim();
                        continue;
	            	} else {
						tmpsb.append("\n" + linetrim.substring(iselse, iselse+4) + "\n" );  // else 문 출력
						sourceline++;
						statemun = 'E';
						linetrim = StringUtils.SubString(linetrim, iselse+5);            
                        linetrim = linetrim.trim();
                        continue;
	            	}
	            }

	            //--- (3.5.6) begin 체크처리 
				int isbegin = linetrim.indexOf("begin");
				if (isbegin != -1) {
					
					if (isbegin == 0) {
						if (statemun == 'N') {
							tmpsb.append("\n");	
							statemun = 'E';
						}
						tmpsb.append(linetrim.substring(0, 5) + "\n");
					    sourceline++;                                         // 문 라인 수 계산
					} else { 
						if (statemun == 'N') {
							tmpsb.append("\n");	
							statemun = 'E';
						}
						tmpsb.append(linetrim.substring(0, isbegin-1) + "\n");
					    sourceline++;                                          
					    tmpsb.append(linetrim.substring(isbegin, isbegin+5) + "\n");
					    sourceline++; 
					    statemun = 'E';
					}
					
					if (isbegin+5 == linetrim.length()) {
						linetrim = "";
					} else {
						linetrim = StringUtils.SubString(linetrim, isbegin+5);
	                    linetrim = linetrim.trim();
					}
					continue;
				}

				//--- (3.5.7) 문 종료 체크처리 
				int isend = linetrim.indexOf(";");
				if (isend != -1) {
					tmpsb.append(linetrim.substring(0, isend+1) + "\n");  
					sourceline++;   
					
					linetrim = linetrim.trim();
					if (isend+1 >= linetrim.length()) {
						linetrim = "";
					} else {
						linetrim = StringUtils.SubString(linetrim, isend+2);
						linetrim = linetrim.trim();
					}
					statemun   = 'E';            // 문장 종료  
					data_state = 'E';            // data 구조체 종료 
					if_state   = 'N';            // if   구조체 종료 
					continue;
				}
			
				//--- (3.5.8) 상기 이외 데이터 처리 
               	if (data_state == 'Y') { //  구조체 데이터 라인별  처리 
					tmpsb.append(linetrim + "\n");
					sourceline++;                                  
					linetrim = "";	
					statemun = 'E';
				} else {                                            // 상기 이외 데이터 처리 
					tmpsb.append(linetrim);
					linetrim = "";
					statemun = 'N';
				}
			}

		}
		
		scanner.close();

		source = tmpsb;
		return source;

	}

	/**
	 * MethodName : CommentReplace
	 * 
	 * @Description : 소스의 주석 및 라인 구분자를 제거한다.
	 * @param : StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(변환된 소스)
	 */
	public StringBuffer MultiLineCommentReplace(StringBuffer source) {
		// StringBuffer tmp = new StringBuffer();
		// tmp.append(source.toString().replaceAll("/\\*(?:.|[\\n\\r])*?\\*/|(?://.*)|[\\n\\r]*",
		// ""));
		// tmp.append(source.toString().replaceAll("/\\*(?:.|[\\n\\r])*?\\*/",
		// ""));
		// source = tmp;
		return source;
	}

	/**
	 * MethodName : CommentReplace
	 * 
	 * @Description : 소스의 주석 및 라인 구분자를 제거한다.
	 * @param : StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(변환된 소스)
	 */

	public StringBuffer SingleLineCommentReplace(StringBuffer source) {
		// StringBuffer tmp = new StringBuffer();
		// String regex = "(?://.*)|(\\\".*?//.*?\\\")";
		// Pattern pattern = Pattern.compile(regex);
		// Matcher matcher = pattern.matcher(source.toString());
		// int j = 0;
		// System.out.println(matcher.toString());
		// while(matcher.find()) {
		// if ((matcher.start(0) > 0) && !(matcher.start(1) > 0)){
		// source.delete(matcher.start(0)-j, matcher.end(0)-j);
		// j = j + matcher.end(0) - matcher.start(0);
		// }

		// }
		// tmp.append(source.toString().replaceAll("[\\n\\r]*", ""));
		// source = tmp;
		return source;
	}

	/**
	 * Method : SpaceReplace
	 * 
	 * @Description : 오퍼레이션 주위의 한칸 공백을 제거한다. <br>
	 *              문자열 안에서는 제외한다.
	 * @param : StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(변환된 소스)
	 */
	public StringBuffer SpaceReplace(StringBuffer source) {
		// String regex =
		// "(\\s)*(\\(|\\)|\\||\\&|\\;|\\,|\\<|\\>|\\!|\\=|\\+|\\-)(\\s)*|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\s)*(\\[|\\(|\\)|\\||\\&|\\;|\\,|\\<|\\>|\\!|\\=|\\+|\\-|\\*|\\%|\\^)(\\s)*|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source.toString());
		int j = 0;

		while (matcher.find()) {
			for (int i = 1; i < matcher.groupCount() + 1; i++) {
				if ((i == 1) && (matcher.start(1) > 0)) {
					source.delete(matcher.start(1) - j, matcher.end(1) - j);
					j++;
				} else if ((i == 3) && (matcher.start(3) > 0)) {
					source.delete(matcher.start(3) - j, matcher.end(3) - j);
					j++;
				}
			}
			// 데이터 체크.
			// System.out.println(matcher.toString());
		}
		return source;
	}

	/**
	 * MethodName : MultiSpaceReplace
	 * 
	 * @Description : 한칸 이상의 공백이나 탭을 한칸으로 변환한다.
	 * @param : StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(변환된 소스)
	 */
	public StringBuffer MultiSpaceReplace(StringBuffer source) {
		/*
		 * //정규식 \s 는 whiteSpace (공백, 탭, 라인(?)포함) String regex =
		 * "(\\s(\\s+))|(\\\"){1}(?:.)*?(\\\")"; Pattern pattern =
		 * Pattern.compile(regex); Matcher matcher =
		 * pattern.matcher(source.toString()); int j = 0; while(matcher.find())
		 * { for(int i = 1; i < matcher.groupCount()+1; i++){ if((i==1) &&
		 * (matcher.start(1) > 0)){ source.replace(matcher.start(1)-j,
		 * matcher.end(1)-j, " "); j = j + (matcher.end(1) -
		 * matcher.start(1))-1; } } } return source;
		 */
		StringBuffer tmp = new StringBuffer();
		tmp.append(source.toString().replaceAll("\\s(\\s)+|\\t", " "));
		source = tmp;
		return source;
	}

	/**
	 * MethodName : AddSourceLineFlag
	 * 
	 * @Description : 소스에 라인 구분자를 추가 한다.
	 * @param : StringBuffer source(변환할 소스)
	 * @return : StringBuffer source(변환된 소스)
	 */
	public StringBuffer AddSourceLineFlag(StringBuffer source) {
		// String regex = "(\\;)|(\\{)|(\\})|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\;)|(\\{)|(\\})|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(source.toString());
		int j = 0;
		while (matcher.find()) {
			for (int i = 1; i < matcher.groupCount() + 1; i++) {
				if ((i == 1) && (matcher.start(1) > 0)) {
					source.insert(matcher.end(1) + j, "\n");
					j++;
				} else if ((i == 2) && (matcher.start(2) > 0)) {
					source.insert(matcher.end(2) + j - 1, "\n");
					j++;
					source.insert(matcher.end(2) + j, "\n");
					j++;
				} else if ((i == 3) && (matcher.start(3) > 0)
						&& (source.toString().length() > matcher.end(3) + j)) {
					source.insert(matcher.end(3) + j, "\n");
					j++;
				}
			}
			// 데이터 체크.
			// System.out.println(matcher.toString());
		}
		/*
		 * tmpsrc = tmpsrc.replaceAll("\\;", "\\;\n"); tmpsrc =
		 * tmpsrc.replaceAll("\\{", "\n\\{\n"); tmpsrc =
		 * tmpsrc.replaceAll("\\}", "\\}\n");
		 */
		// tmp.append(tmpsrc);
		// source = tmp;
		return source;
	}

	/**
	 * MethodName : BracketCheck
	 * 
	 * @Description : 소스 라인 안의 괄호 수를 체크 한다.
	 * @param : String Line(소스 라인)
	 * @return : int(소괄호 수)
	 */
	public int BracketCheckLineNo(String Line) {
		// String regex = "(\\()|(\\))|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\()|(\\))|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Line);
		int bracket = 0;
		while (matcher.find()) {
			if ((matcher.start(1) > 0)) {
				bracket++;
			} else if ((matcher.start(2) > 0)) {
				bracket--;
			}
		}
		return bracket;
	}

	public String BracketCheckLineStr(String line) {
		int bracket = 0;
		StringBuffer tmp = new StringBuffer(line);
		// String regex = "(\\()|(\\))|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\()|(\\))|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);
		while (matcher.find()) {
			if ((matcher.start(1) > 0)) {
				bracket++;
			} else if ((matcher.start(2) > 0)) {
				bracket--;
				if (bracket == 0) {
					if (tmp.toString().length() != matcher.end(2))
						tmp.insert(matcher.end(2), "\n");
					break;
				}
			}
		}
		line = tmp.toString();
		return line;
	}

}
