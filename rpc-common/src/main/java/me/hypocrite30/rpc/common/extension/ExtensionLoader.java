package me.hypocrite30.rpc.common.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * SPI extension loader
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/2 22:23
 */
@Slf4j
public class ExtensionLoader<T> {

    private static final String SERVICE_PATH = "META-INF/services/";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    private final Class<?> type;
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /**
     * must assign to ExtensionLoader the class type which wants to be loaded
     */
    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * To get ExtensionLoader according to extension type.
     *
     * @param type Extension class type
     * @return ExtensionLoader
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type is null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type [ " + type + " ] is not an interface");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type [ " + type + " ] is not annotated with @" + SPI.class.getName());
        }
        // create one if not be found from cache
        ExtensionLoader<?> extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return (ExtensionLoader<T>) extensionLoader;
    }

    /**
     * To get Extension according to service name
     *
     * @param name Extension name
     * @return Extension instance
     */
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

    private void loadPath(Map<String, Class<?>> extensionClasses) {
        String fileName = ExtensionLoader.SERVICE_PATH + this.type.getName();
        try {
            Enumeration<URL> urls;
            // must use the same ClassLoader to load extensions
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            // load resources from fileName path
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // read every line
            while ((line = reader.readLine()) != null) {
                // get index of comment
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // string after # is comment so we ignore it
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // SPI use key-value pair and both must not be empty
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
