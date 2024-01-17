package com.cubegen.fp.loc.Parser;

public abstract class BaseParser {
	String langID="";
	
	/* Loc Parser ���� �Լ�*/
	public abstract StringBuffer LOCParser(StringBuffer source);	
	/* ��Ƽ���� �ּ� ���� */
	protected abstract StringBuffer MultiLineCommentReplace(StringBuffer source);
	/* �̱� ���� �ּ� ���� */
	protected abstract  StringBuffer SingleLineCommentReplace(StringBuffer source);
	/* �ʿ� ���� ��ĭ ���� ���� */
	protected abstract StringBuffer SpaceReplace(StringBuffer source);		
	/* ��ĭ �̻� ���� ��ĭ���� ���� */
	protected abstract StringBuffer MultiSpaceReplace(StringBuffer source);
	/* Ư�� ��ȣ �������� ���� �����ڸ� �߰��Ѵ�. */
	protected abstract StringBuffer AddSourceLineFlag(StringBuffer source);
	
	protected abstract int BracketCheckLineNo(String Line);
	
	protected abstract String BracketCheckLineStr(String line);
}
