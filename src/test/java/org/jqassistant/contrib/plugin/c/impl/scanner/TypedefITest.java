package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.StructDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.VariableDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class TypedefITest extends AbstractPluginIT{
	
	@Test
	public void testTypeDef() {
		store.beginTransaction();
		
        File testFile = new File(getClassesDirectory(TypedefITest.class), "/typedef.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        CAstFileDescriptor castFileDescriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = castFileDescriptor.getTranslationUnit();
        List<FunctionDescriptor> declaredFunctions = translationUnitDescriptor.getDeclaredFunctions();
        
        assertEquals(6, declaredFunctions.size());
        for(CDescriptor cDescriptor : declaredFunctions) {
        	if(cDescriptor instanceof VariableDescriptor) {
        		VariableDescriptor variable = (VariableDescriptor) cDescriptor;
        		List<String> nameList = new ArrayList<String>();
        		nameList.add("zahl");
        		nameList.add("kurt");
        		nameList.add("dieter");
        		nameList.add("antonia");
        		assertTrue(nameList.contains(variable.getName()));
        		switch (variable.getName()) {
				case "zahl":
					assertEquals("Integer", variable.getTypeSpecifiers().getName());
					break;
				default:
					assertEquals("person", variable.getTypeSpecifiers().getName());
					break;
				}
        	} else if(cDescriptor instanceof StructDescriptor) {
        		StructDescriptor struct = (StructDescriptor) cDescriptor;
        		assertEquals("person", struct.getTypedef());
        		assertEquals(2, struct.getDeclaredVariables().size());
        	} else if(cDescriptor instanceof FunctionDescriptor) {
        		FunctionDescriptor function = (FunctionDescriptor) cDescriptor;
        		assertEquals("main", function.getName());
        	}
        }
        
        store.commitTransaction();
	}

}
