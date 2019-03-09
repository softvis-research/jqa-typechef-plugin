package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import org.jqassistant.contrib.plugin.c.api.model.FunctionDescriptor;
import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class DequeUtilsTest extends AbstractPluginIT{
	private ArrayDeque<Object> deque;
	
	@Before
	public void setup() {
		this.deque = new ArrayDeque<>();
		Double double1 = new Double(1.5);
		this.deque.push(double1);
		Double double2 = new Double(2.5);
		this.deque.push(double2);
		Boolean bool1 = new Boolean(true);
		this.deque.push(bool1);
		Boolean bool2 = new Boolean(false);
		this.deque.push(bool2);
	}

	@Test
	public void testBefore() {
		this.deque.push(new String());
		
		assertEquals(true, DequeUtils.before(String.class, Boolean.class, this.deque));
		assertEquals(true, DequeUtils.before(Boolean.class, Double.class, this.deque));
		assertEquals(false, DequeUtils.before(Integer.class, String.class, this.deque));
		assertEquals(false, DequeUtils.before(Double.class, String.class, this.deque));
		assertEquals(true, DequeUtils.before(String.class, Integer.class, this.deque));
	}
	
	@Test
	public void testGetFirstOfType() {
		assertEquals(false, (((Boolean) DequeUtils.getFirstOfType(Boolean.class, this.deque)).booleanValue()));
		assertEquals(2.5, ((Double) DequeUtils.getFirstOfType(Double.class, this.deque)).doubleValue(), 0);
	}
	
	@Test
	public void testGetElementAt() {
		assertEquals(false, ((Boolean) DequeUtils.getElementAt(0, this.deque)).booleanValue()); 
		assertEquals(1.5, ((Double) DequeUtils.getElementAt(3, this.deque)).doubleValue(), 0);
	}
	
	@Test
	public void testRemoveFirstOccurrenceOfType() {
		assertEquals(4, this.deque.size());
		DequeUtils.removeFirstOccurrenceOfType(Boolean.class, this.deque);
		assertEquals(3, this.deque.size());
		assertEquals(true, ((Boolean) DequeUtils.getElementAt(0, this.deque)).booleanValue());
		DequeUtils.removeFirstOccurrenceOfType(Double.class, this.deque);
		assertEquals(2, this.deque.size());
	}
	
	@Test
	public void testGetElementsUnder() {
		this.deque.push(new Boolean(false));
		assertEquals(5, this.deque.size());
		assertEquals(0, DequeUtils.getElementsUnder(Boolean.class, String.class, this.deque).size());
		assertEquals(2, DequeUtils.getElementsUnder(Boolean.class, Boolean.class, this.deque).size());
		assertEquals(2, DequeUtils.getElementsUnder(Boolean.class, Double.class, this.deque).size());
		assertEquals(0, DequeUtils.getElementsUnder(Double.class, Boolean.class, this.deque).size());
		assertEquals(0, DequeUtils.getElementsUnder(String.class, Boolean.class, this.deque).size());
	}
	
	@Test
	public void testReplaceFirstElementOfType() {
		store.beginTransaction();
		Scanner scanner = getScanner();
		ScannerContext context = scanner.getContext();
		this.deque = DequeUtils.replaceFirstElementOfType(Double.class, FunctionDescriptor.class, this.deque, context);
		assertTrue(DequeUtils.getFirstOfType(FunctionDescriptor.class, this.deque) != null);
		assertTrue(DequeUtils.getElementAt(2, this.deque) instanceof FunctionDescriptor);
		store.commitTransaction();
	}
}
