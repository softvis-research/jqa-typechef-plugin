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
import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class CAstFileScannerPluginTest extends AbstractPluginIT{
	
    @Test
    public void testScanMainFunction() {
        store.beginTransaction();
        
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/main_function.ast");
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
        assertEquals("int", functionDescriptor.getReturnType().get(0).getName());
        assertEquals("main", functionDescriptor.getName());
        
        store.commitTransaction();
    }
    
    @Test
    public void testScanTwoFunctions() {
    	store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/two_functions.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        CAstFileDescriptor castFileDescriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = castFileDescriptor.getTranslationUnit();
        List<FunctionDescriptor> declaredFunctions = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(2, declaredFunctions.size());
        assertEquals("multiplyTwoNumbers", declaredFunctions.get(0).getName());
        assertEquals(2, declaredFunctions.get(0).getParameters().size());
        assertEquals("double", declaredFunctions.get(0).getReturnType().get(0).getName());
        assertEquals("factor1", declaredFunctions.get(0).getParameters().get(0).getName());
        assertEquals("double", declaredFunctions.get(0).getParameters().get(0).getTypeSpecifiers().get(0).getName());
        assertEquals("factor2", declaredFunctions.get(0).getParameters().get(1).getName());
        assertEquals("double", declaredFunctions.get(0).getParameters().get(1).getTypeSpecifiers().get(0).getName());
        
        assertEquals("int", declaredFunctions.get(1).getReturnType().get(0).getName());
        assertEquals("main", declaredFunctions.get(1).getName());
        assertEquals(0, declaredFunctions.get(1).getParameters().size());
        
        store.commitTransaction();
    }
    
    @Test
    public void testReturnTypesSimple() {
    	store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/return_types_simple.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        CAstFileDescriptor castFileDescriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = castFileDescriptor.getTranslationUnit();
        List<FunctionDescriptor> declaredFunctions = translationUnitDescriptor.getDeclaredFunctions();
        //switch over function names because order of functions in list varies
        for(FunctionDescriptor descriptor : declaredFunctions) {
        	switch (descriptor.getName()) {
			case "main":
				assertEquals("int", descriptor.getReturnType().get(0).getName());
				assertEquals(0, descriptor.getParameters().size());
				break;
			case "getInt":
				assertEquals("int", descriptor.getReturnType().get(0).getName());
				assertEquals("int", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				assertEquals("number", descriptor.getParameters().get(0).getName());
				break;
			case "makeNothing":
				assertEquals("void", descriptor.getReturnType().get(0).getName());
				break;
			case "getShort":
				assertEquals("short", descriptor.getReturnType().get(0).getName());
				assertEquals("short", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getLong":
				assertEquals("long", descriptor.getReturnType().get(0).getName());
				assertEquals("long", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getFloat":
				assertEquals("float", descriptor.getReturnType().get(0).getName());
				assertEquals("float", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getSigned":
				assertEquals("signed", descriptor.getReturnType().get(0).getName());
				assertEquals("signed", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getUnsigned":
				assertEquals("unsigned", descriptor.getReturnType().get(0).getName());
				assertEquals("unsigned", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getChar":
				assertEquals("char", descriptor.getReturnType().get(0).getName());
				assertEquals("char", descriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			default:
				break;
			}
        }
        assertEquals(10, declaredFunctions.size());
        
        store.commitTransaction();
    }
    
    @Ignore
    @Test
    public void testReturnTypesComplex(){
    	store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/return_types_complex.ast");
        Scanner scanner = getScanner();
        CAstFileDescriptor fileDescriptor = store.create(CAstFileDescriptor.class);
        Descriptor returnedDescriptor = scanner.scan(testFile, fileDescriptor, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        CAstFileDescriptor castFileDescriptor = (CAstFileDescriptor) returnedDescriptor;
        TranslationUnitDescriptor translationUnitDescriptor = castFileDescriptor.getTranslationUnit();
        List<FunctionDescriptor> declaredFunctions = translationUnitDescriptor.getDeclaredFunctions();
        assertEquals(6, declaredFunctions.size());
        for(FunctionDescriptor functionDescriptor : declaredFunctions) {
        	switch (functionDescriptor.getName()) {
			case "getCharArray":
				assertEquals("char", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("*", functionDescriptor.getReturnType().get(1).getName());
				assertEquals("char", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				assertEquals("[]", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(1).getName());
				break;
			case "getUnsignedInt":
				assertEquals("unsigned", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("int", functionDescriptor.getReturnType().get(1).getName());
				assertEquals("*", functionDescriptor.getReturnType().get(2).getName());
				assertEquals("unsigned", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				assertEquals("int", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(1).getName());
				assertEquals("*", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(2).getName());
				break;
			case "getSignedInt":
				assertEquals("signed", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("int", functionDescriptor.getReturnType().get(1).getName());
				assertEquals("signed", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				assertEquals("int", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(1).getName());
				break;
			case "getPointerToInt":
				assertEquals("int", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("*", functionDescriptor.getReturnType().get(1).getName());
				assertEquals("int", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				assertEquals("*", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(1).getName());
				assertEquals("short", functionDescriptor.getParameters().get(1).getTypeSpecifiers().get(0).getName());
				assertEquals("int", functionDescriptor.getParameters().get(1).getTypeSpecifiers().get(1).getName());
				break;
			case "getCharArraySized":
				assertEquals("char", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				assertEquals("[5]", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(1).getName());
				break;
			default:
				break;
			}
        }
        
        store.commitTransaction();
    }
    
}
