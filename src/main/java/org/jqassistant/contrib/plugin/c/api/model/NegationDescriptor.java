package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Not")
public interface NegationDescriptor extends ConditionDescriptor{
	
	@Relation("NEGATES")
	ConditionDescriptor getCondition();
	void setCondition(ConditionDescriptor condition);
}
