package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class FunctionITest extends AbstractPluginIT{
	
    @Test
    public void testScanMainFunction() {
        store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(FunctionITest.class), "/main_function.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        // Scan the xml file and assert that the returned descriptor is a CAstFileDescriptor
        assertThat(returnedDescriptor, CoreMatchers.<Descriptor>instanceOf(CAstFileDescriptor.class));
        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = descriptor.getTranslationUnit();
        assertNotNull(translationUnitDescriptor);
        FunctionDescriptor functionDescriptor = translationUnitDescriptor.getDeclaredFunctions().get(0);
        assertNotNull(functionDescriptor);
        assertEquals("int", functionDescriptor.getReturnType().getName());
        assertEquals("main", functionDescriptor.getName());
        assertEquals(5, functionDescriptor.getLineCount());
        
        store.commitTransaction();
    }
    
    @Test
    public void testScanTwoFunctions() {
    	store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(FunctionITest.class), "/two_functions.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        CAstFileDescriptor castFileDescriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = castFileDescriptor.getTranslationUnit();
        List<FunctionDescriptor> declaredFunctions = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(2, declaredFunctions.size());
        assertEquals("multiplyTwoNumbers", declaredFunctions.get(0).getName());
        assertEquals(2, declaredFunctions.get(0).getParameters().size());
        assertEquals("double", declaredFunctions.get(0).getReturnType().getName());
        assertEquals("factor1", declaredFunctions.get(0).getParameters().get(0).getName());
        assertEquals("double", declaredFunctions.get(0).getParameters().get(0).getTypeSpecifiers().getName());
        assertEquals("factor2", declaredFunctions.get(0).getParameters().get(1).getName());
        assertEquals("double", declaredFunctions.get(0).getParameters().get(1).getTypeSpecifiers().getName());
        assertEquals(3, declaredFunctions.get(0).getLineCount());
        
        assertEquals("int", declaredFunctions.get(1).getReturnType().getName());
        assertEquals("main", declaredFunctions.get(1).getName());
        assertEquals(0, declaredFunctions.get(1).getParameters().size());
        assertEquals(6, declaredFunctions.get(1).getLineCount());
        
        store.commitTransaction();
    }    
}
