package restwars.rest.controller

import javax.validation.ValidatorFactory

class ValidationException(message: String) : Exception(message)

interface ControllerHelper {
    fun <T> validate(validatorFactory: ValidatorFactory, obj: T?): T {
        obj ?: throw ValidationException("Object is null")

        val validation = validatorFactory.validator.validate(obj)
        if (validation.isNotEmpty()) {
            throw ValidationException("Validation failed")
        }

        return obj
    }

}