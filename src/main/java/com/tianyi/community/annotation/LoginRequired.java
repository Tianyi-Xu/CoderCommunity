package com.tianyi.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // used on method
@Retention(RetentionPolicy.RUNTIME) // valid when runtime
public @interface LoginRequired {

}
