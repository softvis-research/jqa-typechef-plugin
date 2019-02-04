package org.jqassistant.contrib.plugin.c.impl.scanner;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.Condition;
import org.jqassistant.contrib.plugin.c.api.model.Declarator;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.ID;
import org.jqassistant.contrib.plugin.c.api.model.InnerStatements;
import org.jqassistant.contrib.plugin.c.api.model.ParameterDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.Specifier;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TypeDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
/**
 * Main scanner plugin class that scans C ast files and create a neo4j graph from it
 * @author Christina Sixtus
 *
 */
@Requires(FileDescriptor.class)
public class CAstFileScannerPlugin extends AbstractScannerPlugin<FileResource, CAstFileDescriptor>{

	private static final Logger logger = LoggerFactory.getLogger(CAstFileScannerPlugin.class);
	private XMLInputFactory inputFactory;
	private XMLStreamReader streamReader;
	private CAstFileDescriptor cAstFileDescriptor;
	private TranslationUnitDescriptor translationUnitDescriptor;
	private ScannerContext context;
	ArrayDeque<Object> descriptorDeque;
	
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
        final FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        
        // Add the C label.
        cAstFileDescriptor = context.getStore().addDescriptorType(fileDescriptor, CAstFileDescriptor.class);
        cAstFileDescriptor.setFileName(item.getFile().getName());
        try {
        	streamReader = inputFactory.createXMLStreamReader(source);

            while (streamReader.hasNext()) {
                int eventType = streamReader.getEventType();
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
                streamReader.next();
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
		switch(streamReader.getLocalName()) {
			case TagNameConstants.TRANSLATIONUNIT:
				translationUnitDescriptor = context.getStore().create(TranslationUnitDescriptor.class);
        		cAstFileDescriptor.setTranslationUnit(translationUnitDescriptor);
        		descriptorDeque.push(translationUnitDescriptor);
        		break;
			case TagNameConstants.ENTRY:
				handleEntryElement();
				break;
			case TagNameConstants.SPECIFIERS:
				if(descriptorDeque.peekFirst() instanceof FunctionDescriptor){
					Specifier specifier = new Specifier();
					descriptorDeque.push(specifier);
				}
				break;
			case TagNameConstants.DECLARATOR:
				Declarator declarator = new Declarator();
				descriptorDeque.push(declarator);
				break;
			case TagNameConstants.ID:
				ID id = new ID();
				descriptorDeque.push(id);
				break;
			case TagNameConstants.NAME:
				try {
					if(descriptorDeque.getFirst() instanceof ID) {
						handleNameElement(streamReader.getElementText());
					}
				} catch (XMLStreamException e) {
					logger.error(e.getMessage());
				}
				break;
			case TagNameConstants.INNERSTATEMENTS:
				InnerStatements innerStatements = new InnerStatements();
				descriptorDeque.push(innerStatements);
				break;
			case TagNameConstants.VALUE:
				try {
					//the element has no attributes and we're currently building a type descriptor
					if(streamReader.getAttributeCount() == 0 && DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque) != null) {
						String elementText = streamReader.getElementText();
						if(!StringUtils.isEmpty(elementText) && DequeUtils.getFirstOfType(Declarator.class, this.descriptorDeque) != null) {
							setInitValueOfArray(elementText);
						}
					}
				} catch (XMLStreamException e1) {
					//ignore if the element has no text
				}
				break;
			case TagNameConstants.CONDITION:
				descriptorDeque.push(new Condition());
				break;
			case TagNameConstants.FEATUREEXPRESSION:
				try {
					String elementText = streamReader.getElementText();
					if(elementText != null && !elementText.equals("1")) {
						parseCondition(elementText);
					}
				} catch (XMLStreamException e) {
					logger.info(e.getMessage());
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
		switch(streamReader.getLocalName()) {
			case TagNameConstants.ENTRY:
				if(descriptorDeque.peekFirst() instanceof FunctionDescriptor) {
					descriptorDeque.removeFirst();
				}
				break;
			case TagNameConstants.SPECIFIERS:
				DequeUtils.removeFirstOccurrenceOfType(Specifier.class, this.descriptorDeque);
				break;
			case TagNameConstants.DECLARATOR:
				DequeUtils.removeFirstOccurrenceOfType(Declarator.class, this.descriptorDeque);
				break;
			case TagNameConstants.ID:
				DequeUtils.removeFirstOccurrenceOfType(ID.class, this.descriptorDeque);
				break;
			case TagNameConstants.INNERSTATEMENTS:
				DequeUtils.removeFirstOccurrenceOfType(InnerStatements.class, this.descriptorDeque);
				DequeUtils.removeFirstOccurrenceOfType(FunctionDescriptor.class, this.descriptorDeque);
				break;
			case TagNameConstants.CONDITION:
				DequeUtils.removeFirstOccurrenceOfType(Condition.class, this.descriptorDeque);
				break;
			default:
				break;
		}
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
			createFunctionDefinition();
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
			TypeDescriptor currentlyStoredType = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
			FunctionDescriptor currentFunction = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
			if(currentlyStoredType != null) {
				currentFunction.getReturnType().add(currentlyStoredType);
				descriptorDeque.remove(currentlyStoredType);
			}
			ParameterDescriptor parameterDescriptor = context.getStore().create(ParameterDescriptor.class);
			descriptorDeque.push(parameterDescriptor);
			break;
		case AttributeValueConstants.VARIABLEDECLARATION:
			VariableDescriptor variableDescriptor = context.getStore().create(VariableDescriptor.class);
			descriptorDeque.push(variableDescriptor);
			break;
		default:
			break;
		}
	}

	/**
	 * Creates a function node and adds it to the translation unit
	 * 
	 * @return void
	 */
	private void createFunctionDefinition() {
		FunctionDescriptor functionDescriptor = context.getStore().create(FunctionDescriptor.class);
		this.translationUnitDescriptor.getDeclaredFunctions().add(functionDescriptor);
		descriptorDeque.push(functionDescriptor);
	}
	
	/**
	 * Gets type as param and decides whether to add it as return type, parameter type or variable type
	 * 
	 * @param type type as String
	 * 
	 * @return void
	 */
	private void storeType(String type) {
		if(DequeUtils.getFirstOfType(InnerStatements.class, descriptorDeque) == null) {
			TypeDescriptor typeDescriptor = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
			//if there is already a typedescriptor add type part to it
			if(typeDescriptor != null) {
				String firstPart = typeDescriptor.getName();
				typeDescriptor.setName(firstPart + " " + type); 
			//if there was no type part yet create new type descriptor
			} else {
				TypeDescriptor newTypeDescriptor = context.getStore().create(TypeDescriptor.class);
				newTypeDescriptor.setName(type);
				descriptorDeque.push(newTypeDescriptor);
			}
		}	
	}
	
	/**
	 * Gets a name as String and decides whether to add it as parameter name, function name or variable name
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
				//if the name tag belongs to a parameter, build that parameter,
				//add it to a function and remove it from the deque
				ParameterDescriptor parameterDescriptor = (ParameterDescriptor) currentObject;
				parameterDescriptor.setName(name);
				TypeDescriptor typeDescriptor = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
				if(typeDescriptor != null) {
					parameterDescriptor.getTypeSpecifiers().add(typeDescriptor);
					descriptorDeque.remove(typeDescriptor);
				}
				FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
				int index = functionDescriptor.getParameters().size();
				parameterDescriptor.setIndex(index);
				functionDescriptor.getParameters().add(parameterDescriptor);
				descriptorDeque.remove(currentObject);
				break;
			} else if(currentObject instanceof FunctionDescriptor && !DequeUtils.before(InnerStatements.class, FunctionDescriptor.class, descriptorDeque)) {
				//if the name belongs to a function, add the return type to that function and remove
				//it from the deque
				TypeDescriptor typeDescriptor = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
				if(typeDescriptor != null) {
					((FunctionDescriptor) currentObject).getReturnType().add(typeDescriptor);
					descriptorDeque.remove(typeDescriptor);
				}
				//if we're already in the inner statements of the function the function name is already set
				((FunctionDescriptor) currentObject).setName(name);
				break;
			} else if(currentObject instanceof VariableDescriptor) {
				//if the name belongs to a variable, add it to the variable
				//and remove the variable from the deque
				((VariableDescriptor) currentObject).setName(name);
				TypeDescriptor typeDescriptor = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
				if(typeDescriptor != null) {
					((VariableDescriptor) currentObject).getTypeSpecifiers().add(typeDescriptor);
					descriptorDeque.remove(typeDescriptor);
				}
				TranslationUnitDescriptor translationUnitDescriptor = (TranslationUnitDescriptor) DequeUtils.getFirstOfType(TranslationUnitDescriptor.class, descriptorDeque);
				translationUnitDescriptor.getDeclaredVariables().add((VariableDescriptor) currentObject);
				descriptorDeque.remove(currentObject);
				break;
			}
		}
	}
	
	/**
	 * Sets the size of the array that is currently processed when it was set at declaration
	 * @param elementText 
	 * 
	 * @return void
	 */
	private void setInitValueOfArray(String initValue) {
		TypeDescriptor typeDescriptor = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
		if(typeDescriptor.getName().contains("[]")) {
			
			String[] parts = typeDescriptor.getName().split("\\[");
			String fullType = "";
			//if there is no part after the array initializer
			if(parts[1].equals("]")){
				fullType = parts[0] + "[" + initValue + "]";
			//if there is a part after the array initializer put this part at the end of it
			} else {
				String[] secondPart = parts[1].split("\\]");
				fullType = parts[0] + "[" + initValue + "]" + secondPart[1];
			}
			typeDescriptor.setName(fullType);
		}
	}
	
	private void parseCondition(String elementText) {
		
		
	}
	
	/**
	 * Returns the descriptor deque that holds the currently processed elements
	 * 
	 * @return {@code ArrayDeque<Object>} with the current elements
	 */
	protected ArrayDeque<Object> getDescriptorDeque(){
		return this.descriptorDeque;
	}
}