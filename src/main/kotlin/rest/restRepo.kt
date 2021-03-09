package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import model.*
import org.jetbrains.exposed.sql.transactions.transaction
import repo.*

fun <T : Item> Application.restRepo(
	productRepo: Repo<Product>,
	productSerializer: KSerializer<Product>,

	saleRepo: Repo<Sale>,
	saleSerializer: KSerializer<Sale>,

	saleCompositionRepo: Repo<SaleComposition>,
	saleCompositionSerializer: KSerializer<SaleComposition>,

	employeeRepo: Repo<Employee>,
	employeeSerializer: KSerializer<Employee>,

	postRepo: Repo<Post>,
	postSerializer: KSerializer<Post>,

	typeRepo: Repo<Type>,
	typeSerializer: KSerializer<Type>,
) {
	routing {
		route("/init") {
			get {
				transaction {
					postTable.insertAndGetIdItem(Post("POST_INITIAL_1"))
					postTable.insertAndGetIdItem(Post("POST_INITIAL_2"))
					employeeTable.insertAndGetIdItem(Employee("EMPLOYEE_INITIAL_1", 1, 100))
					employeeTable.insertAndGetIdItem(Employee("EMPLOYEE_INITIAL_2", 2, 200))
					saleTable.insertAndGetIdItem(Sale(1))
					saleTable.insertAndGetIdItem(Sale(2))
					typesTable.insertAndGetIdItem(Type("TYPES_INITIAL_1"))
					typesTable.insertAndGetIdItem(Type("TYPES_INITIAL_2"))
					productTable.insertAndGetIdItem(Product("PRODUCT_INITIAL_1", 1, 100))
					productTable.insertAndGetIdItem(Product("PRODUCT_INITIAL_2", 2, 200))
					saleCompositionTable.insertAndGetIdItem(SaleComposition(1, 1, 10))
					saleCompositionTable.insertAndGetIdItem(SaleComposition(2, 2, 20))
					true
				}
				call.respond("initialized")
			}
		}

		route("/products") {
			post {
				parseBody(productSerializer)?.let { elem ->
					if (productRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Product created")
					else
						call.respond(HttpStatusCode.NotFound, "$elem")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Product")
			}
			get {
				val groups = productRepo.all()
				if (groups.isNotEmpty()) {
					call.respond(groups)
				} else {
					call.respond(HttpStatusCode.NotFound, "Products not found")
				}
			}
		}
		route("/products/{id}") {
			get {
				parseId()?.let { id ->
					productRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Product with id=$id dont exist")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(productSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (productRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Product updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Product")
			}
			delete {
				parseId()?.let { id ->
					if (productRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Product was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Product with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/products/type={typeid}") {
			get {
				parseId("typeid")?.let { id ->
					typeRepo.all().find { it.id == id } ?: call.respond(
						HttpStatusCode.NotFound,
						"Type with id=$id not found"
					)
					productRepo.all().filter { it.typeID == id }.let { elem ->
						call.respond(elem)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/employee") {
			post {
				parseBody(employeeSerializer)?.let { elem ->
					if (employeeRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Employee created")
					else
						call.respond(HttpStatusCode.NotFound, "$elem")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Employee")
			}
			get {
				val employers = employeeRepo.all()
				if (employers.isNotEmpty()) {
					call.respond(employers)
				} else {
					call.respond(HttpStatusCode.NotFound, "Employers not found")
				}
			}
		}
		route("/employee/{id}") {
			get {
				parseId()?.let { id ->
					employeeRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Employee with id=$id dont exist")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(employeeSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (employeeRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Employee updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Employee")
			}
			delete {
				parseId()?.let { id ->
					if (employeeRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Employee was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Employee with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/sales") {
			post {
				parseBody(saleSerializer)?.let { elem ->
					if (saleRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Sale created")
					else
						call.respond(HttpStatusCode.NotFound, "$elem")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Sale")
			}
			get {
				val employers = saleRepo.all()
				if (employers.isNotEmpty()) {
					call.respond(employers)
				} else {
					call.respond(HttpStatusCode.NotFound, "Sales not found")
				}
			}
		}
		route("/sales/{id}") {
			get {
				parseId()?.let { id ->
					saleRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Sale with id=$id dont exist")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(saleSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (saleRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Sale updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Sale")
			}
			delete {
				parseId()?.let { id ->
					if (saleRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Sale was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Sale with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/sales/{id}/composition") {
			get {
				parseId()?.let { id ->
					saleRepo.all().find { it.id == id } ?: call.respond(
						HttpStatusCode.NotFound,
						"Sale with id=$id not found"
					)
					saleCompositionRepo.all().filter { it.saleID == id }.let { elem ->
						call.respond(elem)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/composition") {
			post {
				parseBody(saleCompositionSerializer)?.let { elem ->
					if (saleCompositionRepo.create(elem))
						call.respond(HttpStatusCode.OK, "SaleComposition created")
					else
						call.respond(HttpStatusCode.NotFound, "$elem")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object SaleComposition")
			}
			get {
				val saleComposition = saleCompositionRepo.all()
				if (saleComposition.isNotEmpty()) {
					call.respond(saleComposition)
				} else {
					call.respond(HttpStatusCode.NotFound, "SaleComposition not found")
				}
			}
		}
		route("/composition/{id}") {
			get {
				parseId()?.let { id ->
					saleCompositionRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "SaleCompositionRepo with id=$id dont exist")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(saleCompositionSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (saleCompositionRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "SaleCompositionRepo updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object SaleCompositionRepo")
			}
			delete {
				parseId()?.let { id ->
					if (saleCompositionRepo.delete(id))
						call.respond(HttpStatusCode.OK, "SaleCompositionRepo was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "SaleCompositionRepo with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/composition/price<{price}") {
			get {
				parseId("price")?.let { price ->
					val temp = saleCompositionRepo.all().filter { sale ->
						(sale.quantity * (productRepo.all().find { it.id == sale.productID }?.price ?: 0)) < price
					}
					call.respond(temp)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/composition/price>{price}") {
			get {
				parseId("price")?.let { price ->
					val temp = saleCompositionRepo.all().filter { sale ->
						(sale.quantity * (productRepo.all().find { it.id == sale.productID }?.price ?: 0)) > price
					}
					call.respond(temp)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/composition/price={price}") {
			get {
				parseId("price")?.let { price ->
					val temp = saleCompositionRepo.all().filter { sale ->
						(sale.quantity * (productRepo.all().find { it.id == sale.productID }?.price ?: 0)) == price
					}
					call.respond(temp)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/types") {
			post {
				parseBody(typeSerializer)?.let { elem ->
					if (typeRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Types was created")
					else
						call.respond(HttpStatusCode.NotFound)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Types")
			}
			get {
				val types = typeRepo.all()
				if (types.isNotEmpty()) {
					call.respond(types)
				} else {
					call.respond(HttpStatusCode.NotFound, "Types not found")
				}
			}
		}
		route("/types/{id}") {
			get {
				parseId()?.let { id ->
					typeRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Types with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(typeSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (typeRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Types updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Types")
			}
			delete {
				parseId()?.let { id ->
					if (typeRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Types was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Types with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/posts") {
			post {
				parseBody(postSerializer)?.let { elem ->
					if (postRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Posts was created")
					else
						call.respond(HttpStatusCode.NotFound)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Posts")
			}
			get {
				val posts = postRepo.all()
				if (posts.isNotEmpty()) {
					call.respond(posts)
				} else {
					call.respond(HttpStatusCode.NotFound, "Posts not found")
				}
			}
		}
		route("/posts/{id}") {
			get {
				parseId()?.let { id ->
					postRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Posts with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(postSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (postRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Posts updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Posts")
			}
			delete {
				parseId()?.let { id ->
					if (postRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Posts was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Posts with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
	}
}

fun PipelineContext<Unit, ApplicationCall>.parseId(id: String = "id") =
	call.parameters[id]?.toIntOrNull()

suspend fun <T> PipelineContext<Unit, ApplicationCall>.parseBody(
	serializer: KSerializer<T>
) =
	try {
		Json.decodeFromString(
			serializer,
			call.receive()
		)
	} catch (e: Throwable) {
		null
	}