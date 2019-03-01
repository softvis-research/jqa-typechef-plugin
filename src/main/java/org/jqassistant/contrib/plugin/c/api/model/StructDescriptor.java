package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Struct")
public interface StructDescriptor extends CDescriptor, NamedDescriptor, TypedDescriptor{

	/**
	 * Returns the variables that were declared in this struct
	 * @return the declared variables
	 */
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
	
	/**
	 * Stores presence conditions for this struct
	 * @return a presence condition, can be a SingleConditionDescriptor, a NegationDescriptor,
	 * an AndDescriptor or an OrDescriptor
	 */
	@Relation("DEPENDS_ON")
	ConditionDescriptor getCondition();
	void setCondition(ConditionDescriptor conditionDescriptor);
}
