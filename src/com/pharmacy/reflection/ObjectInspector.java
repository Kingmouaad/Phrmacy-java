package com.pharmacy.reflection;

import java.lang.reflect.*;
import java.util.*;

/**
 * ObjectInspector — uses Java Reflection to dynamically inspect ANY object at runtime.
 *
 * WHY REFLECTION:
 * - Normally, you need to know a class's structure at COMPILE TIME to access its fields
 *   and methods. But what if an admin wants to see what's inside a Product, Customer,
 *   or Sale object without writing specific code for each one?
 * - Reflection lets us "X-ray" any object at RUNTIME — see its fields, methods,
 *   constructors, and even read/write private values. It's like having a universal
 *   debugger built into the app.
 *
 * HOW IT WORKS:
 * - Every Java class has a hidden `Class` object (obtained via obj.getClass()).
 * - The `Class` object knows everything about the class: its fields, methods,
 *   constructors, parent class, interfaces, annotations, etc.
 * - We use `getDeclaredFields()`, `getDeclaredMethods()`, etc. to discover
 *   the structure, and `setAccessible(true)` to peek at private fields.
 */
public class ObjectInspector {

    /**
     * Inspect an object: print its class info, fields (with values), methods, and constructors.
     * This works on ANY object — Product, Customer, Sale, etc.
     */
    public static void inspect(Object obj) {
        if (obj == null) {
            System.out.println("[ObjectInspector] Cannot inspect null.");
            return;
        }

        Class<?> clazz = obj.getClass();

        printClassInfo(clazz);
        printFields(obj, clazz);
        printMethods(clazz);
        printConstructors(clazz);
        printInterfaces(clazz);
    }

    /**
     * Print basic class metadata.
     */
    private static void printClassInfo(Class<?> clazz) {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║         OBJECT INSPECTOR — Reflection        ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  Class:     " + clazz.getSimpleName());
        System.out.println("║  Package:   " + (clazz.getPackage() != null ? clazz.getPackage().getName() : "default"));
        System.out.println("║  Full Name: " + clazz.getName());
        System.out.println("║  Parent:    " + (clazz.getSuperclass() != null ? clazz.getSuperclass().getSimpleName() : "none"));
        System.out.println("║  Modifiers: " + Modifier.toString(clazz.getModifiers()));
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    /**
     * Print ALL fields (including private and inherited) with their current values.
     * Uses setAccessible(true) to bypass private access.
     */
    private static void printFields(Object obj, Class<?> clazz) {
        System.out.println("\n┌─── FIELDS ──────────────────────────────────┐");

        // Collect fields from this class AND all superclasses
        List<Field> allFields = getAllFields(clazz);

        if (allFields.isEmpty()) {
            System.out.println("│  (no fields)");
        }

        for (Field field : allFields) {
            field.setAccessible(true); // Bypass private access
            String modifier = Modifier.toString(field.getModifiers());
            String type = field.getType().getSimpleName();
            String name = field.getName();
            String value;

            try {
                Object val = field.get(obj);
                if (val == null) {
                    value = "null";
                } else if (val instanceof String) {
                    value = "\"" + val + "\"";
                } else if (val instanceof Collection) {
                    value = "[" + ((Collection<?>) val).size() + " items]";
                } else if (val instanceof Map) {
                    value = "{" + ((Map<?, ?>) val).size() + " entries}";
                } else {
                    value = val.toString();
                }
            } catch (IllegalAccessException e) {
                value = "<access denied>";
            }

            // Determine which class declared this field
            String declaredIn = field.getDeclaringClass().getSimpleName();
            String inherited = !declaredIn.equals(clazz.getSimpleName()) ? " (from " + declaredIn + ")" : "";

            System.out.printf("│  %-12s %-15s %-18s = %s%s%n",
                    modifier, type, name, value, inherited);
        }
        System.out.println("└──────────────────────────────────────────────┘");
    }

    /**
     * Print all declared methods (excluding Object methods).
     */
    private static void printMethods(Class<?> clazz) {
        System.out.println("\n┌─── METHODS ─────────────────────────────────┐");

        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length == 0) {
            System.out.println("│  (no methods)");
        }

        for (Method method : methods) {
            String modifier = Modifier.toString(method.getModifiers());
            String returnType = method.getReturnType().getSimpleName();
            String name = method.getName();

            // Build parameter list
            StringJoiner params = new StringJoiner(", ");
            for (Class<?> paramType : method.getParameterTypes()) {
                params.add(paramType.getSimpleName());
            }

            System.out.printf("│  %-12s %-12s %s(%s)%n",
                    modifier, returnType, name, params.toString());
        }
        System.out.println("└──────────────────────────────────────────────┘");
    }

    /**
     * Print all constructors.
     */
    private static void printConstructors(Class<?> clazz) {
        System.out.println("\n┌─── CONSTRUCTORS ────────────────────────────┐");

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            String modifier = Modifier.toString(constructor.getModifiers());

            StringJoiner params = new StringJoiner(", ");
            for (Class<?> paramType : constructor.getParameterTypes()) {
                params.add(paramType.getSimpleName());
            }

            System.out.printf("│  %-12s %s(%s)%n",
                    modifier, clazz.getSimpleName(), params.toString());
        }
        System.out.println("└──────────────────────────────────────────────┘");
    }

    /**
     * Print all interfaces implemented by this class.
     */
    private static void printInterfaces(Class<?> clazz) {
        System.out.println("\n┌─── INTERFACES ──────────────────────────────┐");

        Set<Class<?>> allInterfaces = getAllInterfaces(clazz);
        if (allInterfaces.isEmpty()) {
            System.out.println("│  (none)");
        }
        for (Class<?> iface : allInterfaces) {
            System.out.println("│  ✔ " + iface.getSimpleName() + " (" + iface.getName() + ")");
        }
        System.out.println("└──────────────────────────────────────────────┘\n");
    }

    // ═══════════════════════════════════════
    // Dynamic field access (read/write private fields)
    // ═══════════════════════════════════════

    /**
     * Dynamically GET a field value by name — even private fields.
     * This is the power of Reflection: accessing things you normally can't.
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            System.out.println("[ObjectInspector] Error reading field '" + fieldName + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Dynamically SET a field value by name — even private fields.
     * USE WITH CAUTION — bypasses all access controls.
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.setAccessible(true);
            field.set(obj, value);
            System.out.println("[ObjectInspector] Set '" + fieldName + "' = " + value);
        } catch (Exception e) {
            System.out.println("[ObjectInspector] Error setting field '" + fieldName + "': " + e.getMessage());
        }
    }

    /**
     * Dynamically INVOKE a method by name — even private methods.
     */
    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        try {
            // Find matching method by name and arg count
            Method[] methods = obj.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    method.setAccessible(true);
                    return method.invoke(obj, args);
                }
            }
            // Also check superclass methods
            methods = obj.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    return method.invoke(obj, args);
                }
            }
            throw new NoSuchMethodException(methodName);
        } catch (Exception e) {
            System.out.println("[ObjectInspector] Error invoking '" + methodName + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Compare two objects field-by-field using reflection.
     * Useful for debugging: "what's different between these two products?"
     */
    public static void compareObjects(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            System.out.println("[ObjectInspector] Cannot compare null objects.");
            return;
        }

        Class<?> clazz1 = obj1.getClass();
        Class<?> clazz2 = obj2.getClass();

        System.out.println("\n┌─── COMPARISON ──────────────────────────────┐");
        System.out.println("│  Object A: " + clazz1.getSimpleName());
        System.out.println("│  Object B: " + clazz2.getSimpleName());
        System.out.println("├──────────────────────────────────────────────┤");

        List<Field> fields = getAllFields(clazz1);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object val1 = field.get(obj1);
                Object val2 = null;

                Field field2 = findField(clazz2, field.getName());
                if (field2 != null) {
                    field2.setAccessible(true);
                    val2 = field2.get(obj2);
                }

                boolean same = Objects.equals(val1, val2);
                String icon = same ? "  " : "≠ ";
                System.out.printf("│  %s%-18s A=%-20s B=%s%n",
                        icon, field.getName(),
                        val1 != null ? val1.toString() : "null",
                        val2 != null ? val2.toString() : "null");
            } catch (IllegalAccessException e) {
                // skip
            }
        }
        System.out.println("└──────────────────────────────────────────────┘\n");
    }

    // ═══════════════════════════════════════
    // Helper methods
    // ═══════════════════════════════════════

    /**
     * Get ALL fields including inherited ones from the entire class hierarchy.
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Find a field by name in the entire class hierarchy.
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Get ALL interfaces including those from superclasses.
     */
    private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        return interfaces;
    }
}
