package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface DependsOnDescriptor{

	/**
	 * Stores presence conditions for this element
	 * @return a presence condition, can be a SingleConditionDescriptor, a NegationDescriptor,
	 * an AndDescriptor or an OrDescriptor
	 */
	@Relation("DEPENDS_ON")
	ConditionDescriptor getCondition();
	void setCondition(ConditionDescriptor conditionDescriptor);
}
