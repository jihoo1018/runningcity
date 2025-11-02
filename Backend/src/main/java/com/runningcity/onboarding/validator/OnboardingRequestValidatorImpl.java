package com.runningcity.onboarding.validator;

import com.runningcity.onboarding.dto.OnboardingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OnboardingRequestValidatorImpl implements ConstraintValidator<OnboardingRequestValidator, OnboardingRequest> {

    @Override
    public boolean isValid(OnboardingRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        // 워치가 없는데 심박수를 입력한 경우 검증 실패
        if (Boolean.FALSE.equals(request.getHasSmartWatch()) && request.getRestingHeartRate() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("스마트워치가 없는 경우 심박수를 입력할 수 없습니다.")
                    .addPropertyNode("restingHeartRate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

