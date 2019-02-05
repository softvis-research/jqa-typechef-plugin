package org.jqassistant.contrib.plugin.c.impl.scanner;

import org.jqassistant.contrib.plugin.c.api.ConditionsBaseListener;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.CompleteConditionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.NegativeConditionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.SingleConditionContext;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.NegationDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;

import com.buschmais.jqassistant.core.store.api.Store;

public class CConditionsListener extends ConditionsBaseListener{

	private Store store;
	private ConditionDescriptor resultCondition;
	
	public CConditionsListener(Store store) {
		this.store = store;
	}
	
	@Override
	public void enterCompleteCondition(CompleteConditionContext ctx) {
		if(ctx.singleCondition() != null) {
			SingleConditionDescriptor descriptor = buildSingleConditionDescriptor(ctx.singleCondition());
			this.resultCondition = descriptor;
		}
		
		if(ctx.negativeCondition() != null) {
			NegationDescriptor descriptor = buildNegationDescriptor(ctx.negativeCondition());
			this.resultCondition = descriptor;
		}
	}
	
	/**
	 * Builds a SingleConditionDescriptor for a single condition,
	 * e.g. definedEx(FLAG)
	 * @param singleConditionCtx
	 * @return SingleConditionDescriptor a descriptor for a single condition
	 */
	private SingleConditionDescriptor buildSingleConditionDescriptor(SingleConditionContext singleConditionCtx) {
		SingleConditionDescriptor singleConditionDescriptor = this.store.create(SingleConditionDescriptor.class);
		singleConditionDescriptor.setMacroName(singleConditionCtx.MACRONAME().toString());
		return singleConditionDescriptor;
	}
	
	/**
	 * Build Negation node from parsed negated expression
	 * @param negativeConditionCtx
	 * @return NegationDescriptor containing negated subelements
	 */
	private NegationDescriptor buildNegationDescriptor(NegativeConditionContext negativeConditionCtx) {
		NegationDescriptor negationDescriptor = store.create(NegationDescriptor.class);
		SingleConditionDescriptor singleConditionDescriptor = null;
		if(negativeConditionCtx.singleCondition() != null) {
			singleConditionDescriptor = buildSingleConditionDescriptor(negativeConditionCtx.singleCondition());
		}
		negationDescriptor.setCondition(singleConditionDescriptor);
		return negationDescriptor;
	}

	public ConditionDescriptor getResultCondition() {
		return resultCondition;
	}
}
