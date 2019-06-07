package com.rrpm.mzom.projectrrpm.annotations;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class NonEmptyValidator implements ConstraintValidator<Annotation,List<?>> {


    @Override
    public void initialize(Annotation constraintAnnotation) {

    }

    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        return !value.isEmpty();
    }

}
