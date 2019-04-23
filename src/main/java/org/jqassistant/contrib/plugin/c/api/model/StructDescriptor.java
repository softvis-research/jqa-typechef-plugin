package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Struct")
public interface StructDescriptor extends CDescriptor, NamedDescriptor, DependsOnDescriptor, SourceFileDescriptor{

	/**
	 * Returns the variables that were declared in this struct
	 * @return the declared variables
	 */
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
}
