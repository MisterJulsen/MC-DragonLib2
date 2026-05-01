package de.mrjulsen.mcdragonlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {
    String displayName() default "";
    Category category() default Category.GENERAL;

    public static enum Category {
        GENERAL;
    }
}
