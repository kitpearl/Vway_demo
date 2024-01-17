package com.cubegen.fp.loc.Parser;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cubegen.common.StringUtils;

public class TALLocParser extends BaseParser {
	int inputline;     // �Է� �ҽ� ���μ�
	int blankline;     // ���� ���μ�
	int commentline;   // �ּ����μ�
	int sourceline;    // �� ���μ�

	/**
	 * MethodName : LOCParser
	 * 
	 * @Description : �ҽ��� ���� �� �ֵ��� ��ȯ.
	 * @param : StringBuffer source(��ȯ�� �ҽ�)
	 * @return : StringBuffer source(��ȯ�� �ҽ�)
	 */
	public StringBuffer LOCParser(StringBuffer source) {

		// --- (1) TAL ������ ����Ʈ �ۼ� (�̻��) -----
		ArrayList<String> talComd = new ArrayList<String>();

		talComd.add("wlform");
		talComd.add("call");
		talComd.add("return");
		talComd.add("do");
		

		// --- (2) TAL �ҽ� READ(INPUT) ����ó��
		StringBuffer tmpsb = new StringBuffer();
		Scanner scanner = new Scanner(source.toString()).useDelimiter("\n");

		// source = MultiLineCommentReplace(source);
		// source = SingleLineCommentReplace(source);
		// source = SpaceReplace(source);
		// source = AddSourceLineFlag(source);

		// --- (3) ���δ��� �ҽ� �Ľ� ó��
		String line = "";
		char   lineendkbn = ' ';
		int    intab, 
		       inkbn, inkbn1, inkbn2, inkbn3, inkbn4, inkbn5 = 0;
		char   data_state = 'E';   // �����ͱ��� ���� 
		int    ifkbn      = 0;    
		char   if_state   = 'E';   // if �� ����   
		char   statemun   = 'E';   // �� ���� üũ�� 
		
		while (scanner.hasNext()) {                       // �ҽ� �����Ͱ� �ִ� �ݺ�ó�� 

			line = scanner.next();                       // �ҽ����� ���� �Է�
			inputline++;                                 // �Է� �ҽ� ���μ� ���
			// System.out.println("�Է� �ҽ� = " + line);  // OK

			int charsu = 0;                              // ! ���� ����
			char cflag = ' ';                            // �ּ����� 'C'
			String templine = "";                        // �ּ� ���� �� ���� �ҽ�

			// --- (3.1) �ּ������� ���� (!, --)
			for (int i = 0; i < line.length(); i++) {
				
				//--- (3.1.1) (!) �ּ� ���� ��
				if (line.charAt(i) == '!') {
					if (cflag == 'C') {
						cflag = ' ';
					} else {
						cflag = 'C';
					}
					continue;
				}
				
				//--- (3.1.2) (--) �ּ����� ��
				if (i+1 < line.length()){
				    if ((line.charAt(i)== '-') && (line.charAt(i+1)== '-')){
					cflag = 'C';
				    }
				}    
				
				//--- (3.1.3) �ּ� �̿� ������ ���� 
				if (cflag != 'C') {
					templine += line.charAt(i);
				}

				//--- (3.1.4) �ּ� ���� ���ڼ� üũ 
				if ((i < 71) && (line.charAt(i) != ' ')) {
					charsu++;
				}
			}
					
			templine = templine.trim();                            // �¿� �������� ���� �ҽ�
			//System.out.println("�ּ����Ŷ��μҽ�=" + templine);  // OK

			// --- (3.3) �ּ� �� ������� ���
			if (templine.length() == 0) { // ���� ���� üũ
				if (charsu > 0) {
					commentline++;                                  // �ּ����� ���
				} else {
					blankline++;                                    // ���� ���� �� ���
				}
				continue;
			}

			// --- (3.4) token ���� �� �� �ҽ� ���� ���� ����
			templine = templine.replaceAll("\t", " ");
			String token = "";                                     // ��ū ������ �۾���
			String[] tokenarray = new String[50];                  // ���δ��� ��ū�� �迭
			int it = 0;
			String linetrim = "";
			linetrim += templine.charAt(0);
			token    += templine.charAt(0);
			for (int i = 1; i < templine.length(); i++) {

				// --- (3.4.1) �� �ҽ� ���� ���� ����
				if ((i>1) && (templine.charAt(i-1)   == ' ') &&
					(templine.charAt(i)     == ' ')) {
					continue;
				} else if (templine.charAt(i) != ' '){
                    	linetrim += templine.charAt(i);  //
                    	token    += templine.charAt(i);  //
				}

				// --- (3.4.2) token ó��
				if ((token.length() > 0) && (templine.charAt(i) == ' ')) {
				    linetrim += templine.charAt(i); 
					//tokenarray[it] = token;
					token = "";
					it++;
				}
			}
	
			// ---(3.4.3) ���� token ���� ( ��ū�� �̻�� )
			if (token.length() > 0) {
				//tokenarray[it] = token;
				token = "";
			}
			linetrim += ' ';
			
			linetrim = linetrim.toLowerCase();    // �ҽ�(�빮��) --> �ҽ�(�ҹ���) ����
			//System.out.println("�������Ŷ��μҽ�=" + templine);  // OK
			
			
			
			// --- (3.5) �� ���� üũ   ��   �� �ҽ� ���� ���
			//System.out.println("�Ľ̴������ =" + linetrim);
			
			while (linetrim.length() > 0) {
								
				int isincl = linetrim.indexOf("?");
				if (isincl != -1) {
					tmpsb.append(linetrim + "\n");                
					sourceline++;                                 
					linetrim = "";
					break;
				}
				
				// --- (3.5.1) ������ ����ü ����  üũ
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
				
				//--- (3.5.2) else if üũ 		
				int iselseif = linetrim.indexOf("else if");
				if (iselseif != -1){
					if (statemun == 'N') {
						tmpsb.append("\n");	
						statemun = 'E';
					}
					tmpsb.append(linetrim.substring(iselseif, iselseif+5) + "\n" );  // else�� ���
					sourceline++;
					statemun = 'E';
					linetrim = StringUtils.SubString(linetrim, iselseif+5);            
                    linetrim = linetrim.trim();
                    continue;
				}
								
				//--- (3.5.3) if then ó��  
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
						tmpsb.append(linetrim.substring(ifkbn, isifthen-1) + "\n");   // if�� ���
						sourceline++;
						tmpsb.append(linetrim.substring(isifthen, isifthen+4) + "\n" );  // then�� ���
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
				
				
				//--- (3.5.4) then üũó��   
				int isthen = linetrim.indexOf("then");
				if (isthen != -1) {
			        if (isthen > 0 ){
			        	tmpsb.append(linetrim.substring(0, isthen-1) + "\n");    // if �� ���
			        	sourceline++;
			        	tmpsb.append(linetrim.substring(isthen, isthen+4) + "\n" );  // then�� ���
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
			        	tmpsb.append(linetrim.substring(isthen, isthen+4) + "\n" );  // then�� ���
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
					
				//--- (3.5.5) else üũó��   
				int iselse = linetrim.indexOf("else");
	            if (iselse != -1) {
	            	if (iselse > 0) { 
	            		if (statemun == 'N') {
							tmpsb.append("\n");	
							statemun = 'E';
						}
	            		tmpsb.append(linetrim.substring(0, iselse-1) + "\n");        // else�� ���
						sourceline++;
						tmpsb.append(linetrim.substring(iselse, iselse+4) + "\n" );  // else �� ���
						sourceline++;
						statemun = 'E';
						linetrim = StringUtils.SubString(linetrim, iselse+5);            
                        linetrim = linetrim.trim();
                        continue;
	            	} else {
						tmpsb.append("\n" + linetrim.substring(iselse, iselse+4) + "\n" );  // else �� ���
						sourceline++;
						statemun = 'E';
						linetrim = StringUtils.SubString(linetrim, iselse+5);            
                        linetrim = linetrim.trim();
                        continue;
	            	}
	            }

	            //--- (3.5.6) begin üũó�� 
				int isbegin = linetrim.indexOf("begin");
				if (isbegin != -1) {
					
					if (isbegin == 0) {
						if (statemun == 'N') {
							tmpsb.append("\n");	
							statemun = 'E';
						}
						tmpsb.append(linetrim.substring(0, 5) + "\n");
					    sourceline++;                                         // �� ���� �� ���
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

				//--- (3.5.7) �� ���� üũó�� 
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
					statemun   = 'E';            // ���� ����  
					data_state = 'E';            // data ����ü ���� 
					if_state   = 'N';            // if   ����ü ���� 
					continue;
				}
			
				//--- (3.5.8) ��� �̿� ������ ó�� 
               	if (data_state == 'Y') { //  ����ü ������ ���κ�  ó�� 
					tmpsb.append(linetrim + "\n");
					sourceline++;                                  
					linetrim = "";	
					statemun = 'E';
				} else {                                            // ��� �̿� ������ ó�� 
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
	 * @Description : �ҽ��� �ּ� �� ���� �����ڸ� �����Ѵ�.
	 * @param : StringBuffer source(��ȯ�� �ҽ�)
	 * @return : StringBuffer source(��ȯ�� �ҽ�)
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
	 * @Description : �ҽ��� �ּ� �� ���� �����ڸ� �����Ѵ�.
	 * @param : StringBuffer source(��ȯ�� �ҽ�)
	 * @return : StringBuffer source(��ȯ�� �ҽ�)
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
	 * @Description : ���۷��̼� ������ ��ĭ ������ �����Ѵ�. <br>
	 *              ���ڿ� �ȿ����� �����Ѵ�.
	 * @param : StringBuffer source(��ȯ�� �ҽ�)
	 * @return : StringBuffer source(��ȯ�� �ҽ�)
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
			// ������ üũ.
			// System.out.println(matcher.toString());
		}
		return source;
	}

	/**
	 * MethodName : MultiSpaceReplace
	 * 
	 * @Description : ��ĭ �̻��� �����̳� ���� ��ĭ���� ��ȯ�Ѵ�.
	 * @param : StringBuffer source(��ȯ�� �ҽ�)
	 * @return : StringBuffer source(��ȯ�� �ҽ�)
	 */
	public StringBuffer MultiSpaceReplace(StringBuffer source) {
		/*
		 * //���Խ� \s �� whiteSpace (����, ��, ����(?)����) String regex =
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
	 * @Description : �ҽ��� ���� �����ڸ� �߰� �Ѵ�.
	 * @param : StringBuffer source(��ȯ�� �ҽ�)
	 * @return : StringBuffer source(��ȯ�� �ҽ�)
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
			// ������ üũ.
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
	 * @Description : �ҽ� ���� ���� ��ȣ ���� üũ �Ѵ�.
	 * @param : String Line(�ҽ� ����)
	 * @return : int(�Ұ�ȣ ��)
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
