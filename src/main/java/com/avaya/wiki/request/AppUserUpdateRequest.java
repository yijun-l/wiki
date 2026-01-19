package com.avaya.wiki.request;

import jakarta.validation.constraints.Size;

public class AppUserUpdateRequest {
    @Size(max = 100, message = "Email cannot exceed 50 characters")
    private String email;

    @Size(max = 100, message = "Password cannot exceed 50 characters")
    private String passwordHash;

    @Size(max = 50, message = "Nickname cannot exceed 50 characters")
    private String nickname;

    @Size(max = 100, message = "Avatar URL cannot exceed 100 characters")
    private String avatarUrl;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
