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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.sun.xml.internal.rngom.digested.DDataPattern.Param;

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
                }
                streamReader.next();
            }
        	
        } catch (Exception e){
        	logger.error(e.getMessage());
        }
		return cAstFileDescriptor;
	}

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
				handleNameElement(streamReader.getElementText());
			} catch (XMLStreamException e) {
				logger.error(e.getMessage());
			}
				break;
			case TagNameConstants.INNERSTATEMENTS:
				InnerStatements innerStatements = new InnerStatements();
				descriptorDeque.push(innerStatements);
				break;
			default:
				break;
		}
	}

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

	//TODO: make setting of return type and parameter type to new function 
	private void handleEntryElement() {
		switch (streamReader.getAttributeValue(0)) {
		case AttributeValueConstants.FUNCTIONDEFINITION:
			createFunctionDefinition();
			break;
		case AttributeValueConstants.INTSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("int");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("int");
			}
			break;
		case AttributeValueConstants.DOUBLESPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("double");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("double");
			}
			break;
		case AttributeValueConstants.VOIDSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("void");
			}
			break;
		case AttributeValueConstants.SHORTSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("short");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("short");
			}
			break;
		case AttributeValueConstants.LONGSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("long");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("long");
			}
			break;
		case AttributeValueConstants.FLOATSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("float");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("float");
			}
			break;
		case AttributeValueConstants.SIGNEDSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("signed");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("signed");
			}
			break;
		case AttributeValueConstants.UNSIGNEDSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("unsigned");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("unsigned");
			}
			break;
		case AttributeValueConstants.CHARSPECIFIER:
			if(descriptorDeque.peekFirst() instanceof Specifier) {
				storeReturnType("char");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("char");
			}
			break;
		case AttributeValueConstants.POINTER:
			if(descriptorDeque.peekFirst() instanceof Declarator) {
				storeReturnType("*");
			} else if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("*");
			}
			break;
		case AttributeValueConstants.ARRAY:
			if(descriptorDeque.peekFirst() instanceof ParameterDescriptor) {
				storeParameterType("[]");
			}
			break;
		case AttributeValueConstants.PARAMETERDECLARATION:
			if(descriptorDeque.peekFirst() instanceof Declarator) {
				ParameterDescriptor parameterDescriptor = context.getStore().create(ParameterDescriptor.class);
				descriptorDeque.push(parameterDescriptor);
			}
			break;
		default:
			break;
		}
	}

	private void createFunctionDefinition() {
		FunctionDescriptor functionDescriptor = context.getStore().create(FunctionDescriptor.class);
		this.translationUnitDescriptor.getDeclaredFunctions().add(functionDescriptor);
		descriptorDeque.push(functionDescriptor);
	}
	
	private void storeReturnType(String returnType) {
		FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
		TypeDescriptor typeDescriptor = context.getStore().create(TypeDescriptor.class);
		typeDescriptor.setName(returnType);
		functionDescriptor.getReturnType().add(typeDescriptor);		
	}
	
	private void handleNameElement(String name) {
		Iterator<Object> it = descriptorDeque.iterator();
		while(it.hasNext()) {
			Object currentObject = it.next();
			if(currentObject instanceof ParameterDescriptor) {
				((ParameterDescriptor) currentObject).setName(name);
				FunctionDescriptor functionDescriptor = (FunctionDescriptor) DequeUtils.getFirstOfType(FunctionDescriptor.class, descriptorDeque);
				functionDescriptor.getParameters().add((ParameterDescriptor) currentObject);
				descriptorDeque.remove(currentObject);
				break;
			}
			
			//if we're already in the inner statements of the function the function name is already set
			if(currentObject instanceof FunctionDescriptor && !DequeUtils.before(InnerStatements.class, FunctionDescriptor.class, descriptorDeque)) {
				((FunctionDescriptor) currentObject).setName(name);
				break;
			}
		}
		
	}
	
	private void storeParameterType(String type) {
		ParameterDescriptor descriptor = (ParameterDescriptor) descriptorDeque.getFirst();
		TypeDescriptor typeDescriptor = context.getStore().create(TypeDescriptor.class);
		typeDescriptor.setName(type);
		descriptor.getTypeSpecifiers().add(typeDescriptor);
	}
	
	protected ArrayDeque<Object> getDescriptorDeque(){
		return this.descriptorDeque;
	}
}