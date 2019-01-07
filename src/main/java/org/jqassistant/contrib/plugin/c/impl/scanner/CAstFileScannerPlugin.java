package org.jqassistant.contrib.plugin.c.impl.scanner;

import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

@Requires(FileDescriptor.class)
public class CAstFileScannerPlugin extends AbstractScannerPlugin<FileResource, CAstFileDescriptor>{

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
	public CAstFileDescriptor scan(FileResource item, String path, Scope scope,
			Scanner scanner) throws IOException {
		ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        Source source = new StreamSource(item.createStream());
        final FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        // Add the C label.
        final CAstFileDescriptor cAstFileDescriptor = store.addDescriptorType(fileDescriptor, CAstFileDescriptor.class);
        try {
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(source);

            while (streamReader.hasNext()) {
                int eventType = streamReader.getEventType();
                switch (eventType) {
                	case XMLStreamConstants.START_ELEMENT:
                	if(streamReader.getLocalName().equals("de.fosd.typechef.parser.c.TranslationUnit")) {
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