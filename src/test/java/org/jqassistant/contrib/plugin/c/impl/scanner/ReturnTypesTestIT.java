package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jqassistant.contrib.plugin.c.api.model.CAstFileDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.TranslationUnitDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class ReturnTypesTestIT extends AbstractPluginIT{

	@Test
    public void testReturnTypesSimple() {
    	store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(ReturnTypesTestIT.class), "/return_types_simple.ast");
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
    
    @Test
    public void testReturnTypesComplex(){
    	store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(ReturnTypesTestIT.class), "/return_types_complex.ast");
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
				assertEquals("char *", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("char []", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getUnsignedInt":
				assertEquals("unsigned int *", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("unsigned int *", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getSignedInt":
				assertEquals("signed int", functionDescriptor.getReturnType().get(0).getName());
				assertEquals("signed int", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			case "getPointerToInt":
				assertEquals("int *", functionDescriptor.getReturnType().get(0).getName());
				List<String> parameterTypeList = new ArrayList<>();
				parameterTypeList.add("int *");
				parameterTypeList.add("short int");
				assertTrue(parameterTypeList.contains(functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName()));
				assertTrue(parameterTypeList.contains(functionDescriptor.getParameters().get(1).getTypeSpecifiers().get(0).getName()));
				break;
			case "getCharArraySized":
				assertEquals("char [5]", functionDescriptor.getParameters().get(0).getTypeSpecifiers().get(0).getName());
				break;
			default:
				break;
			}
        }
        
        store.commitTransaction();
    }
}
