package com.eggheadengineers.nimons360.core.validation

import android.util.Patterns

private const val NAME_MAX_LENGTH = 80
private const val LOCATION_NAME_MAX_LENGTH = 80
private const val DESCRIPTION_MAX_LENGTH = 300
private const val NOTIFICATION_MAX_LENGTH = 240
private const val JOIN_CODE_MAX_LENGTH = 32

fun validateEmail(value: String): String? {
    val trimmed = value.trim()
    return when {
        trimmed.isBlank() -> "Enter your email"
        trimmed.length > 120 -> "Email is too long"
        !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "Enter a valid email address"
        else -> null
    }
}

fun validatePassword(value: String): String? = when {
    value.isBlank() -> "Enter your password"
    value.length < 6 -> "Password must be at least 6 characters"
    value.length > 128 -> "Password is too long"
    else -> null
}

fun validatePersonName(value: String): String? = validateRequiredText(
    value = value,
    emptyMessage = "Enter your name",
    maxLength = NAME_MAX_LENGTH,
    tooLongMessage = "Name must be $NAME_MAX_LENGTH characters or fewer",
)

fun validateFamilyName(value: String): String? = validateRequiredText(
    value = value,
    emptyMessage = "Enter family name",
    maxLength = NAME_MAX_LENGTH,
    tooLongMessage = "Family name must be $NAME_MAX_LENGTH characters or fewer",
)

fun validateJoinCode(value: String): String? = validateRequiredText(
    value = value,
    emptyMessage = "Enter the family code",
    maxLength = JOIN_CODE_MAX_LENGTH,
    tooLongMessage = "Family code must be $JOIN_CODE_MAX_LENGTH characters or fewer",
)

fun validateMarkedLocation(name: String, description: String): String? =
    validateRequiredText(
        value = name,
        emptyMessage = "Enter a location name",
        maxLength = LOCATION_NAME_MAX_LENGTH,
        tooLongMessage = "Location name must be $LOCATION_NAME_MAX_LENGTH characters or fewer",
    ) ?: validateOptionalText(
        value = description,
        maxLength = DESCRIPTION_MAX_LENGTH,
        tooLongMessage = "Description must be $DESCRIPTION_MAX_LENGTH characters or fewer",
    )

fun validateNotificationMessage(value: String): String? = validateRequiredText(
    value = value,
    emptyMessage = "Write a short message before sending.",
    maxLength = NOTIFICATION_MAX_LENGTH,
    tooLongMessage = "Message must be $NOTIFICATION_MAX_LENGTH characters or fewer",
)

private fun validateRequiredText(
    value: String,
    emptyMessage: String,
    maxLength: Int,
    tooLongMessage: String,
): String? {
    val trimmed = value.trim()
    return when {
        trimmed.isBlank() -> emptyMessage
        trimmed.length > maxLength -> tooLongMessage
        else -> null
    }
}

private fun validateOptionalText(
    value: String,
    maxLength: Int,
    tooLongMessage: String,
): String? = if (value.trim().length > maxLength) tooLongMessage else null
