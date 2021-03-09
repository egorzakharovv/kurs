package repo

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

@Serializable
class TestItem(
    val name: String,
    override var id: Int = -1
) : Item

class TestItemTable : ItemTable<TestItem>() {
    val name = varchar("name", 50)
    override fun fill(builder: UpdateBuilder<Int>, item: TestItem) {
        builder[name] = item.name
    }
    override fun readResult(result: ResultRow) =
        TestItem(
            result[name],
            result[id].value
        )
}

val testItemTable = TestItemTable()

class TestItemClass(id: EntityID<Int>) : ItemClass<TestItem>(id) {
    companion object : IntEntityClass<TestItemClass>(testItemTable) {}

    var name by testItemTable.name

    override val obj: TestItem
        get() = TestItem(name, id.value)

    override fun fill(item: TestItem) {
        name = item.name
    }
}