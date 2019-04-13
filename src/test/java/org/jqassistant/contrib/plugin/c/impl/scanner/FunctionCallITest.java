package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.EnumDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class FunctionCallITest extends AbstractPluginIT{

	@Test
	public void testFunctionCalls() {
		
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(FunctionCallITest.class), "/function_calls.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<FunctionDescriptor> functionList = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(3, functionList.size());
        
        for(CDescriptor cDescriptor : functionList) {
        	if(cDescriptor instanceof FunctionDescriptor) {
        		FunctionDescriptor function = (FunctionDescriptor) cDescriptor;
        		if(function.getName().equals("main")) {
        			assertEquals(1, function.getInvokedFunctions().size());
        			assertEquals("max", function.getInvokedFunctions().get(0).getName());
        		}
        	}
        }
        store.commitTransaction();
	}
}
