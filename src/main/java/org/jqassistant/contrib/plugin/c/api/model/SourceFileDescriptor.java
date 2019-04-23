package org.jqassistant.contrib.plugin.c.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

public interface SourceFileDescriptor {

    @Property("fileName")
    String getFileName();

    void setFileName(String fileName);
}
