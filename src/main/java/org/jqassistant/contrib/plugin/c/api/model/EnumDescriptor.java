package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Enum")
public interface EnumDescriptor extends CDescriptor, NamedDescriptor, DependsOnDescriptor{

	/**
	 * Returns the constants that were declared in this enum
	 * @return the declared constants
	 */
	@Relation("DECLARES")
	List<EnumConstantDescriptor> getDeclaredConstants();
	void setDeclaredConstants(List<EnumConstantDescriptor> declaredConstants);
}
