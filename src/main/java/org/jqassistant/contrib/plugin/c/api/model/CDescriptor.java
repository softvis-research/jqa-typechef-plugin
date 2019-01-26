package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Descriptor that labels elements of C source code
 * @author Christina Sixtus
 *
 */
@Label("C")
public interface CDescriptor extends Descriptor{
}
