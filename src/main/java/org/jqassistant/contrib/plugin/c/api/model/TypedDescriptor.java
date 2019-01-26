package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor that adds a relation "OF_TYPE" to a node that connects to a type descriptor
 * @author Christina Sixtus
 *
 */
public interface TypedDescriptor {

	/**
	 * Returns the type descriptor that the relation connects the node to
	 * @return the type descriptor
	 */
	@Relation("OF_TYPE")
    TypeDescriptor getType();
    void setType(TypeDescriptor type);
}
