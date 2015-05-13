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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import org.gaixie.jibu.JibuException;
import org.gaixie.jibu.model.Criteria;
import org.gaixie.jibu.utils.BeanConverter;
import org.gaixie.jibu.utils.BundleHandler;

/**
 * Servlet 工具类。
 */
public class ServletUtils {

    public static final String COMPANY = "NodTo";
    public static final String[] SIGNED_MENU = {
        "/settings"
    };

    /**
     * 可以公用的 head 部分的 html 片段。
     *
     * @param title title 标签内容。
     * @return 公用的 head 部分的 html 片段
     */
    public static String head(String title) {
        StringBuilder sb = new StringBuilder();
        //--------------------------------------------
        // 执行下面的命令生成html输出的Java语句，如果页面太长，可以直接指定输出前 9 行：
        // mac osx 下 sed 语法和 linux 不同，执行下面的命令：
        // curl http://ui.nodto.cn/signin.html | head -n 9 | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//;s/"/\\"/g;s/.*/sb.append("&\\n");/'
        // curl http://ui.nodto.cn/signin.html | sed '1,9!d;s/^[[:blank:]]*//;s/[[:blank:]]*$//;s/"/\\"/g;s/.*/sb.append("&\\n");/'
        // -------------------------------------------
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("<meta charset=\"utf-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        sb.append("<title>"+COMPANY+" - "+title+"</title>\n");
        sb.append("<link rel=\"stylesheet\" href=\"http://cdn.bootcss.com/pure/0.6.0/pure-min.css\">\n");
        sb.append("<link rel=\"stylesheet\" href=\"http://cdn.bootcss.com/pure/0.6.0/grids-responsive-min.css\">\n");
        sb.append("<link rel=\"stylesheet\" href=\"/css/er.css\">\n");

        return sb.toString();
    }

    /**
     * 得到从 head 结束到正文 #main.header 内容之前的 html 片段。
     *
     * @param req HttpServletRequest
     */
    public static String body(HttpServletRequest req, String title) {
        HttpSession ses = req.getSession(false);
        String value = req.getServletPath();
        Locale locale = ServletUtils.getLocale(req);
        BundleHandler rb = new BundleHandler(locale);
        StringBuilder sb = new StringBuilder();
        //--------------------------------------------
        // 执行下面的命令生成html输出的Java语句，如果页面太长，可以直接指定输出 10 ~ 24 行：
        // mac osx 下 sed 语法和 linux 不同，执行下面的命令：
        // curl http://ui.nodto.cn/signin.html | sed '10,24!d;s/^[[:blank:]]*//;s/[[:blank:]]*$//;s/"/\\"/g;s/.*/sb.append("&\\n");/'
        // -------------------------------------------
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<div id=\"wrapper\">\n");
        sb.append("<div id=\"menu\">\n");
        sb.append("<div class=\"pure-menu\">\n");
        sb.append("<a class=\"pure-menu-heading\" href=\"/\">"+COMPANY+"</a>\n");
        sb.append("<ul class=\"pure-menu-list\">\n");

        if (ses != null) {
            for (String module : SIGNED_MENU) {
                sb.append("<li class=\"pure-menu-item");
                if (value.startsWith(module)) sb.append(" pure-menu-selected");
                sb.append("\"><a href=\""+module+"\" class=\"pure-menu-link\">"+rb.get(module)+"</a></li>\n");
            }
            sb.append("<li class=\"pure-menu-item\"><a href=\"/signout\" class=\"pure-menu-link\">"+rb.get("/signout")+"</a></li>\n");
        } else {
            sb.append("<li class=\"pure-menu-item");
            if (value.equals("/signin")) sb.append(" pure-menu-selected");
            sb.append("\"><a href=\"/signin\" class=\"pure-menu-link\">"+rb.get("/signin")+"</a></li>\n");
        }

        sb.append("</ul>\n");
        sb.append("</div> <!-- /.pure-menu -->\n");
        sb.append("</div> <!-- /#menu -->\n");
        sb.append("\n");
        sb.append("<div id=\"main\">\n");
        sb.append("<a href=\"javascript:;\" class=\"menu-button\"><span class=\"burger-icon\"></span></a>\n");
        sb.append("\n");
        sb.append("<div class=\"header\">\n");
        sb.append("<legend>"+title+"</legend>\n");
        sb.append("</div> <!-- /.header -->\n");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * 得到从 #main.content 结束到页面最后的 html 片段。
     *
     */
    public static String footer() {
        StringBuilder sb = new StringBuilder();
        //--------------------------------------------
        // 执行下面的命令生成html输出的Java语句，指定输出最后 24 行：
        // mac osx 下 sed 语法和 linux 不同，执行下面的命令：
        // curl http://ui.nodto.cn/signin.html | tail -n 24 | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//;s/"/\\"/g;s/.*/sb.append("&\\n");/'
        // -------------------------------------------
        sb.append("\n");
        sb.append("<div class=\"footer\">\n");
        sb.append("<div class=\"pure-g\">\n");
        sb.append("<div class=\"pure-u-1 pure-u-md-1-2\">\n");
        sb.append("<ul class=\"legal-language\">\n");
        sb.append("<li><a href=\"?locale=en\">English</a></li>\n");
        sb.append("<li><a href=\"?locale=zh_CN\">简体中文</a></li>\n");
        sb.append("</ul>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"pure-u-1 pure-u-md-1-2\">\n");
        sb.append("<p class=\"legal-copyright\">\n");
        sb.append("© 2014 NodTo Inc. All rights reserved.\n");
        sb.append("</p>\n");
        sb.append("</div>\n");
        sb.append("</div> <!-- /.pure-g -->\n");
        sb.append("</div> <!-- /.footer -->\n");
        sb.append("</div> <!-- /#main -->\n");
        sb.append("</div> <!-- /#wrapper -->\n");
        sb.append("\n");
        sb.append("<script src=\"http://cdn.bootcss.com/jquery/2.1.3/jquery.min.js\"></script>\n");
        sb.append("<script src=\"/js/er.js\"></script>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * 将 HttpServletRequest 中的值按照约定自动装入指定的 Javabean。
     * <p>
     * 如果 request 中的 name 满足 ClassName.property 的格式，会自动
     * 取出对应的 value 并装入Bean 相应属性 。如：User.password=123456，
     * 那么会将 123456 set 到 User 的 password 属性中。 </p>
     *
     * @param cls 要生成的 Bean Class。
     * @param req HttpServletRequest
     * @return 生成的 Javabean
     */
    public static <T> T httpToBean(Class<T> cls, HttpServletRequest req)
        throws JibuException {
        Map<String,String> map = new HashMap<String,String>();
        Enumeration names = req.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = req.getParameter(name);
            int idx = name.lastIndexOf('.');
            if (!value.trim().isEmpty() && idx > 0) {
                map.put(name.substring(idx+1), value);
            }
        }
        T bean = BeanConverter.mapToBean(cls, map);
        return bean;
    }

    /**
     * 将 HttpServletRequest 中的值按照约定自动装入指定的 Javabean。
     * <p>
     * 如果 request 中的 name 满足 ClassName.property 的格式，会自动
     * 取出对应的 value 并装入Bean 相应属性 。如：User.password=123456，
     * 那么会将 123456 set 到 User 的 password 属性中。 </p>
     *
     * @param cls 要生成的 Bean Class。
     * @param req HttpServletRequest
     * @param hasPre 判断 req 中参数名是否用类名做前缀， true 含前缀，false 不含前缀
     * @return 生成的 Javabean
     */
    public static <T> T httpToBean(Class<T> cls, HttpServletRequest req, boolean hasPre)
        throws JibuException {
        if(hasPre) return httpToBean(cls,req);

        Map<String,String> map = new HashMap<String,String>();
        Enumeration names = req.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = req.getParameter(name);
            if (!value.trim().isEmpty())
                map.put(name, value);
        }
        T bean = BeanConverter.mapToBean(cls, map);
        return bean;
    }

    /**
     * 将 HttpServletRequest 中用于分页和排序的值按照约定自动装入 Criteria 对象。
     * <p>
     * 接收的参数有： limit , start , dir , sort, sortOthers。
     * 如果 limit 的值大于 0 ，分页有效。如果 sort 非 null，排序有效。
     * 如果默认的 sort 属性不能唯一标识一行，需要使用 sortOthers 属性来对其它列排序，
     * 参数的格式如 "serial_no, created_ts"。</p>
     *
     * @param req HttpServletRequest
     * @return 如果没有有效的 limit 和 sort，返回 null，否则返回 Criteria 实例。
     */
    public static Criteria httpToCriteria(HttpServletRequest req) {
        Criteria criteria = new Criteria();

        if (null != req.getParameter("limit") &&
            null != req.getParameter("start")) {

            int limit = Integer.parseInt(req.getParameter("limit"));
            int start = Integer.parseInt(req.getParameter("start"));
            criteria.setLimit(limit);
            criteria.setStart(start);
        }

        criteria.setDir(req.getParameter("dir"));
        criteria.setSort(req.getParameter("sort"));

        if (criteria.getSort() != null && null != req.getParameter("sortOthers")) {
            criteria.setSort(criteria.getSort()+","+req.getParameter("sortOthers"));
        }

        if (criteria.getLimit() <=0 || criteria.getSort() == null) {
            return null;
        }
        return criteria;
    }

    /**
     * 从 Session 中得到当前用户的用户名 。
     *
     * @param req HttpServletRequest。
     * @return username
     */
    public static String getUsername(HttpServletRequest req) {
        HttpSession ses = req.getSession(false);
        String username = null;
        if (null!=ses) {
            username = (String) ses.getAttribute("username");
        }
        return username;
    }

    /**
     * 从 HttpServletRequest 中的 header 解码用户验证信息。
     *
     * @param req HttpServletRequest
     * @return username and password
     */
    public static String[] decodeBasicAuth(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth==null) return null;

        //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
        auth = auth.replaceFirst("[B|b]asic ", "");
        //Decode the Base64 into byte[]
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);
        //If the decode fails in any case
        if(decodedBytes == null || decodedBytes.length == 0){
            return null;
        }
        //Now we can convert the byte[] into a splitted array :
        // - the first one is login,
        // - the second one password
        return new String(decodedBytes).split(":", 2);
    }

    /**
     * 取得给定 cookie 名的值
     *
     * @param req HttpServletRequest
     * @param name cookie 名称
     * @return 指定 cookie 的值
     */
    public static String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 设置 cookie
     *
     * @param res HttpServletResponse
     * @param name cookie 名称
     * @param value cookie 的值
     * @param maxAge cookie 有效期
     */
    public static void addCookie(HttpServletResponse res, 
                                 String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        res.addCookie(cookie);
    }

    /**
     * 删除给定名称的 cookie
     *
     * @param res HttpServletResponse
     * @param name cookie 名称
     */
    public static void removeCookie(HttpServletResponse res, String name) {
        addCookie(res, name, null, 0);
    }

    /**
     * 从 Cookie 中得到设定的 Locale 。
     *
     * @param req HttpServletRequest
     * @return locale，如果 cookie 中没有设置 locale，返回客户端默认的Locale
     */
    public static Locale getLocale(HttpServletRequest req) {
        String localeCode = req.getParameter("locale");

        if (localeCode == null)
            localeCode = getCookieValue(req, "user_locale");

        if (localeCode != null) {
            return convertToLocale(localeCode);
        }

        return req.getLocale();
    }

    /**
     * 将一个字符串转化为 Locale 。
     *
     * @param localeCode 用于转化为 Locale对象的字符串，格式如 zh_CN, en 等。
     * @return Locale
     */
    public static Locale convertToLocale(String localeCode) {
        Locale locale = null;
        String[] elements = localeCode.split("_");

        switch (elements.length) {
        case 1:
            locale = new Locale(elements[0]);
            break;
        case 2:
            locale = new Locale(elements[0], elements[1]);
            break;
        case 3:
            locale = new Locale(elements[0], elements[1], elements[2]);
            break;
        default:
            throw new RuntimeException("Can't handle localeCode = \"" + localeCode + "\".  Elements.length = " + elements.length);
        }
        return locale;
    }
}
