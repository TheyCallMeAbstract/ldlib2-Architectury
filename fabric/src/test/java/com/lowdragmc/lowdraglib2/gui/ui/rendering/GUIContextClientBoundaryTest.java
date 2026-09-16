package com.lowdragmc.lowdraglib2.gui.ui.rendering;

import net.fabricmc.api.Environment;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GUIContextClientBoundaryTest {
    @Test
    void guiContextDoesNotUseMemberLevelEnvironmentAnnotations() {
        var annotatedFields = Arrays.stream(GUIContext.class.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> hasEnvironment(findField(name)))
                .toList();
        var annotatedMethods = Arrays.stream(GUIContext.class.getDeclaredMethods())
                .map(Method::getName)
                .distinct()
                .filter(name -> Arrays.stream(GUIContext.class.getDeclaredMethods())
                        .filter(method -> method.getName().equals(name))
                        .anyMatch(this::hasEnvironment))
                .toList();

        assertTrue(annotatedFields.isEmpty(), "Expected no member-level @Environment fields, found: " + annotatedFields);
        assertTrue(annotatedMethods.isEmpty(), "Expected no member-level @Environment methods, found: " + annotatedMethods);
    }

    private Field findField(String name) {
        try {
            return GUIContext.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private boolean hasEnvironment(Field field) {
        return field.isAnnotationPresent(Environment.class);
    }

    private boolean hasEnvironment(Method method) {
        return method.isAnnotationPresent(Environment.class);
    }
}
