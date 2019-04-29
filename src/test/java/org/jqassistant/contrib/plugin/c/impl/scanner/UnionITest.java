package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.UnionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class UnionITest extends AbstractPluginIT{

	@Test
	public void testUnionDeclaration() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(UnionITest.class), "/unions.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<UnionDescriptor> unionList = translationUnitDescriptor.getDeclaredUnions();
        assertEquals(6, unionList.size());
        List<String> unionNameList = new ArrayList<>();
        unionNameList.add("test");
        unionNameList.add("test1");
        
        for(CDescriptor cDescriptor : unionList) {
        	if(cDescriptor instanceof UnionDescriptor) {
        		UnionDescriptor union = (UnionDescriptor) cDescriptor;
        		assertEquals("unions.c", union.getFileName());
        		assertTrue(unionNameList.contains(union.getName()));
        		assertEquals(3, union.getDeclaredVariables().size());
        		for(VariableDescriptor variable : union.getDeclaredVariables()) {
        			assertEquals("unions.c", variable.getFileName());
        			switch (variable.getName()) {
					case "a":
						assertEquals("char", variable.getTypeSpecifiers().getName());
						break;
					case "b":
						assertEquals("int", variable.getTypeSpecifiers().getName());
						break;
					case "c":
						assertEquals("double", variable.getTypeSpecifiers().getName());
						break;
					default:
						break;
					}
        		}
        	} else if(cDescriptor instanceof VariableDescriptor) {
        		VariableDescriptor variable = (VariableDescriptor) cDescriptor;
        		List<String> variableNameList = new ArrayList<>();
        		variableNameList.add("test2");
        		variableNameList.add("newTest");
        		variableNameList.add("severalTests");
        		assertTrue(variableNameList.contains(variable.getName()));
        		assertEquals("unions.c", variable.getFileName());
        		if(variable.getName().equals("test2")) {
        			assertEquals("union test1", variable.getTypeSpecifiers().getName());
        		} else if(variable.getName().equals("severalTests")){
        			assertEquals("union test [20]", variable.getTypeSpecifiers().getName());
        		} else {	
        			assertEquals("union test", variable.getTypeSpecifiers().getName());
        		}
        	}
        }
        
        store.commitTransaction();
	}
	
	@Test
	public void testConditionalUnion() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(UnionITest.class), "/conditional_union.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<UnionDescriptor> unionList = translationUnitDescriptor.getDeclaredUnions();
        assertEquals(2, unionList.size());
        for(CDescriptor cDescriptor : unionList) {
        	if(cDescriptor instanceof UnionDescriptor) {
        		UnionDescriptor union = (UnionDescriptor) cDescriptor;
        		assertEquals("conditional_union.c", union.getFileName());
        		assertEquals("test", union.getName());
        		assertEquals(3, union.getDeclaredVariables().size());
        		assertEquals("FLAG", ((SingleConditionDescriptor)union.getCondition()).getMacroName());
        	}
        }

        store.commitTransaction();
	}
}
