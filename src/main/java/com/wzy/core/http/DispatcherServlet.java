package com.wzy.core.http;

import com.wzy.core.annotation.Autowired;
import com.wzy.core.annotation.Controller;
import com.wzy.core.annotation.RequertMapping;
import com.wzy.core.annotation.Serivce;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    final List<String> clazzNames = new ArrayList<String>();
    final Map<String, Object> beans = new HashMap<String, Object>();
    final Map<String, Object> handleMethod = new HashMap<String, Object>();

    public DispatcherServlet() {super();}

    @Override
    public void init() throws ServletException {

        try {
            //读取配置文件（也可以从zk上读取）
            Properties file = new Properties();
            file.load(new FileInputStream(new File("H:\\workspaceForIdea\\MySpringMVC\\src\\main\\resources\\application.properties")));
            String scanPath = file.getProperty("package.scan", "");

            // 1. 包扫描
            scanPackage(scanPath);

            //2. 类实例化
            classInstance();

            //3. 依赖注入
            inject();

            //4. 映射
            handleMapping();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleMapping() {
        if (beans.isEmpty()) {return;}

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            //获取实例
            Object instance = entry.getValue();

            if (instance.getClass().isAnnotationPresent(Controller.class)) {
                //获取类上的RequertMapping
                RequertMapping requertMapping = instance.getClass().getAnnotation(RequertMapping.class);
                //获取RequertMapping定义的路径值
                String path = requertMapping.value();
                //System.out.println("requertMapping: " +instance.getClass().getName()+"  "+ path);

                //获取类中的所有方法
                Method [] methods = instance.getClass().getMethods();
                for (Method method : methods) {
                    //获取方法上的RequertMapping
                    RequertMapping methodMapping = method.getAnnotation(RequertMapping.class);
                    if (methodMapping == null) {
                        //System.out.println(method.getName()+ " methodMapping is null");
                        continue;
                    }
                    String value = methodMapping.value();
                    //System.out.println("methodMapping: " +method.getName()+"  "+ value);
                    //绑定url方法映射
                    handleMethod.put(path + value, method);

                }
            }
        }
    }

    private void inject() throws IllegalAccessException {
        if (beans.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry :  beans.entrySet()) {
            //获取实例
            Object instance = entry.getValue();
            //获取类中的所有成员属性
            Field [] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                //判断成员属性有无@Autowired
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = (Autowired)field.getAnnotation(Autowired.class);
                    String value = autowired.value();
                    //System.out.println("autowired: "+value);
                    field.setAccessible(true);

                    //给属性注入实例
                    field.set(instance, beans.get(value));
                }
            }
        }
    }

    private void classInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (clazzNames.isEmpty()) {return;}

        for (String name : clazzNames) {
            String realName = name.replace(".class","");
            //System.out.println("---> "+realName);
            Class clazz = Class.forName(realName);
            Object instance = clazz.newInstance();//java.lang.InstantiationException: com.wzy.core.annotation.Autowired

            //当前类上是否标注了@Controller
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controller = (Controller) clazz.getAnnotation(Controller.class);

                //当前类上是否标注了@RequertMapping
                if (clazz.isAnnotationPresent(RequertMapping.class)) {
                    RequertMapping requertMapping = (RequertMapping) clazz.getAnnotation(RequertMapping.class);
                    String mappingValue = requertMapping.value();

                    //把映射路径和类实例绑定
                    beans.put(mappingValue, instance);
                }
            }

            //当前类上是否标注了@Serivce
            if (clazz.isAnnotationPresent(Serivce.class)) {
                Serivce serivce = (Serivce) clazz.getAnnotation(Serivce.class);
                //@Serivce("userSer") 注意：若值为空，则取类的名字，首字母小写
                beans.put(serivce.value(), instance);
            }
        }

    }

    private void scanPackage (String basePath) {
        String path = basePath.replaceAll("\\.","/");
        URL url = this.getClass().getClassLoader().getResource(path);
        //递归扫描
        String filePath = url.getFile();
        File [] files = new File(filePath).listFiles();
        for (File file : files) {
            //是否目录
            if (file.isDirectory()) {
                scanPackage(basePath + "." + file.getName());
            } else {
                //类是以"."格式分割
                clazzNames.add(basePath + "." + file.getName());
            }
        }
        
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //new HttpServerHandler().hander(req,resp);

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.replaceAll(contextPath, "");
        Method method = (Method) handleMethod.get(path);
        Object instance = beans.get("/" + path.split("/")[1]);

        try {
            //这里假设没有参数
            Object object = method.invoke(instance,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }
}
