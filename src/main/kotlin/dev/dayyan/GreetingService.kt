package dev.dayyan

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GreetingService {
    fun greeting(name: String): String {
        return "hello $name"
    }
}