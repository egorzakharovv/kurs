import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import repo.*
import rest.restRepo

fun Application.module() {
	Database.connect(
		"jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
		driver = "org.h2.Driver"
	)
	install(ContentNegotiation) {json()}
	install(CORS) {
		method(HttpMethod.Options)
		method(HttpMethod.Get)
		method(HttpMethod.Post)
		method(HttpMethod.Put)
		method(HttpMethod.Delete)
		method(HttpMethod.Patch)
		header(HttpHeaders.AccessControlAllowHeaders)
		header(HttpHeaders.ContentType)
		header(HttpHeaders.AccessControlAllowOrigin)
		allowCredentials = true
		anyHost()
	}
	transaction {
		SchemaUtils.create(productTable)
		SchemaUtils.create(saleTable)
		SchemaUtils.create(saleCompositionTable)
		SchemaUtils.create(employeeTable)
		SchemaUtils.create(postTable)
		SchemaUtils.create(typesTable)
	}
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
fun main() {
	embeddedServer(
		Netty,
		watchPaths = listOf("gradleproject"),
		module = Application::module,
		port = 8080	).start(wait = true)
}