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
package org.flowable.common.engine.impl.de.odysseus.el.tree.impl.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.flowable.common.engine.impl.de.odysseus.el.TestCase;
import org.flowable.common.engine.impl.de.odysseus.el.tree.Bindings;
import org.flowable.common.engine.impl.javax.el.ELException;
import org.junit.jupiter.api.Test;

class AstBinaryTest extends TestCase {

    private final Bindings bindings = new Bindings(null, null, null);

    AstBinary parseNode(String expression) {
        return (AstBinary) parse(expression).getRoot().getChild(0);
    }

    @Test
    void testEval() {
        assertThat(parseNode("${4+2}").eval(bindings, null)).isEqualTo(6L);
        assertThat(parseNode("${4*2}").eval(bindings, null)).isEqualTo(8L);
        assertThat(parseNode("${4/2}").eval(bindings, null)).isEqualTo(2d);
        assertThat(parseNode("${4%2}").eval(bindings, null)).isEqualTo(0L);

        assertThat(parseNode("${true && false}").eval(bindings, null)).isEqualTo(false);

        assertThat(parseNode("${true || false}").eval(bindings, null)).isEqualTo(true);

        assertThat(parseNode("${1 == 1}").eval(bindings, null)).isEqualTo(true);
        assertThat(parseNode("${1 == 2}").eval(bindings, null)).isEqualTo(false);
        assertThat(parseNode("${2 == 1}").eval(bindings, null)).isEqualTo(false);

        assertThat(parseNode("${1 != 1}").eval(bindings, null)).isEqualTo(false);
        assertThat(parseNode("${1 != 2}").eval(bindings, null)).isEqualTo(true);
        assertThat(parseNode("${2 == 1}").eval(bindings, null)).isEqualTo(false);

        assertThat(parseNode("${1 < 1}").eval(bindings, null)).isEqualTo(false);
        assertThat(parseNode("${1 < 2}").eval(bindings, null)).isEqualTo(true);
        assertThat(parseNode("${2 < 1}").eval(bindings, null)).isEqualTo(false);

        assertThat(parseNode("${1 > 1}").eval(bindings, null)).isEqualTo(false);
        assertThat(parseNode("${1 > 2}").eval(bindings, null)).isEqualTo(false);
        assertThat(parseNode("${2 > 1}").eval(bindings, null)).isEqualTo(true);

        assertThat(parseNode("${1 <= 1}").eval(bindings, null)).isEqualTo(true);
        assertThat(parseNode("${1 <= 2}").eval(bindings, null)).isEqualTo(true);
        assertThat(parseNode("${2 <= 1}").eval(bindings, null)).isEqualTo(false);

        assertThat(parseNode("${1 >= 1}").eval(bindings, null)).isEqualTo(true);
        assertThat(parseNode("${1 >= 2}").eval(bindings, null)).isEqualTo(false);
        assertThat(parseNode("${2 >= 1}").eval(bindings, null)).isEqualTo(true);
    }

    @Test
    void testAppendStructure() {
        StringBuilder s;
        s = new StringBuilder();
        parseNode("${1+1}").appendStructure(s, bindings);
        parseNode("${1*1}").appendStructure(s, bindings);
        parseNode("${1/1}").appendStructure(s, bindings);
        parseNode("${1%1}").appendStructure(s, bindings);
        assertThat(s.toString()).isEqualTo("1 + 11 * 11 / 11 % 1");

        s = new StringBuilder();
        parseNode("${1<1}").appendStructure(s, bindings);
        parseNode("${1>1}").appendStructure(s, bindings);
        parseNode("${1<=1}").appendStructure(s, bindings);
        parseNode("${1>=1}").appendStructure(s, bindings);
        assertThat(s.toString()).isEqualTo("1 < 11 > 11 <= 11 >= 1");

        s = new StringBuilder();
        parseNode("${1==1}").appendStructure(s, bindings);
        parseNode("${1!=1}").appendStructure(s, bindings);
        assertThat(s.toString()).isEqualTo("1 == 11 != 1");

        s = new StringBuilder();
        parseNode("${1&&1}").appendStructure(s, bindings);
        parseNode("${1||1}").appendStructure(s, bindings);
        assertThat(s.toString()).isEqualTo("1 && 11 || 1");
    }

    @Test
    void testIsLiteralText() {
        assertThat(parseNode("${1+1}").isLiteralText()).isFalse();
    }

    @Test
    void testIsLeftValue() {
        assertThat(parseNode("${1+1}").isLeftValue()).isFalse();
    }

    @Test
    void testGetType() {
        assertThat(parseNode("${1+1}").getType(bindings, null)).isNull();
    }

    @Test
    void testIsReadOnly() {
        assertThat(parseNode("${1+1}").isReadOnly(bindings, null)).isTrue();
    }

    @Test
    void testSetValue() {
        assertThatThrownBy(() -> parseNode("${1+1}").setValue(bindings, null, null))
                .isInstanceOf(ELException.class);
    }

    @Test
    void testGetValue() {
        assertThat(parseNode("${1+1}").getValue(bindings, null, null)).isEqualTo(2L);
        assertThat(parseNode("${1+1}").getValue(bindings, null, String.class)).isEqualTo("2");
    }

    @Test
    void testGetValueReference() {
        assertThat(parseNode("${1+1}").getValueReference(bindings, null)).isNull();
    }

    @Test
    void testOperators() {
        assertThat((Boolean) parseNode("${true and true}").getValue(bindings, null, Boolean.class)).isTrue();
        assertThat((Boolean) parseNode("${true and false}").getValue(bindings, null, Boolean.class)).isFalse();
        assertThat((Boolean) parseNode("${false and true}").getValue(bindings, null, Boolean.class)).isFalse();
        assertThat((Boolean) parseNode("${false and false}").getValue(bindings, null, Boolean.class)).isFalse();

        assertThat((Boolean) parseNode("${true or true}").getValue(bindings, null, Boolean.class)).isTrue();
        assertThat((Boolean) parseNode("${true or false}").getValue(bindings, null, Boolean.class)).isTrue();
        assertThat((Boolean) parseNode("${false or true}").getValue(bindings, null, Boolean.class)).isTrue();
        assertThat((Boolean) parseNode("${false or false}").getValue(bindings, null, Boolean.class)).isFalse();
    }
}
