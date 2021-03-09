package repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

abstract class ItemClass<T : Item>(id: EntityID<Int>) : IntEntity(id) {
    abstract val obj: T
    abstract fun fill(item: T)
}