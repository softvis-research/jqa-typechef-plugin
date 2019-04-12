package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.EnumConstantDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.EnumDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class EnumITest extends AbstractPluginIT{

	@Test
	public void testEnumDeclaration() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(EnumITest.class), "/enums.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<EnumDescriptor> enumList = translationUnitDescriptor.getDeclaredEnums();
        assertEquals(5, enumList.size());
        List<String> enumNameList = new ArrayList<>();
        enumNameList.add("week_first");
        enumNameList.add("constants");
        enumNameList.add("week_second");
        List<String> enumConstantNameList = null;
        for(CDescriptor cDescriptor : enumList) {
        	if(cDescriptor instanceof EnumDescriptor) {
        		EnumDescriptor enumDescriptor = (EnumDescriptor) cDescriptor;
        		assertTrue(enumNameList.contains(enumDescriptor.getName()));
        		switch (enumDescriptor.getName()) {
				case "week_first":
					assertEquals(3, enumDescriptor.getDeclaredConstants().size());
					enumConstantNameList = new ArrayList<>();
					enumConstantNameList.add("Mon");
					enumConstantNameList.add("Tue");
					enumConstantNameList.add("Wed");
					for(EnumConstantDescriptor constant : enumDescriptor.getDeclaredConstants()) {
						assertTrue(enumConstantNameList.contains(constant.getName()));
					}
					break;
				case "constants":
					assertEquals(3, enumDescriptor.getDeclaredConstants().size());
					enumConstantNameList = new ArrayList<>();
					enumConstantNameList.add("constant1");
					enumConstantNameList.add("constant2");
					enumConstantNameList.add("constant3");
					for(EnumConstantDescriptor constant : enumDescriptor.getDeclaredConstants()) {
						assertTrue(enumConstantNameList.contains(constant.getName()));
					}
					break;
				case "week_second":
					assertEquals(4, enumDescriptor.getDeclaredConstants().size());
					break;
				default:
					break;
        		}
        	} else if(cDescriptor instanceof VariableDescriptor) {
        		List<String> variableNameList = new ArrayList<>();
        		variableNameList.add("newDay");
        		variableNameList.add("day");
        		VariableDescriptor variable = (VariableDescriptor) cDescriptor;
        		if(variable.getName().equals("newDay")) {
        			assertEquals("enum week_second", variable.getTypeSpecifiers().get(0).getName());
        		} else {
        			assertEquals("enum week_first", variable.getTypeSpecifiers().get(0).getName());
        		}
        	}
        }
        
        store.commitTransaction();
	}
	
	@Test
	public void testConditionalEnum() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(EnumITest.class), "/conditional_enum.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<EnumDescriptor> enumList = translationUnitDescriptor.getDeclaredEnums();
        assertEquals(2, enumList.size());
        
        for(CDescriptor cDescriptor : enumList) {
        	if(cDescriptor instanceof EnumDescriptor) {
        		EnumDescriptor enumDescriptor = (EnumDescriptor) cDescriptor;
        		assertEquals("FLAG", ((SingleConditionDescriptor) enumDescriptor.getCondition()).getMacroName());
        		List<EnumConstantDescriptor> enumConstants = enumDescriptor.getDeclaredConstants();
        		assertEquals(3, enumConstants.size());
        		assertEquals("FLAG", ((SingleConditionDescriptor) enumConstants.get(0).getCondition()).getMacroName());
        	}
        }
        
        store.commitTransaction();
	}
}
