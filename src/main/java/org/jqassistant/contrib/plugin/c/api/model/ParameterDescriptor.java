package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Parameter")
public interface ParameterDescriptor extends CDescriptor, NamedDescriptor{

    @Property("index")
    int getIndex();
    void setIndex(int index);
	/**
	 * Returns the type specifiers and qualifiers of the parameter
	 * @return the type specifiers and qualifiers
	 */
	@Relation("OF_TYPE")
	List<TypeDescriptor> getTypeSpecifiers();
	void setTypeSpecifiers(List<TypeDescriptor> typeSpecifiers);
}
