package service;

import spring.BeanPostProcesser;
import spring.Component;

@Component
public class XiaoyuanBeanPostProcesser implements BeanPostProcesser {


    @Override
    public Object postProcesserBeforeInitialization(String beanName, Object bean) {
        //，拿到了名字和Bean对象，可以针对Bean做一些事情
        return bean;
    }

    @Override
    public Object postProcesserAfterInitialization(String beanName, Object bean) {

        //我这里甚至可以换一个对象，也就是说可以实现AOP的代理对象

      return  bean;
    }
}
