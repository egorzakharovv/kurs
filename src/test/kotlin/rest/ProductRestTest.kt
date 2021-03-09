package rest

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.json
import io.ktor.server.testing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import repo.RepoDSL
import repo.TestItem
import kotlin.test.Test
import kotlin.test.assertEquals


class ProductRestTest {
	private val testPath = "/group"

	@Test
	fun restRepoMapTest() {
		testRest {
			restRepo<Item>(
				RepoDSL(productTable),
				Product.serializer(),
				RepoDSL(saleTable),
				Sale.serializer(),
				RepoDSL(saleCompositionTable),
				SaleComposition.serializer(),
				RepoDSL(employeeTable),
				Employee.serializer(),
				RepoDSL(postTable),
				Post.serializer(),
				RepoDSL(typesTable),
				Type.serializer()
			)
		}
	}


	private fun testRest(
		restModule: Application.() -> Unit
	) {
		withTestApplication({
			install(ContentNegotiation) {
				json()
			}
			Database.connect(
				"jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
				driver = "org.h2.Driver"
			)
			transaction {
				SchemaUtils.create(productTable)
				SchemaUtils.create(saleTable)
				SchemaUtils.create(saleCompositionTable)
				SchemaUtils.create(employeeTable)
				SchemaUtils.create(postTable)
				SchemaUtils.create(typesTable)
			}
			restModule()
		}) {
			// Post
			val itemsJson =
				arrayOf("one", "two", "three")
					.map {
						Json.encodeToString(
							Product.serializer(),
							Product(it)
						)
					}

			itemsJson.map {
				println(it)
				handleRequest(HttpMethod.Post, testPath) {
					setBodyAndHeaders(it)
				}.apply {
					assertStatus(HttpStatusCode.OK)
				}
			}
			handleRequest(HttpMethod.Post, testPath) {
				setBodyAndHeaders("Wrong JSON")
			}.apply {
				assertStatus(HttpStatusCode.BadRequest)
			}

			// Get
			val users = handleRequest(HttpMethod.Get, testPath).run {
				assertStatus(HttpStatusCode.OK)
				parseResponse(
					ListSerializer(Product.serializer())
				)
			}
			assertEquals(3, users?.size)
            var asd = handleRequest(HttpMethod.Get, "$testPath/${users?.first()?.id}").run {
                assertStatus(HttpStatusCode.OK)
                val user = parseResponse(Product.serializer())
                assertEquals(users?.first()?.name, user?.name)
            }

			val items = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(Product.serializer())
                )
            }
            assertEquals(3, items?.size)
            handleRequest(HttpMethod.Get, "$testPath/${items?.first()?.id}").run {
                assertStatus(HttpStatusCode.OK)
                val item = parseResponse(Product.serializer())
                assertEquals(items?.first()?.name, item?.name)
            }

            // Put
            val three = items?.find { it.name == "three" }!!
            val threeNew = TestItem("three new", three.id)
            handleRequest(HttpMethod.Put, "$testPath/${threeNew.id}") {
                setBodyAndHeaders(Json.encodeToString(TestItem.serializer(), threeNew))
            }.run {
                assertStatus(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Get, "$testPath/${threeNew.id}").run {
                assertStatus(HttpStatusCode.OK)
                val item = parseResponse(TestItem.serializer())
                assertEquals("three new", item?.name)
            }

            // Delete
            val two = items.find { it.name == "two" }!!
            handleRequest(HttpMethod.Delete, "$testPath/${two.id}").run {
                assertStatus(HttpStatusCode.OK)
            }

            // Final check
            val itemsNewName = handleRequest(HttpMethod.Get, testPath).run {
                assertStatus(HttpStatusCode.OK)
                parseResponse(
                        ListSerializer(TestItem.serializer())
                )
            }?.map { it.name }!!

            assert(itemsNewName.size == 2)
            assert(itemsNewName.contains("one"))
            assert(itemsNewName.contains("three new"))

		}
	}
}

fun TestApplicationCall.assertStatus(status: HttpStatusCode) =
	assertEquals(status, response.status())

fun TestApplicationRequest.setBodyAndHeaders(body: String) {
	setBody(body)
	addHeader("Content-Type", "application/json")
	addHeader("Accept", "application/json")
}

fun <T> TestApplicationCall.parseResponse(
	serializer: KSerializer<T>
) =
	try {
		Json.decodeFromString(
			serializer,
			response.content ?: ""
		)
	} catch (e: Throwable) {
		null
	}