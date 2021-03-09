package repo

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

private fun repoTest(
    repo: Repo<TestItem>
) {
    arrayOf("one", "two", "three")
        .map {
            repo.create(TestItem(it))
        }

    val one = repo.read()
        .find { it.name == "one" }!!

    assertEquals(3, repo.read().size)
    assertEquals("one", repo.read(one.id)?.name)

    repo.update(one.id, TestItem("new one"))
    assertEquals(3, repo.read().size)
    assertEquals("new one", repo.read(one.id)?.name)

    repo.delete(one.id)
    assertEquals(2, repo.read().size)
    assertEquals(null, repo.read(one.id))
}

class RepoTest {

    @Test
    fun testRepoDSL() {
        Database.connect(
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(testItemTable)
        }
        repoTest(RepoDSL(testItemTable))
        transaction {
            SchemaUtils.drop(testItemTable)
        }
    }
}