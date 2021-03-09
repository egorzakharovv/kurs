package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class SaleComposition(
	val saleID: Int,
	val productID: Int,
	val quantity: Int,
	override var id: Int = -1
) : Item

class SaleCompositionTable : ItemTable<SaleComposition>() {
	override val primaryKey = PrimaryKey(id, name = "sale_composition_id_pk")
	val saleID = integer("saleid").references(saleTable.id, onDelete = ReferenceOption.CASCADE)
	val productID = integer("productid").references(productTable.id, onDelete = ReferenceOption.CASCADE)
	val quantity = integer("quantity")

	override fun fill(builder: UpdateBuilder<Int>, item: SaleComposition) {
		builder[saleID] = item.saleID
		builder[productID] = item.productID
		builder[quantity] = item.quantity
	}

	override fun readResult(result: ResultRow) =
		SaleComposition(
			result[saleID],
			result[productID],
			result[quantity],
			result[id].value
		)
}

val saleCompositionTable = SaleCompositionTable()