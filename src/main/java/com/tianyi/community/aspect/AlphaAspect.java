//package com.tianyi.community.aspect;
//
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.stereotype.Component;
//
//@Component
//@Aspect
//public class AlphaAspect {
//    // all return value, in all function(with any parameter) in class under the package.service
//    @Pointcut("execution(* com.tianyi.community.service.*.*(..))")
//    public void pointcut() {
//    }
//    @Before("pointcut()")  // in respect to the pointcut function
//    public void before() {
//        System.out.println("before");
//    }
//    @After("pointcut()")
//    public void after() {
//        System.out.println("after");
//    }
//    @AfterReturning("pointcut()")
//    public void afterReturning() {
//        System.out.println("afterReturning");
//    }
//    @AfterThrowing("pointcut()")
//    public void afterThrowing() {
//        System.out.println("afterThrowing");
//    }
//    @Around("pointcut()")
//    public Object around(ProceedingJoinPoint joinPoint) throws  Throwable {
//        System.out.println("before");
//        Object obj = joinPoint.proceed();
//        System.out.println("after");
//        return obj;
//    }
//
//
//}
