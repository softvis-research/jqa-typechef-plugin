grammar Conditions;

completeCondition: singleCondition || negativeCondition;








negativeCondition : '!'singleCondition;
singleCondition : 'definedEx('MACRONAME')';

MACRONAME : [a-zA-Z0-9]+ ;