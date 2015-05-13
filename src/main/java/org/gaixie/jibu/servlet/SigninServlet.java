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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.text.MessageFormat;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gaixie.jibu.JibuException;
import org.gaixie.jibu.mail.JavaMailSender;
import org.gaixie.jibu.model.User;
import org.gaixie.jibu.model.Token;
import org.gaixie.jibu.service.UserService;
import org.gaixie.jibu.servlet.ServletUtils;
import org.gaixie.jibu.utils.BundleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 响应登录请求的 Servlet。
 *
 */
@Singleton
public class SigninServlet extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(SigninServlet.class);

    @Inject
    private Injector injector;

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        String value = req.getServletPath();
        if ("/session".equals(value)) {
            session(req, res);
        } else if ("/signout".equals(value)) {
            signout(req, res);
        } else if ("/signin".equals(value)) {
            signin(req, res, null);
        } else if ("/forgot".equals(value)) {
            forgot(req, res);
        } else if ("/forgotreset".equals(value)) {
            forgotreset(req, res);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        doGet(req, res);
    }

    protected void session(HttpServletRequest req,
                           HttpServletResponse res) throws IOException {
        UserService userService = injector.getInstance(UserService.class);
        String loginname = req.getParameter("loginname");
        String password = req.getParameter("password");

        User user = userService.get(loginname, password);
        if (user != null) {
            HttpSession ses = req.getSession(true);
            Token token = userService.generateToken("signin",user.getUsername(),null);

            if (token != null) {
                int expire = (int) ((token.getExpiration_ts()).getTime() - System.currentTimeMillis())/1000;
                ServletUtils.addCookie(res, "user_session", token.getValue(), expire);
            }

            ServletUtils.addCookie(res, "dotcom_user", user.getUsername(), 60*60*24*365*20);
            ses.setAttribute("username", user.getUsername());
            String return_to = req.getParameter("return_to");
            if (return_to != null)
                res.sendRedirect(return_to);
            else
                res.sendRedirect("/");
            return;
        } else {
            signin(req, res, "signin.message.001");
        }
    }

    protected void signout(HttpServletRequest req,
                           HttpServletResponse res) throws IOException {
        HttpSession ses = req.getSession(false);
        if (ses != null)
            ses.invalidate();

        String username = ServletUtils.getCookieValue(req, "dotcom_user");
        String tokenValue = ServletUtils.getCookieValue(req, "user_session");

        UserService userService = injector.getInstance(UserService.class);
        userService.signout(username,tokenValue);

        ServletUtils.removeCookie(res,"dotcom_user");
        ServletUtils.removeCookie(res,"user_session");

        res.sendRedirect("/");
    }

    protected void signin(HttpServletRequest req, HttpServletResponse res,
                          String message) throws IOException {
        HttpSession ses = req.getSession(false);
        if (ses != null)
            ses.invalidate();

        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));
        StringBuilder sb = new StringBuilder();
        //--------------------------------------------
        // 执行下面的命令生成html输出的Java语句，如果页面太长，可以直接指定输出 24 ~ 38 行：
        // mac osx 下 sed 语法和 linux 不同，执行下面的命令：
        // curl http://ui.nodto.cn/index.html | sed '24,38!d;s/^[[:blank:]]*//;s/[[:blank:]]*$//;s/"/\\"/g;s/.*/sb.append("&\\n");/'
        // -------------------------------------------
        sb.append("<div class=\"content\">\n");
        if (message != null)
            sb.append("<div class=\"msg-error\">"+rb.get(message)+"</div>");
        sb.append("<form class=\"pure-form pure-form-stacked\" action=\"/session\" method=\"post\">\n");
        sb.append("<fieldset>\n");
        sb.append("<label for=\"loginname\">"+rb.get("signin.loginname")+"</label>\n");
        sb.append("<input id=\"loginname\" name=\"loginname\" type=\"text\">\n");
        sb.append("<label for=\"password\">"+rb.get("signin.password")+" <a href=\"/forgot\">"+rb.get("signin.forgot.link")+"</a></label>\n");
        sb.append("<input id=\"password\" name=\"password\" type=\"password\">\n");

        String return_to = req.getParameter("return_to");
        if (return_to != null)
            sb.append("<input name=\"return_to\" type=\"hidden\" value=\""+return_to+"\">\n");

        sb.append("<button type=\"submit\" class=\"pure-button\">"+rb.get("button.signin")+"</button>\n");
        sb.append("</fieldset>\n");
        sb.append("</form>\n");
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("signin.title"))
                   + ServletUtils.body(req, rb.get("signin.title"))
                   + sb.toString()
                   + ServletUtils.footer());
        pw.close();
    }

    protected void forgot(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        String email = req.getParameter("email");
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));

        // 表示提交了一个密码重置请求
        if (email != null) {
            UserService userService = injector.getInstance(UserService.class);
            Token token = userService.generateToken("password", null, email);

            if (token != null) {
                User user = userService.get(token.getCreated_by());
                String tokenUrl = req.getRequestURL()
                    + "reset?key=" + token.getValue();
                String text = rb.get("signin.forgot.email.tpl");
                Object[] args = { user.getFullname(), email,
                                  tokenUrl };
                JavaMailSender sender = new JavaMailSender();
                sender.send(email,rb.get("signin.forgotreset.title"),
                            MessageFormat.format(text, args));
                forgot(req, res,
                       "<div class=\"msg-success\">"+rb.get("signin.message.003")+"</div>", false);
                return;
            } else {
                forgot(req, res,
                       "<div class=\"msg-error\">"+rb.get("signin.message.004")+"</div>", true);
                return;
            }
        }

        forgot(req, res,
               "<div class=\"msg-info\">"+rb.get("signin.message.002")+"</div>", true);
    }

    private void forgot(HttpServletRequest req, HttpServletResponse res,
                        String message, boolean showForm) throws IOException {
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"content\">\n");
        sb.append(message);
        if (showForm) {
            sb.append("<form class=\"pure-form pure-form-stacked\" action=\"/forgot\" method=\"post\">\n");
            sb.append("<fieldset>\n");
            sb.append("<label for=\"email\">"+rb.get("signin.forgot.email")+"</label>\n");
            sb.append("<input id=\"email\" name=\"email\" type=\"text\">\n");
            sb.append("<button type=\"submit\" class=\"pure-button\">"+rb.get("button.submit")+"</button>\n");
            sb.append("</fieldset>\n");
            sb.append("</form>\n");
        }
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("signin.forgot.title"))
                   + ServletUtils.body(req, rb.get("signin.forgot.title"))
                   + sb.toString()
                   + ServletUtils.footer());
        pw.close();
    }

    protected void forgotreset(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        UserService userService = injector.getInstance(UserService.class);
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));

        String key = req.getParameter("key");
        String password = req.getParameter("password");
        String repassword = req.getParameter("repassword");

        if (password == null) {
            forgotreset(req, res,
                        "<div class=\"msg-info\">"+
                        rb.get("signin.message.005")+"</div>\n", true);
        } else if (!password.equals(repassword)) {
            forgotreset(req, res,
                        "<div class=\"msg-error\">"+
                        rb.get("signin.message.006")+ "</div>\n", true);
        } else {
            try {
                userService.resetPassword(password,key);
                forgotreset(req, res,
                            "<div class=\"msg-success\">"+
                            rb.get("message.001")+"</div>\n", false);
            } catch (JibuException e) {
                forgotreset(req, res,
                            "<div class=\"msg-error\">"+
                            rb.get("message.002")+" "+e.getMessage()+
                            "</div>\n", true);
            }

        }

    }

    private void forgotreset(HttpServletRequest req, HttpServletResponse res,
                             String message, boolean showForm) throws IOException {
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));

        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"content\">\n");
        sb.append(message);
        if (showForm) {
            sb.append("<form class=\"pure-form pure-form-stacked\" action=\"/forgotreset\" method=\"post\">\n");
            sb.append("<fieldset>\n");
            sb.append("<label for=\"password\">"+rb.get("signin.forgotreset.password")+"</label>\n");
            sb.append("<input id=\"password\" name=\"password\" type=\"text\">\n");
            sb.append("<label for=\"repassword\">"+rb.get("signin.forgotreset.repassword")+"</label>\n");
            sb.append("<input id=\"repassword\" name=\"repassword\" type=\"text\">\n");
            sb.append("<input name=\"key\" type=\"hidden\" value=\""+req.getParameter("key")+"\">\n");
            sb.append("<button type=\"submit\" class=\"pure-button\">"+rb.get("button.submit")+"</button>\n");
            sb.append("</fieldset>\n");
            sb.append("</form>\n");
        }
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("signin.forgotreset.title"))
                   + ServletUtils.body(req, rb.get("signin.forgotreset.title"))
                   + sb.toString()
                   + ServletUtils.footer());
        pw.close();
    }
}
