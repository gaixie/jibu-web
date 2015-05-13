/**
 * Copyright (C) 2014 Gaixie.ORG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gaixie.jibu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gaixie.jibu.model.Token;
import org.gaixie.jibu.service.UserService;
import org.gaixie.jibu.servlet.ServletUtils;
import org.gaixie.jibu.utils.BundleHandler;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * SigninFilter 对所有请求进行拦截并验证是否登录，以及是否需要自动登录。
 * <p>
 * 对于特定的 URL 请求进行拦截，判断 HttpSession 中是否有 username。
 * 后缀为 .y 的 Servlet 所有有效用户都可以访问。后缀为 .z 的 Servlet 只有高级用户才能访问。</p>
 */
@Singleton
public class SigninFilter implements Filter {
    @Inject private Injector injector;

    /**
     * The filter configuration object we are associated with. If this value is
     * null, this filter instance is not currently configured.
     */
    private FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        String value = ((HttpServletRequest) req).getServletPath();
        boolean allowedRequest = false;

        // 如果页面有多语言的请求，将新的语言保存在 cookie中
        // 有效期为当前浏览器会话。
        String localeCode = req.getParameter("locale");
        if (localeCode != null) {
            ServletUtils.addCookie((HttpServletResponse) res, 
                                   "user_locale", localeCode, -1);
        }

        /*
         * 所有的请求都过这个 Filter
         * 先判断有没有 session，如果没有，再从 cookie 中取出之前保存的 user_session
         * 与数据库中保存 token 的去比对，如果比对成功，创建一个新的 session，更新
         * user_session 的有效期，并保存到数据库以及 cookie 中。
         */
        HttpSession ses = ((HttpServletRequest) req).getSession(false);
        if (null == ses) {
            String username = 
                ServletUtils.getCookieValue((HttpServletRequest) req, "dotcom_user");
            String userSession = 
                ServletUtils.getCookieValue((HttpServletRequest) req, "user_session");

            UserService userService = injector.getInstance(UserService.class);
            Token token = userService.signinByToken(username, userSession);
            if (token != null) {
                int expire = (int) ((token.getExpiration_ts()).getTime() - System.currentTimeMillis())/1000;
                ServletUtils.addCookie((HttpServletResponse) res, "user_session",
                                       token.getValue(), expire);
                ses = ((HttpServletRequest) req).getSession(true);
                ses.setAttribute("username", username);
                allowedRequest = true;
            }
        } else {
            allowedRequest = true;
        }

        // 如果登录验证未通过，将所有请求（除了登录操作之外）重定向到登录页面。
        if (!allowedRequest) {
            if (!value.matches("/|/signin|/session|/forgot.*")) {
                ((HttpServletResponse) res).sendRedirect("/signin?return_to="+URLEncoder.encode(value,"UTF-8"));
                return;
            }
        }

        // 如果登录验证已通过，直接将对登录页面重定向到首页。
        if (allowedRequest) {
            if (value.matches("/signin|/session")) {
                ((HttpServletResponse) res).sendRedirect("/");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    public void destroy() {
        this.filterConfig = null;
    }
}
