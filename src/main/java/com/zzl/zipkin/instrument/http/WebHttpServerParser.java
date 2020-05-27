package com.zzl.zipkin.instrument.http;

import brave.SpanCustomizer;
import brave.http.HttpAdapter;
import brave.internal.Nullable;

/**
 * Parses the request and response into reasonable defaults for http server spans. Subclass to
 * customize, for example, to add tags based on user ID.
 */
public class WebHttpServerParser extends brave.http.HttpServerParser {

    /**
     * Customizes the span based on the request received from the client.
     *
     * <p>{@inheritDoc}
     */
    @Override
    public <Req> void request(HttpAdapter<Req, ?> adapter, Req req,
                              SpanCustomizer customizer) {
        super.request(adapter, req, customizer);

        /**
         * 记录用户id
         */
        String userId = adapter.requestHeader(req, "userId");
        if (userId != null) {
            customizer.tag("userId", userId);
        }
        //记录客户端traceId
        String clientTraceId = adapter.requestHeader(req, "http_x_request_id");
        if (clientTraceId != null) {
            customizer.tag("clientTraceId", clientTraceId);
        }

    }

    /**
     * Customizes the span based on the response sent to the client.
     *
     * <p>{@inheritDoc}
     */
    @Override
    public <Resp> void response(HttpAdapter<?, Resp> adapter, @Nullable Resp res,
                                @Nullable Throwable error, SpanCustomizer customizer) {
        super.response(adapter, res, error, customizer);
    }
}
