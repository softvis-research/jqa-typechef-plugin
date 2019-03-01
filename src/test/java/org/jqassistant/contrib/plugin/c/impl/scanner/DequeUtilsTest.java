package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import java.util.ArrayDeque;
import org.jqassistant.contrib.plugin.c.api.model.Declarator;
import org.jqassistant.contrib.plugin.c.api.model.ID;
import org.jqassistant.contrib.plugin.c.api.model.InnerStatements;
import org.jqassistant.contrib.plugin.c.api.model.Specifier;
import org.junit.Before;
import org.junit.Test;

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
		ArrayDeque<Object> deque2 = new ArrayDeque<>();
		deque2.push(new Specifier());
		deque2.push(new Declarator());
		deque2.push(new ID());
		
		assertEquals(true, DequeUtils.before(ID.class, Declarator.class, deque2));
		assertEquals(true, DequeUtils.before(ID.class, Specifier.class, deque2));
		assertEquals(false, DequeUtils.before(Specifier.class, ID.class, deque2));
		assertEquals(false, DequeUtils.before(Declarator.class, ID.class, deque2));
		assertEquals(false, DequeUtils.before(InnerStatements.class, TagNameConstants.class, deque2));
		assertEquals(true, DequeUtils.before(Declarator.class, InnerStatements.class, deque2));
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
		this.deque.push(new Specifier());
		assertEquals(5, this.deque.size());
		assertEquals(0, DequeUtils.getElementsUnder(Specifier.class, String.class, this.deque).size());
		assertEquals(2, DequeUtils.getElementsUnder(Specifier.class, Boolean.class, this.deque).size());
		assertEquals(2, DequeUtils.getElementsUnder(Boolean.class, Double.class, this.deque).size());
		assertEquals(0, DequeUtils.getElementsUnder(Double.class, Boolean.class, this.deque).size());
		assertEquals(0, DequeUtils.getElementsUnder(String.class, Boolean.class, this.deque).size());
	}
}
