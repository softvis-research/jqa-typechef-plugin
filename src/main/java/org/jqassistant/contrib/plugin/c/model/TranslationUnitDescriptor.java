package org.jqassistant.contrib.plugin.c.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("TranslationUnit")
public interface TranslationUnitDescriptor extends CDescriptor{
	
	@Property("name")
	String getName();
	void setName(String name);
}
