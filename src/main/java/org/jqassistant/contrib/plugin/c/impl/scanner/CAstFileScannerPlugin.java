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
import org.jqassistant.contrib.plugin.c.api.model.Condition;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.DependsOnDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.ParameterDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.StructDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TypeDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.UnionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
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
	private TypeDescriptor currentlyStoredType;
	private ScannerContext context;
	private ArrayDeque<Object> descriptorDeque;
	
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
					checkStructOrUnion(streamReader.getElementText());
				} catch (XMLStreamException e) {
					logger.error(e.getMessage());
				} finally {
					this.descriptorDeque.pop();
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
	 */
	private void handleEndElement() {
		switch(this.streamReader.getLocalName()) {
			//It if is the end of a big element like struct or variable, check conditions for this element and add it to the translation unit.
			case TagNameConstants.ENTRY:
				TranslationUnitDescriptor translationUnit = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, this.descriptorDeque);
				if(descriptorDeque.peekFirst() instanceof FunctionDescriptor) {
					FunctionDescriptor function = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(FunctionDescriptor.class);
					translationUnit.getDeclaredFunctions().add(function);
				} else if(descriptorDeque.peekFirst() instanceof StructDescriptor) {
					StructDescriptor struct = (StructDescriptor) DequeUtils.getFirstOfType(StructDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(StructDescriptor.class);
					translationUnit.getDeclaredStructs().add(struct);
				} else if(descriptorDeque.peekFirst() instanceof VariableDescriptor) {
					VariableDescriptor variable = (VariableDescriptor) DequeUtils.getFirstOfType(VariableDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(VariableDescriptor.class);
					StructDescriptor struct = (StructDescriptor) DequeUtils.getFirstOfType(StructDescriptor.class, this.descriptorDeque);
					if(struct != null) {
						struct.getDeclaredVariables().add(variable);
					} else {
						translationUnit.getDeclaredVariables().add(variable);
					}
				} else if(descriptorDeque.peekFirst() instanceof UnionDescriptor) {
					UnionDescriptor union = (UnionDescriptor) DequeUtils.getFirstOfType(UnionDescriptor.class, this.descriptorDeque);
					checkConditionsForElement(UnionDescriptor.class);
					translationUnit.getDeclaredUnions().add(union);
				} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
					FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
					int index = functionDescriptor.getParameters().size();
					ParameterDescriptor parameter = (ParameterDescriptor) DequeUtils.getFirstOfType(ParameterDescriptor.class, this.descriptorDeque);
					parameter.setIndex(index);
					functionDescriptor.getParameters().add(parameter);
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
			VariableDescriptor variableDescriptor = context.getStore().create(VariableDescriptor.class);
			this.descriptorDeque.pop();
			descriptorDeque.push(variableDescriptor);
			break;
		case AttributeValueConstants.STRUCTORUNIONSPECIFIER:
			// At first a declaration is translated into a VariableDescriptor but after finding out it is a struct, replace it.
			this.descriptorDeque = DequeUtils.replaceFirstElementOfType(VariableDescriptor.class, StructDescriptor.class, this.descriptorDeque, this.context);
			break;
		case AttributeValueConstants.STRUCTVARIABLEDECLARATION:
			VariableDescriptor structVariable = context.getStore().create(VariableDescriptor.class);
			this.descriptorDeque.pop();
			this.descriptorDeque.push(structVariable);
			break;
		default:
			break;
		}
	}
	 
	private void checkStructOrUnion(String isUnion) {
		if(isUnion.equals("true")) {
			UnionDescriptor union = (UnionDescriptor) DequeUtils.getFirstOfType(UnionDescriptor.class, this.descriptorDeque);
			if(union == null) {
				// Struct and Unions share the same declaration element, so replace a struct by a union if you find out it is in fact a union.
				this.descriptorDeque = DequeUtils.replaceFirstElementOfType(StructDescriptor.class, UnionDescriptor.class, this.descriptorDeque, this.context);
			}
		}
	}
	
	private void handleParameterDeclaration() {
		FunctionDescriptor currentFunction = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
		if(this.currentlyStoredType != null && currentFunction != null) {
			currentFunction.getReturnType().add(currentlyStoredType);
			this.currentlyStoredType = null;
		} else if(currentFunction == null) {
			//A function declaration looks like a variable first, so replace it if you find parameters.
			VariableDescriptor variable = (VariableDescriptor) DequeUtils.getFirstOfType(VariableDescriptor.class, this.descriptorDeque);
			if(variable != null) {
				String name =  variable.getName();
				this.descriptorDeque = DequeUtils.replaceFirstElementOfType(VariableDescriptor.class, FunctionDescriptor.class, this.descriptorDeque, this.context);
				FunctionDescriptor function = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, this.descriptorDeque);
				if(!StringUtils.isEmpty(name)) {
					function.setName(name);
				}
				function.getReturnType().add(this.currentlyStoredType);
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
					this.currentlyStoredType.setName(firstPart + " " + type); 
				}
			//If there is no type descriptor yet, create a new type descriptor.
			} else {
				this.currentlyStoredType = context.getStore().create(TypeDescriptor.class);
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
					parameterDescriptor.getTypeSpecifiers().add(this.currentlyStoredType);
					this.currentlyStoredType = null;
				}
				break;
			} else if(currentObject instanceof FunctionDescriptor && !this.descriptorDeque.contains(TagNameConstants.INNERSTATEMENTS)) {
				if(this.currentlyStoredType != null) {
					((FunctionDescriptor) currentObject).getReturnType().add(this.currentlyStoredType);
					this.currentlyStoredType = null;
				}
				((FunctionDescriptor) currentObject).setName(name);
				break;
			} else if(currentObject instanceof VariableDescriptor) {
				VariableDescriptor variableDescriptor = (VariableDescriptor) currentObject;
				variableDescriptor.setName(name);
				
				if(this.currentlyStoredType != null) {
					variableDescriptor.getTypeSpecifiers().add(this.currentlyStoredType);
					this.currentlyStoredType = null;
				}
				break;
			} else if(currentObject instanceof StructDescriptor) {
				StructDescriptor structDescriptor = (StructDescriptor) currentObject;
				structDescriptor.setName(name);
				break;
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
				descriptor.setCondition(conditionDescriptor);
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