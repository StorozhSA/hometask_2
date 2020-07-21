package ru.skillbranch.kotlinexample

import org.junit.After
import org.junit.Assert
import org.junit.Test
import ru.skillbranch.kotlinexample.extensions.dropLastUntil
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    /**
    Добавьте метод в UserHolder для очистки значений UserHolder после выполнения каждого теста,
    это необходимо чтобы тесты можно было запускать одновременно

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
    map.clear()
    }
     */
    @After
    fun after() {
        UserHolder.clearHolder()
    }

    @Test
    fun register_user_success() {
        val holder = UserHolder
        val user = holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_blank() {
        val holder = UserHolder
        holder.registerUser("", "John_Doe@unknown.com", "testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUser("John Jr Doe", "John_Doe@unknown.com", "testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_exist() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
    }

    @Test
    fun register_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971 11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
        Assert.assertNotNull(user.accessCode)
        Assert.assertEquals(6, user.accessCode?.length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_blank() {
        val holder = UserHolder
        holder.registerUserByPhone("", "+7 (917) 971 11-11")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (XXX) XX XX-XX")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_failby_phone_illegal_exist() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
    }

    @Test
    fun login_user_success() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        val successResult = holder.loginUser("john_doe@unknown.com", "testPass")

        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult = holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_fail() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")

        val failResult = holder.loginUser("john_doe@unknown.com", "test")

        Assert.assertNull(failResult)
    }

    @Test
    fun normalize_phone_null() {
        Assert.assertNull(User.normalizePhone(null))
    }

    @Test
    fun login_user_not_found() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com", "testPass")

        val failResult = holder.loginUser("john_cena@unknown.com", "test")

        Assert.assertNull(failResult)
    }

    @Test
    fun request_access_code() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val oldAccess = user.accessCode
        holder.requestAccessCode("+7 (917) 971-11-11")

        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult = holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertNotEquals(oldAccess, user.accessCode!!)
        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun import_user() {
        val csvList: List<String> = listOf(
            " John Doe ;JohnDoe@unknow.com;" + encryptForTests("testPass") + ";;",
            " Mumb Ndi ;mumbndi@unknow.com;" + encryptForTests("pass1") + ";;"
        )

        val usrList = UserHolder.importUsers(csvList)
//        usrList.forEach {
//            println("============")
//            println(it.userInfo)
//        }
        Assert.assertEquals(2, usrList.size)
    }

    @Test
    fun drop_last_until1() {
        var successResult = listOf(1, 2, 3).dropLastUntil { it == 2 }
        Assert.assertEquals(listOf(1), successResult)
    }

    @Test
    fun drop_last_until2() {
        var successResult =
            "House Nymeros Martell of Sunspear".split(" ").dropLastUntil { it == "of" }
        Assert.assertEquals("House Nymeros Martell".split(" "), successResult)
    }

    @Test
    fun login_import_user_success() {
        val holder = UserHolder

        val csvList: List<String> = listOf(
            " John Doe ;John_Doe@unknown.com;" + encryptForTests("testPass") + ";;",
            " Mumb Ndi ;mumbndi@unknow.com;" + encryptForTests("pass1") + ";;"
        )

        val usrList = holder.importUsers(csvList)

        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=csv}
        """.trimIndent()

        val successResult = holder.loginUser("john_doe@unknown.com", "testPass")

        Assert.assertEquals(expectedInfo, successResult)
    }


    private fun encryptForTests(password: String): String {
        var salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        val hash = salt.plus(password).md5()
        println("Generated for tests salt and hash $salt:$hash")
        return "$salt:$hash"
    }

    private fun String.md5(): String {
        val digest = MessageDigest.getInstance("MD5").digest(toByteArray())
        return BigInteger(1, digest).toString(16).padStart(32, '0')
    }
}
