package spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AAAapplicationContext {
    private Class Bukaixinclass;
    private ConcurrentMap<String,BeanDefinition>beanDefinitionConcurrentMap=new ConcurrentHashMap<String,BeanDefinition>();
    //这个属性是存储Bean的
    private  ConcurrentHashMap<String,Object>singletonObjects=new ConcurrentHashMap<>();
    //这是一个单例池
    private ArrayList<BeanPostProcesser>beanPostProcesserList=new ArrayList<>();
    //用于存放自己写的那个Bean处理器类
    public AAAapplicationContext(Class appConfig) {
        this.Bukaixinclass=appConfig;
        if (appConfig.isAnnotationPresent(ConponetScan.class))
        {
            ConponetScan conponetScanAnnotation =(ConponetScan) appConfig.getAnnotation(ConponetScan.class)  ;
            String path =conponetScanAnnotation.value();
            path = path.replace(".","/");

            ClassLoader classLoader = AAAapplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        String className=fileName.substring(fileName.indexOf("com"),fileName.indexOf(".class"));
                        //这里是直接切割的，实际实现不这样
                        className =className.replace("\\",".");
                        System.out.println(className);
                        try {
                            Class clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(ConponetScan.class)){
                                //到这就是扫描判断完成了，开始把类解析成 Bean 定义并注册到容器


                                //BeanPostProcesser的生成
                                if (BeanPostProcesser.class.isAssignableFrom(clazz)){
                                    BeanPostProcesser instance = clazz.newInstance();
                                    beanPostProcesserList.add(instance);
                                }
                                //为什么BeanPostProcesser作为一个Bean要单独放置并且单独提前生成，为什么不走其他Bean的流程
                                //普通 Bean 是“被处理的对象”，而 BeanPostProcessor 是“处理别人的工具”
                                //先生成它才能处理别人，它的职责是拦截容器中其他所有 Bean 的创建过程，在它们的初始化前后“插一脚”，所以要最先生成


                                //那它为什么要作为一个Bean存在呢，直接放在流程里写成方法不好吗
                                //它是对Bean的初始化前后进行的处理，写成Bean就可以由程序员定义
                                // 如果直接把 BeanPostProcessor 的逻辑硬编码进 Bean 的创建流程里，Spring 就失去了让用户在不修改框架源码的前提下，无限扩展容器行为的能力。


                                //Bean

                                Component component =(Component) clazz.getAnnotation(Component.class);
                                String beanName=component.value();
                                if(beanName.equals("")) {
                                    beanName= Introspector.decapitalize(clazz.getSimpleName());


                                }
                                BeanDefinition beanDefinition=new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class))
                                {
                                    Scope scopeAnnotation =(Scope) clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                }else{
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionConcurrentMap.put(beanName,beanDefinition);
                            }
                        }catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
             //创建单例bean
            for ( String beanName : beanDefinitionConcurrentMap.keySet()) {
                BeanDefinition beanDefinition = beanDefinitionConcurrentMap.get(beanName);
                if(beanDefinition.getScope().equals("singleton")){
                    Object bean=createBean(beanName ,beanDefinition);
                }

            }
        }

    }
    private Object createBean(String beanName,BeanDefinition beanDefinition){
        Class clazz=beanDefinition.getType();
        try {
            Object instance = clazz.getConstructor().newInstance();
            //依赖注入
            for (Field f : clazz.getDeclaredFields()) {
                if(f.isAnnotationPresent(Autowired.class)){
                    f.setAccessible(true);
                    f.set(instance,getBean(f.getName()));
                }

            }

            //aware回调
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }


            //初始化前调用BeanPostProcesser
            for (BeanPostProcesser beanPostProcesser:beanPostProcesserList){
                beanPostProcesser.postProcesserBeforeInitialization(beanName,instance);
            }


            //初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }


            //初始化后调用BeanPostProcesser
            for (BeanPostProcesser beanPostProcesser:beanPostProcesserList){
                beanPostProcesser.postProcesserAfterInitialization(beanName,instance);
            }

            //aware回调和初始化都是，监听接口实现，然后执行方法，两者的区别是
            // 回调能够从spring拿到东西，就是beanName，初始化就是单纯执行方法


            //BeanPostProcessor(Bean的后置处理器）  初始化后AOP





            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


        return null;
    }


    public Object getBean(String beanName){//这里就是在定义beanName,传进来的参数被赋值成beanName
        BeanDefinition beanDefinition= beanDefinitionConcurrentMap.get(beanName);

        if(beanDefinition==null){
            throw new NullPointerException("beanName is null");

        }else {
            String scope=beanDefinition.getScope();
            if(scope.equals("singleton")){
                Object  bean= singletonObjects.get(beanName);
                if (bean==null)
                {
                    Object  bean=createBean(beanName,beanDefinition);
                    singletonObjects.put(beanName,0);
                }
                return bean;

            }else {
                //多例
                return createBean(beanName,beanDefinition);
            }
        }


        return null;
    }
}
/*如果类里面有@ComponentScan，就开始扫描，先用类加载器拿到绝对路径，然后创建file对象存里面，看是不是文件夹，是文件夹就存file数组里，用for循环，
由绝对路径拿到包名，再用clazz.isAnnotationPresent(ConponetScan.class)判断是不是Bean
 */
