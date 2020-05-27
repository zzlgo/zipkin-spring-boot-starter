package com.zzl.zipkin.autoconfigure;

import com.zzl.zipkin.instrument.slf4j.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zzl on 2019/3/27.
 * @description 用于获取客户端传递的http_x_request_id
 */
public class FilterLoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(FilterLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            /**
             * 关联客户端traceId
             */
            if (request instanceof HttpServletRequest) {
                String traceId = ((HttpServletRequest) request).getHeader("http_x_request_id");
                if (traceId != null && !"".equals(traceId)) {
                    LogUtil.putFilter(traceId);
                    LOG.info("client traceId={}", traceId);
                    LogUtil.clearFilter();
                }

            }
        } catch (Exception ignored) {

        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
