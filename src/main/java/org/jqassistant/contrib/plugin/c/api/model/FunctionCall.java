package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

public class FunctionCall {

	private String functionName;
	private List<String> argumentList;
	
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	public List<String> getArgumentList() {
		return argumentList;
	}
	public void setArgumentList(List<String> argumentList) {
		this.argumentList = argumentList;
	}
}
