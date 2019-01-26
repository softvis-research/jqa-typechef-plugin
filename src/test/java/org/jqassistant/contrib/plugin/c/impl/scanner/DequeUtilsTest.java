package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;

import org.jqassistant.contrib.plugin.c.api.model.Declarator;
import org.jqassistant.contrib.plugin.c.api.model.ID;
import org.jqassistant.contrib.plugin.c.api.model.InnerStatements;
import org.jqassistant.contrib.plugin.c.api.model.Specifier;
import org.junit.Test;

public class DequeUtilsTest {

	@Test
	public void testBefore() {
		CAstFileScannerPlugin plugin = new CAstFileScannerPlugin();
		plugin.initialize();
		plugin.getDescriptorDeque().push(new Specifier());
		plugin.getDescriptorDeque().push(new Declarator());
		plugin.getDescriptorDeque().push(new ID());
		
		assertEquals(true, DequeUtils.before(ID.class, Declarator.class, plugin.getDescriptorDeque()));
		assertEquals(true, DequeUtils.before(ID.class, Specifier.class, plugin.getDescriptorDeque()));
		assertEquals(false, DequeUtils.before(Specifier.class, ID.class, plugin.getDescriptorDeque()));
		assertEquals(false, DequeUtils.before(Declarator.class, ID.class, plugin.getDescriptorDeque()));
		assertEquals(false, DequeUtils.before(InnerStatements.class, TagNameConstants.class, plugin.getDescriptorDeque()));
		assertEquals(true, DequeUtils.before(Declarator.class, InnerStatements.class, plugin.getDescriptorDeque()));
	}
	
	@Test
	public void testGetFirstOfType() {
		CAstFileScannerPlugin plugin = new CAstFileScannerPlugin();
		plugin.initialize();
		Double double1 = new Double(1.5);
		plugin.getDescriptorDeque().push(double1);
		Double double2 = new Double(2.5);
		plugin.getDescriptorDeque().push(double2);
		Boolean bool1 = new Boolean(true);
		plugin.getDescriptorDeque().push(bool1);
		Boolean bool2 = new Boolean(false);
		plugin.getDescriptorDeque().push(bool2);
		
		assertEquals(false, ((Boolean) DequeUtils.getFirstOfType(Boolean.class, plugin.getDescriptorDeque())).booleanValue());
		assertEquals(2.5, ((Double) DequeUtils.getFirstOfType(Double.class, plugin.getDescriptorDeque())).doubleValue(), 0);
	}
	
	@Test
	public void testGetElementAt() {
		CAstFileScannerPlugin plugin = new CAstFileScannerPlugin();
		plugin.initialize();
		Double double1 = new Double(1.5);
		plugin.getDescriptorDeque().push(double1);
		Double double2 = new Double(2.5);
		plugin.getDescriptorDeque().push(double2);
		Boolean bool1 = new Boolean(true);
		plugin.getDescriptorDeque().push(bool1);
		Boolean bool2 = new Boolean(false);
		plugin.getDescriptorDeque().push(bool2);
		
		assertEquals(false, ((Boolean) DequeUtils.getElementAt(0, plugin.getDescriptorDeque())).booleanValue()); 
		assertEquals(1.5, ((Double) DequeUtils.getElementAt(3, plugin.getDescriptorDeque())).doubleValue(), 0);
	}
}
