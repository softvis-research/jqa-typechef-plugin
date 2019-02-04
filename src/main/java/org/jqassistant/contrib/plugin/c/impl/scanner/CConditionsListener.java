package org.jqassistant.contrib.plugin.c.impl.scanner;

import org.jqassistant.contrib.plugin.c.api.ConditionsBaseListener;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.CompleteConditionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.SingleConditionContext;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

public class CConditionsListener extends ConditionsBaseListener{

	private ScannerContext context;
	private ConditionDescriptor resultCondition;
	
	public CConditionsListener(ScannerContext context) {
		this.context = context;
	}
	
	@Override
	public void enterCompleteCondition(CompleteConditionContext ctx) {
		SingleConditionContext singleCondition = ctx.singleCondition();
		SingleConditionDescriptor singleConditionDescriptor = context.getStore().create(SingleConditionDescriptor.class);
		singleConditionDescriptor.setMacroName(singleCondition.MACRONAME().toString());
		this.resultCondition = singleConditionDescriptor;
	}

	public ConditionDescriptor getResultCondition() {
		return resultCondition;
	}
}
