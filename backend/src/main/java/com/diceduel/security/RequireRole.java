package com.diceduel.security;

import com.diceduel.entity.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative ACL guard. When placed on a controller method or class, the
 * {@link AuthInterceptor} requires an authenticated session whose role is one
 * of the listed values before the handler runs. This makes the backend the
 * authoritative gate: even if the frontend exposes an action, the request is
 * rejected here unless the caller truly holds the role.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    Role[] value();
}
