package repo

interface Repo<T : Item> {

	fun create(element: T): Boolean // null if element was in repo

	fun read(id: Int): T? // null if id is absent

	fun read(): List<T> // read all

	fun update(id: Int, element: T): Boolean // false if id is absent

	fun delete(id: Int): Boolean // false if id is absent

	fun all(): List<T> // read all
}