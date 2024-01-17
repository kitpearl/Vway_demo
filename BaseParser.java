package com.cubegen.fp.loc.Parser;

public abstract class BaseParser {
	String langID="";
	
	/* Loc Parser 시작 함수*/
	public abstract StringBuffer LOCParser(StringBuffer source);	
	/* 멀티라인 주석 제거 */
	protected abstract StringBuffer MultiLineCommentReplace(StringBuffer source);
	/* 싱글 라인 주석 제거 */
	protected abstract  StringBuffer SingleLineCommentReplace(StringBuffer source);
	/* 필요 없는 한칸 공백 제거 */
	protected abstract StringBuffer SpaceReplace(StringBuffer source);		
	/* 한칸 이상 공백 한칸으로 변경 */
	protected abstract StringBuffer MultiSpaceReplace(StringBuffer source);
	/* 특정 기호 기준으로 라인 구분자를 추가한다. */
	protected abstract StringBuffer AddSourceLineFlag(StringBuffer source);
	
	protected abstract int BracketCheckLineNo(String Line);
	
	protected abstract String BracketCheckLineStr(String line);
}
end
