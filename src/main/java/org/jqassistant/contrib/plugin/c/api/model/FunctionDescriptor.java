package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Function")
public interface FunctionDescriptor extends CDescriptor, NamedDescriptor{
	
	/*@Relation.Incoming
	TranslationUnitDescriptor getDeclaringTranslationUnit();*/
	
	@Relation("HAS")
	List<ParameterDescriptor> getParameters();
	void setParameters(List<ParameterDescriptor> parameters);
	
	@Relation("RETURNS")
	List<TypeDescriptor> getReturnType();
	void setReturnType(List<TypeDescriptor> returnType);
	
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
	
	@Relation("INVOKES")
	List<FunctionDescriptor> getInvokedFunctions();
	void setInvokedFunctions(List<FunctionDescriptor> invokedFunctions);
	
}
