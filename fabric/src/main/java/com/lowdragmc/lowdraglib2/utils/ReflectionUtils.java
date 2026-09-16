package com.lowdragmc.lowdraglib2.utils;

import com.lowdragmc.lowdraglib2.LDLib2;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@UtilityClass
public final class ReflectionUtils {

    public static Class<?> getRawType(Type type, Class<?> fallback) {
        var rawType = getRawType(type);
        return rawType != null ? rawType : fallback;
    }

    public static Class<?> getRawType(Type type) {
        return switch (type) {
            case Class<?> aClass -> aClass;
            case GenericArrayType genericArrayType -> getRawType(genericArrayType.getGenericComponentType());
            case ParameterizedType parameterizedType -> getRawType(parameterizedType.getRawType());
            case null, default -> null;
        };
    }

    /**
     * Finds all classes annotated with the given annotation by scanning mod JARs via ASM.
     * This is the Fabric equivalent of NeoForge's ModFileScanData-based scanning.
     */
    public static <A extends Annotation> void findAnnotationClasses(Class<A> annotationClass,
                                                                    @Nullable Predicate<Map<String, Object>> annotationPredicate,
                                                                    Consumer<Class<?>> consumer,
                                                                    Runnable onFinished) {
        String annotationDescriptor = org.objectweb.asm.Type.getType(annotationClass).getDescriptor();

        scanModJars((classNode, modPath) -> {
            if (classNode.visibleAnnotations == null) return;
            for (var ann : classNode.visibleAnnotations) {
                if (annotationDescriptor.equals(ann.desc)) {
                    if (annotationPredicate == null || annotationPredicate.test(convertAnnotationValues(ann))) {
                        try {
                            String className = classNode.name.replace('/', '.');
                            consumer.accept(Class.forName(className, false, ReflectionUtils.class.getClassLoader()));
                        } catch (Throwable throwable) {
                            LDLib2.LOGGER.error("Failed to load class: {}", classNode.name, throwable);
                        }
                    }
                    break;
                }
            }
        });

        onFinished.run();
    }

    /**
     * Finds all static fields annotated with the given annotation by scanning mod JARs via ASM.
     */
    public static <A extends Annotation> void findAnnotationStaticField(Class<A> annotationClass,
                                                                        @Nullable Predicate<Map<String, Object>> annotationPredicate,
                                                                        BiConsumer<Field, Object> consumer,
                                                                        Runnable onFinished) {
        String annotationDescriptor = org.objectweb.asm.Type.getType(annotationClass).getDescriptor();

        scanModJars((classNode, modPath) -> {
            String className = classNode.name.replace('/', '.');
            for (var field : classNode.fields) {
                if (field.visibleAnnotations != null) {
                    for (var ann : field.visibleAnnotations) {
                        if (annotationDescriptor.equals(ann.desc)) {
                            if (annotationPredicate == null || annotationPredicate.test(convertAnnotationValues(ann))) {
                                try {
                                    var clazz = Class.forName(className, false, ReflectionUtils.class.getClassLoader());
                                    var javaField = clazz.getDeclaredField(field.name);
                                    if (Modifier.isStatic(javaField.getModifiers())) {
                                        javaField.setAccessible(true);
                                        consumer.accept(javaField, javaField.get(null));
                                    } else {
                                        LDLib2.LOGGER.error("Field is not static: {} in {}", field.name, className);
                                    }
                                } catch (Throwable throwable) {
                                    LDLib2.LOGGER.error("Failed to load static field: {} in {}", field.name, className, throwable);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        });

        onFinished.run();
    }

    /**
     * Finds all static methods annotated with the given annotation by scanning mod JARs via ASM.
     */
    public static <A extends Annotation> void findAnnotationStaticMethod(Class<A> annotationClass,
                                                                         @Nullable Predicate<Map<String, Object>> annotationPredicate,
                                                                         Consumer<Method> consumer,
                                                                         Runnable onFinished) {
        String annotationDescriptor = org.objectweb.asm.Type.getType(annotationClass).getDescriptor();

        scanModJars((classNode, modPath) -> {
            String className = classNode.name.replace('/', '.');
            for (var method : classNode.methods) {
                if (method.visibleAnnotations != null) {
                    for (var ann : method.visibleAnnotations) {
                        if (annotationDescriptor.equals(ann.desc)) {
                            if (annotationPredicate == null || annotationPredicate.test(convertAnnotationValues(ann))) {
                                try {
                                    var clazz = Class.forName(className, false, ReflectionUtils.class.getClassLoader());
                                    for (var javaMethod : clazz.getDeclaredMethods()) {
                                        if (javaMethod.getName().equals(method.name) &&
                                                method.desc.equals(org.objectweb.asm.Type.getMethodDescriptor(javaMethod))) {
                                            if (Modifier.isStatic(javaMethod.getModifiers())) {
                                                javaMethod.setAccessible(true);
                                                consumer.accept(javaMethod);
                                            } else {
                                                LDLib2.LOGGER.error("Method is not static: {} in {}", method.name, className);
                                            }
                                            break;
                                        }
                                    }
                                } catch (Throwable throwable) {
                                    LDLib2.LOGGER.error("Failed to load static method: {} in {}", method.name, className, throwable);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        });

        onFinished.run();
    }

    // ============================================================
    // Internal ASM-based classpath scanning
    // ============================================================

    @FunctionalInterface
    private interface ClassVisitor {
        void visit(ClassNode classNode, Path jarPath);
    }

    /**
     * Scans all mod JARs from FabricLoader for class nodes.
     */
    private static void scanModJars(ClassVisitor visitor) {
        var scanned = new HashSet<Path>();
        Consumer<Path> scanRoot = root -> {
            Path normalized = root.toAbsolutePath().normalize();
            if (!scanned.add(normalized)) {
                return;
            }
            if (normalized.toString().endsWith(".jar")) {
                scanJarFile(normalized, visitor);
            } else if (java.nio.file.Files.isDirectory(normalized)) {
                scanModDirectory(normalized, visitor);
            }
        };
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            mod.getRootPaths().forEach(scanRoot);
        }
        // In a Loom dev environment FabricLoader exposes only a project mod's resources root
        // (e.g. build/resources/main); the compiled classes live on the JVM classpath instead.
        // Scan those class output directories too, or annotation-driven registries stay empty in dev.
        String classPath = System.getProperty("java.class.path");
        if (classPath != null && !classPath.isBlank()) {
            for (String entry : classPath.split(java.io.File.pathSeparator)) {
                if (entry.isBlank()) {
                    continue;
                }
                Path path = Path.of(entry).toAbsolutePath().normalize();
                if (!java.nio.file.Files.isDirectory(path)) {
                    continue;
                }
                if (!java.nio.file.Files.isDirectory(path.resolve("com/lowdragmc/lowdraglib2"))) {
                    continue;
                }
                scanRoot.accept(path);
            }
        }
    }

    private static void scanJarFile(Path jarPath, ClassVisitor visitor) {
        try (var jarFile = new JarFile(jarPath.toFile())) {
            var enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                var entry = enumeration.nextElement();
                if (entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF")) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        var classReader = new ClassReader(is);
                        var classNode = new ClassNode();
                        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                        visitor.visit(classNode, jarPath);
                    } catch (IOException e) {
                        // Skip unreadable classes
                    }
                }
            }
        } catch (IOException e) {
            LDLib2.LOGGER.warn("Failed to scan JAR: {}", jarPath, e);
        }
    }

    private static void scanModDirectory(Path dirPath, ClassVisitor visitor) {
        try (var walk = java.nio.file.Files.walk(dirPath)) {
            walk.filter(p -> p.toString().endsWith(".class"))
                    .forEach(classFile -> {
                        try (InputStream is = java.nio.file.Files.newInputStream(classFile)) {
                            var classReader = new ClassReader(is);
                            var classNode = new ClassNode();
                            classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                            visitor.visit(classNode, dirPath);
                        } catch (IOException e) {
                            // Skip unreadable classes
                        }
                    });
        } catch (IOException e) {
            LDLib2.LOGGER.warn("Failed to scan directory: {}", dirPath, e);
        }
    }

    /**
     * Converts ASM annotation values to a Map<String, Object> for the predicate.
     */
    private static Map<String, Object> convertAnnotationValues(AnnotationNode ann) {
        var map = new HashMap<String, Object>();
        if (ann.values != null) {
            for (int i = 0; i < ann.values.size(); i += 2) {
                map.put((String) ann.values.get(i), ann.values.get(i + 1));
            }
        }
        return map;
    }
}
