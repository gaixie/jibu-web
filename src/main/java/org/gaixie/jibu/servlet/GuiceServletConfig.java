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

import org.gaixie.jibu.JibuConfig;
import org.gaixie.jibu.dao.DAOModule;
import org.gaixie.jibu.service.ServiceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

/**
 * GuiceServletConfig 是一个 Listener 类，在应用 deploy 时被触发。
 * <p>
 * 这个类应该在 web.xml 中配置，如：</p>
 *
 * <pre>
 * {@code
 * <listener>
 *   <listener-class>org.gaixie.jibu.servlet.GuiceServletConfig</listener-class>
 * </listener>
 * }
 * </pre>
 *
 * 由于不同的应用，需要 Guice 注入的内容不同，所以应该有自己的 GuiceServletConfig。
 */
public class GuiceServletConfig extends GuiceServletContextListener {

    /**
     * 得到一个 Injector。
     * <p>
     * 将所有 Jibu 中需要 Guice 注入的类初始化到 Injector。</p>
     *
     * @return 实例化的 Injector
     */
    @Override
    protected Injector getInjector() {
        String databaseType = JibuConfig.getProperty("databaseType");
        return Guice.createInjector(new ServiceModule(),
                                    new DAOModule(databaseType),
                                    new ServletModule() {
                @Override
                protected void configureServlets() {
                    // 带后缀的文件都不过SigninFilter
                    // filter("/*").through(SigninFilter.class);
                    filterRegex("^([^.]+)$").through(SigninFilter.class);
                    serve("/").with(IndexServlet.class);
                    serve("/signin","/session","/signout",
                          "/forgot","/forgotreset").with(SigninServlet.class);
                    serve("/settings","/settings/profile",
                          "/settings/password","/settings/email").with(SettingServlet.class);
                }
            });
    }
}
