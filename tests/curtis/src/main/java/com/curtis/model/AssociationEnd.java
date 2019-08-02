package com.curtis.model;

public @interface AssociationEnd
{
    String[] role() default "";
}
