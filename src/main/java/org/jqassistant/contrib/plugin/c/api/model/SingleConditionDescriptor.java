package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label(value = "SingleCondition", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface SingleConditionDescriptor extends ConditionDescriptor, FullQualifiedNameDescriptor{

	@Property("MacroName")
	String getMacroName();
	void setMacroName(String macroName);
	
}
