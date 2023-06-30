package com.apollo.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;

import com.apollo.init.TargetObjectBeanManager;
import com.google.common.collect.Maps;

/**
 * 自定义服务加载实现
 *
 * @author liguang
 */
public class SelfServiceLoader {

    /**
     * apollo管理对象默认地址
     */
    private static final String PREFIX = "META-INF/";

    private static Map<String, Enumeration<URL>> configMap = Maps.newConcurrentMap();

    /**
     * 加载目标对象的管理工厂对象
     *
     * @return
     */
    public static void load(Class<?> cls) {
        if (configMap.containsKey(cls.getName())) {
            return;
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        cl = cl == null ? ClassLoader.getSystemClassLoader() : cl;
//        Class<InitObjectFactory> service=InitObjectFactory.class;
        Enumeration<URL> configs = null;
        try {
            configs = cl.getResources(PREFIX + cls.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (configs != null) {
            configMap.put(cls.getName(), configs);
        }
        while (configs.hasMoreElements()) {
            List<String> list = parse(cls, configs.nextElement());
            for (String readline : list) {
                parseInitObjectFactory(cls, readline);
            }
        }
    }

    /**
     * 解析具体初始化工厂对象
     *
     * @param readLine
     * @return
     */
    private static void parseInitObjectFactory(Class<?> service, String readLine) {
        String[] strs = readLine.split("=");
        if (strs == null || strs.length <= 1) {
            fail(service, "配置异常，必须是namespace=具体工厂全路径，异常信息：" + readLine);
        }
        try {
            Class<?> cls = Class.forName(strs[1]);
            TargetObjectBeanManager.addInitObjectFactory(strs[0], service.cast(cls.newInstance()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析目标文件
     *
     * @param service
     * @param u
     * @return
     * @throws ServiceConfigurationError
     */
    private static List<String> parse(Class<?> service, URL u) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        List<String> names = new ArrayList<String>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names)) >= 0) {
            }
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }
        return names;
    }


    /**
     * 解析每一行数据
     *
     * @param service
     * @param u
     * @param r
     * @param lc
     * @param names
     * @return
     * @throws IOException
     * @throws ServiceConfigurationError
     */
    private static int parseLine(Class<?> service, URL u, BufferedReader r, int lc,
                                 List<String> names) throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
                fail(service, u, lc, "Illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
//            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
//                cp = ln.codePointAt(i);
//                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')){
//                    fail(service, u, lc, "Illegal provider-class name: " + ln);
//                }
//
//            }
            if (!names.contains(ln)) {
                names.add(ln);
            }
        }
        return lc + 1;
    }


    /**
     * 抛出加载失败异常
     *
     * @param service
     * @param msg
     * @throws ServiceConfigurationError
     */
    private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    /**
     * 抛出加载失败异常
     *
     * @param service
     * @param u
     * @param line
     * @param msg
     * @throws ServiceConfigurationError
     */
    private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }


    /**
     * 抛出加载失败异常
     *
     * @param service
     * @param msg
     * @param cause
     * @throws ServiceConfigurationError
     */
    private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

}
