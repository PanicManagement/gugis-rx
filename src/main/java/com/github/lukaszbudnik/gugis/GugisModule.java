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


import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import lombok.extern.slf4j.Slf4j;
import org.atteo.classindex.ClassIndex;
import rx.Observable;
import rx.functions.Func1;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GugisModule extends AbstractModule {

    private final boolean validating;

    public GugisModule() {
        this(true);
    }

    public GugisModule(boolean validating) {
        this.validating = validating;
    }

    @Override
    protected void configure() {
        GugisReplicatorInterceptor gugisReplicatorInterceptor = new GugisReplicatorInterceptor();
        requestInjection(gugisReplicatorInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Propagate.class), gugisReplicatorInterceptor);

        List<String> validationErrors = new ArrayList<String>();

        for (Class<?> compositeClass : ClassIndex.getAnnotated(Composite.class)) {
            Composite compositeAnnotation = compositeClass.getAnnotation(Composite.class);
            if (!compositeAnnotation.autodiscover()) {
                if (log.isDebugEnabled()) {
                    log.debug("Composite class " + compositeClass.getCanonicalName() + " has autodiscover flag set to false. Skipping.");
                }
                continue;
            }

            if (log.isDebugEnabled()) {
                log.debug("About to bind composite component " + compositeClass);
            }

            bind(compositeClass);

            Class classInterface = compositeClass.getInterfaces()[0];
            Multibinder<?> multibinder = Multibinder.newSetBinder(binder(), classInterface);
            long primariesCount = bind(multibinder, classInterface, Primary.class);
            long secondariesCount = bind(multibinder, classInterface, Secondary.class);

            if (validating) {
                if (log.isDebugEnabled()) {
                    log.debug("About to validate bindings for " + compositeClass);
                }
                if (primariesCount == 0 && secondariesCount == 0) {
                    log.error("No implementations found for " + compositeClass);
                    validationErrors.add("No implementations found for " + compositeClass);
                } else if (primariesCount == 0) {
                    List<String> methodsMarkedPrimary = getMethodsMarkedWithPropagation(compositeClass, Propagation.PRIMARY);
                    if (methodsMarkedPrimary.size() > 0) {
                        log.error("Composite component " + compositeClass + " methods " + methodsMarkedPrimary + " marked with @Propagate(propagation = Propagation.PRIMARY) but no primary implementations found");
                        validationErrors.add("Composite component " + compositeClass + " methods " + methodsMarkedPrimary + " marked with @Propagate(propagation = Propagation.PRIMARY) but no primary implementations found");
                    }
                } else if (secondariesCount == 0) {
                    List<String> methodsMarkedSecondary = getMethodsMarkedWithPropagation(compositeClass, Propagation.SECONDARY);
                    if (methodsMarkedSecondary.size() > 0) {
                        log.error("Composite component " + compositeClass + " methods " + methodsMarkedSecondary + " marked with @Propagate(propagation = Propagation.SECONDARY) but no secondary implementations found");
                        validationErrors.add("Composite component " + compositeClass + " methods " + methodsMarkedSecondary + " marked with @Propagate(propagation = Propagation.SECONDARY) but no secondary implementations found");
                    }
                }
            }
        }

        if (validationErrors.size() > 0) {
            String errorMessage = ErrorMessageBuilder.buildErrorMessageFromStrings("The following creation errors were found:", validationErrors);
            throw new GugisCreationException(errorMessage);
        }

    }

    private List<String> getMethodsMarkedWithPropagation(final Class<?> compositeClass, final Propagation propagation) {
        return Observable.from(compositeClass.getMethods()).filter(new Func1<Method, Boolean>() {
            @Override
            public Boolean call(Method method) {
                Propagate propagate = method.getAnnotation(Propagate.class);
                if (propagate == null) {
                    return false;
                }
                if (propagate.propagation() == propagation) {
                    return true;
                }
                return false;
            }
        }).map(new Func1<Method, String>() {
            @Override
            public String call(Method method) {
                return method.toString().replace(compositeClass.getCanonicalName() + ".", "");
            }
        }).toList().toBlocking().first();
    }

    private long bind(final Multibinder multibinder, final Class<?> classInterface, Class<? extends Annotation> annotation) {

        return Observable.from(ClassIndex.getAnnotated(annotation)).filter(new Func1<Class<?>, Boolean>() {
            @Override
            public Boolean call(Class<?> c) {
                return classInterface.isAssignableFrom(c);
            }
        }).map(new Func1<Class<?>, Boolean>() {
            @Override
            public Boolean call(Class<?> c) {
                if (log.isDebugEnabled()) {
                    log.debug("Binding " + c + " to " + classInterface);
                }
                multibinder.addBinding().to(c);
                return true;
            }
        }).count().toBlocking().first();
    }
}
