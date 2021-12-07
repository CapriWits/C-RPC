package me.hypocrite30.rpc.core.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * The Method「scan(String... basePackages)」 is core of scanning
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/6 22:19
 */
public class MyClassPathBeanScanner extends ClassPathBeanDefinitionScanner {

    public MyClassPathBeanScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        // add filter by annotation type
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    /**
     * scan from basePackages
     *
     * @param basePackages base packages
     * @return number of scanned
     */
    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }

}
