package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hamcrest.CoreMatchers;
import org.jqassistant.contrib.plugin.c.api.ConditionsLexer;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.NegationDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class ConditionsParserTest extends AbstractPluginIT{

	@Test
	public void testSingleCondition() {
		String text = "definedEx(FLAG)";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(SingleConditionDescriptor.class));
		SingleConditionDescriptor singleConditionDescriptor = (SingleConditionDescriptor) descriptor;
		assertEquals("FLAG", singleConditionDescriptor.getMacroName());
		
		store.commitTransaction();
	}
	
	@Test
	public void testNegation() {
		String text = "!definedEx(FLAG)";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(NegationDescriptor.class));
		NegationDescriptor negationDescriptor = (NegationDescriptor) descriptor;
		SingleConditionDescriptor singleCondition = (SingleConditionDescriptor) negationDescriptor.getCondition();
		assertEquals("FLAG", singleCondition.getMacroName());
		
		store.commitTransaction();
	}
}
