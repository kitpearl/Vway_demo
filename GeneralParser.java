package com.cubegen.fp.loc.Parser;
/**
 * GeneralParser
 * @Description  : 	LOC �ҽ� ���� ���(JAVA, PROC, TAL, COBOL) �� �ƴ� �ڿ����� 
 * 									���� ����, ��ĭ�̻� ���鸸 �����Ѵ�. 
 */
public class GeneralParser extends BaseParser {
	
	public StringBuffer LOCParser(StringBuffer source){		
		source = MultiSpaceReplace(source);
		source = SpaceLineReplace(source);
		return source;
	}
	
	/* ��Ƽ ���� �ּ� ���� */
	protected StringBuffer MultiLineCommentReplace(StringBuffer source){
		return source;
	}
	
	/* �̱� ���� �ּ� ���� */
	protected StringBuffer SingleLineCommentReplace(StringBuffer source){
		return source;
	}
	
	/* �ʿ� ���� ��ĭ ���� ���� */
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
	 * ��ĭ �̻� �����̳� ���� ��ĭ���� ����
	 */
	protected StringBuffer MultiSpaceReplace(StringBuffer source){
		StringBuffer tmp = new StringBuffer();
		//���� ' '�̰ų�  \t ���� �ϳ� �̻��� ��� ��ĭ���� ��ȯ�Ѵ�.
		tmp.append(source.toString().replaceAll("( |\\t)( |\\t)+|(\\t)+", " "));
		source = tmp;
		return source;
	}
	
	/* Ư�� ��ȣ �������� ���� �����ڸ� �߰��Ѵ�. */
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