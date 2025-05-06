package dev.dayyan.auth

import jakarta.ws.rs.NameBinding
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@NameBinding
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class ApiKeyProtected
