package spring;

public interface BeanPostProcesser {

    public Object postProcesserBeforeInitialization(String beanName,Object bean);

    public Object postProcesserAfterInitialization(String beanName,Object bean);



}
