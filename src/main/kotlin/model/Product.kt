package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Product(
	val name: String,
	val typeID: Int,
	val price: Int,
	override var id: Int = -1
) : Item

class ProductTable : ItemTable<Product>() {
	override val primaryKey = PrimaryKey(id, name = "product_id_pk")
	val typeID = integer("typeid").references(typesTable.id, onDelete = ReferenceOption.CASCADE)
	val price = integer("price")
	val name = varchar("name", 50)

	override fun fill(builder: UpdateBuilder<Int>, item: Product) {
		builder[name] = item.name
		builder[typeID] = item.typeID
		builder[price] = item.price
	}

	override fun readResult(result: ResultRow) =
		Product(
			result[name],
			result[typeID],
			result[price],
			result[id].value
		)
}

val productTable = ProductTable()