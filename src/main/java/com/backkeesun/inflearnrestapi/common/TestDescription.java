package com.backkeesun.inflearnrestapi.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 대상
@Retention(RetentionPolicy.SOURCE) // life cycle
public @interface TestDescription {
    String value(); //입력값.
//    String useDefault() default "a"; //기본값을 지정하는 경우

}
