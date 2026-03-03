/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.engine.impl.json;

/**
 * @author Filip Hrisafov
 */
public interface VariableJsonMapper {

    Object readTree(String textValue);

    Object readTree(byte[] bytes);

    Object deepCopy(Object value);

    boolean isJsonNode(Object value);

    Object transformToJsonNode(Object value);

    /**
     * Wraps a raw JSON node object (e.g., Jackson {@code JsonNode}) into a {@link FlowableJsonNode} abstraction.
     *
     * @param value the raw JSON node
     * @return the wrapped node, or {@code null} if the value is {@code null}
     */
    FlowableJsonNode wrapJsonNode(Object value);

    FlowableObjectNode createObjectNode();

    FlowableArrayNode createArrayNode();
}
