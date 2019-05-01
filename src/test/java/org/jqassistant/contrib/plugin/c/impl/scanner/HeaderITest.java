package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.CDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.StructDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.UnionDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class HeaderITest extends AbstractPluginIT{
	
	@Test
	public void testStructUnionHeader() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(HeaderITest.class), "/struct_union_header.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        List<FunctionDescriptor> elementList = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(3, elementList.size());
        
        for(CDescriptor cDescriptor : elementList) {
        	if(cDescriptor instanceof StructDescriptor) {
        		StructDescriptor struct = (StructDescriptor) cDescriptor;
        		assertEquals("adresse", struct.getName());
        		assertEquals(5, struct.getDeclaredVariables().size());
        		assertEquals("struct_union_header.c", struct.getFileName());
        	} else if(cDescriptor instanceof UnionDescriptor) {
        		UnionDescriptor union = (UnionDescriptor) cDescriptor;
        		assertEquals("test", union.getName());
        		assertEquals(3, union.getDeclaredVariables().size());
        		assertEquals("struct_union_header.c", union.getFileName());
        	} else if(cDescriptor instanceof FunctionDescriptor) {
        		FunctionDescriptor function = (FunctionDescriptor) cDescriptor;
        		assertEquals("main", function.getName());
        		assertEquals("struct_union_header.c", function.getFileName());
        	}
        }
        store.commitTransaction();
	}

}
