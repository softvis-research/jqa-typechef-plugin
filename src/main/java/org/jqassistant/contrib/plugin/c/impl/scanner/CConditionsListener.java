package org.jqassistant.contrib.plugin.c.impl.scanner;

import org.jqassistant.contrib.plugin.c.api.ConditionsBaseListener;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.AndExpressionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.CompleteConditionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.ExpressionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.NegativeConditionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.OrExpressionContext;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser.SingleConditionContext;
import org.jqassistant.contrib.plugin.c.api.model.AndDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.NegationDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.OrDescriptor;
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
		//The uppermost expression can be a SingleCondition, a NegativeCondition or an AndCondition
		if(ctx.singleCondition() != null) {
			SingleConditionDescriptor descriptor = buildSingleConditionDescriptor(ctx.singleCondition());
			this.resultCondition = descriptor;
		}
		
		if(ctx.negativeCondition() != null) {
			NegationDescriptor descriptor = buildNegationDescriptor(ctx.negativeCondition());
			this.resultCondition = descriptor;
		}
		
		if(ctx.andExpression() != null) {
			AndDescriptor descriptor = buildAndDescriptor(ctx.andExpression());
			this.resultCondition = descriptor;
		}
		
		if(ctx.orExpression() != null) {
			OrDescriptor descriptor = buildOrDescriptor(ctx.orExpression());
			this.resultCondition = descriptor;
		}
	}
	
	/**
	 * Checks which type of expression the expression is and treats it accordingly. 
	 * @param expression ExpressionContext
	 * @return Descriptor of a type that extends ConditionDescriptor,
	 * it can be AndDescriptor, SingleConditionDescriptor and NegationDescriptor 
	 */
	private ConditionDescriptor handleExpression(ExpressionContext expression) {
		if(expression.andExpression() != null) {
			AndDescriptor andDescriptor = buildAndDescriptor(expression.andExpression());
			return andDescriptor;
		} else if(expression.orExpression() != null) {
			OrDescriptor orDescriptor = buildOrDescriptor(expression.orExpression());
			return orDescriptor;
		} else if(expression.singleCondition() != null) {
			SingleConditionDescriptor singleConditionDescriptor = buildSingleConditionDescriptor(expression.singleCondition());
			return singleConditionDescriptor;
		} else if(expression.negativeCondition() != null) {
			NegationDescriptor negationDescriptor = buildNegationDescriptor(expression.negativeCondition());
			return negationDescriptor;
		} else {
			return null;
		}
	}
	
	/**
	 * Given an AndExpressionContext build an AndDescriptor and treat children.
	 * @param andExpression AndExpressionContext
	 * @return AndDescriptor with children
	 */
	private AndDescriptor buildAndDescriptor(AndExpressionContext andExpression) {
		AndDescriptor andDescriptor = this.store.create(AndDescriptor.class);
		if(andExpression.expression() != null) {
			for(ExpressionContext expression : andExpression.expression()) {
				andDescriptor.getConnectedConditions().add(handleExpression(expression));
			}
		}
		return andDescriptor;
	}

	private OrDescriptor buildOrDescriptor(OrExpressionContext orExpression) {
		OrDescriptor orDescriptor = this.store.create(OrDescriptor.class);
		if(orExpression.expression() != null) {
			for(ExpressionContext expression : orExpression.expression()) {
				orDescriptor.getConnectedConditions().add(handleExpression(expression));
			}
		}
		return orDescriptor;
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
