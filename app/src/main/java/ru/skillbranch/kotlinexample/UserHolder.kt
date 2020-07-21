package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(fullName: String, email: String, password: String): User =
        User.makeUser(fullName, email = email, password = password).also {
            if (map.containsKey(it.login) || map.containsValue(it)) {
                throw IllegalArgumentException("A user with this email already exists")
            }
            map[it.login] = it
        }

    fun registerUserByPhone(fullName: String, phone: String): User =
        User.makeUser(fullName, phone = phone).also {
            if (map.containsKey(it.login) || map.containsValue(it)) {
                throw IllegalArgumentException("A user with this phone already exists")
            }
            map[it.login] = it
        }

    fun registerUserByCSV(fullName: String, email: String, phone: String, saltHash: String): User =
        User.makeUser(fullName, email = email, phone = phone, saltHash = saltHash).also {
            if (map.containsKey(it.login) || map.containsValue(it)) {
                throw IllegalArgumentException("A user with this phone already exists")
            }
            map[it.login] = it
        }

    fun loginUser(login: String, password: String): String? =
        map[User.normalizePhone(login.trim())
            ?: login.trim()]?.let { if (it.checkPassword(password)) it.userInfo else null }


    fun requestAccessCode(login: String) {
        map[User.normalizePhone(login.trim())]?.let {
            it.changePassword(it.accessCode!!, it.generateAccessCode())
            it.sendAccessCodeToUser(it.login, it.accessCode!!)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun importUsers(list: List<String>): List<User> {
        val res: MutableList<User> = mutableListOf()
        list.forEach {
            val sv: List<String> = it.split(";").map { it1 -> it1.trim() }
            val user = registerUserByCSV(sv[0], sv[1], sv[3], sv[2])
            res.add(user)
        }
        return res
    }


}