package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("And")
public interface AndDescriptor extends ConditionDescriptor{

	@Relation("CONNECTS")
	List<ConditionDescriptor> getConnectedConditions();
	void setConnectedConditions(List<ConditionDescriptor> connectedConditions);
}
