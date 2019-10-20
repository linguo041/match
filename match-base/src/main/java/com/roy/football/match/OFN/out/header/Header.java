package com.roy.football.match.OFN.out.header;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Header {
	String title() default "header.default";
	
	int order() default 0;
	
	boolean writable() default true;
}
