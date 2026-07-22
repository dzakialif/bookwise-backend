package com.dzaki.bookwise.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService {
    
    private final Validator validator;

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);

        if(violations.size() != 0) {
            throw new ConstraintViolationException(violations);
        }
    }
}
