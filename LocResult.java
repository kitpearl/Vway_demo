package com.cubegen.fp.loc;

public class LocResult {

	int newLineCount=0;
	int oldLineCount=0;
	int addLines=0;
	int chgLines=0;
	int delLines=0;
	String error="";
	
	public int getNewLineCount() {
  	return newLineCount;
  }
	public void setNewLineCount(int newLineCount) {
  	this.newLineCount = newLineCount;
  }
	public int getOldLineCount() {
  	return oldLineCount;
  }
	public void setOldLineCount(int oldLineCount) {
  	this.oldLineCount = oldLineCount;
  }
	public int getAddLines() {
  	return addLines;
  }
	public void setAddLines(int addLines) {
  	this.addLines = addLines;
  }
	public int getChgLines() {
  	return chgLines;
  }
	public void setChgLines(int chgLines) {
  	this.chgLines = chgLines;
  }
	public int getDelLines() {
  	return delLines;
  }
	public void setDelLines(int delLines) {
  	this.delLines = delLines;
  }
	public String getError() {
  	return error;
  }
	public void setError(String error) {
  	this.error = error;
  }
	
	public String toString() {
	  return "newLine:"+newLineCount+", oldLine:"+oldLineCount+ ", ADD:"+addLines+ ", CHG:"+chgLines+ ", DEL:"+delLines;
  }
	
}
