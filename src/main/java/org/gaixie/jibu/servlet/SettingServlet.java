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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.validator.routines.EmailValidator;
import org.gaixie.jibu.JibuException;
import org.gaixie.jibu.model.User;
import org.gaixie.jibu.service.UserService;
import org.gaixie.jibu.utils.BundleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 响应用户设置相关操作的请求。
 */
@Singleton
public class SettingServlet extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(SettingServlet.class);

    @Inject
    private Injector injector;

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        String value = req.getServletPath();
        if ("/settings/password".equals(value)) {
            password(req, res, null);
        } else if ("/settings/profile".equals(value)) {
            profile(req, res, null);
        } else if ("/settings/email".equals(value)) {
            email(req, res, null);
        } else if ("/settings".equals(value)) {
            res.sendRedirect("/settings/profile");
        }
    }

    public void doPost(HttpServletRequest req,  HttpServletResponse res)
        throws IOException{
        doGet(req, res);
    }

    protected void password(HttpServletRequest req, HttpServletResponse res,
                            String message) throws IOException {
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));
        StringBuilder sb = new StringBuilder();
        String msg = null;

        UserService userService = injector.getInstance(UserService.class);
        String username = ServletUtils.getUsername(req);


        if ("update".equals(req.getParameter("ci"))) {
            User user = userService.get(username,
                                        req.getParameter("curpassword"));

            if (user != null) {
                String newPwd = req.getParameter("newpassword");
                String rePwd = req.getParameter("repassword");
                if ((newPwd != null && !newPwd.trim().isEmpty()) &&
                    (rePwd != null && !rePwd.trim().isEmpty())) {
                    if (newPwd.equals(rePwd)) {
                        User updateUser = new User();
                        updateUser.setId(user.getId());
                        updateUser.setPassword(newPwd);
                        try {
                            userService.update(updateUser);
                            msg = "<div class=\"msg-success\">"+rb.get("message.001")+"</div>\n";
                        } catch (JibuException e) {
                            msg = "<div class=\"msg-error\">"+rb.get("message.002")+" "+e.getMessage()+"</div>\n";
                        }
                    } else
                        msg = "<div class=\"msg-error\">"+rb.get("setting.message.003")+"</div>\n";
                } else
                    msg = "<div class=\"msg-error\">"+rb.get("setting.message.002")+"</div>\n";
            } else
                msg = "<div class=\"msg-error\">"+rb.get("setting.message.001")+"</div>\n";
        }

        sb.append("<div class=\"content\">\n");
        menu(req,rb,sb);
        sb.append("<legend>"+rb.get("settings.password.title")+"</legend>\n");
        if (msg != null) sb.append(msg);
        sb.append("<form class=\"pure-form pure-form-stacked\" action=\"/settings/password?ci=update\" method=\"post\">\n");
        sb.append("<fieldset>\n");
        sb.append("<label for=\"curpassowrd\">"+rb.get("settings.password.curpassword")+"</label>\n");
        sb.append("<input id=\"curpassword\" name=\"curpassword\" type=\"password\">\n");
        sb.append("<label for=\"newpassword\">"+rb.get("settings.password.newpassword")+"</label>\n");
        sb.append("<input id=\"newpassword\" name=\"newpassword\" type=\"password\">\n");
        sb.append("<label for=\"repassword\">"+rb.get("settings.password.repassword")+"</label>\n");
        sb.append("<input id=\"repassword\" name=\"repassword\" type=\"password\">\n");
        sb.append("<button type=\"submit\" class=\"pure-button\">"+rb.get("button.update")+"</button>\n");
        sb.append("</fieldset>\n");
        sb.append("</form>\n");
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("settings.title"))
                   + ServletUtils.body(req, rb.get("settings.title"))
                   + sb.toString()
                   + ServletUtils.footer());
        pw.close();
    }

    protected void profile(HttpServletRequest req, HttpServletResponse res,
                           String message) throws IOException {
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));
        StringBuilder sb = new StringBuilder();
        String msg = null;

        UserService userService = injector.getInstance(UserService.class);
        String username = ServletUtils.getUsername(req);
        User user = userService.get(username);

        if ("update".equals(req.getParameter("ci"))) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setUsername(req.getParameter("username"));
            updateUser.setFullname(req.getParameter("fullname"));
            try {
                userService.update(updateUser);
                // 重新取一遍更新后的数据
                user = userService.get(user.getId());
                // 更新 session 和 cookie
                ServletUtils.addCookie(res, "dotcom_user", user.getUsername(), 60*60*24*365*20);
                HttpSession ses = req.getSession(false);
                if (ses != null)
                    ses.setAttribute("username", user.getUsername());

                msg = "<div class=\"msg-success\">"+rb.get("message.001")+"</div>\n";
            } catch (JibuException e) {
                msg = "<div class=\"msg-error\">"+rb.get("message.002")+" "+e.getMessage()+"</div>\n";
            }
        }

        sb.append("<div class=\"content\">\n");
        menu(req,rb,sb);
        sb.append("<legend>"+rb.get("settings.profile.title")+"</legend>\n");
        if (msg != null) sb.append(msg);
        sb.append("<form class=\"pure-form pure-form-stacked\" action=\"/settings/profile?ci=update\" method=\"post\">\n");
        sb.append("<fieldset>\n");
        sb.append("<label for=\"username\">"+rb.get("settings.profile.username")+"</label>\n");
        sb.append("<input id=\"username\" name=\"username\" value=\""+user.getUsername()+"\" type=\"text\">\n");
        sb.append("<label for=\"fullname\">"+rb.get("settings.profile.fullname")+"</label>\n");
        sb.append("<input id=\"fullname\" name=\"fullname\" value=\""+user.getFullname()+"\" type=\"text\">\n");
        sb.append("<button type=\"submit\" class=\"pure-button\">"+rb.get("button.update")+"</button>\n");
        sb.append("</fieldset>\n");
        sb.append("</form>\n");
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("settings.title"))
                   + ServletUtils.body(req, rb.get("settings.title"))
                   + sb.toString()
                   + ServletUtils.footer());
        pw.close();
    }

    protected void email(HttpServletRequest req, HttpServletResponse res,
                         String message) throws IOException {
        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));
        StringBuilder sb = new StringBuilder();
        String msg = null;

        UserService userService = injector.getInstance(UserService.class);
        String username = ServletUtils.getUsername(req);
        User user = userService.get(username);

        if ("update".equals(req.getParameter("ci"))) {
            User vUser = userService.get(username,
                                         req.getParameter("curpassword"));

            if (vUser != null) {
                String newEmail = req.getParameter("newemail");
                EmailValidator validator = EmailValidator.getInstance();
                if (validator.isValid(newEmail)) {
                    User updateUser = new User();
                    updateUser.setId(user.getId());
                    updateUser.setEmailaddress(newEmail);
                    try {
                        userService.update(updateUser);
                        user.setEmailaddress(newEmail);
                        msg = "<div class=\"msg-success\">"+rb.get("message.001")+"</div>\n";
                    } catch (JibuException e) {
                        msg = "<div class=\"msg-error\">"+rb.get("message.002")+" "+e.getMessage()+"</div>\n";
                    }
                } else
                    msg = "<div class=\"msg-error\">"+rb.get("setting.message.004")+"</div>\n";
            } else
                msg = "<div class=\"msg-error\">"+rb.get("setting.message.001")+"</div>\n";
        }

        sb.append("<div class=\"content\">\n");
        menu(req,rb,sb);
        sb.append("<legend>"+rb.get("settings.email.title")+"</legend>\n");
        if (msg != null) sb.append(msg);
        sb.append("<form class=\"pure-form pure-form-stacked\" action=\"/settings/email?ci=update\" method=\"post\">\n");
        sb.append("<fieldset>\n");
        sb.append("<label for=\"curpassword\">"+rb.get("settings.email.curpassword")+"</label>\n");
        sb.append("<input id=\"curpassword\" name=\"curpassword\" type=\"password\">\n");
        sb.append("<label for=\"curemail\">"+rb.get("settings.email.curemail")+"</label>\n");
        sb.append("<input id=\"curemail\" name=\"curemail\" value=\""+user.getEmailaddress()+"\" type=\"email\" readonly>\n");
        sb.append("<label for=\"newemail\">"+rb.get("settings.email.newemail")+"</label>\n");
        sb.append("<input id=\"newemail\" name=\"newemail\" type=\"email\">\n");
        sb.append("<button type=\"submit\" class=\"pure-button\">"+rb.get("button.update")+"</button>\n");
        sb.append("</fieldset>\n");
        sb.append("</form>\n");
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("settings.title"))
                   + ServletUtils.body(req, rb.get("settings.title"))
                   + sb.toString()
                   + ServletUtils.footer());
        pw.close();
    }

    private void menu(HttpServletRequest req, BundleHandler rb, StringBuilder sb) {
        String value = req.getServletPath();
        String[] modules = {
            "/settings/profile",
            "/settings/password",
            "/settings/email"
        };

        sb.append("<div class=\"pure-menu pure-menu-horizontal\">\n");
        sb.append("<ul class=\"pure-menu-list\">\n");
        for (String module : modules) {
            sb.append("<li class=\"pure-menu-item");
            if (value.equals(module)) sb.append(" pure-menu-selected");
            sb.append("\"><a href=\""+module+"\" class=\"pure-menu-link\">"+rb.get(module)+"</a></li>\n");
        }
        sb.append("</ul>\n");
        sb.append("</div> <!-- /.pure-menu -->\n");
    }
}
