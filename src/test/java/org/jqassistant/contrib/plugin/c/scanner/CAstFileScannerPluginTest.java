package org.jqassistant.contrib.plugin.c.scanner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.hamcrest.CoreMatchers;
import org.jqassistant.contrib.plugin.c.model.CAstFileDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class CAstFileScannerPluginTest extends AbstractPluginIT{

    @Test
    public void scanAstFile() {
        store.beginTransaction();
        // Scan the test xml file located as resource in the classpath
        File testFile = new File(getClassesDirectory(CAstFileScannerPluginTest.class), "/main_function.ast");
        Scanner scanner = getScanner();
        Descriptor returnedDescriptor = scanner.scan(testFile, testFile.getAbsolutePath(), DefaultScope.NONE);
        
        // Scan the xml file and assert that the returned descriptor is a CAstFileDescriptor
        assertThat(returnedDescriptor, CoreMatchers.<Descriptor>instanceOf(CAstFileDescriptor.class));
        CAstFileDescriptor descriptor = (CAstFileDescriptor) returnedDescriptor;
        assertNotNull(descriptor.getTranslationUnit());
        
        store.commitTransaction();
    }
    
}
