package org.jqassistant.contrib.plugin.c.impl.scanner;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.jqassistant.contrib.plugin.c.api.ConditionsLexer;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser;
import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.Condition;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.Declaration;
import org.jqassistant.contrib.plugin.c.api.model.Declaration.DeclarationType;
import org.jqassistant.contrib.plugin.c.api.model.DependsOnDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.EnumConstantDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.EnumDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionArgument;
import org.jqassistant.contrib.plugin.c.api.model.FunctionCall;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.ParameterDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.ReadsDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.StructDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.Type;
import org.jqassistant.contrib.plugin.c.api.model.TypeDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.UnionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.WritesDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
/**
 * Main scanner plugin class that scans C ast files and creates a neo4j graph from it
 * @author Christina Sixtus
 *
 */
@Requires(FileDescriptor.class)
public class CAstFileScannerPlugin extends AbstractScannerPlugin<FileResource, CAstFileDescriptor>{

	private static final Logger logger = LoggerFactory.getLogger(CAstFileScannerPlugin.class);
	private XMLInputFactory inputFactory;
	private XMLStreamReader streamReader;
	private CAstFileDescriptor cAstFileDescriptor;
	private Type currentlyStoredType;
	private ScannerContext context;
	private ArrayDeque<Object> descriptorDeque;
	private String functionName;
	
    @Override
    public void initialize() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        descriptorDeque = new ArrayDeque<>();
    }

	@Override
	public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
		return path.toLowerCase().endsWith(".ast");
	}

	@Override
	public CAstFileDescriptor scan(FileResource item, String path, Scope scope,
			Scanner scanner) throws IOException {
        Source source = new StreamSource(item.createStream());
        context = scanner.getContext();
        this.currentlyStoredType = null;
        this.functionName = null;
        final FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        
        // Add the C label.
        cAstFileDescriptor = context.getStore().addDescriptorType(fileDescriptor, CAstFileDescriptor.class);
        cAstFileDescriptor.setFileName(item.getFile().getName());
        try {
        	streamReader = inputFactory.createXMLStreamReader(source);

            while (streamReader.hasNext()) {
                int eventType = streamReader.next();
                switch (eventType) {
                	case XMLStreamConstants.START_ELEMENT:
                		handleStartElement();
                		break;
                	case XMLStreamConstants.END_ELEMENT:
                		handleEndElement();
                		break;     
                	default:
                		break;
                }
            }
        } catch (Exception e){
        	logger.error(e.getMessage());
        }
		return cAstFileDescriptor;
	}

	/**
	 * Switches over the names of the xml start elements and processes them further
	 * 
	 * @return void
	 */
	private void handleStartElement() {
		this.descriptorDeque.push(streamReader.getLocalName());
		boolean getElementTextCalled = false;
		switch(streamReader.getLocalName()) {
			case TagNameConstants.TRANSLATIONUNIT:
				TranslationUnitDescriptor translationUnitDescriptor = context.getStore().create(TranslationUnitDescriptor.class);
        		cAstFileDescriptor.setTranslationUnit(translationUnitDescriptor);
        		this.descriptorDeque.pop();
        		descriptorDeque.push(translationUnitDescriptor);
        		break;
			case TagNameConstants.ENTRY:
				handleEntryElement();
				break;
			case TagNameConstants.NAME:
				try {
					if(this.descriptorDeque.contains("id")) {
						getElementTextCalled = true;
						handleNameElement(streamReader.getElementText());
					} else if(this.descriptorDeque.contains(TagNameConstants.P)) {
						getElementTextCalled = true;
						this.functionName = streamReader.getElementText();
					} else if(this.descriptorDeque.contains("AssignmentExpression")) {
						getElementTextCalled = true;
						handleAssignment(streamReader.getElementText());
					}
				} catch (XMLStreamException e) {
					logger.error(e.getMessage());
				} finally {
					/*
					 * getElementText() calls streamReader.next() until an END_ELEMENT event is the current element
					 * but I call next() as well so I miss the end element of the featureexpression
					 */
					if(getElementTextCalled) {
						this.descriptorDeque.pop();
					}
				}
				break;
			case TagNameConstants.VALUE:
				try {
					//the element has no attributes and we're currently building a type descriptor
					if(streamReader.getAttributeCount() == 0 && this.currentlyStoredType != null) {
						getElementTextCalled = true;
						String elementText = streamReader.getElementText();
						if(!StringUtils.isEmpty(elementText)) {
							setInitValueOfArray(elementText);
						}
					}
				} catch (XMLStreamException e1) {
					logger.error(e1.getMessage());
				} finally {
					if(getElementTextCalled) {
						this.descriptorDeque.pop();
					}
				}
				break;
			case TagNameConstants.CONDITION:
				this.descriptorDeque.pop();
				this.descriptorDeque.push(new Condition());
				break;
			case TagNameConstants.FEATUREEXPRESSION:
				try {
					String elementText = streamReader.getElementText();
					if(elementText != null && !elementText.equals("1")) {
						Condition condition = (Condition) DequeUtils.getFirstOfType(Condition.class, this.descriptorDeque);
						condition.setConditionText(elementText);
					}
				} catch (XMLStreamException e) {
					logger.info(e.getMessage());
				} finally {
					this.descriptorDeque.pop();
				}
				break;
			case TagNameConstants.ISUNION:
				try {
					storeStructOrUnionType(streamReader.getElementText());
				} catch (XMLStreamException e) {
					logger.error(e.getMessage());
				} finally {
					this.descriptorDeque.pop();
				}
				break;
			case TagNameConstants.TYPENAME:
				if(this.currentlyStoredType != null) {
					try {
						storeType(streamReader.getElementText());
					} catch (XMLStreamException e) {
						logger.error(e.getMessage());
					} finally {
						this.descriptorDeque.pop();
					}
				}
				break;
			case TagNameConstants.S:
				if(streamReader.getAttributeValue(0).equals(AttributeValueConstants.FUNCTIONCALL)) {
					FunctionCall functionCall = new FunctionCall();
					if(this.functionName != null) {
						functionCall.setFunctionName(this.functionName);
					}
					this.descriptorDeque.pop();
					this.descriptorDeque.push(functionCall);
				}
				break;
			case TagNameConstants.LINE:
				FunctionDescriptor function = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
				if(function != null && this.descriptorDeque.contains("specifiers") && function.getFirstLineNumber() == null) {
					try {
						function.setFirstLineNumber(Integer.parseInt(streamReader.getElementText()));
					} catch (NumberFormatException | XMLStreamException e) {
						logger.error(e.getMessage());
					} finally {
						this.descriptorDeque.pop();
					}
				} else if(function != null && this.descriptorDeque.contains("ReturnStatement") && this.descriptorDeque.contains("__2") && function.getLastLineNumber() == null) {
					try {
						function.setLastLineNumber(Integer.parseInt(streamReader.getElementText()));
					} catch (NumberFormatException | XMLStreamException e) {
						logger.error(e.getMessage());
					} finally {
						this.descriptorDeque.pop();
					}
				}
				break;
			case TagNameConstants.EXPR:
				if(streamReader.getAttributeValue(0).equals(AttributeValueConstants.ASSIGNMENTEXPRESSION)) {
					this.descriptorDeque.pop();
					this.descriptorDeque.push("AssignmentExpression");
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Switches over the names of the xml end elements and processes them further
	 * 
	 * @return void
	 * @throws Exception 
	 */
	private void handleEndElement() throws Exception {
		switch(this.streamReader.getLocalName()) {
			//If it is the end of a big element like struct or variable, check conditions for this element and add it to the translation unit.
			case TagNameConstants.ENTRY:
				TranslationUnitDescriptor translationUnit = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, this.descriptorDeque);
				if(descriptorDeque.peekFirst() instanceof FunctionDescriptor) {
					FunctionDescriptor function = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(FunctionDescriptor.class);
					
					if(function.getFirstLineNumber() != null && function.getLastLineNumber() != null) {
						function.setLineCount(function.getLastLineNumber() - function.getFirstLineNumber() + 1);
					}
					boolean sameFunction = false;
					
					//if function already exists, don't add it to the translation unit
					for(CDescriptor cDescriptor : translationUnit.getDeclaredFunctions()) {
						if(cDescriptor instanceof FunctionDescriptor) {
							FunctionDescriptor declaredFunction = (FunctionDescriptor) cDescriptor;
							if(declaredFunction.getFullQualifiedName().equals(function.getFullQualifiedName())) {
								sameFunction = true;
							}
						}
					}
					if(!sameFunction) {
						translationUnit.getDeclaredFunctions().add(function);
					}
				} else if(descriptorDeque.peekFirst() instanceof StructDescriptor) {
					StructDescriptor struct = (StructDescriptor) DequeUtils.getFirstOfType(StructDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(StructDescriptor.class);
					translationUnit.getDeclaredStructs().add(struct);
				} else if(descriptorDeque.peekFirst() instanceof VariableDescriptor) {
					VariableDescriptor variable = (VariableDescriptor) DequeUtils.getFirstOfType(VariableDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(VariableDescriptor.class);
					StructDescriptor struct = (StructDescriptor) DequeUtils.getFirstOfType(StructDescriptor.class, this.descriptorDeque);
					UnionDescriptor union = (UnionDescriptor) DequeUtils.getFirstOfType(UnionDescriptor.class, this.descriptorDeque);
					if(struct != null && union != null) {
						if(DequeUtils.before(StructDescriptor.class, UnionDescriptor.class, this.descriptorDeque)) {
							struct.getDeclaredVariables().add(variable);
						} else {
							union.getDeclaredVariables().add(variable);
						}
					} else if(struct != null) {
						struct.getDeclaredVariables().add(variable);
					} else if(union != null) {
						union.getDeclaredVariables().add(variable);
					} else {
						translationUnit.getDeclaredVariables().add(variable);
					}
				} else if(descriptorDeque.peekFirst() instanceof Declaration) {
					Declaration declaration = (Declaration) DequeUtils.getFirstOfType(Declaration.class, this.descriptorDeque);
					if(declaration.getDeclarationType() == DeclarationType.VARIABLE) {
						this.descriptorDeque = DequeUtils.replaceFirstElementOfType(Declaration.class, VariableDescriptor.class, this.descriptorDeque, this.context);
						VariableDescriptor variable = (VariableDescriptor) DequeUtils.getFirstOfType(VariableDescriptor.class, this.descriptorDeque);
						checkConditionsForElement(VariableDescriptor.class);
						variable.setName(declaration.getName());
						TypeDescriptor type = context.getStore().create(TypeDescriptor.class);
						type.setName(declaration.getType());
						variable.setTypeSpecifiers(type);
						translationUnit.getDeclaredVariables().add(variable);
					} else {
						throw new Exception("Line 249: Declaration with other type than variable not expected. Please check.");
					}
				} else if(descriptorDeque.peekFirst() instanceof UnionDescriptor) {
					UnionDescriptor union = (UnionDescriptor) DequeUtils.getFirstOfType(UnionDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(UnionDescriptor.class);
					translationUnit.getDeclaredUnions().add(union);
				} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
					FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
					ParameterDescriptor parameter = (ParameterDescriptor) this.descriptorDeque.peekFirst();
					
					//if function has already been declared, parameter already exists
					if(!functionDescriptor.getParameters().contains(parameter)) {
						int index = functionDescriptor.getParameters().size();
						parameter.setIndex(index);
						functionDescriptor.getParameters().add(parameter);
					}
				} else if(descriptorDeque.peekFirst() instanceof EnumConstantDescriptor) {
					EnumDescriptor enumDescriptor = (EnumDescriptor) DequeUtils.getFirstOfType(EnumDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(EnumConstantDescriptor.class);
					if(enumDescriptor != null) {
						enumDescriptor.getDeclaredConstants().add((EnumConstantDescriptor) descriptorDeque.peekFirst());
					}
				} else if(descriptorDeque.peekFirst() instanceof EnumDescriptor) {
					EnumDescriptor enumDescriptor = (EnumDescriptor) descriptorDeque.peekFirst();
					checkConditionsForElement(EnumDescriptor.class);
					translationUnit.getDeclaredEnums().add(enumDescriptor);
				}
				break;
			case TagNameConstants.S:
				if(descriptorDeque.peekFirst() instanceof FunctionCall) {
					FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
					if(functionDescriptor != null) {
						FunctionDescriptor resolvedFunction = resolveFunctionCall((FunctionCall) this.descriptorDeque.peekFirst());
						if(resolvedFunction != null) {
							functionDescriptor.getInvokedFunctions().add(resolvedFunction);
						}
					}
				}
				break;
			default:
				break;
		}
		this.descriptorDeque.pop();
	}

	/**
	 * Switches over the attribute values of the entry tag and processes them further 
	 * because this tag is used for many purposes
	 * 
	 * @return void
	 */
	private void handleEntryElement() {
		switch (streamReader.getAttributeValue(0)) {
		case AttributeValueConstants.FUNCTIONDEFINITION:
			FunctionDescriptor function = context.getStore().create(FunctionDescriptor.class);
			this.descriptorDeque.pop();
			descriptorDeque.push(function);
			break;
		case AttributeValueConstants.INTSPECIFIER:
			storeType("int");
			break;
		case AttributeValueConstants.DOUBLESPECIFIER:
			storeType("double");
			break;
		case AttributeValueConstants.VOIDSPECIFIER:
			storeType("void");
			break;
		case AttributeValueConstants.SHORTSPECIFIER:
			storeType("short");
			break;
		case AttributeValueConstants.LONGSPECIFIER:
			storeType("long");
			break;
		case AttributeValueConstants.FLOATSPECIFIER:
			storeType("float");
			break;
		case AttributeValueConstants.SIGNEDSPECIFIER:
			storeType("signed");
			break;
		case AttributeValueConstants.UNSIGNEDSPECIFIER:
			storeType("unsigned");
			break;
		case AttributeValueConstants.CHARSPECIFIER:
			storeType("char");
			break;
		case AttributeValueConstants.PRIMITIVETYPESPECIFIER:
			storeType("");
			break;
		case AttributeValueConstants.POINTER:
			storeType("*");
			break;
		case AttributeValueConstants.ARRAY:
			storeType("[]");
			break;
		case AttributeValueConstants.CONSTSPECIFIER:
			storeType("const");
			break;
		case AttributeValueConstants.VOLATILESPECIFIER:
			storeType("volatile");
			break;
		case AttributeValueConstants.PARAMETERDECLARATION:
			handleParameterDeclaration();
			break;
		case AttributeValueConstants.DECLARATION:
			Declaration declaration = new Declaration();
			declaration.setDeclarationType(DeclarationType.VARIABLE);
			this.descriptorDeque.pop();
			descriptorDeque.push(declaration);
			break;
		case AttributeValueConstants.STRUCTVARIABLEDECLARATION:
			VariableDescriptor structVariable = context.getStore().create(VariableDescriptor.class);
			checkStructOrUnion();
			this.descriptorDeque.pop();
			this.descriptorDeque.push(structVariable);
			break;
		case AttributeValueConstants.ENUMSPECIFIER:
			storeType("enum");
			break;
		case AttributeValueConstants.ENUMERATOR:
			EnumConstantDescriptor enumConstant = context.getStore().create(EnumConstantDescriptor.class);
			createEnum();
			this.descriptorDeque.pop();
			this.descriptorDeque.push(enumConstant);
			break;
		case AttributeValueConstants.ID:
			if(DequeUtils.getFirstOfType(FunctionCall.class, this.descriptorDeque) != null) {
				this.descriptorDeque.pop();
				this.descriptorDeque.push(new FunctionArgument());
			}
			break;
		case AttributeValueConstants.RETURNSTATEMENT:
			this.descriptorDeque.pop();
			this.descriptorDeque.push("ReturnStatement");
			break;
		default:
			break;
		}
	}

	private void createEnum() {
		Declaration variableDeclaration = (Declaration) DequeUtils.getFirstOfType(Declaration.class, this.descriptorDeque);
		if(variableDeclaration != null) {
			this.descriptorDeque = DequeUtils.replaceFirstElementOfType(Declaration.class, EnumDescriptor.class, this.descriptorDeque, this.context);
			EnumDescriptor enumDescriptor = (EnumDescriptor) DequeUtils.getFirstOfType(EnumDescriptor.class, this.descriptorDeque);
			enumDescriptor.setName(variableDeclaration.getName());
		}
		
	}	

	private FunctionDescriptor resolveFunctionCall(FunctionCall functionCall) {
		String fullQualifiedName = this.cAstFileDescriptor.getFileName() + "_" + functionCall.getFunctionName();
		FunctionDescriptor calledFunction = this.context.getStore().find(FunctionDescriptor.class, fullQualifiedName);
		return calledFunction;
	}

	/**
	 * After tag isUnion showed up, check if the current declaration is of type struct or union and store it as type.
	 * @param isUnion tag content that contains true if it is a union and false if it is a struct
	 * @return void
	 */
	private void storeStructOrUnionType(String isUnion) {
		if(isUnion.equals("true")) {
			storeType("union");
		} else {
			storeType("struct");
		}
	}
	
	/**
	 * Create a TypeDescriptor, fetch the name and type from the currently stored type
	 * and set the currently stored type to null
	 * @return TypeDescriptor the generated TypeDescriptor
	 */
	private TypeDescriptor createTypeDescriptor() {
		TypeDescriptor typeDescriptor = context.getStore().create(TypeDescriptor.class);
		typeDescriptor.setName(currentlyStoredType.getName());
		this.currentlyStoredType = null;
		
		return typeDescriptor;
	}
	
	/**
	 * If a struct or union variable showed up, check if the current declaration is still stored as a variable declaration
	 * and convert it to a struct or union declaration if necessary.
	 * @return void
	 */
	private void checkStructOrUnion() {
		Declaration variableDeclaration = (Declaration) DequeUtils.getFirstOfType(Declaration.class, this.descriptorDeque);
		if(variableDeclaration != null) {
			String type = variableDeclaration.getType();
			if(type.equals("struct")) {
				this.descriptorDeque = DequeUtils.replaceFirstElementOfType(Declaration.class, StructDescriptor.class, this.descriptorDeque, this.context);
				StructDescriptor struct = (StructDescriptor) DequeUtils.getFirstOfType(StructDescriptor.class, this.descriptorDeque);
				struct.setName(variableDeclaration.getName());
			} else {
				this.descriptorDeque = DequeUtils.replaceFirstElementOfType(Declaration.class, UnionDescriptor.class, this.descriptorDeque, this.context);
				UnionDescriptor union = (UnionDescriptor) DequeUtils.getFirstOfType(UnionDescriptor.class, this.descriptorDeque);
				union.setName(variableDeclaration.getName());
			}
		}
	}
	
	private void handleParameterDeclaration() {
		FunctionDescriptor currentFunction = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
		if(this.currentlyStoredType != null && currentFunction != null) {
			currentFunction.setReturnType(createTypeDescriptor());
		} else if(currentFunction == null) {
			//A function declaration looks like a variable first, so replace it if you find parameters.
			Declaration variableDeclaration = (Declaration) DequeUtils.getFirstOfType(Declaration.class, this.descriptorDeque);
			if(variableDeclaration != null) {
				String name =  variableDeclaration.getName();
				this.descriptorDeque = DequeUtils.replaceFirstElementOfType(Declaration.class, FunctionDescriptor.class, this.descriptorDeque, this.context);
				FunctionDescriptor function = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
				if(!StringUtils.isEmpty(name)) {
					function.setName(name);
				}
				function.setReturnType(createTypeDescriptor());
				this.currentlyStoredType = null;
			}
		}
		ParameterDescriptor parameterDescriptor = context.getStore().create(ParameterDescriptor.class);
		this.descriptorDeque.pop();
		descriptorDeque.push(parameterDescriptor);
	}
	
	/**
	 * Gets type as param and stores it in the current TypeDescriptor
	 * 
	 * @param type type as String
	 * 
	 * @return void
	 */
	private void storeType(String type) {
		if(!this.descriptorDeque.contains(TagNameConstants.INNERSTATEMENTS)) {
			//If there is already a typedescriptor add the type part to it.
			if(this.currentlyStoredType != null) {
				String firstPart = this.currentlyStoredType.getName();
				//error in ast -> sometimes type occurs twice
				if(!firstPart.contains(type)) {
					if(firstPart != "") {
						this.currentlyStoredType.setName(firstPart + " " + type);
					} else {
						this.currentlyStoredType.setName(type);
					}
					 
				}
			//If there is no type descriptor yet, create a new type descriptor.
			} else {
				this.currentlyStoredType = new Type();
				this.currentlyStoredType.setName(type);
			}
		}	
	}
	
	/**
	 * Gets a name as String and decides whether to add it as parameter name, function name, struct name or variable name
	 * 
	 * @param name
	 * 
	 * @return void
	 */
	private void handleNameElement(String name) {
		Iterator<Object> it = descriptorDeque.iterator();
		while(it.hasNext()) {
			Object currentObject = it.next();
			if(currentObject instanceof ParameterDescriptor) {
				ParameterDescriptor parameterDescriptor = (ParameterDescriptor) currentObject;
				parameterDescriptor.setName(name);
				
				if(this.currentlyStoredType != null) {
					parameterDescriptor.setTypeSpecifiers(createTypeDescriptor());
				}
				break;
			} else if(currentObject instanceof FunctionArgument) {
				FunctionCall functionCall = (FunctionCall) DequeUtils.getFirstOfType(FunctionCall.class, this.descriptorDeque);
				functionCall.getArgumentList().add(name);
				break;
			} else if(currentObject instanceof FunctionDescriptor && !this.descriptorDeque.contains(TagNameConstants.INNERSTATEMENTS)) {
				String fullQualifiedName = this.cAstFileDescriptor.getFileName() + "_" + name;
				FunctionDescriptor function = (FunctionDescriptor) currentObject;
				FunctionDescriptor sameFunction = this.context.getStore().find(FunctionDescriptor.class, fullQualifiedName);
				
				if(sameFunction != null) {
					this.descriptorDeque = DequeUtils.replaceCertainElement(function, sameFunction, this.descriptorDeque);
				} else {
					if(this.currentlyStoredType != null) {
						function.setReturnType(createTypeDescriptor());
					}
					function.setName(name);
					function.setFullQualifiedName(fullQualifiedName);
				}
				
				break;
			} else if(currentObject instanceof EnumConstantDescriptor) {
				((EnumConstantDescriptor) currentObject).setName(name);
				break;
			} else if(currentObject instanceof Declaration) {
				Declaration declaration = (Declaration) currentObject;
				
				if(declaration.getDeclarationType() == DeclarationType.VARIABLE) {
					//If the variable is of type struct or union, the name of the struct or union is stored as name of the variable descriptor.
					if(declaration.getName() != null && declaration.getType() != null) {
						String completeType = declaration.getType() + " " + declaration.getName();
						declaration.setType(completeType);
					}
				}
				declaration.setName(name);
				
				if(this.currentlyStoredType != null) {
					declaration.setType(this.currentlyStoredType.getName());
					this.currentlyStoredType = null;
				}
				break;
			} else if(currentObject instanceof VariableDescriptor) {
				VariableDescriptor variableDescriptor = (VariableDescriptor) currentObject;
				//If the variable is of type struct or union, the name of the struct or union is stored as name of the variable descriptor.
				if(variableDescriptor.getName() != null && variableDescriptor.getTypeSpecifiers() != null) {
					String completeType = variableDescriptor.getTypeSpecifiers().getName() + " " + variableDescriptor.getName();
					variableDescriptor.getTypeSpecifiers().setName(completeType);
				}
				variableDescriptor.setName(name);
				
				if(this.currentlyStoredType != null) {
					variableDescriptor.setTypeSpecifiers(createTypeDescriptor());
					this.currentlyStoredType = null;
				}
				break;
			//If a struct, union or enum already exist and a name comes up, it is the instantiation of a variable
			//with the type of this struct, union or enum
			} else if(currentObject instanceof StructDescriptor) {
				StructDescriptor structDescriptor = (StructDescriptor) currentObject;
				VariableDescriptor variable = context.getStore().create(VariableDescriptor.class);
				variable.setName(name);
				TypeDescriptor type = context.getStore().create(TypeDescriptor.class);
				type.setName("struct " + structDescriptor.getName());
				variable.setTypeSpecifiers(type);
				TranslationUnitDescriptor translationUnit = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, this.descriptorDeque);
				translationUnit.getDeclaredVariables().add(variable);
				break;
			} else if(currentObject instanceof UnionDescriptor) {
				UnionDescriptor union = (UnionDescriptor) currentObject;
				VariableDescriptor variable = context.getStore().create(VariableDescriptor.class);
				variable.setName(name);
				TypeDescriptor type = context.getStore().create(TypeDescriptor.class);
				type.setName("union " + union.getName());
				variable.setTypeSpecifiers(type);
				TranslationUnitDescriptor translationUnit = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, this.descriptorDeque);
				translationUnit.getDeclaredVariables().add(variable);
				break;
			} else if(currentObject instanceof EnumDescriptor) {
				EnumDescriptor enumDescriptor = (EnumDescriptor) currentObject;
				VariableDescriptor variable = context.getStore().create(VariableDescriptor.class);
				variable.setName(name);
				TypeDescriptor type = context.getStore().create(TypeDescriptor.class);
				type.setName("enum " + enumDescriptor.getName());
				variable.setTypeSpecifiers(type);
				TranslationUnitDescriptor translationUnit = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, this.descriptorDeque);
				translationUnit.getDeclaredVariables().add(variable);
				break;
			}
		}
	}

	/**
	 * If an assignment expression comes up, check if there is a global variable involved
	 * and add a READ or WRITE relationship to the current function
	 * @param name String name of the current element
	 */
	private void handleAssignment(String name) {
		FunctionDescriptor currentFunction = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
		VariableDescriptor calledVariable = null;
		if(currentFunction != null) {
			TranslationUnitDescriptor translationUnit = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, this.descriptorDeque);
			for(CDescriptor cDescriptor : translationUnit.getDeclaredVariables()) {
				//returns all elements that have the relation DECLARES with the translation unit
				if(cDescriptor instanceof VariableDescriptor) {
					VariableDescriptor variable = (VariableDescriptor) cDescriptor;
					if(variable.getName().equals(name)) {
						calledVariable = variable;
						break;
					}
				}
			}
			if(calledVariable != null) {
				//value is assigned to the variable
				if(this.descriptorDeque.contains("target")) {
					context.getStore().create(currentFunction, WritesDescriptor.class, calledVariable);
				//variable is part of the assignment value
				} else if(this.descriptorDeque.contains("source")) {
					context.getStore().create(currentFunction, ReadsDescriptor.class, calledVariable);
				}
			}
			
		}	
	}

	/**
	 * Checks if there is a condition defined for this element and if that's the case,
	 * parses the condition and stores it as condition for the object.
	 * @param typeOfElement The class of the element for which the condition should be checked.
	 * @return void
	 */
	private <A extends DependsOnDescriptor> void checkConditionsForElement(Class<A> typeOfElement) {
		@SuppressWarnings("unchecked")
		A descriptor = (A) DequeUtils.getFirstOfType(typeOfElement, this.descriptorDeque);
		List<Condition> conditionsForElement = DequeUtils.getElementsUnder(typeOfElement, Condition.class, this.descriptorDeque);
		for(Condition condition : conditionsForElement) {
			if(!StringUtils.isEmpty(condition.getConditionText())) {
				ConditionDescriptor conditionDescriptor = parseCondition(condition.getConditionText());
				if(descriptor.getCondition() != null && descriptor.getCondition().equals(conditionDescriptor)) {
					this.context.getStore().delete(conditionDescriptor);
				} else {
					descriptor.setCondition(conditionDescriptor);
				}
			}
		}
	}
	
	/**
	 * Sets the size of the array that is currently processed
	 * @param initValue size of the array 
	 * 
	 * @return void
	 */
	private void setInitValueOfArray(String initValue) {
		if(this.currentlyStoredType.getName().contains("[]")) {
			
			String[] parts = this.currentlyStoredType.getName().split("\\[");
			String fullType = "";
			//if there is no part after the array initializer
			if(parts[1].equals("]")){
				fullType = parts[0] + "[" + initValue + "]";
			//if there is a part after the array initializer put this part at the end of it
			} else {
				String[] secondPart = parts[1].split("\\]");
				fullType = parts[0] + "[" + initValue + "]" + secondPart[1];
			}
			this.currentlyStoredType.setName(fullType);
		}
	}
	
	/**
	 * Parses condition with antlr parser and returns subgraph with condition nodes
	 * @param elementText String to parse
	 * @return ConditionDescriptor descriptor containing subgraph with condition nodes
	 */
	private ConditionDescriptor parseCondition(String elementText) {
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(elementText));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.context.getStore());
		walker.walk(listener, tree);
		return listener.getResultCondition();
	}
}