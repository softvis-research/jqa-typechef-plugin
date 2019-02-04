grammar Conditions;

completeCondition: singleCondition;








negativeCondition : '!'singleCondition;
singleCondition : 'definedEx('MACRONAME')';

MACRONAME : [a-zA-Z0-9]+ ;