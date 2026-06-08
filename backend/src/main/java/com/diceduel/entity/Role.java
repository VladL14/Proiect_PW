package com.diceduel.entity;

/**
 * Access control roles used by the Dice Duel ACL system.
 *
 * <p>The hierarchy is intentionally simple: {@link #GUEST} can only reach a
 * small set of public, read-only resources, {@link #USER} can play and manage
 * their own data, and {@link #ADMIN} can manage every account, match and replay.
 */
public enum Role {
    GUEST,
    USER,
    ADMIN
}
