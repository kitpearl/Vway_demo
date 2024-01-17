package com.cubegen.fp.loc.Parser;

import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProCLocParser extends BaseParser {
	int bracket = 0;		//괄호 갯수 ( 열면 + 닫으면 -)
	String tab = "\t"; 	//Tab String 
	String intab = "";	//소스에 입력될 TabString	
	public StringBuffer LOCParser(StringBuffer source){		 
		source = AddBeforeSourceLineFlag(source);		
		source = MultiLineCommentReplace(source);
		source = SingleLineCommentReplace(source);
		source = MultiSpaceReplace(source);
		source = SpaceReplace(source);
		source = AddSourceLineFlag(source);
		
		StringBuffer tmpsb = new StringBuffer();
		String line = "";
		String tmpline = "";				
		int mbracket = 0;		
		boolean statement = false;					
		Scanner scanner = new Scanner(source.toString()).useDelimiter("\n");						
		
		while(scanner.hasNext()){
			line = scanner.next().trim();
			
			if("".equals(line)){
				continue;
			}
			
			intab = "";
			for (int i=0; i < mbracket; i++){
				intab += tab;
			}						
			if ("{".equals(line)) {
				++mbracket;
			} else if (line.indexOf("}") == 0) {				
				--mbracket;
				intab = "";
				for(int i=0; i < mbracket; i++){
					intab += tab;
				}
			} else if (";".equals(line) && tmpsb.length() == 0 ) {											
				continue;
			} else if (";".equals(line) && tmpsb.length() > 0 && tmpsb.substring(tmpsb.length()-1, tmpsb.length()).equals("\n")) {
				tmpsb.insert(tmpsb.length()-1, line);								
				continue;
			} else if (",".equals(line) && tmpsb.length() > 0 && tmpsb.substring(tmpsb.length()-1, tmpsb.length()).equals("\n")) {
				tmpsb.insert(tmpsb.length()-1, line);								
				continue;
			}
			
			if( ((line.length() > 2) && ("for".equals(line.substring(0, 3))))    || 
					((line.length() > 1) && ("if".equals(line.substring(0, 2))))	   ||
					((line.length() > 6) && ("else if".equals(line.substring(0, 7))))||
					((line.length() > 4) && ("while".equals(line.substring(0, 5))))  ||
					((line.length() > 5) && ("switch".equals(line.substring(0, 6)))) )
			{								
				tmpsb.append(intab);
				statement = true;
				
				bracket = 0;
				while(statement){
					tmpline = BracketCheckLineStr(line);
					tmpsb.append(tmpline);
					if(bracket == 0){
						statement = false;
						break;
					} else if( bracket != 0 && line.equals("{")){
						tmpsb.insert(tmpsb.length()-1, "\n" + intab);	
						//괄호 짝이 안맞는 경우도 있음.
						++mbracket;
						bracket = 0;
						statement = false;
						break;
					} else if(scanner.hasNext()){
						line = scanner.next().trim();											
					} else {
						break;
					}
				}				
				tmpsb.append("\n" + "/**/");							
			} else if ( (line.length() > 8) && "exec sql".equalsIgnoreCase(line.substring(0, 8)) ) {
				tmpline = SqlCheckLineStr(line);
				tmpsb.append(intab + tmpline + "\n");
			} else {	   	 	  				
				tmpsb.append(intab + line + "\n");
			}		
		}		
		
		scanner.close();
		
		if(source.toString().trim().length() != 0){
			tmpsb.delete(tmpsb.length()-1, tmpsb.length());
		}
		
		source = tmpsb;
						
		return source;		
	}
	
	//#으로 시작하는 include, define, ifdef, #endif 는 ';'이 없어서 임시로 삽입.  
	protected StringBuffer AddBeforeSourceLineFlag(StringBuffer source){
		String regex = "#(.)*|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);		
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;
    while(matcher.find()) {
	  	if((matcher.start(1) > -1) && (!source.substring(matcher.start(1)+j, matcher.end(1)+j).equals(";"))){	  	
	  		source.insert(matcher.end(1)+j, ";");
	  		j++;	  		
    	}
    }
		return source;
	}
	
	protected StringBuffer MultiLineCommentReplace(StringBuffer source){
		//StringBuffer tmp = new StringBuffer();		
		//tmp.append(source.toString().replaceAll("/\\*(?:.|[\\n\\r])*?\\*/|(?://.*)", ""));
		//tmp.append(source.toString().replaceAll("/\\*(?:.|[\\n\\r])*?\\*/|(?://.*)|[\\n\\r]*", ""));
		//tmp.append(source.toString().replaceAll("([^/]|\\G)/\\*(?:.|[\\n\\r])*?\\*/", ""));
		//source = tmp;		
		//return source;
		StringBuffer tmp = new StringBuffer();
		String regex = "(/\\*(?:.|[\\n\\r])*?\\*/)|((?://.*))|(\\\".*?/\\*(?:.)*?\\*/.*?\\\")|(\\\".*?//.*?\\\")";
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
	
	//포인터에서 * 처리?
	protected StringBuffer SpaceReplace(StringBuffer source){
		String regex = "(\\s)*(\\[|\\(|\\)|\\||\\&|\\;|\\,|\\<|\\>|\\!|\\=|\\+|\\-|\\*|\\%|\\^)(\\s)*|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";			
		//String regex = "( |\\t)*(\\[|\\(|\\)|\\||\\&|\\;|\\,|\\<|\\>|\\!|\\=|\\+|\\-|\\*)( |\\t)*|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;    
    while(matcher.find()) {    							  		  	
  		if(matcher.start(1) > -1){
  			source.delete(matcher.start(1)-j, matcher.end(1)-j);
  			j++;
  		}
  		if(matcher.start(3) > -1){
  			source.delete(matcher.start(3)-j, matcher.end(3)-j);
  			j++;
  		}    	
    }        
    return source;
	}
	
	protected StringBuffer MultiSpaceReplace(StringBuffer source){		
		StringBuffer tmp = new StringBuffer();
		//tmp.append(source.toString().replaceAll("( |\\t)( |\\t)*", " "));
		tmp.append(source.toString().replaceAll("\\s(\\s)+|(\\t)+", " "));
		source = tmp;		
		return source;				
	}
	
	protected StringBuffer AddSourceLineFlag(StringBuffer source){
		//String regex = "(\\;)|(\\{)|(\\})|(\\\"){1}(?:.)*?(\\\")";
		String regex = "(\\;)|(\\{)|(\\})|(\\#)|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
		Pattern pattern = Pattern.compile(regex);		
    Matcher matcher = pattern.matcher(source.toString());    
    int j = 0;
    while(matcher.find()) {	  		  	
  		if(matcher.start(1) > -1){
  			source.insert(matcher.end(1)+j, "\n");
  			j++;
  		}
  		if(matcher.start(2) > -1){
  			source.insert(matcher.end(2)+j-1, "\n");
  			j++;	  			
  			source.insert(matcher.end(2)+j, "\n");
  			j++;
  		}
  		if((matcher.start(3) > -1) && (source.toString().length() > matcher.end(3)+j)){
  			source.insert(matcher.end(3)+j-1, "\n");
  			j++;	  			
  			source.insert(matcher.end(3)+j, "\n");
  			j++;
  		}
  		if(matcher.start(4) > -1){
  			source.insert(matcher.end(4)+j-1, "\n");
  			j++;	  			  			
  		}
    }		
		return source;		
	}
	
	protected int BracketCheckLineNo(String Line){
		return 0;		
	}
	
	protected String BracketCheckLineStr(String line){
		StringBuffer tmp = new StringBuffer(line);
		String regex = "(\\()|(\\))|(\\\"\\\")|(\\\"){1}(?:.)*?([^\\\\]\\\"|\\\\\\\\\\\")";
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
	
	protected String SqlCheckLineStr(String line) {
		/*
		String SqlType = "";
		StringBuffer tmp = new StringBuffer();				
		StringTokenizer tokenizer = new StringTokenizer(line, " ,", true);
		String token = "";
		
		//1차 - 키워드 단위로 라인구분.
		while(tokenizer.hasMoreTokens()){
			token = tokenizer.nextToken();			
			System.out.print(token);
			if(token.equalsIgnoreCase("select") || 
				 token.equalsIgnoreCase("from")   ||
				 token.equalsIgnoreCase("insert") ||
				 token.equalsIgnoreCase("where")  ||
				 token.equalsIgnoreCase("and")
				){
				tmp.append("\n" + intab);
				tmp.append(token);
			}else{
				tmp.append(token);
			}
		}*/
		String SqlType = "";
		int sqlbracket = 0;
		int sqlQuation = 0;
		StringBuffer tmp = new StringBuffer();		
		StringTokenizer tokenizer = new StringTokenizer(line, " ,()'", true);
		String token = "";		
		
		//2차 - 키워드, 필드 단위로 라인구분.
		while(tokenizer.hasMoreTokens()){
			token = tokenizer.nextToken();			
			//System.out.print(token+"/");						 			
			if(token.equalsIgnoreCase("select") || 
				 token.equalsIgnoreCase("from")   ||				 
				 token.equalsIgnoreCase("where")  ||
				 token.equalsIgnoreCase("and")		||
				 token.equalsIgnoreCase("or")			||
				 token.equalsIgnoreCase("insert") ||
				 token.equalsIgnoreCase("values") ||
				 token.equalsIgnoreCase("update")	||
				 token.equalsIgnoreCase("set")		||
				 token.equalsIgnoreCase("delete")	||
				 token.equalsIgnoreCase("order")	||
				 token.equalsIgnoreCase("group")  			 
				 //|| (token.equalsIgnoreCase("into") && SqlType.equalsIgnoreCase("select"))
				){
				SqlType = token;
				tmp.append("\n" + intab + tab + token);				
			} else if( token.equalsIgnoreCase("execute") ) {
					SqlType = token;
					tmp.append(token);				
			} else if( SqlType.equalsIgnoreCase("execute") && token.equalsIgnoreCase("begin") ) {				
				tmp.append("\n" + intab + tab + token);				
			} else if( token.equalsIgnoreCase(",") && (sqlbracket ==  1) && (sqlQuation%2 == 0) &&
					       (SqlType.equalsIgnoreCase("insert") || SqlType.equalsIgnoreCase("values"))
					     ) {				
				tmp.append(token + "\n" + intab + tab);				
			} else if((token.equals("(")) && (sqlQuation%2 == 0) && (sqlbracket ==  0) && 
				  		  (SqlType.equalsIgnoreCase("insert") || SqlType.equalsIgnoreCase("values"))
				 			 ) {
				tmp.append("\n" + intab + tab + token + "\n" + intab + tab);
				sqlbracket++;
			} else if((token.equals(")")) && (sqlQuation%2 == 0) && (sqlbracket ==  1)  && 
							  (SqlType.equalsIgnoreCase("insert") || SqlType.equalsIgnoreCase("values"))
							 ) {
				tmp.append("\n" + intab + tab + token);				
				sqlbracket--;				
			} else if(token.equalsIgnoreCase(",") && (sqlbracket ==  0) && (sqlQuation%2 == 0)) {				
				tmp.append(token);
				tmp.append("\n" + intab + tab);				
			} else if(token.equals("(") && (sqlQuation%2 == 0)) {
				tmp.append(token);
				sqlbracket++;
			} else if(token.equals(")") && (sqlQuation%2 == 0)) {
				tmp.append(token);
				sqlbracket--;				
			} else if(token.equals("'")) {
				tmp.append(token);
				sqlQuation++;
			} else {
				tmp.append(token);
			}
		}				
		line = tmp.toString();
		return line;
	}
}