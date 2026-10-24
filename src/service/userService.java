package service;

import spring.Autowired;
import spring.BeanNameAware;
import spring.Component;
import spring.InitializingBean;

@Component()
public class userService implements BeanNameAware, InitializingBean {

    @Autowired
    private OrderService orderService;
    private String beanName;
    private String XXX;


    @Override
    public void setBeanName(String beanName) {

        this.beanName=beanName;
        //具体的实现逻辑是在业务里写的，spring的aware回调只是把调用函数的控制权交给框架，由框架传一个beanName并调用函数

    }


    @Override
    public void afterPropertiesSet() {
        //初始化方法，写自己的业务逻辑
    }
}
