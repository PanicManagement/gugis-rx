/**
 * Copyright (C) 2015 Łukasz Budnik <lukasz.budnik@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.lukaszbudnik.gugis;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import rx.Observable;

import java.util.List;

public final class ErrorMessageBuilder {

    public static String buildErrorMessageFromStrings(String prefix, List<String> errors) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(prefix);
        for (int i = 0; i < errors.size(); i++) {
            messageBuilder.append("\n\n");
            messageBuilder.append(i + 1);
            messageBuilder.append(") ");
            messageBuilder.append(errors.get(i));
        }
        return messageBuilder.toString();
    }

    public static String buildErrorMessageFromTries(String prefix, List<Try<Object>> tries) {
        List<String> errors = FluentIterable.from(tries).filter(new Predicate<Try<Object>>() {
            @Override
            public boolean apply(Try<Object> input) {
                return input.isFailure();
            }
        }).transform(new Function<Try<Object>, String>() {
            @Override
            public String apply(Try<Object> input) {
                return input.failure().toString();
            }
        }).toList();
        return buildErrorMessageFromStrings(prefix, errors);
    }

    public static String buildErrorMessageFromObservableTries(String prefix, Observable<Try<Object>> tries) {
        return buildErrorMessageFromTries(prefix, tries.toList().toBlocking().first());
    }

}
