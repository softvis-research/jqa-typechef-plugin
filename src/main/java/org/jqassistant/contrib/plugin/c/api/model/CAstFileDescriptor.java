package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface CAstFileDescriptor extends CDescriptor, FileDescriptor, NamedDescriptor{
	
	/**
	 * Returns the contained translation units of the ast file
	 * @return the contained taranslation units
	 */
	@Relation("CONTAINS")
	TranslationUnitDescriptor getTranslationUnit();
	void setTranslationUnit(TranslationUnitDescriptor translationUnit);

}
