package com.backkeesun.inflearnrestapi.common;

import org.aspectj.weaver.ast.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * {@code @DisplayName} is used to declare a {@linkplain #value custom display
 * name} for the annotated test class or test method.
 *
 * <p>Display names are typically used for test reporting in IDEs and build
 * tools and may contain spaces, special characters, and even emoji.
 *
 * @see Test
 */

@Target(ElementType.METHOD) // 대상
@Retention(RetentionPolicy.SOURCE) // life cycle
public @interface TestDescription {
    String value(); //입력값.
//    String useDefault() default "a"; //기본값을 지정하는 경우

}
