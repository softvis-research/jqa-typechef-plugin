package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Function")
public interface FunctionDescriptor extends CDescriptor, NamedDescriptor{
	
	/**
	 * Returns the parameter of the function
	 * @return the parameter of the function
	 */
	@Relation("HAS")
	List<ParameterDescriptor> getParameters();
	void setParameters(List<ParameterDescriptor> parameters);
	
	/**
	 * Returns a list with the parts of the return type, e.g. unsigned and int
	 * @return parts of the return type
	 */
	@Relation("RETURNS")
	List<TypeDescriptor> getReturnType();
	void setReturnType(List<TypeDescriptor> returnType);
	
	/**
	 * Returns the variables that were declared in this function
	 * @return the declared variables of this function
	 */
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
	
	/**
	 * Returns the functions that were invoked in this function
	 * @return the invoke functions
	 */
	@Relation("INVOKES")
	List<FunctionDescriptor> getInvokedFunctions();
	void setInvokedFunctions(List<FunctionDescriptor> invokedFunctions);
	
}
