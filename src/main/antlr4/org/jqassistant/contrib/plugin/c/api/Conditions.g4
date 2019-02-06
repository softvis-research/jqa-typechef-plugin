grammar Conditions;

completeCondition : singleCondition | negativeCondition | andExpression | orExpression;

andExpression : '(' expression (AND expression)+ ')';

orExpression : '(' expression (OR expression)+ ')';

expression : singleCondition | negativeCondition | andExpression | orExpression;

negativeCondition : '!'singleCondition;

singleCondition : DEFINEDEX MACRONAME ')' | DEFINED MACRONAME ')';

DEFINED : 'defined(';
DEFINEDEX : 'definedEx(';
AND : '&amp;&amp;' | '&&';
OR : '||';
MACRONAME : [a-zA-Z0-9_]+ ;
WS : [ \r\t\n]+ -> skip;