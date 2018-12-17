package org.jqassistant.contrib.plugin.c.scanner;

import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jqassistant.contrib.plugin.c.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.model.TranslationUnitDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;

@Requires(FileDescriptor.class)
public class CAstFileScannerPlugin extends AbstractXmlFileScannerPlugin<CAstFileDescriptor>{

	private static final Logger logger = LoggerFactory.getLogger(CAstFileScannerPlugin.class);
	private XMLInputFactory inputFactory;

    @Override
    public void initialize() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

	@Override
	public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
		return path.toLowerCase().endsWith(".ast");
	}

	@Override
	public CAstFileDescriptor scan(FileResource item, CAstFileDescriptor cAstFileDescriptor, String path, Scope scope,
			Scanner scanner) throws IOException {
		ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        Source source = new StreamSource(item.createStream());
        try {
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(source);

            while (streamReader.hasNext()) {
                int eventType = streamReader.getEventType();
                switch (eventType) {
                	case XMLStreamConstants.START_ELEMENT:
                	if(streamReader.getLocalName().equals("translationUnit")) {
                		TranslationUnitDescriptor translationUnit = store.create(TranslationUnitDescriptor.class);
                		cAstFileDescriptor.setTranslationUnit(translationUnit);
                	}
                	break;
                	
                }
                streamReader.next();
            }
        } catch (Exception e){
        	logger.error(e.getMessage());
        }
		return cAstFileDescriptor;
	}
}