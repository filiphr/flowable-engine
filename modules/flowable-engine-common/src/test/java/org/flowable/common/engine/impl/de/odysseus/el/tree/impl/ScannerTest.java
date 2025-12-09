/*
 * Copyright 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.engine.impl.de.odysseus.el.tree.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.AND;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.COLON;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.COMMA;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.DIV;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.DOT;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.EMPTY;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.END_EVAL;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.EOF;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.EQ;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.FALSE;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.FLOAT;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.GE;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.GT;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.IDENTIFIER;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.INSTANCEOF;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.INTEGER;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.LBRACK;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.LE;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.LPAREN;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.LT;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.MINUS;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.MOD;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.MUL;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.NE;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.NOT;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.NULL;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.OR;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.PLUS;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.QUESTION;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.RBRACK;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.RPAREN;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.START_EVAL_DEFERRED;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.START_EVAL_DYNAMIC;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.STRING;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.TEXT;
import static org.flowable.common.engine.impl.de.odysseus.el.tree.impl.Scanner.Symbol.TRUE;

import java.util.ArrayList;
import java.util.List;

import org.flowable.common.engine.impl.de.odysseus.el.TestCase;
import org.junit.jupiter.api.Test;

class ScannerTest extends TestCase {

    Scanner.Symbol[] symbols(String expression) throws Scanner.ScanException {
        List<Scanner.Symbol> list = new ArrayList<>();
        Scanner scanner = new Scanner(expression);
        Scanner.Token token = scanner.next();
        while (token.getSymbol() != EOF) {
            list.add(token.getSymbol());
            token = scanner.next();
        }
        return list.toArray(new Scanner.Symbol[0]);
    }

    @Test
    void testInteger() throws Scanner.ScanException {
        Scanner.Symbol[] expected = { START_EVAL_DYNAMIC, INTEGER, END_EVAL };

        assertThat(symbols("${0}")).containsExactly(expected);
        assertThat(symbols("${1}")).containsExactly(expected);
        assertThat(symbols("${01234567890}")).containsExactly(expected);
    }

    @Test
    void testFloat() throws Scanner.ScanException {
        Scanner.Symbol[] expected = { START_EVAL_DYNAMIC, FLOAT, END_EVAL };

        assertThat(symbols("${0.}")).containsExactly(expected);
        assertThat(symbols("${023456789.}")).containsExactly(expected);
        assertThat(symbols("${.0}")).containsExactly(expected);
        assertThat(symbols("${.023456789}")).containsExactly(expected);
        assertThat(symbols("${0.0}")).containsExactly(expected);

        assertThat(symbols("${0e0}")).containsExactly(expected);
        assertThat(symbols("${0E0}")).containsExactly(expected);
        assertThat(symbols("${0e+0}")).containsExactly(expected);
        assertThat(symbols("${0E+0}")).containsExactly(expected);
        assertThat(symbols("${0e+0}")).containsExactly(expected);
        assertThat(symbols("${0E+0}")).containsExactly(expected);

        assertThat(symbols("${.0e0}")).containsExactly(expected);
        assertThat(symbols("${.0E0}")).containsExactly(expected);
        assertThat(symbols("${.0e+0}")).containsExactly(expected);
        assertThat(symbols("${.0E+0}")).containsExactly(expected);
        assertThat(symbols("${.0e-0}")).containsExactly(expected);
        assertThat(symbols("${.0E-0}")).containsExactly(expected);

        assertThat(symbols("${0.e0}")).containsExactly(expected);
        assertThat(symbols("${0.E0}")).containsExactly(expected);
        assertThat(symbols("${0.e+0}")).containsExactly(expected);
        assertThat(symbols("${0.E+0}")).containsExactly(expected);
        assertThat(symbols("${0.e-0}")).containsExactly(expected);
        assertThat(symbols("${0.E-0}")).containsExactly(expected);

        assertThat(symbols("${0.0e0}")).containsExactly(expected);
        assertThat(symbols("${0.0E0}")).containsExactly(expected);
        assertThat(symbols("${0.0e+0}")).containsExactly(expected);
        assertThat(symbols("${0.0E+0}")).containsExactly(expected);
        assertThat(symbols("${0.0e-0}")).containsExactly(expected);
        assertThat(symbols("${0.0E-0}")).containsExactly(expected);
    }

    @Test
    void testString() throws Scanner.ScanException {
        Scanner.Symbol[] expected = { START_EVAL_DYNAMIC, STRING, END_EVAL };

        assertThat(symbols("${'foo'}")).containsExactly(expected);
        assertThat(symbols("${'f\"o'}")).containsExactly(expected);
        assertThat(symbols("${'f\\'o'}")).containsExactly(expected);
        assertThat(symbols("${'f\\\"o'}")).containsExactly(expected);
        assertThatThrownBy(() -> symbols("${'f\\oo'}")).isInstanceOf(Scanner.ScanException.class);
        assertThatThrownBy(() -> symbols("${'foo")).isInstanceOf(Scanner.ScanException.class);

        assertThat(symbols("${\"foo\"}")).containsExactly(expected);
        assertThat(symbols("${\"f\\\"o\"}")).containsExactly(expected);
        assertThat(symbols("${\"f'o\"}")).containsExactly(expected);
        assertThat(symbols("${\"f\\'o\"}")).containsExactly(expected);
        assertThatThrownBy(() -> symbols("${\"f\\oo\"}")).isInstanceOf(Scanner.ScanException.class);
        assertThatThrownBy(() -> symbols("${\"foo")).isInstanceOf(Scanner.ScanException.class);
    }

    @Test
    void testKeywords() throws Scanner.ScanException {
        assertThat(symbols("${null}")).containsExactly(START_EVAL_DYNAMIC, NULL, END_EVAL);
        assertThat(symbols("${true}")).containsExactly(START_EVAL_DYNAMIC, TRUE, END_EVAL);
        assertThat(symbols("${false}")).containsExactly(START_EVAL_DYNAMIC, FALSE, END_EVAL);
        assertThat(symbols("${empty}")).containsExactly(START_EVAL_DYNAMIC, EMPTY, END_EVAL);
        assertThat(symbols("${div}")).containsExactly(START_EVAL_DYNAMIC, DIV, END_EVAL);
        assertThat(symbols("${mod}")).containsExactly(START_EVAL_DYNAMIC, MOD, END_EVAL);
        assertThat(symbols("${not}")).containsExactly(START_EVAL_DYNAMIC, NOT, END_EVAL);
        assertThat(symbols("${and}")).containsExactly(START_EVAL_DYNAMIC, AND, END_EVAL);
        assertThat(symbols("${or}")).containsExactly(START_EVAL_DYNAMIC, OR, END_EVAL);
        assertThat(symbols("${le}")).containsExactly(START_EVAL_DYNAMIC, LE, END_EVAL);
        assertThat(symbols("${lt}")).containsExactly(START_EVAL_DYNAMIC, LT, END_EVAL);
        assertThat(symbols("${eq}")).containsExactly(START_EVAL_DYNAMIC, EQ, END_EVAL);
        assertThat(symbols("${ne}")).containsExactly(START_EVAL_DYNAMIC, NE, END_EVAL);
        assertThat(symbols("${ge}")).containsExactly(START_EVAL_DYNAMIC, GE, END_EVAL);
        assertThat(symbols("${gt}")).containsExactly(START_EVAL_DYNAMIC, GT, END_EVAL);
        assertThat(symbols("${instanceof}")).containsExactly(START_EVAL_DYNAMIC, INSTANCEOF, END_EVAL);

        assertThat(symbols("${xnull}")).containsExactly(START_EVAL_DYNAMIC, IDENTIFIER, END_EVAL);
        assertThat(symbols("${nullx}")).containsExactly(START_EVAL_DYNAMIC, IDENTIFIER, END_EVAL);
    }

    @Test
    void testOperators() throws Scanner.ScanException {
        assertThat(symbols("${*}")).containsExactly(START_EVAL_DYNAMIC, MUL, END_EVAL);
        assertThat(symbols("${/}")).containsExactly(START_EVAL_DYNAMIC, DIV, END_EVAL);
        assertThat(symbols("${%}")).containsExactly(START_EVAL_DYNAMIC, MOD, END_EVAL);
        assertThat(symbols("${+}")).containsExactly(START_EVAL_DYNAMIC, PLUS, END_EVAL);
        assertThat(symbols("${-}")).containsExactly(START_EVAL_DYNAMIC, MINUS, END_EVAL);
        assertThat(symbols("${?}")).containsExactly(START_EVAL_DYNAMIC, QUESTION, END_EVAL);
        assertThat(symbols("${:}")).containsExactly(START_EVAL_DYNAMIC, COLON, END_EVAL);
        assertThat(symbols("${[}")).containsExactly(START_EVAL_DYNAMIC, LBRACK, END_EVAL);
        assertThat(symbols("${]}")).containsExactly(START_EVAL_DYNAMIC, RBRACK, END_EVAL);
        assertThat(symbols("${(}")).containsExactly(START_EVAL_DYNAMIC, LPAREN, END_EVAL);
        assertThat(symbols("${)}")).containsExactly(START_EVAL_DYNAMIC, RPAREN, END_EVAL);
        assertThat(symbols("${,}")).containsExactly(START_EVAL_DYNAMIC, COMMA, END_EVAL);
        assertThat(symbols("${.}")).containsExactly(START_EVAL_DYNAMIC, DOT, END_EVAL);
        assertThat(symbols("${&&}")).containsExactly(START_EVAL_DYNAMIC, AND, END_EVAL);
        assertThat(symbols("${||}")).containsExactly(START_EVAL_DYNAMIC, OR, END_EVAL);
        assertThat(symbols("${!}")).containsExactly(START_EVAL_DYNAMIC, NOT, END_EVAL);
        assertThat(symbols("${<=}")).containsExactly(START_EVAL_DYNAMIC, LE, END_EVAL);
        assertThat(symbols("${<}")).containsExactly(START_EVAL_DYNAMIC, LT, END_EVAL);
        assertThat(symbols("${==}")).containsExactly(START_EVAL_DYNAMIC, EQ, END_EVAL);
        assertThat(symbols("${!=}")).containsExactly(START_EVAL_DYNAMIC, NE, END_EVAL);
        assertThat(symbols("${>=}")).containsExactly(START_EVAL_DYNAMIC, GE, END_EVAL);
        assertThat(symbols("${>}")).containsExactly(START_EVAL_DYNAMIC, GT, END_EVAL);

        assertThatThrownBy(() -> symbols("${&)")).isInstanceOf(Scanner.ScanException.class);
        assertThatThrownBy(() -> symbols("${|)")).isInstanceOf(Scanner.ScanException.class);
        assertThatThrownBy(() -> symbols("${=)")).isInstanceOf(Scanner.ScanException.class);
    }

    @Test
    void testWhitespace() throws Scanner.ScanException {
        assertThat(symbols("${\t\n\r }")).containsExactly(START_EVAL_DYNAMIC, END_EVAL);
    }

    @Test
    void testIdentifier() throws Scanner.ScanException {
        Scanner.Symbol[] expected = { START_EVAL_DYNAMIC, IDENTIFIER, END_EVAL };

        assertThat(symbols("${foo}")).containsExactly(expected);
        assertThat(symbols("${foo_1}")).containsExactly(expected);
    }

    @Test
    void testText() throws Scanner.ScanException {
        Scanner.Symbol[] expected = { TEXT };

        assertThat(symbols("foo")).containsExactly(expected);
        assertThat(symbols("foo\\")).containsExactly(expected);
        assertThat(symbols("foo\\$")).containsExactly(expected);
        assertThat(symbols("foo\\#")).containsExactly(expected);
        assertThat(symbols("foo\\${")).containsExactly(expected);
        assertThat(symbols("foo\\#{")).containsExactly(expected);
        assertThat(symbols("\\${foo}")).containsExactly(expected);
        assertThat(symbols("\\${foo}")).containsExactly(expected);
    }

    @Test
    void testMixed() throws Scanner.ScanException {
        assertThat(symbols("foo${")).containsExactly(TEXT, START_EVAL_DYNAMIC);
        assertThat(symbols("${bar")).containsExactly(START_EVAL_DYNAMIC, IDENTIFIER);
        assertThat(symbols("${}bar")).containsExactly(START_EVAL_DYNAMIC, END_EVAL, TEXT);
        assertThat(symbols("foo${}bar")).containsExactly(TEXT, START_EVAL_DYNAMIC, END_EVAL, TEXT);
    }

    @Test
    void testDeferred() throws Scanner.ScanException {
        assertThat(symbols("#{")).containsExactly(START_EVAL_DEFERRED);
    }
}