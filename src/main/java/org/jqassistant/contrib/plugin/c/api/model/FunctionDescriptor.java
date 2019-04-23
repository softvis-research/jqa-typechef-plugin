package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label(value = "Function", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface FunctionDescriptor extends CDescriptor, NamedDescriptor, DependsOnDescriptor, FullQualifiedNameDescriptor{
	
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
	

    /**
     * Return the first line number of the function.
     * @return The first line number of the function.
     */
    Integer getFirstLineNumber();
    void setFirstLineNumber(Integer firstLineNumber);

    /**
     * Return the last line number of the function.
     * @return The last line number of the function.
     */
    Integer getLastLineNumber();
    void setLastLineNumber(Integer lastLineNumber);

    /**
     * Return the total number of lines of the function.
     * @return The number of lines of the function.
     */
    int getLineCount();
    void setLineCount(int lineCount);
    
    /**
     * Return all read accesses to fields this method performs.
     *
     * @return All read accesses to fields this method performs.
     */
    List<ReadsDescriptor> getReads();

    /**
     * Return all write accesses to fields this method performs.
     *
     * @return All write accesses to fields this method performs.
     */
    List<WritesDescriptor> getWrites();
}
