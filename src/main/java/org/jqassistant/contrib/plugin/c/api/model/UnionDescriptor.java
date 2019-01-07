package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Union")
public interface UnionDescriptor extends CDescriptor, NamedDescriptor, TypedDescriptor{

	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	
}
