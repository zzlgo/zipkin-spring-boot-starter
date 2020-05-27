package com.zzl.zipkin.instrument.slf4j;

import org.slf4j.MDC;

/**
 * Created by zzl on 2019/1/2.
 * <p>  对slf4j mdc设置，来手动管理追踪参数
 * 用于异步回调，用于设置filter，日志业务参数埋点
 */
public class LogUtil {

    private static final String LEGACY_FILTER_NAME = "filter";
    private static final String LEGACY_TRACE_ID_NAME = "X-B3-TraceId";
    private static final String LEGACY_SPAN_ID_NAME = "X-B3-SpanId";

    /**
     * 适配日志搜索，filter可以是业务id，可以与日志的traceId建立映射关系
     *
     * @param filter
     */
    public static void putFilter(String filter) {
        if (filter != null) {
            MDC.put(LEGACY_FILTER_NAME, filter);
        } else {
            MDC.remove(LEGACY_FILTER_NAME);
        }
    }

    /**
     * 清除filter
     */
    public static void clearFilter() {

        MDC.remove(LEGACY_FILTER_NAME);
    }

    /**
     * 获取当前线程的traceId
     */
    public static String getCurrentTraceId() {
        return MDC.get(LEGACY_TRACE_ID_NAME);
    }


    /**
     * 设置当前线程的traceId
     *
     * @param traceId
     */
    public static void putCurrentTraceId(String traceId) {

        if (traceId != null) {
            MDC.put(LEGACY_TRACE_ID_NAME, traceId);
        } else {
            MDC.remove(LEGACY_TRACE_ID_NAME);
        }
    }

    /**
     * 获取当前线程的spanId
     */
    public static String getCurrentSpanId() {
        return MDC.get(LEGACY_SPAN_ID_NAME);
    }


    /**
     * 设置当前线程的spanId
     *
     * @param spanId
     */
    public static void putCurrentSpanId(String spanId) {
        if (spanId != null) {
            MDC.put(LEGACY_SPAN_ID_NAME, spanId);
        } else {
            MDC.remove(LEGACY_SPAN_ID_NAME);
        }
    }
}
