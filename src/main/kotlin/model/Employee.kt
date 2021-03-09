package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Employee(
	val name: String,
	val postID: Int,
	val salary: Int,
	override var id: Int = -1
) : Item

class EmployeeTable : ItemTable<Employee>() {
	override val primaryKey = PrimaryKey(id, name = "employee_id_pk")
	val name = varchar("name", 50)
	val postID = integer("postid").references(postTable.id, onDelete = ReferenceOption.CASCADE)
	val salary = integer("salary")

	override fun fill(builder: UpdateBuilder<Int>, item: Employee) {
		builder[name] = item.name
		builder[postID] = item.postID
		builder[salary] = item.salary
	}

	override fun readResult(result: ResultRow) =
		Employee(
			result[name],
			result[postID],
			result[salary],
			result[id].value
		)
}

val employeeTable = EmployeeTable()