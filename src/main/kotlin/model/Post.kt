package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Post(
	val name: String,
	override var id: Int = -1
) : Item

class PostTable : ItemTable<Post>() {
	override val primaryKey = PrimaryKey(id, name = "post_id_pk")
	val name = varchar("name", 50)

	override fun fill(builder: UpdateBuilder<Int>, item: Post) {
		builder[name] = item.name
	}

	override fun readResult(result: ResultRow) =
		Post(
			result[name],
			result[id].value
		)
}

val postTable = PostTable()