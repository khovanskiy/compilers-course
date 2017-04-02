grammar Language;

@header {
package ru.ifmo.ctddev.khovanskiy.compilers.parser;
}

compilationUnit
    :  compoundStatement? EOF
    ;

compoundStatement
    :   singleStatement singleStatement*
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
    :   variableDefinition (',' variableDefinition)*
    ;

singleStatement
    :   declaration
    |   expressionStatement ';'?
    |   selectionStatement ';'?
    |   jumpStatement ';'?
    |   iterationStatement ';'?
    ;

expressionStatement
    :   expression
    ;

selectionStatement
    :   'if' expression 'then' compoundStatement ('elif' expression 'then' compoundStatement)* ('else' compoundStatement)? 'fi' #if
    ;

jumpStatement
    :   'goto' Identifier
    |   'continue'
    |   'break'
    |   'return' expression?
    ;

iterationStatement
    :   'while' expression 'do' compoundStatement 'od'                          #while
    |   'repeat' compoundStatement 'until' expression                                   #repeat
    |   'for' init=expression? ',' condition=expression? ',' loop=expression? 'do' compoundStatement 'od'   #for
    ;

//----------------------------------------------------------------------------------------------------------------------
// Expression
//----------------------------------------------------------------------------------------------------------------------
functionCall
    :   name=Identifier '(' expression? (',' expression)* ')'
    ;

arrayAccess
    :   name=Identifier ('[' expression ']')+
    ;

variableAccess
    :   Identifier
    ;

memoryAccess
	:	variableAccess
	|	arrayAccess
	;

assignment
    :   memoryAccess assignmentOperator=':=' expression
    ;

skip
    :   'skip'
    ;

expression
    :   skip
    |   functionCall
    |   memoryAccess
    |   '(' expression ')'
    |   literal
    |   '[' constantList? ']'
//    |   memoryAccess postfixOperator=('++' | '--')
//    |   prefixOperator=('++' | '--') memoryAccess
    |   unaryOperator=('+' | '-' | '~' | '!') expression
    |   left=expression binaryOperator=('*' | '/' | '%') right=expression
    |   left=expression binaryOperator=('+' | '-') right=expression
    |   left=expression binaryOperator=('>>' | '<<') right=expression
    |   left=expression binaryOperator=('>' | '<' | '<=' | '>=') right=expression
    |   left=expression binaryOperator=('==' | '!=') right=expression
    |   left=expression binaryOperator='&' right=expression
    |   left=expression binaryOperator='^' right=expression
    |   left=expression binaryOperator='|' right=expression
    |   left=expression binaryOperator='&&' right=expression
    |   left=expression binaryOperator='||' right=expression
    |   assignment
    ;

constantList
    :   literal (',' literal)*
    ;

//----------------------------------------------------------------------------------------------------------------------
literal
	:	IntegerLiteral
	|   CharacterLiteral
	|	StringLiteral
	|	NullLiteral
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
CharacterLiteral
	:	'\'' SingleCharacter '\''
	;

fragment
SingleCharacter
	:	~['\\]
	;

//----------------------------------------------------------------------------------------------------------------------
StringLiteral
	:	'"' StringCharacters? '"'
	;

fragment
StringCharacters
	:	StringCharacter+
	;

fragment
StringCharacter
	:	~["\\]
	;

//----------------------------------------------------------------------------------------------------------------------
NullLiteral
    :   '{}'
    |   'null'
    ;

IntegerLiteral
    :   Digit+
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
