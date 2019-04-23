package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.NegationDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class SourceFileITest extends AbstractPluginIT{

	@Test
	public void testSourceFiles() {
        store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(SourceFileITest.class), "/beispiel_header.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        // Scan the xml file and assert that the returned descriptor is a CAstFileDescriptor
        assertThat(returnedDescriptor, CoreMatchers.<Descriptor>instanceOf(CAstFileDescriptor.class));
        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        assertNotNull(translationUnitDescriptor);
        List<FunctionDescriptor> functionList = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(3, functionList.size());
        
        for(FunctionDescriptor function : functionList) {
        	if(function.getName().equals("mult")) {
        		assertEquals("header_funktionen.h", function.getFileName());
        		assertEquals("FUNKTIONEN_H", ((SingleConditionDescriptor)((NegationDescriptor)function.getCondition()).getCondition()).getMacroName());
        	} else if(function.getName().equals("add")) {
        		assertEquals("header_funktionen.h", function.getFileName());
        	} else if(function.getName().equals("main")) {
        		assertEquals("beispiel_header.c", function.getFileName());
        	}
        }
        
        store.commitTransaction();
	}
}
