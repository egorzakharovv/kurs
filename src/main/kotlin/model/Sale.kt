package model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable
import java.time.LocalDate

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = LocalDate::class)
object DateSerializer : KSerializer<LocalDate> {

	override fun serialize(output: Encoder, obj: LocalDate) {
		output.encodeString(obj.toString())
	}

	override fun deserialize(input: Decoder): LocalDate {
		return LocalDate.parse(input.decodeString())
	}
}

@Serializable
class Sale(
	val employeeID: Int,
	@Serializable(with=DateSerializer::class)
	val date: LocalDate = LocalDate.now(),
	override var id: Int = -1
) : Item

class SaleTable : ItemTable<Sale>() {
	override val primaryKey = PrimaryKey(id, name = "sale_id_pk")
	val employeeID = integer("employeeid").references(employeeTable.id, onDelete = ReferenceOption.CASCADE)
	val date = date("date")

	override fun fill(builder: UpdateBuilder<Int>, item: Sale) {
		builder[employeeID] = item.employeeID
		builder[date] = item.date
	}

	override fun readResult(result: ResultRow) =
		Sale(
			result[employeeID],
			result[date],
			result[id].value
		)
}

val saleTable = SaleTable()