package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.StructDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class StructTestIT extends AbstractPluginIT{

	@Test
	public void testStructDeclaration() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(StructTestIT.class), "/struct.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<StructDescriptor> structList = translationUnitDescriptor.getDeclaredStructs();
        assertEquals(2, structList.size());
        
        for(CDescriptor cDescriptor : structList) {
        	if(cDescriptor instanceof StructDescriptor) {
        		StructDescriptor struct = (StructDescriptor) cDescriptor;
        		assertEquals(5, struct.getDeclaredVariables().size());
        		for(VariableDescriptor variable : struct.getDeclaredVariables()) {
        			switch (variable.getName()) {
					case "name":
						assertEquals("char [50]", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "strasse":
						assertEquals("char [100]", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "hausnummer":
						assertEquals("short", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "plz":
						assertEquals("long", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "stadt":
						assertEquals("char [50]", variable.getTypeSpecifiers().get(0).getName());
						break;
					default:
						break;
					}
        		}
        	}
        }
        
        store.commitTransaction();
	}
	
	@Test
	public void testConditionalStruct() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(StructTestIT.class), "/conditional_struct.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<StructDescriptor> structList = translationUnitDescriptor.getDeclaredStructs();
        assertEquals(2, structList.size());
        
        for(CDescriptor cDescriptor : structList) {
        	if(cDescriptor instanceof StructDescriptor) {
        		StructDescriptor struct = (StructDescriptor) cDescriptor;
        		assertEquals(5, struct.getDeclaredVariables().size());
        		for(VariableDescriptor variable : struct.getDeclaredVariables()) {
        			switch (variable.getName()) {
					case "name":
						assertEquals("char [50]", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "strasse":
						assertEquals("char [100]", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "hausnummer":
						assertEquals("short", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "plz":
						assertEquals("long", variable.getTypeSpecifiers().get(0).getName());
						break;
					case "stadt":
						assertEquals("char [50]", variable.getTypeSpecifiers().get(0).getName());
						break;
					default:
						break;
					}
        		}
        		assertNotNull(struct.getCondition());
        		assertEquals("FLAG", ((SingleConditionDescriptor) struct.getCondition()).getMacroName());
        	}
        }
        
        store.commitTransaction();
	}
}
