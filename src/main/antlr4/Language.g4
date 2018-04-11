grammar Language;

@header {
package ru.ifmo.ctddev.khovanskiy.compilers.ast.parser;

import ru.ifmo.ctddev.khovanskiy.compilers.ast.AST;
import java.util.List;
import java.util.ArrayList;
}

compilationUnit returns [AST.CompilationUnit ast]
    :  compoundStatement? EOF
    {
        $ast = new AST.CompilationUnit($compoundStatement.ast);
    }
    ;

compoundStatement returns [AST.CompoundStatement ast]
    @init {
        List<AST.SingleStatement> statements = new ArrayList<>();
    }
    @after {
       $ast = new AST.CompoundStatement(statements);
    }
    :   (singleStatement {statements.add($singleStatement.ast);})+
    ;

declaration returns [AST.SingleStatement ast]
    :   functionDefinition { $ast = $functionDefinition.ast; } ';'?
    ;

functionDefinition returns [AST.FunctionDefinition ast]
    :   'fun' name=Identifier '(' argumentDefinitionList? ')' 'begin' compoundStatement 'end'
    {
        $ast = new AST.FunctionDefinition($name.text, $argumentDefinitionList.ctx == null ? null : $argumentDefinitionList.list, $compoundStatement.ast);
    }
    ;

variableDefinition returns [AST.VariableDefinition ast]
    :   name=Identifier { $ast = new AST.VariableDefinition($name.text); }
    ;

argumentDefinitionList returns [List<AST.VariableDefinition> list]
    @init {
        List<AST.VariableDefinition> list = new ArrayList<>();
    }
    @after {
        $list = list;
    }
    :   variableDefinition { list.add($variableDefinition.ast); } (',' variableDefinition { list.add($variableDefinition.ast); })*
    ;

singleStatement returns [AST.SingleStatement ast]
    :   declaration { $ast = $declaration.ast; }
    |   assignmentStatement { $ast = $assignmentStatement.ast; } ';' ?
    |   expressionStatement { $ast = $expressionStatement.ast; } ';'?
    |   selectionStatement { $ast = $selectionStatement.ast; } ';'?
    |   jumpStatement {$ast = $jumpStatement.ast; } ';'?
    |   iterationStatement {$ast = $iterationStatement.ast; } ';'?
    ;

expressionStatement returns [AST.ExpressionStatement ast]
    :   functionCall { $ast = new AST.ExpressionStatement($functionCall.ast); }
    ;

selectionStatement returns [AST.SelectionStatement ast]
    @init
    {
        List<AST.IfCase> cases = new ArrayList<>();
    }
    @after
    {
        $ast = new AST.IfStatement(cases);
    }
    :
    'if' expression 'then' compoundStatement { cases.add(new AST.IfCase($expression.ast, $compoundStatement.ast)); }
    ('elif' expression 'then' compoundStatement { cases.add(new AST.IfCase($expression.ast, $compoundStatement.ast)); })*
    ('else' compoundStatement { cases.add(new AST.IfCase(null, $compoundStatement.ast)); })? 'fi'
    ;

jumpStatement returns [AST.JumpStatement ast]
    :   'goto' label=Identifier
    {
        $ast = new AST.GotoStatement($label.text);
    }
    |   name=Identifier ':'
    {
        $ast = new AST.LabelStatement($name.text);
    }
    |   'continue'
    {
        $ast = new AST.ContinueStatement();
    }
    |   'break'
    {
        $ast = new AST.BreakStatement();
    }
    |   'return' expression?
    {
        $ast = new AST.ReturnStatement($expression.ctx == null ? null : $expression.ast);
    }
    |   'skip'
    {
        $ast = new AST.SkipStatement();
    }
    ;

iterationStatement returns [AST.IterationStatement ast]
    :   'while' expression 'do' compoundStatement 'od'
    { $ast = new AST.WhileStatement($expression.ast, $compoundStatement.ast); }       #while
    |   'repeat' compoundStatement 'until' expression
    { $ast = new AST.RepeatStatement($compoundStatement.ast, $expression.ast); }       #repeat
    |   'for' (init=assignmentStatement | 'skip')? ',' condition=expression? ',' (loop=assignmentStatement | 'skip')? 'do' compoundStatement 'od'
    { $ast = new AST.ForStatement($init.ctx == null ? null : $init.ast, $condition.ast, $loop.ctx == null ? null : $loop.ast, $compoundStatement.ast); }  #for
    ;

//----------------------------------------------------------------------------------------------------------------------
// Expression
//----------------------------------------------------------------------------------------------------------------------
argumentList returns [List<AST.Expression> list]
    @init {
        List<AST.Expression> list = new ArrayList<>();
    }
    @after {
        $list = list;
    }
    : expression { list.add($expression.ast); } (',' expression { list.add($expression.ast); })*
    ;

functionCall returns [AST.FunctionCall ast]
    :   name=Identifier '(' argumentList? ')'
    {
        $ast = new AST.FunctionCall($name.text, $argumentList.ctx == null ? null : $argumentList.list);
    }
    ;

arrayAccess returns [AST.ArrayAccessExpression ast]
    :   variableAccess '[' expression ']'
    {
        $ast = new AST.ArrayAccessExpression($variableAccess.ast, $expression.ast);
    }
    |   pointer=arrayAccess '[' expression ']'
    {
        $ast = new AST.ArrayAccessExpression($pointer.ast, $expression.ast);
    }
    ;

variableAccess returns [AST.VariableAccessExpression ast]
    :   name=Identifier
    {
        $ast = new AST.VariableAccessExpression($name.text);
    }
    ;

memoryAccess returns [AST.MemoryAccessExpression ast]
	:	variableAccess { $ast = $variableAccess.ast; }
	|	arrayAccess { $ast = $arrayAccess.ast; }
	;

assignmentStatement returns [AST.AssignmentStatement ast]
    :   memoryAccess ':=' expression
    {
        $ast = new AST.AssignmentStatement($memoryAccess.ast, $expression.ast);
    }
    ;

arrayCreation returns [AST.ArrayCreationExpression ast]
    : '[' argumentList? ']'
    {
        $ast = new AST.ArrayCreationExpression($argumentList.list);
    }
    ;

expression returns [AST.Expression ast]
    :   functionCall { $ast = $functionCall.ast; }
    |   arrayCreation { $ast = $arrayCreation.ast; }
    |   memoryAccess { $ast = $memoryAccess.ast; }
    |   '(' expression ')' { $ast = $expression.ast; }
    |   literal { $ast = $literal.ast; }
//    |   memoryAccess postfixOperator=('++' | '--')
//    |   prefixOperator=('++' | '--') memoryAccess
    |   unaryOperator=('+' | '-' | '~' | '!') expression
    |   left=expression binaryOperator=('*' | '/' | '%') right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator=('+' | '-') right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator=('>>' | '<<') right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator=('>' | '<' | '<=' | '>=') right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator=('==' | '!=') right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator='&' right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator='^' right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator='|' right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator='&&' right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    |   left=expression binaryOperator=('||' | '!!') right=expression
    {
        $ast = new AST.BinaryExpression($binaryOperator.text, $left.ast, $right.ast);
    }
    ;

//----------------------------------------------------------------------------------------------------------------------
literal returns [AST.Literal ast]
	:   IntegerLiteral { $ast = new AST.IntegerLiteral(Integer.parseInt($IntegerLiteral.text)); }
	|   CharacterLiteral { $ast = new AST.CharacterLiteral($CharacterLiteral.text); }
	|	StringLiteral { $ast = new AST.StringLiteral($StringLiteral.text); }
	|	NullLiteral { $ast = new AST.NullLiteral(); }
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
