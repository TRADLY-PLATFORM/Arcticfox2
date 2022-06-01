package tradly.social.common.network.converters

abstract class Mapper<in E, T> {
    abstract fun mapFrom(from:E):T
    abstract fun mapFromList(from: List<E>):List<T>
}