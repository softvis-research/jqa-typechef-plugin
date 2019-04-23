package org.jqassistant.contrib.plugin.c.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for a translation unit of a c program
 * Contains the declared elements like functions and global variables
 * @author Christina Sixtus
 *
 */
@Label("TranslationUnit")
public interface TranslationUnitDescriptor extends CDescriptor, NamedDescriptor, SourceFileDescriptor{

	/**
	 * Returns the declared functions of the translation unit
	 * @return the declared functions
	 */
	@Relation("DECLARES")
	List<FunctionDescriptor> getDeclaredFunctions();
	void setDeclaredFunctions(List<FunctionDescriptor> declaredFunctions);
	
	/**
	 * Returns the declared variables of the translation unit
	 * @return the declared variables
	 */
	@Relation("DECLARES")
	List<VariableDescriptor> getDeclaredVariables();
	void setDeclaredVariables(List<VariableDescriptor> declaredVariables);
	
	/**
	 * Returns the declared structs of the translation unit
	 * @return the declared structs
	 */
	@Relation("DECLARES")
	List<StructDescriptor> getDeclaredStructs();
	void setDeclaredStructs(List<StructDescriptor> declaredStructs);
	
	/**
	 * Returns the declared unions of the translation unit
	 * @return the declared unions
	 */
	@Relation("DECLARES")
	List<UnionDescriptor> getDeclaredUnions();
	void setDeclaredUnions(List<UnionDescriptor> declaredUnions);
	
	/**
	 * Returns the declared enums of the translation unit
	 * @return the declared enums
	 */
	@Relation("DECLARES")
	List<EnumDescriptor> getDeclaredEnums();
	void setDeclaredEnums(List<EnumDescriptor> declaredEnums);
}
