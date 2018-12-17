package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface CAstFileDescriptor extends XmlFileDescriptor, CDescriptor{
	
	@Relation("CONTAINS")
	TranslationUnitDescriptor getTranslationUnit();
	void setTranslationUnit(TranslationUnitDescriptor translationUnit);

}
