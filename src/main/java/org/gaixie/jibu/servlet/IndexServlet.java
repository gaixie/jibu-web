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

import org.gaixie.jibu.model.User;
import org.gaixie.jibu.service.UserService;
import org.gaixie.jibu.utils.BundleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 首页 Servlet。
 */
@Singleton
public class IndexServlet extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(IndexServlet.class);

    @Inject
    private Injector injector;

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        loadPage(req, res);
    }

    public void doPost(HttpServletRequest req,  HttpServletResponse res)
        throws IOException{
        doGet(req, res);
    }

    protected void loadPage(HttpServletRequest req, HttpServletResponse res) 
        throws IOException {
        String username = ServletUtils.getUsername(req);

        BundleHandler rb = new BundleHandler(ServletUtils.getLocale(req));
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"content\">\n");
        sb.append("</div> <!-- /.content -->\n");

        PrintWriter pw = res.getWriter();
        pw.println(ServletUtils.head(rb.get("index.title"))
                   + ServletUtils.body(req, (username != null ? username : rb.get("index.title")))
                   + sb.toString() 
                   + ServletUtils.footer());
        pw.close();
    }
}
