package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("EnumConstant")
public interface EnumConstantDescriptor extends CDescriptor, NamedDescriptor, DependsOnDescriptor, SourceFileDescriptor{
}
