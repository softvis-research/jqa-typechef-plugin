package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertTrue;

import java.io.File;
import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class RealExampleTest extends AbstractPluginIT{
	
	@Test
	public void testMainAst() {
		store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(RealExampleTest.class), "/main.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);

        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        assertTrue(descriptor.getTranslationUnit() != null);
        
        store.commitTransaction();
	}

}
