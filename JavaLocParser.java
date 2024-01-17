package com.cubegen.fp.loc.Parser;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cubegen.common.StringUtils;

/**
 * ClassName     : JavaLocParser
 * @Description  : Line Of Count 자바  파서
 */
public class JavaLocParser extends BaseParser {		
	int bracket = 0;		//괄호 갯수 ( 열면 + 닫으면 -)
	char tab = '\t'; 	//Tab String 
	String intab = "";	//소스에 입력될 TabString
	/**
	 * MethodName   : LOCParser
	 * @Description : 소스를 비교할 수 있도록 변환.
	 * @param       : StringBuffer source(변환할 소스)
	 * @return      : StringBuffer source(변환된 소스)
	 */
	public StringBuffer LOCParser(StringBuffer source) {
		StringBuffer tmpsb = new StringBuffer();
		String lineStr = "";
		String tmpline = "";
		int mbracket = 0;
				
		boolean statement = false;		
		
		source = MultiLineCommentReplace(source);
		source = SingleLineCommentReplace(source);		
		source = SpaceReplace(source);
		source = MultiSpaceReplace(source);
		source = AddSourceLineFlag(source);
		
		Scanner scanner = new Scanner(source.toString()).useDelimiter("\n");						
		
		while(scanner.hasNext()){
			lineStr = scanner.next().trim();
			
			if("".equals(lineStr)) continue;			
			
			intab = "";
			intab = StringUtils.lPad(intab, mbracket, tab);
			/*
			for (int i=0; i < mbracket; i++) {
				intab += tab;
			}			*/
			
			if ("{".equals(lineStr)) ++mbracket;
			else if ("}".equals(lineStr)) {
				--mbracket;
				intab = "";
				intab = StringUtils.lPad(intab, mbracket, tab);
				/*
				for(int i=0; i < mbracket; i++){
					intab += tab;
				}
				*/
			} else if (";".equals(lineStr) && tmpsb.substring(tmpsb.length()-1, tmpsb.length()).equals("\n"))  {
				tmpsb.insert(tmpsb.length()-1, lineStr);								
				continue;
			} else if (",".equals(lineStr) && tmpsb.substring(tmpsb.length()-1, tmpsb.length()).equals("\n"))  {
				tmpsb.insert(tmpsb.length()-1, lineStr);								
				continue;
			}
			
			if( ((lineStr.length() > 2)&&("for".equals(lineStr.substring(0, 3)))) 		||
					((lineStr.length() > 1)&&("if".equals(lineStr.substring(0, 2))))			||
					((lineStr.length() > 6)&&("else if".equals(lineStr.substring(0, 7)))) ||
					((lineStr.length() > 4)&&("while".equals(lineStr.substring(0, 5)))) 	||
					((lineStr.length() > 5)&&("switch".equals(lineStr.substring(0, 6))))
			){
				tmpsb.append(intab);
				statement = true;

				bracket = 0;
				while(statement){
					tmpline = BracketCheckLineStr(lineStr);
					tmpsb.append(tmpline);
					if(bracket == 0){
						statement = false;
						break;
					} else if(scanner.hasNext()){
						lineStr = scanner.next().trim();
					} else {
						break;
					}						
				}
				tmpsb.append("\n");
			} else {				
				tmpsb.append(intab + lineStr + "\n");
			}
		}
		scanner.close();
		if(source.toString().trim().length() != 0){
			tmpsb.delete(tmpsb.length()-1, tmpsb.length());
		}
		source = tmpsb;					
		return source;
	}
	
	/**
	 * MethodName   : CommentReplace
	 * @Description : 소스의 주석 및 라인 구분자를 제거한다.
	 * @param       : StringBuffer source(변환할 소스)
	 * @return      : StringBuffer source(변환된 소스)	  
	 */
	protected StringBuffer MultiLineCommentReplace(StringBuffer source) {				
		
		//StringBuffer tmp = new StringBuffer();		
		//tmp.append(source.toString().replaceAll("/\\*(?:.|[\\n\\r])*?\\*/|(?://.*)|[\\n\\r]*", ""));
		//tmp.append(source.toString().replaceAll("([^/]|\\G)/\\*(?:.|[\\n\\r])*?\\*/", ""));
		//source = tmp;		
		//return source;
		StringBuffer tmp = new StringBuffer();
		String regex = "(/\\*(?:.|[\\n\\r])*?\\*/)|" +
				"((?://.*))|" +
				"(\\\".*?/\\*(?:.)*?\\*/.*?\\\")|" +
				"(\\\".*?//.*?\\\")";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;
    //System.out.println(matcher.toString());
    while(matcher.find()) {
    	if ((matcher.start(0) > -1) && (!(matcher.start(3) > -1)) && !(matcher.start(4) > -1)){
  			source.delete(matcher.start(0)-j, matcher.end(0)-j);
  			j = j + matcher.end(0) - matcher.start(0);    			
    	}    	
    }

    tmp.append(source.toString().replaceAll("[\\n\\r]*", ""));
    source = tmp;
    return source;		
	}
	
	protected StringBuffer SingleLineCommentReplace(StringBuffer source) {
		/*
		StringBuffer tmp = new StringBuffer();
		String regex = "(?://.*)|(\\\".*?//.*?\\\")";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;
    //System.out.println(matcher.toString());
    while(matcher.find()) {
    	if ((matcher.start(0) > -1) && !(matcher.start(1) > -1)){
  			source.delete(matcher.start(0)-j, matcher.end(0)-j);
  			j = j + matcher.end(0) - matcher.start(0);    			
    	}  		
    }
    tmp.append(source.toString().replaceAll("[\\n\\r]*", ""));
    source = tmp;
    */
    return source;
	}

	/**
	 * Method       : SpaceReplace
	 * @Description : 오퍼레이션 주위의 한칸 공백을 제거한다. 
	 * 							  <br> 문자열 안에서는 제외한다.
	 * @param       : StringBuffer source(변환할 소스)
	 * @return      : StringBuffer source(변환된 소스)
	 */
	protected StringBuffer SpaceReplace(StringBuffer source) {
		//String regex = "(\\s)*(\\(|\\)|\\||\\&|\\;|\\,|\\<|\\>|\\!|\\=|\\+|\\-)(\\s)*|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\s)*(\\[|\\(|\\)|\\||\\&|\\;|\\,|\\<|\\>|\\!|\\=|\\+|\\-|\\*|\\%|\\^)(\\s)*|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")|(\\\'){1}(?:.)*?([^\\\\]\\\'|\\\\\\\\\\\')";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;
    
    while(matcher.find()) {    						
	  	for(int i = 1; i < matcher.groupCount()+1; i++){	  	
	  		if((i==1) && (matcher.start(1) > -1)){
	  			source.delete(matcher.start(1)-j, matcher.end(1)-j);
	  			j++;
	  		}else if((i==3) && (matcher.start(3) > -1)){
	  			source.delete(matcher.start(3)-j, matcher.end(3)-j);
	  			j++;
	  		}
    	}
	  	//데이터 체크.
	  	//System.out.println(matcher.toString());
    }    
    return source;
	}
	/**
	 * MethodName   : MultiSpaceReplace
	 * @Description : 한칸 이상의 공백이나 탭을  한칸으로 변환한다.
	 * @param       : StringBuffer source(변환할 소스)
	 * @return      : StringBuffer source(변환된 소스)
	 */
	protected StringBuffer MultiSpaceReplace(StringBuffer source) {
		/*
		//정규식 \s 는 whiteSpace (공백, 탭, 라인(?)포함)
		String regex = "(\\s(\\s+))|(\\\"){1}(?:.)*?(\\\")";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;    
    while(matcher.find()) {    						
	  	for(int i = 1; i < matcher.groupCount()+1; i++){	  	
	  		if((i==1) && (matcher.start(1) > 0)){
	  			source.replace(matcher.start(1)-j, matcher.end(1)-j, " ");	  			
	  			j = j + (matcher.end(1) - matcher.start(1))-1;	  			
	  		}
    	}	  	
	  }
		return source;
		*/		
		StringBuffer tmp = new StringBuffer();
		//tmp.append(source.toString().replaceAll("\\s\\s+", " "));
		tmp.append(source.toString().replaceAll("\\s(\\s)+|(\\t)+", " "));
		source = tmp;		
		return source;
									
	}
	
	/**
	 * MethodName   : AddSourceLineFlag
	 * @Description : 소스에 라인 구분자를 추가 한다.
	 * @param       : StringBuffer source(변환할 소스)
	 * @return      : StringBuffer source(변환된 소스)
	 */
	protected StringBuffer AddSourceLineFlag(StringBuffer source) {
		//String regex = "(\\;)|(\\{)|(\\})|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\;)|(\\{)|(\\})|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")|(\\\'){1}(?:.)*?([^\\\\]\\\'|\\\\\\\\\\\')";
		Pattern pattern = Pattern.compile(regex);
		
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;
    while(matcher.find()) {
	  	for(int i = 1; i < matcher.groupCount()+1; i++){	  	
	  		if((i==1) && (matcher.start(1) > -1)){
	  			source.insert(matcher.end(1)+j, "\n");
	  			j++;
	  		}
	  		if((i==2) && (matcher.start(2) > -1)){
	  			source.insert(matcher.end(2)+j-1, "\n");
	  			j++;	  			
	  			source.insert(matcher.end(2)+j, "\n");
	  			j++;
	  		}
	  		if((i==3) && (matcher.start(3) > -1) && (source.toString().length() > matcher.end(3)+j)){
	  			source.insert(matcher.end(3)+j-1, "\n");
	  			j++;	  			
	  			source.insert(matcher.end(3)+j, "\n");
	  			j++;
	  		}
    	}
	  	//데이터 체크.
	  	//System.out.println(matcher.toString());
    }
		/*
		tmpsrc = tmpsrc.replaceAll("\\;", "\\;\n");
		tmpsrc = tmpsrc.replaceAll("\\{", "\n\\{\n");
		tmpsrc = tmpsrc.replaceAll("\\}", "\\}\n");
		*/
		//tmp.append(tmpsrc);		
		//source = tmp;	
		return source;
	}
	/**
	 * MethodName   : BracketCheck
	 * @Description : 소스 라인 안의 괄호 수를 체크 한다. 
	 * @param       : String Line(소스 라인)
	 * @return      : int(소괄호 수)
	 */
	protected int BracketCheckLineNo(String Line) {
		//String regex = "(\\()|(\\))|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\()|(\\))|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")|(\\\'){1}(?:.)*?([^\\\\]\\\'|\\\\\\\\\\\')";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(Line);
    int bracket = 0;
		while(matcher.find()) {
	  	if((matcher.start(1) > -1)){
	  		bracket++;
	  	}else if((matcher.start(2) > -1)){
	  		bracket--;
	  	}
		}
		return bracket;
	}
	
	protected String BracketCheckLineStr(String line) {
		//ArrayList<String> statementStr = new ArrayList<String>();							
		//StringUtils.inStartsWith(line, statementStr);
		
		StringBuffer tmp = new StringBuffer(line);
		//String regex = "(\\()|(\\))|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\()|(\\))|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")|(\\\'){1}(?:.)*?([^\\\\]\\\'|\\\\\\\\\\\')";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(line);        
		while(matcher.find()) {
		  if((matcher.start(1) > -1)){
	  		bracket++;
	  	}else if((matcher.start(2) > -1)){
	  		bracket--;
	  		if(bracket == 0){
	  			if(tmp.toString().length()!= matcher.end(2))
	  				tmp.insert(matcher.end(2), "\n" + intab + tab);
					break;
				}
	  	}
		}
		line = tmp.toString();
		return line;
	}
}