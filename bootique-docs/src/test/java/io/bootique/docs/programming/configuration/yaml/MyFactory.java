package io.bootique.docs.programming.configuration.yaml;


import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.docs.programming.configuration.MyService;

// tag::MyFactory[]
@BQConfig
public class MyFactory {

    private int intProperty;
    private String stringProperty;

    @BQConfigProperty
    public void setIntProperty(int i) {
        this.intProperty = i;
    }

    @BQConfigProperty
    public void setStringProperty(String s) {
        this.stringProperty = s;
    }

    // factory method
    public MyService createMyService(SomeOtherService soService) {
        return new MyServiceImpl(soService, intProperty, stringProperty);
    }
}
// end::MyFactory[]


