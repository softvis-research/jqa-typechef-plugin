package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label(value = "Union", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface UnionDescriptor extends CDescriptor, NamedDescriptor, DependsOnDescriptor, SourceFileDescriptor, FullQualifiedNameDescriptor{

	/**
	 * Returns the declared variables of this union
	 * @return the declared variables
	 */
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
	
	/**
	 * Returns the declared structs of the translation unit
	 * @return the declared structs
	 */
	@Relation("DECLARES")
	List<StructDescriptor> getDeclaredStructs();
	void setDeclaredStructs(List<StructDescriptor> declaredStructs);
	
	/**
	 * Returns the declared unions of the translation unit
	 * @return the declared unions
	 */
	@Relation("DECLARES")
	List<UnionDescriptor> getDeclaredUnions();
	void setDeclaredUnions(List<UnionDescriptor> declaredUnions);
}
