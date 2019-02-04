package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("SingleCondition")
public interface SingleConditionDescriptor extends ConditionDescriptor{

	@Property("MacroName")
	String getMacroName();
	void setMacroName(String macroName);
	
}
