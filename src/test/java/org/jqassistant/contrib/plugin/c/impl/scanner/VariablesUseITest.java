package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class VariablesUseITest extends AbstractPluginIT{

	@Test
	public void testGlobalVariableUse() {
		store.beginTransaction();
	    
	    File testFile = new File(getClassesDirectory(VariablesUseITest.class), "/global_variable_use.ast");
	    Scanner scanner = getScanner();
	    CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
	    Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

	    CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
	    TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
	    List<FunctionDescriptor> functionList = translationUnitDescriptor.getDeclaredFunctions();
	    assertEquals(2, functionList.size());
	    
	    for(CDescriptor cDescriptor : functionList) {
	    	if(cDescriptor instanceof FunctionDescriptor) {
	    		FunctionDescriptor function = (FunctionDescriptor) cDescriptor;
	    		if(function.getName().equals("main")) {
	    			assertEquals(1, function.getWrites().size());
	    			assertEquals("g", function.getWrites().get(0).getVariable().getName());
	    			assertEquals(1, function.getReads().size());
	    			assertEquals("g", function.getWrites().get(0).getVariable().getName());
	    		}
	    	} else if(cDescriptor instanceof VariableDescriptor) {
	    		assertEquals("g", ((VariableDescriptor)cDescriptor).getName());
	    	}
	    }
	    
	    store.commitTransaction();
	}
}
