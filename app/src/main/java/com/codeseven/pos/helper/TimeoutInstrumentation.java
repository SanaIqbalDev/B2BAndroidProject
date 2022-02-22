package com.codeseven.pos.helper;


import android.app.Instrumentation;

import java.util.concurrent.TimeUnit;

import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TimeoutInstrumentation extends SimpleInstrumentation {
    @Override
    public DataFetcher<?> instrumentDataFetcher(
            DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters
    ) {
        return environment ->
                Observable.fromCallable(() -> dataFetcher.get(environment))
                        .subscribeOn(Schedulers.computation())
                        .timeout(3, TimeUnit.SECONDS)
                        .blockingFirst();
    }


}