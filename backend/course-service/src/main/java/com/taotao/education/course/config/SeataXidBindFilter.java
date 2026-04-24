package com.taotao.education.course.config;

import io.seata.core.context.RootContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SeataXidBindFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String xid = request.getHeader(RootContext.KEY_XID);
        boolean needBind = StringUtils.hasText(xid) && !StringUtils.hasText(RootContext.getXID());
        if (needBind) {
            RootContext.bind(xid);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (needBind) {
                RootContext.unbind();
            }
        }
    }
}
