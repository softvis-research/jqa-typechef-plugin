package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class ConditionsTest extends AbstractPluginIT{
	
	@Test
	public void testIfDefUndefined() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/ifdef_undefined.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<VariableDescriptor> variableList = translationUnitDescriptor.getDeclaredVariables();
        for(CDescriptor cDescriptor : variableList) {
        	if(cDescriptor instanceof VariableDescriptor) {
        		VariableDescriptor variable = (VariableDescriptor) cDescriptor;
        		if(variable.getName().equals("number")) {
            		assertNotNull(variable.getCondition());
            		assertEquals("FLAG", ((SingleConditionDescriptor)variable.getCondition()).getMacroName());
            	}
        	}
        }
        
        store.commitTransaction();
	}
	
	@Test
	public void testIfElifUndefined() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/ifelif_undefined.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<VariableDescriptor> variableList = translationUnitDescriptor.getDeclaredVariables();
        
        store.commitTransaction();
	}

}
