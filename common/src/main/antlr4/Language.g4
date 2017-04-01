grammar Language;

@header {
package ru.ifmo.ctddev.khovanskiy.compilers.parser;
}

compilationUnit
    :  compoundStatement? EOF
    ;

compoundStatement
    :   singleStatement?
    |   compoundStatement singleStatement
    ;

declaration
    :   functionDefinition ';'?
    ;

functionDefinition
    :   'fun' Identifier '(' argumentDefinitionList? ')' 'begin' compoundStatement 'end'
    ;

variableDefinition
    :   Identifier
    ;

argumentDefinitionList
    :   variableDefinition
    |   (variableDefinition ',')* variableDefinition
    ;

singleStatement
    :   declaration
    |   expressionStatement ';'?
    |   selectionStatement ';'?
    |   jumpStatement ';'?
    |   iterationStatement ';'?
    ;

statement
    :   singleStatement?
    |   compoundStatement
    ;

expressionStatement
    :   expression
    ;

selectionStatement
    :   'if' expression 'then' statement ('elif' expression 'then' statement)* ('else' statement)?
    ;

jumpStatement
    :   'goto' Identifier
    |   'continue'
    |   'break'
    |   'return' expression?
    ;

iterationStatement
    :   'while' expression 'do' compoundStatement 'od'                          #while
    |   'repeat' statement 'until' expression                                   #repeat
    |   'for' expression? ',' expression? ',' expression? 'do' compoundStatement 'od'   #for
    ;

//----------------------------------------------------------------------------------------------------------------------
// Expression
//----------------------------------------------------------------------------------------------------------------------
primaryExpression
    :   Identifier
    |   constant
    ;

functionCall
    :   Identifier '(' argumentExpressionList? ')'
    ;

arrayAccess
    : Identifier ('[' expression ']')+
    ;

postfixExpression
    :   primaryExpression
    |   arrayAccess
    |   functionCall
    |   postfixExpression '++'
    |   postfixExpression '--'
    ;

argumentExpressionList
    :   assignmentExpression
    |   (assignmentExpression ',')* assignmentExpression
    ;

unaryExpression
    :   postfixExpression
    |   '++' unaryExpression
    |   '--' unaryExpression
    |   unaryOperator castExpression
    ;

unaryOperator
    :   '&' | '*' | '+' | '-' | '~' | '!'
    ;

castExpression
    :   unaryExpression
    ;

multiplicativeExpression
    :   castExpression
    |   multiplicativeExpression '*' castExpression
    |   multiplicativeExpression '/' castExpression
    |   multiplicativeExpression '%' castExpression
    ;

additiveExpression
    :   multiplicativeExpression
    |   additiveExpression '+' multiplicativeExpression
    |   additiveExpression '-' multiplicativeExpression
    ;

shiftExpression
    :   additiveExpression
    |   shiftExpression '<<' additiveExpression
    |   shiftExpression '>>' additiveExpression
    ;

relationalExpression
    :   shiftExpression
    |   relationalExpression '<' shiftExpression
    |   relationalExpression '>' shiftExpression
    |   relationalExpression '<=' shiftExpression
    |   relationalExpression '>=' shiftExpression
    ;

equalityExpression
    :   relationalExpression
    |   equalityExpression '==' relationalExpression
    |   equalityExpression '!=' relationalExpression
    ;

andExpression
    :   equalityExpression
    |   andExpression '&' equalityExpression
    ;

exclusiveOrExpression
    :   andExpression
    |   exclusiveOrExpression '^' andExpression
    ;

inclusiveOrExpression
    :   exclusiveOrExpression
    |   inclusiveOrExpression '|' exclusiveOrExpression
    ;

logicalAndExpression
    :   inclusiveOrExpression
    |   logicalAndExpression '&&' inclusiveOrExpression
    ;

logicalOrExpression
    :   logicalAndExpression
    |   logicalOrExpression '||' logicalAndExpression
    ;

conditionalExpression
    :   logicalOrExpression ('?' expression ':' conditionalExpression)?
    ;

assignmentExpression
    :   conditionalExpression
    |   unaryExpression assignmentOperator assignmentExpression
    ;

assignmentOperator
    :   ':='
    ;

expression
    :   assignmentExpression
    |   (assignmentExpression ',')* assignmentExpression
    ;

//----------------------------------------------------------------------------------------------------------------------
Identifier
    :   IdentifierNondigit
        (IdentifierNondigit | Digit)*
    ;

fragment
IdentifierNondigit
    :   Nondigit
    ;

fragment
Nondigit
    :   [a-zA-Z_]
    ;

fragment
Digit
    :   [0-9]
    ;

//----------------------------------------------------------------------------------------------------------------------
StringLiteral
    :   EncodingPrefix? '"' SCharSequence? '"'
    ;
fragment
EncodingPrefix
    :   'u8'
    |   'u'
    |   'U'
    |   'L'
    ;
fragment
SCharSequence
    :   SChar+
    ;
fragment
SChar
    :   ~["\\\r\n]
    |   EscapeSequence
    |   '\\\n'   // Added line
    |   '\\\r\n' // Added line
    ;

fragment
EscapeSequence
    :   SimpleEscapeSequence
    ;

fragment
SimpleEscapeSequence
    :   '\\' ['"?abfnrtv\\]
    ;

//----------------------------------------------------------------------------------------------------------------------
constant
    :   IntegerConstant
    |   arrayConstant
    ;

IntegerConstant
    :   Digit+
    ;

arrayConstant
    :   '[' constantList? ']'
    ;

constantList
    :   constant
    |   (constant ',')* constant
    ;

//----------------------------------------------------------------------------------------------------------------------
Whitespace
    :   [ \t]+
        -> skip
    ;

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> skip
    ;

BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;

LineComment
    :   '//' ~[\r\n]*
        -> skip
    ;
