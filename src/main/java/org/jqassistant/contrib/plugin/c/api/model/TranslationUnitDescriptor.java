package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("TranslationUnit")
public interface TranslationUnitDescriptor extends CDescriptor, NamedDescriptor{

	@Relation("DECLARES")
	List<FunctionDescriptor> getDeclaredFunctions();
	void setDeclaredFunctions(List<FunctionDescriptor> declaredFunctions);
	
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
	
	@Relation("DECLARES")
	List<StructDescriptor> getDeclaredStructs();
	void setDeclaredStructs(List<StructDescriptor> declaredStructs);
	
	@Relation("DECLARES")
	List<UnionDescriptor> getDeclaredUnions();
	void setDeclaredUnions(List<UnionDescriptor> declaredUnions);
	
	@Relation("DECLARES")
	List<EnumDescriptor> getDeclaredEnums();
	void setDeclaredEnums(List<EnumDescriptor> declaredEnums);
}
