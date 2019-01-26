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

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
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
				if(descriptorDeque.peekFirst() instanceof TypeDescriptor && streamReader.getAttributeCount() == 0) {
					setInitValueOfArray();
				}
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
				if(descriptorDeque.peekFirst() instanceof Specifier) {
					descriptorDeque.removeFirst();
				}
				break;
			case TagNameConstants.DECLARATOR:
				if(descriptorDeque.peekFirst() instanceof Declarator) {
					descriptorDeque.removeFirst();
				}
				break;
			case TagNameConstants.ID:
				if(descriptorDeque.peekFirst() instanceof ID) {
					descriptorDeque.removeFirst();
				}
				break;
			case TagNameConstants.INNERSTATEMENTS:
				if(descriptorDeque.peekFirst() instanceof InnerStatements) {
					descriptorDeque.removeFirst();
					//after the end of the inner statements the whole function is finished
					if(descriptorDeque.peekFirst() instanceof FunctionDescriptor) {
						descriptorDeque.removeFirst();
					}
				}
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
		case AttributeValueConstants.PARAMETERDECLARATION:
			if(descriptorDeque.peekFirst() instanceof Declarator) {
				ParameterDescriptor parameterDescriptor = context.getStore().create(ParameterDescriptor.class);
				descriptorDeque.push(parameterDescriptor);
			}
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
		if(descriptorDeque.peekFirst() instanceof Specifier || (descriptorDeque.peekFirst() instanceof Declarator && (DequeUtils.getElementAt(1, descriptorDeque) instanceof FunctionDescriptor))) {
			storeReturnType(type);
		} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor || (descriptorDeque.peekFirst() instanceof Declarator && (DequeUtils.getElementAt(1, descriptorDeque) instanceof ParameterDescriptor))) {
			storeParameterType(type);
		} else if(descriptorDeque.peekFirst() instanceof VariableDescriptor || (descriptorDeque.peekFirst() instanceof Declarator && (DequeUtils.getElementAt(1, descriptorDeque) instanceof VariableDescriptor))) {
			storeVariableType(type);
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
				((ParameterDescriptor) currentObject).setName(name);
				TypeDescriptor typeDescriptor = (TypeDescriptor) DequeUtils.getFirstOfType(TypeDescriptor.class, descriptorDeque);
				if(typeDescriptor != null) {
					((ParameterDescriptor) currentObject).getTypeSpecifiers().add(typeDescriptor);
					descriptorDeque.remove(typeDescriptor);
				}
				FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
				functionDescriptor.getParameters().add((ParameterDescriptor) currentObject);
				descriptorDeque.remove(currentObject);
				break;
			} else if(currentObject instanceof FunctionDescriptor && !DequeUtils.before(InnerStatements.class, FunctionDescriptor.class, descriptorDeque)) {
				//if we're already in the inner statements of the function the function name is already set
				((FunctionDescriptor) currentObject).setName(name);
				break;
			} else if(currentObject instanceof VariableDescriptor) {
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
	 * Gets return type as String and adds it to the currently active function descriptor
	 * 
	 * @param returnType return type as String
	 * 
	 * @return void
	 */
	private void storeReturnType(String returnType) {
		FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
		TypeDescriptor typeDescriptor = context.getStore().create(TypeDescriptor.class);
		typeDescriptor.setName(returnType);
		functionDescriptor.getReturnType().add(typeDescriptor);		
	}
	
	/**
	 * Gets type as String and adds it to the currently processed parameter
	 * 
	 * @param type type as String
	 * 
	 * @return void
	 */
	private void storeParameterType(String type) {
		ParameterDescriptor descriptor = (ParameterDescriptor) descriptorDeque.getFirst();
		TypeDescriptor typeDescriptor = context.getStore().create(TypeDescriptor.class);
		typeDescriptor.setName(type);
		if(type.equals("[]")) {
			descriptorDeque.push(typeDescriptor);
		} else {
			descriptor.getTypeSpecifiers().add(typeDescriptor);
		}
	}
	
	/**
	 * Gets type as String and adds it to the currently processed variable
	 * 
	 * @param type type as String
	 * 
	 * @return void
	 */
	private void storeVariableType(String type) {
		VariableDescriptor descriptor = (VariableDescriptor) DequeUtils.getFirstOfType(VariableDescriptor.class, descriptorDeque);
		TypeDescriptor typeDescriptor = context.getStore().create(TypeDescriptor.class);
		typeDescriptor.setName(type);
		if(type.equals("[]")) {
			descriptorDeque.push(typeDescriptor);
		} else {
			descriptor.getTypeSpecifiers().add(typeDescriptor);
		}
	}
	
	/**
	 * Sets the size of the array that is currently processed when it was set at declaration
	 * 
	 * @return void
	 */
	private void setInitValueOfArray() {
		String initValue = "";
		try {
			initValue = streamReader.getElementText();
		} catch (XMLStreamException e) {
			logger.error(e.getMessage());
		}
		TypeDescriptor typeDescriptor = (TypeDescriptor) descriptorDeque.peekFirst();
		if(typeDescriptor.getName().equals("[]")) {
			typeDescriptor.setName("[" + initValue + "]");
		}
		
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