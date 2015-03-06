/**
 * Copyright 2015 DuraSpace, Inc.
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
package org.fcrepo.kernel.services.functions;


/**
 * Predicate to match nodes with any of the given mixin types
 * @author armintor@gmail.com
 *
 */
public class AnyTypesPredicate extends BooleanTypesPredicate {
    /**
     * True if any of the types specified match.
     * @param types the types
     */
    public AnyTypesPredicate(final String...types) {
        super(types);
    }

    @Override
    protected boolean test(final int matched) {
        return matched > 0;
    }

}