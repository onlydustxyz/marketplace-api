package onlydust.com.marketplace.kernel.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EventIdResolver extends TypeIdResolverBase {
    private static final ThreadLocal<Reflections> reflections = ThreadLocal.withInitial(() -> new Reflections("onlydust.com"));

    private final Map<String, Class<?>> typeMap = new HashMap<>();
    private JavaType baseType;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;

        record EventAnnotatedClass(Class<?> aClass, EventType eventType) {
        }

        reflections.get().getSubTypesOf(baseType.getRawClass()).stream()
                .map(aClass -> new EventAnnotatedClass(aClass, aClass.getAnnotation(EventType.class)))
                .filter(annotatedClass -> Objects.nonNull(annotatedClass.eventType()))
                .forEach(annotatedClass -> {
                    final var eventType = annotatedClass.eventType().value();
                    if (typeMap.containsKey(eventType)) {
                        throw new IllegalArgumentException("Duplicate EventType value: @EventType(\"%s\")".formatted(eventType));
                    }
                    typeMap.put(eventType, annotatedClass.aClass());
                });
    }

    @Override
    public String idFromValue(Object o) {
        return idFromValueAndType(o, o.getClass());
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        final var eventType = aClass.getAnnotation(EventType.class);
        if (eventType == null || !typeMap.containsKey(eventType.value())) {
            throw new IllegalArgumentException("Class " + aClass.getName() + " is not annotated with EventType or is not a subtype of " + baseType.getRawClass().getName());
        }
        return aClass.getAnnotation(EventType.class).value();
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        final var subType = typeMap.get(id);
        if (subType == null) {
            throw new IllegalArgumentException("No class annotated with @EventType(\"%s\") could be found".formatted(id));
        }
        return context.constructSpecializedType(baseType, subType);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }
}
