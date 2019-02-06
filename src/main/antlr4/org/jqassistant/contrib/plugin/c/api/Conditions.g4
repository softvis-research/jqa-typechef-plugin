grammar Conditions;

completeCondition : singleCondition | negativeCondition | andExpression;

andExpression : '(' expression AND expression ')';

expression : singleCondition | negativeCondition | andExpression;

negativeCondition : '!'singleCondition;

singleCondition : DEFINEDEX MACRONAME ')' | DEFINED MACRONAME ')';

DEFINED : 'defined(';
DEFINEDEX : 'definedEx(';
AND : '&amp;&amp;' | '&&';
MACRONAME : [a-zA-Z0-9]+ ;
WS : [ \r\t\n]+ -> skip;