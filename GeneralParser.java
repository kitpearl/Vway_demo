package com.cubegen.fp.loc.Parser;
/**
 * GeneralParser
 * @Description  : 	LOC 소스 정리 대상(JAVA, PROC, TAL, COBOL) 이 아닌 자원들은 
 * 									공백 라인, 한칸이상 공백만 정리한다. 
 */
public class GeneralParser extends BaseParser {
	
	public StringBuffer LOCParser(StringBuffer source){		
		source = MultiSpaceReplace(source);
		source = SpaceLineReplace(source);
		return source;
	}
	
	/* 멀티 라인 주석 제거 */
	protected StringBuffer MultiLineCommentReplace(StringBuffer source){
		return source;
	}
	
	/* 싱글 라인 주석 제거 */
	protected StringBuffer SingleLineCommentReplace(StringBuffer source){
		return source;
	}
	
	/* 필요 없는 한칸 공백 제거 */
	protected StringBuffer SpaceReplace(StringBuffer source){
		return source;
	}
	
	protected StringBuffer SpaceLineReplace(StringBuffer source){
		StringBuffer tmp = new StringBuffer();
		tmp.append(source.toString().replaceAll("\\s(\\s)+", "\n"));
		source = tmp;
		return source;		
	}
	
	/**  
	 * 한칸 이상 공백이나 탭을 한칸으로 변경
	 */
	protected StringBuffer MultiSpaceReplace(StringBuffer source){
		StringBuffer tmp = new StringBuffer();
		//공백 ' '이거나  \t 탭이 하나 이상인 경우 한칸으로 변환한다.
		tmp.append(source.toString().replaceAll("( |\\t)( |\\t)+|(\\t)+", " "));
		source = tmp;
		return source;
	}
	
	/* 특정 기호 기준으로 라인 구분자를 추가한다. */
	protected StringBuffer AddSourceLineFlag(StringBuffer source){
		return source;
	}
	
	protected int BracketCheckLineNo(String Line){
		return 0;
	}
	
	protected String BracketCheckLineStr(String line){
		return "";
	}
}