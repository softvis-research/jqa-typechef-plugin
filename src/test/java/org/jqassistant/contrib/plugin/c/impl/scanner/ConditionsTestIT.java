package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.AndDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.NegationDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class ConditionsTestIT extends AbstractPluginIT{
	
	@Test
	public void testIfDefUndefined() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(ConditionsTestIT.class), "/ifdef_undefined.ast");
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
        
        File testFile = new File(getClassesDirectory(ConditionsTestIT.class), "/ifelif_undefined.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<VariableDescriptor> variableList = translationUnitDescriptor.getDeclaredVariables();
        assertEquals(4, variableList.size());
        for(CDescriptor cDescriptor : variableList) {
        	if(cDescriptor instanceof VariableDescriptor) {
        		VariableDescriptor variable = (VariableDescriptor) cDescriptor;
        		if(variable.getName().equals("number")) {
        			assertNotNull(variable.getCondition());
            		assertEquals("FLAG", ((SingleConditionDescriptor)variable.getCondition()).getMacroName());
        		} else if(variable.getName().equals("number2")) {
        			assertNotNull(variable.getCondition());
        			assertTrue(variable.getCondition() instanceof AndDescriptor);
        			assertEquals(2, ((AndDescriptor) variable.getCondition()).getConnectedConditions().size());
        		} else if(variable.getName().equals("number3")) {
        			assertNotNull(variable.getCondition());
        			assertTrue(variable.getCondition() instanceof AndDescriptor);
        			assertTrue(((AndDescriptor) variable.getCondition()).getConnectedConditions().get(0) instanceof NegationDescriptor);
        			assertTrue(((AndDescriptor) variable.getCondition()).getConnectedConditions().get(1) instanceof NegationDescriptor);
        		}
        	}
        }
        
        store.commitTransaction();
	}
	
	@Test
	public void testIfDefMethod() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(ConditionsTestIT.class), "/ifdef_method.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<FunctionDescriptor> functions = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(3, functions.size());
        for(CDescriptor cDescriptor : functions) {
        	if(cDescriptor instanceof FunctionDescriptor) {
        		FunctionDescriptor function = (FunctionDescriptor) cDescriptor;
        		if(function.getName().equals("getInt")) {
        			assertNotNull(function.getCondition());
            		assertEquals("FLAG", ((SingleConditionDescriptor)function.getCondition()).getMacroName());
        		} else if(function.getName().equals("getDouble")) {
        			assertNotNull(function.getCondition());
        			assertTrue(function.getCondition() instanceof NegationDescriptor);
        			NegationDescriptor negation = (NegationDescriptor) function.getCondition();
        			assertEquals("FLAG", ((SingleConditionDescriptor)negation.getCondition()).getMacroName());
        		} else if(function.getName().equals("main")) {
        			assertNull(function.getCondition());
        		}
        	}
        }
        
        store.commitTransaction();
	}
	
	@Test
	public void testIfDefTranslationUnit() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(ConditionsTestIT.class), "/ifdef_translationUnit.h.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<FunctionDescriptor> members = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(2, members.size());
        for(CDescriptor member : members) {
        	if(member instanceof VariableDescriptor) {
        		VariableDescriptor variable = (VariableDescriptor) member;
        		assertEquals("number", variable.getName());
        		assertNotNull(variable.getCondition());
        		SingleConditionDescriptor condition = (SingleConditionDescriptor) variable.getCondition();
        		assertEquals("FLAG", condition.getMacroName());
        	} else if(member instanceof FunctionDescriptor) {
        		FunctionDescriptor function = (FunctionDescriptor) member;
        		assertEquals("testFunction", function.getName());
        		assertEquals(1, function.getParameters().size());
        		//ast has an error here, condition should be set for this function as well
        	}
        }
        
        
        store.commitTransaction();
	}

}
