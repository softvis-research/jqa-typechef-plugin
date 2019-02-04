package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;

import org.jqassistant.contrib.plugin.c.api.model.Declarator;
import org.jqassistant.contrib.plugin.c.api.model.ID;
import org.jqassistant.contrib.plugin.c.api.model.InnerStatements;
import org.jqassistant.contrib.plugin.c.api.model.Specifier;
import org.junit.BeforeClass;
import org.junit.Test;

public class DequeUtilsTest {
	static CAstFileScannerPlugin plugin;
	
	@BeforeClass
	public static void setup() {
		plugin = new CAstFileScannerPlugin();
		plugin.initialize();
		Double double1 = new Double(1.5);
		plugin.getDescriptorDeque().push(double1);
		Double double2 = new Double(2.5);
		plugin.getDescriptorDeque().push(double2);
		Boolean bool1 = new Boolean(true);
		plugin.getDescriptorDeque().push(bool1);
		Boolean bool2 = new Boolean(false);
		plugin.getDescriptorDeque().push(bool2);
	}

	@Test
	public void testBefore() {
		CAstFileScannerPlugin plugin2 = new CAstFileScannerPlugin();
		plugin2.initialize();
		plugin2.getDescriptorDeque().push(new Specifier());
		plugin2.getDescriptorDeque().push(new Declarator());
		plugin2.getDescriptorDeque().push(new ID());
		
		assertEquals(true, DequeUtils.before(ID.class, Declarator.class, plugin2.getDescriptorDeque()));
		assertEquals(true, DequeUtils.before(ID.class, Specifier.class, plugin2.getDescriptorDeque()));
		assertEquals(false, DequeUtils.before(Specifier.class, ID.class, plugin2.getDescriptorDeque()));
		assertEquals(false, DequeUtils.before(Declarator.class, ID.class, plugin2.getDescriptorDeque()));
		assertEquals(false, DequeUtils.before(InnerStatements.class, TagNameConstants.class, plugin2.getDescriptorDeque()));
		assertEquals(true, DequeUtils.before(Declarator.class, InnerStatements.class, plugin2.getDescriptorDeque()));
	}
	
	@Test
	public void testGetFirstOfType() {
		assertEquals(false, ((Boolean) DequeUtils.getFirstOfType(Boolean.class, plugin.getDescriptorDeque())).booleanValue());
		assertEquals(2.5, ((Double) DequeUtils.getFirstOfType(Double.class, plugin.getDescriptorDeque())).doubleValue(), 0);
	}
	
	@Test
	public void testGetElementAt() {
		assertEquals(false, ((Boolean) DequeUtils.getElementAt(0, plugin.getDescriptorDeque())).booleanValue()); 
		assertEquals(1.5, ((Double) DequeUtils.getElementAt(3, plugin.getDescriptorDeque())).doubleValue(), 0);
	}
	
	@Test
	public void testRemoveFirstOccurrenceOfType() {
		assertEquals(4, plugin.getDescriptorDeque().size());
		DequeUtils.removeFirstOccurrenceOfType(Boolean.class, plugin.getDescriptorDeque());
		assertEquals(3, plugin.getDescriptorDeque().size());
		assertEquals(true, ((Boolean) DequeUtils.getElementAt(0, plugin.getDescriptorDeque())).booleanValue());
		DequeUtils.removeFirstOccurrenceOfType(Double.class, plugin.getDescriptorDeque());
		assertEquals(2, plugin.getDescriptorDeque().size());
		
		setup();
	}
	
	@Test
	public void testGetElementsUnder() {
		plugin.getDescriptorDeque().push(new Specifier());
		assertEquals(5, plugin.getDescriptorDeque().size());
		assertEquals(0, DequeUtils.getElementsUnder(Specifier.class, String.class, plugin.getDescriptorDeque()).size());
		assertEquals(2, DequeUtils.getElementsUnder(Specifier.class, Boolean.class, plugin.getDescriptorDeque()).size());
		assertEquals(2, DequeUtils.getElementsUnder(Boolean.class, Double.class, plugin.getDescriptorDeque()).size());
		assertEquals(0, DequeUtils.getElementsUnder(Double.class, Boolean.class, plugin.getDescriptorDeque()).size());
		assertEquals(0, DequeUtils.getElementsUnder(String.class, Boolean.class, plugin.getDescriptorDeque()).size());
		
		setup();
	}
}
