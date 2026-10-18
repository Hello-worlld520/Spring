package spring;

import java.io.File;
import java.net.URL;

public class AAAapplicationContext {
    private Class Bukaixinclass;
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
                                //Bean
                            }
                        }catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        public Object getBean(){
            return null;
        }
    }
}
/*如果类里面有@ComponentScan，就开始扫描，先用类加载器拿到绝对路径，然后创建file对象存里面，看是不是文件夹，是文件夹就存file数组里，用for循环，
由绝对路径拿到包名，再用clazz.isAnnotationPresent(ConponetScan.class)判断是不是Bean
 */
