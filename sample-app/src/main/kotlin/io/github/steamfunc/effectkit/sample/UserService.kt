package io.github.steamfunc.effectkit.sample

import io.github.steamfunc.effectkit.annotations.IO
import io.github.steamfunc.effectkit.annotations.IOEffect.*
import io.github.steamfunc.effectkit.annotations.Intent

data class User(val id: Long, val name: String, val email: String)

object UserService {

    @Intent("ID로 사용자 조회")
    @IO(STORAGE_READ)
    fun findById(id: Long): User? {
        // DB 조회 (stub)
        return User(id, "Alice", "alice@example.com")
    }

    @Intent("신규 사용자 저장")
    @IO(STORAGE_WRITE)
    fun save(user: User) {
        // DB 저장 (stub)
    }

    @Intent("외부 OAuth 서버로 사용자 인증 후 로컬 저장")
    @IO(NETWORK_READ, STORAGE_WRITE)
    fun authenticateAndSave(token: String): User {
        // 외부 API 호출 후 DB 저장 (stub)
        return User(1L, "Bob", "bob@example.com")
    }

    @Intent("이름으로 사용자 검색 후 알림 발송")
    @IO(STORAGE_READ, NETWORK_WRITE)
    fun searchAndNotify(name: String): List<User> {
        // DB 조회 후 알림 API 호출 (stub)
        return emptyList()
    }

    @Intent("입력값 유효성 검사")
    @IO(PURE)
    fun validate(email: String): Boolean {
        return email.contains("@")
    }
}
