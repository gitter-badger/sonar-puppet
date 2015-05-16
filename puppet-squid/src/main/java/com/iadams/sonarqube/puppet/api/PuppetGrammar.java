/**
 * Sonar Puppet Plugin
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Iain Adams
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.iadams.sonarqube.puppet.api;

import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerfulGrammarBuilder;

import static com.iadams.sonarqube.puppet.api.PuppetKeyword.*;
import static com.iadams.sonarqube.puppet.api.PuppetPunctuator.*;
import static com.iadams.sonarqube.puppet.api.PuppetTokenType.*;
import static com.iadams.sonarqube.puppet.api.PuppetTokenType.NEWLINE;
import static com.iadams.sonarqube.puppet.api.PuppetTokenType.VARIABLE;
import static com.sonar.sslr.api.GenericTokenType.*;
import static com.sonar.sslr.api.GenericTokenType.LITERAL;
import static com.sonar.sslr.api.GenericTokenType.EOF;

/**
 * Created by iwarapter
 */
public enum PuppetGrammar  implements GrammarRuleKey {

    CONDITION,
    OPERAND,
    ATTRIBUTE,

    //EXPRESSIONS
    EXPRESSION,
    ARITH_EXP,
    BOOL_EXP,
    COMP_EXP,
    MATCH_EXP,
    NOT_EXP,
    MINUS_EXP,
    BRACKET_EXP,

    ARITH_OP,
    ASSIGNMENT_EXPRESSION,
    BOOL_OPERATOR,
    COMP_OPERATOR,
    A_OPER,
    M_OPER,
    MATCH_OPERATOR,

    SHIFT_OPER,

    RIGHT_VALUE,
    LITERAL_LIST,
    ARGUMENT_EXPRESSION_LIST,
    NAME,
    NAMESPACE_SEP,
    QUALIFIED_IDENTIFIER,

    //SIMPLE STATEMENTS
    SIMPLE_STMT,
    STATEMENT,
    RESOURCE,
    RESOURCE_NAME,
    DEFINE_STMT,
    DEFINE_NAME,
    VIRTUALRESOURCE,
    COLLECTION,
    FUNC_CALL,
    PARAM_LIST,
    PARAMETER,
    NODE_STMT,
    NODE_NAME,
    INCLUDE_STMT,

    //CONDITIONAL STATEMENTS
    CONDITION_CLAUSE,

    // Compound statements
    COMPOUND_STMT,
    CLASSDEF,
    CLASSNAME,
    IF_STMT,
    UNLESS_STMT,
    CASE_STMT,
    CASES,
    SELECTOR_STMT,
    SELECTOR_CASE,
    CONTROL_VAR,

    // Top-level components

    FILE_INPUT;

    public static LexerfulGrammarBuilder create() {
        LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();

        b.rule(FILE_INPUT).is(b.zeroOrMore(b.firstOf(NEWLINE, STATEMENT)), EOF);
        b.rule(STATEMENT).is(b.firstOf(EXPRESSION, SIMPLE_STMT, COMPOUND_STMT, RESOURCE));

        grammar(b);
        conditionalStatements(b);
        compoundStatements(b);
        simpleStatements(b);
        expressions(b);

        b.setRootRule(FILE_INPUT);
        b.buildWithMemoizationOfMatchesForAllRules();

        return b;
    }

    public static void grammar(LexerfulGrammarBuilder b) {

        b.rule(NAME).is(IDENTIFIER);

        b.rule(FUNC_CALL).is(
                NAME,
                LPAREN,
                ARGUMENT_EXPRESSION_LIST,
                RPAREN);

        b.rule(ARGUMENT_EXPRESSION_LIST).is(EXPRESSION, b.zeroOrMore(COMMA, EXPRESSION));

        b.rule(ATTRIBUTE).is(IDENTIFIER,
                FARROW,
                b.firstOf(SELECTOR_STMT, EXPRESSION, LITERAL_LIST, IDENTIFIER),
                b.optional(COMMA));

        b.rule(RESOURCE).is(IDENTIFIER,
                LBRACE,
                RESOURCE_NAME,
                COLON,
                b.oneOrMore(ATTRIBUTE),
                RBRACE);
        b.rule(RESOURCE_NAME).is(b.firstOf(LITERAL, IDENTIFIER));
    }

    /**
     * Conditional Statements
     * https://docs.puppetlabs.com/puppet/3.7/reference/lang_conditional.html
     * @param b
     */
    public static void conditionalStatements(LexerfulGrammarBuilder b){
/*
        //b.rule(CONDITIONAL_STMT).is(b.firstOf(
        //        IF_STMT,
        //        UNLESS_STMT));
        b.rule(STATEMENT).is(IF_STMT);
        b.rule(STATEMENT).is(b.firstOf(
                EXPRESSION_STATEMENT,
                COMPOUND_STATEMENT,
                RETURN_STATEMENT,
                CONTINUE_STATEMENT,
                BREAK_STATEMENT,
                IF_STMT,
                WHILE_STATEMENT,
                NO_COMPLEXITY_STATEMENT));
        b.rule(CONDITION_CLAUSE).is(LPAREN, EXPRESSION, RPAREN);
        b.rule(IF_STMT).is(IF, CONDITION_CLAUSE, STATEMENT);
        */
    }

    /**
     * Simple Statements
     *
     * @param b
     */
    public static void simpleStatements(LexerfulGrammarBuilder b) {
        b.rule(SIMPLE_STMT).is(b.firstOf(
                DEFINE_STMT,
                NODE_STMT,
                INCLUDE_STMT));

        b.rule(DEFINE_STMT).is(DEFINE,
                DEFINE_NAME,
                b.optional(PARAM_LIST),
                LBRACE,
                b.zeroOrMore(STATEMENT),
                RBRACE);

        b.rule(DEFINE_NAME).is(QUALIFIED_IDENTIFIER);

        b.rule(PARAM_LIST).is(LPAREN,
                b.zeroOrMore(PARAMETER, b.optional(COMMA)),
                RPAREN);

        b.rule(PARAMETER).is(VARIABLE,
                b.optional(EQUALS, OPERAND));

        b.rule(NODE_STMT).is(NODE,
                NODE_NAME,
                b.zeroOrMore(COMMA, b.firstOf(LITERAL, IDENTIFIER)),
                b.optional(INHERITS, LITERAL),
                LBRACE,
                b.optional(b.zeroOrMore(STATEMENT)),
                RBRACE);

        b.rule(NODE_NAME).is(b.firstOf(LITERAL, IDENTIFIER));

        b.rule(INCLUDE_STMT).is("include", b.firstOf(LITERAL, QUALIFIED_IDENTIFIER));
    }

    /**
     * Compound Statements
     *
     * @param b
     */
    public static void compoundStatements(LexerfulGrammarBuilder b) {
        b.rule(COMPOUND_STMT).is(b.firstOf(CLASSDEF,
                IF_STMT,
                CASE_STMT));

        b.rule(CLASSDEF).is(
                CLASS,
                CLASSNAME,
                b.optional(PARAM_LIST),
                b.optional(ASSIGNMENT_EXPRESSION),
                b.optional(INHERITS, CLASSNAME),
                LBRACE,
                b.zeroOrMore(STATEMENT),
                RBRACE);

        b.rule(CLASSNAME).is(QUALIFIED_IDENTIFIER);

        b.rule(IF_STMT).is(IF,
                CONDITION,
                LBRACE,
                b.zeroOrMore(STATEMENT),
                RBRACE,
                b.optional(ELSIF, CONDITION, LBRACE, b.zeroOrMore(STATEMENT), RBRACE),
                b.optional(ELSE, LBRACE,b.zeroOrMore(STATEMENT),RBRACE));

        b.rule(CASE_STMT).is(CASE, b.firstOf(VARIABLE, EXPRESSION), LBRACE,
                b.zeroOrMore(CASES),
                RBRACE);
        b.rule(CASES).is(b.firstOf(NAME, LITERAL, VARIABLE, FUNC_CALL), COLON, LBRACE, b.zeroOrMore(STATEMENT), RBRACE);

        b.rule(SELECTOR_STMT).is(
                CONTROL_VAR,
                QMARK,
                LBRACE,
                b.oneOrMore(SELECTOR_CASE),
                b.optional(
                        DEFAULT,
                        FARROW,
                        b.firstOf(NAME, LITERAL, VARIABLE, FUNC_CALL, SELECTOR_STMT),
                        b.optional(COMMA)),
                RBRACE);

        b.rule(SELECTOR_CASE).is(
                b.firstOf(NAME, LITERAL, VARIABLE, FUNC_CALL),
                FARROW,
                b.firstOf(NAME, LITERAL, VARIABLE, FUNC_CALL, SELECTOR_STMT),
                b.optional(COMMA));

        b.rule(CONTROL_VAR).is(b.firstOf(VARIABLE, FUNC_CALL));
    }

    /**
     * Expressions
     * https://docs.puppetlabs.com/puppet/latest/reference/lang_expressions.html
     * @param b
     */
    public static void expressions(LexerfulGrammarBuilder b){

        b.rule(CONDITION).is(b.firstOf(COMP_EXP, FUNC_CALL));

        b.rule(OPERAND).is(b.firstOf(
                LITERAL_LIST,
                VARIABLE,
                TRUE,
                FALSE));

        /*<exp> ::=  <exp> <arithop> <exp>
                | <exp> <boolop> <exp>
                | <exp> <compop> <exp>
                | <exp> <matchop> <regex>
                | ! <exp>
                | - <exp>
                | "(" <exp> ")"
                | <rightvalue>*/
        b.rule(EXPRESSION).is(b.firstOf(
                ASSIGNMENT_EXPRESSION,
                ARITH_EXP,
                BOOL_EXP,
                COMP_EXP,
                MATCH_EXP,
                NOT_EXP,
                MINUS_EXP,
                BRACKET_EXP,
                RIGHT_VALUE));

        b.rule(ARITH_EXP).is(OPERAND, ARITH_OP, OPERAND);
        b.rule(BOOL_EXP).is(OPERAND, BOOL_OPERATOR, OPERAND);
        b.rule(COMP_EXP).is(OPERAND, COMP_OPERATOR, OPERAND);
        b.rule(MATCH_EXP).is(OPERAND, MATCH_OPERATOR, OPERAND);
        b.rule(NOT_EXP).is(NOT, EXPRESSION);
        b.rule(MINUS_EXP).is(MINUS, OPERAND);
        b.rule(BRACKET_EXP).is(LPAREN, EXPRESSION, RPAREN);
        b.rule(ASSIGNMENT_EXPRESSION).is(VARIABLE, EQUALS ,LITERAL_LIST);

        //<arithop> ::= "+" | "-" | "/" | "*" | "<<" | ">>"
        b.rule(ARITH_OP).is(b.firstOf(
                SHIFT_OPER,
                A_OPER,
                M_OPER));

        b.rule(SHIFT_OPER).is(b.firstOf(
                RSHIFT,
                LSHIFT));

        b.rule(A_OPER).is(b.firstOf(
                PLUS,
                MINUS));

        b.rule(M_OPER).is(b.firstOf(
                MUL,
                DIV));

        //<boolop>  ::= "and" | "or"
        b.rule(BOOL_OPERATOR).is(b.firstOf(
                AND,
                OR,
                NOT));

        //<compop>  ::= "==" | "!=" | ">" | ">=" | "<=" | "<"
        b.rule(COMP_OPERATOR).is(b.firstOf(
                ISEQUAL,
                NOTEQUAL,
                GREATERTHAN,
                GREATEREQUAL,
                LESSEQUAL,
                LESSTHAN));

        //<matchop>  ::= "=~" | "!~"
        b.rule(MATCH_OPERATOR).is(b.firstOf(
                MATCH,
                NOMATCH));

        //<rightvalue> ::= <variable> | <function-call> | <literals>
        b.rule(RIGHT_VALUE).is(b.firstOf(
                FUNC_CALL,
                VARIABLE,
                LITERAL_LIST));

        //<literals> ::= <float> | <integer> | <hex-integer> | <octal-integer> | <quoted-string>
        b.rule(LITERAL_LIST).is(b.firstOf(
                FLOAT,
                INTEGER,
                HEX_INTEGER,
                OCTAL_INTEGER,
                LITERAL));

        //<regex> ::= '/regex/'



        b.rule(NAMESPACE_SEP).is(COLON, COLON);
        b.rule(QUALIFIED_IDENTIFIER).is(IDENTIFIER, b.zeroOrMore(NAMESPACE_SEP, IDENTIFIER));
    }
}
