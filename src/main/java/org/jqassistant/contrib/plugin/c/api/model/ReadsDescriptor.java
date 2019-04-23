package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Defines a READs relation between a function and a variable.
 */
@Relation("READS")
public interface ReadsDescriptor extends Descriptor{

    @Outgoing
    FunctionDescriptor getFunction();

    @Incoming
    VariableDescriptor getVariable();
}