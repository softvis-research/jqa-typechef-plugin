package org.jqassistant.contrib.plugin.c.api.model;

public class Declaration {

	private String name;
	private String type;
	private DeclarationType declarationType;
	private String fileName;
	
	public enum DeclarationType{
		VARIABLE,
		FUNCTION,
		STRUCT,
		UNION,
		ENUM,
		TYPEDEF
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(DeclarationType declarationType) {
		this.declarationType = declarationType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
