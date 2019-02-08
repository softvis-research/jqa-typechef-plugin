package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Variable")
public interface VariableDescriptor extends CDescriptor, NamedDescriptor{

	/**
	 * Returns the type specifiers and qualifiers of this variable
	 * @return the type specifiers and qualifiers
	 */
	@Relation("OF_TYPE")
	List<TypeDescriptor> getTypeSpecifiers();
	void setTypeSpecifiers(List<TypeDescriptor> typeSpecifiers);
	
	/**
	 * Stores presence conditions for this variable
	 * @return a presence condition, can be a SingleConditionDescriptor, a NegationDescriptor,
	 * an AndDescriptor or an OrDescriptor
	 */
	@Relation("DEPENDS_ON")
	ConditionDescriptor getCondition();
	void setCondition(ConditionDescriptor conditionDescriptor);
}
