# SPI 机制

SPI 全称为 Service Provider Interface，是一种服务发现机制。SPI 的本质是将接口实现类的全限定名配置在文件中，并由服务加载器读取配置文件，加载实现类。这样可以在运行时，动态为接口替换实现类。正因此特性，可以很容易的通过 SPI 机制为我们的程序提供拓展功能。

## SPI实现

- SPI 注解

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
```

- `getExtension` 根据名称获取实现类

```java
public T getExtension(String name) {
    if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("Extension name is null or empty");
    }
    // create one if not be found from cache
    Holder<Object> holder = cachedInstances.get(name);
    if (holder == null) {
        cachedInstances.putIfAbsent(name, new Holder<>());
        holder = cachedInstances.get(name);
    }
    // create a singleton using double check
    Object instance = holder.getObject();
    if (instance == null) {
        synchronized (holder) {
            instance = holder.getObject();
            if (instance == null) {
                instance = createExtension(name);
                holder.setObject(instance);
            }
        }
    }
    return (T) instance;
}
```

- `createExtension` 根据名称创建实现类
  - 本质是根据全限定名获取 Class 实例，然后反射创建实现类

```java
private T createExtension(String name) {
    // get all extension classes of T type & get one by name
    Class<?> clazz = getExtensionClasses().get(name);
    if (clazz == null) {
        throw new RuntimeException("No such extension [ " + name + " ]");
    }
    T instance = ((T) EXTENSION_INSTANCES.get(clazz));
    if (instance == null) {
        try {
            EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
            instance = ((T) EXTENSION_INSTANCES.get(clazz));
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage());
        }
    }
    return instance;
}
```

- `getExtensionClasses ` 源码

```java
private Map<String, Class<?>> getExtensionClasses() {
    // get loaded extension from cache
    Map<String, Class<?>> classes = cachedClasses.getObject();
    // double check
    if (classes == null) {
        synchronized (cachedClasses) {
            classes = cachedClasses.getObject();
            if (classes == null) {
                classes = new HashMap<>();
                // load all extensions from extension path
                loadPath(classes);
                cachedClasses.setObject(classes);
            }
        }
    }
    return classes;
}
```

## Reference

[https://dubbo.apache.org/zh/docs/v2.7/dev/source/dubbo-spi/](https://dubbo.apache.org/zh/docs/v2.7/dev/source/dubbo-spi/)