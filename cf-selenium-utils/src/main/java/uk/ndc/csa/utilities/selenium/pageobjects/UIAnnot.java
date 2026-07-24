package uk.ndc.csa.utilities.selenium.pageobjects;

import java.lang.annotation.*;
import java.lang.annotation.Retention;

@Retention(RetentionPolicy.RUNTIME)

@Deprecated
public @interface UIAnnot {
	public String type() default "";
	public String[] group1() default {};
	public String[] group2() default {};
	public String[] group3() default {};
	public String[] group4() default {};
	public String[] group5() default {};
	

}
