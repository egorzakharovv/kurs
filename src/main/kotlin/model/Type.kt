package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Type(
	val name: String,
	override var id: Int = -1
) : Item

class TypesTable : ItemTable<Type>() {
	override val primaryKey = PrimaryKey(id, name = "type_id_pk")
	val name = varchar("name", 50)

	override fun fill(builder: UpdateBuilder<Int>, item: Type) {
		builder[name] = item.name
	}

	override fun readResult(result: ResultRow) = Type(result[name], result[id].value)
}

val typesTable = TypesTable()