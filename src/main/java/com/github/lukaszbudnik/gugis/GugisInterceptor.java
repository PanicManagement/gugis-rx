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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.MethodUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
public class GugisInterceptor implements MethodInterceptor {

    @Inject
    private Injector injector;

    private Random random = new Random();

    @Override
    public Object invoke(MethodInvocation i) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Method " + i.getMethod() + " is called on " + i.getThis() + " with args " + i.getArguments());
        }

        Propagate propagate = i.getMethod().getAnnotation(Propagate.class);

        Class clazz = i.getMethod().getDeclaringClass().getInterfaces()[0];

        if (log.isDebugEnabled()) {
            log.debug("About to find bindings for " + clazz);
            log.debug("Propagation set to " + propagate.propagation());
            log.debug("Allow failure set to " + propagate.allowFailure());
        }

        List<Binding<Object>> bindings = injector.findBindingsByType(TypeLiteral.get(clazz));

        if (bindings.size() == 0) {
            log.error("No bindings found for " + clazz);
            throw new GugisException("No bindings found for " + clazz);
        }

        if (log.isDebugEnabled()) {
            log.debug("Found " + bindings.size() + " bindings for " + clazz);
        }


        Observable<Try<Object>> resultsObservable = null;
        switch (propagate.propagation()) {
//            case FASTEST: {
//                Observable<Binding<Object>> bindingsObservable = Observable.from(bindings);
//                Observable<Try<Object>> executedObservable = executeBindings(propagate.allowFailure(), bindingsObservable, i.getMethod().getName(), i.getArguments());
//                resultsObservable = executedObservable.debounce(5, TimeUnit.MILLISECONDS);
//                break;
//            }
            case PRIMARY: {
                resultsObservable = executeRxBindingsForRole(i, bindings, propagate.allowFailure(), Primary.class);
                break;
            }
            case SECONDARY: {
                resultsObservable = executeRxBindingsForRole(i, bindings, propagate.allowFailure(), Secondary.class);
                break;
            }
            case RANDOM: {
                ArrayList<Binding<Object>> modifiableBindings = new ArrayList<Binding<Object>>(bindings);
                Collections.shuffle(modifiableBindings);
                for (Binding<Object> binding : modifiableBindings) {
                    Observable<Binding<Object>> bindingsObservable = Observable.just(binding);
                    Observable<Try<Object>> executedObservable = executeBindings(propagate.allowFailure(), bindingsObservable, i.getMethod().getName(), i.getArguments());

                    Try<Object> tryObject = executedObservable.toBlocking().first();

                    if (tryObject.isSuccess()) {
                        resultsObservable = Observable.just(tryObject);
                        break;
                    }
                }
                break;
            }
            default: {
                // handles ALL
                Observable<Binding<Object>> bindingsObservable = Observable.from(bindings);
                resultsObservable = executeBindings(propagate.allowFailure(), bindingsObservable, i.getMethod().getName(), i.getArguments());
                break;
            }
        }

        List<Try<Object>> results = resultsObservable.toList().toBlocking().first();

        List<Try<Object>> successes = FluentIterable.from(results).filter(new Predicate<Try<Object>>() {
            @Override
            public boolean apply(Try<Object> input) {
                return input.isSuccess();
            }
        }).toList();

        if (successes.size() == 0) {
            String errorMessage = ErrorMessageBuilder.buildErrorMessageFromTries("No result for " + propagate.propagation() + " found for " + clazz.getCanonicalName() + "." + i.getMethod().getName(), results);
            throw new GugisException(errorMessage);
        }

        // all implementations should be homogeneous and should return same value for same arguments
        Try<Object> tryObject = successes.get(0);

        if (log.isDebugEnabled()) {
            log.debug("Method " + i.getMethod() + " returns " + tryObject.get());
        }

        return tryObject.get();
    }

    public Observable<Try<Object>> executeBindings(final boolean allowFailure, Observable<Binding<Object>> bindings, final String methodName, final Object[] arguments) {

        Observable<Try<Object>> executedBindingsObservable = bindings.subscribeOn(Schedulers.newThread()).map(new Func1<Binding<Object>, Try<Object>>() {
            @Override
            public Try<Object> call(Binding<Object> binding) {
                try {
                    Object component = binding.getProvider().get();
                    return new Success<Object>(MethodUtils.invokeMethod(component, methodName, arguments));
                } catch (InvocationTargetException e) {
                    if (!allowFailure) {
                        // pass the original exception thrown
                        throw new GugisException(e.getCause());
                    }
                    return new Failure<Object>(e.getCause());
                } catch (Exception e) {
                    throw new GugisException(e);
                }
            }
        });

        return executedBindingsObservable;
    }

    private Observable<Try<Object>> executeRxBindingsForRole(MethodInvocation i, List<Binding<Object>> bindings, Boolean allowFailure, final Class<? extends Annotation> role) {

        Observable<Binding<Object>> filtered = Observable.from(bindings).filter(new Func1<Binding<Object>, Boolean>() {
            @Override
            public Boolean call(Binding<Object> binding) {
                return binding.getProvider().get().getClass().isAnnotationPresent(role);
            }
        });

        Observable<Try<Object>> resultsObservable = executeBindings(allowFailure, filtered, i.getMethod().getName(), i.getArguments());
        return resultsObservable;
    }

}
