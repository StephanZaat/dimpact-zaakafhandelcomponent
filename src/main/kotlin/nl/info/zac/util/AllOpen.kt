package nl.info.zac.util

/**
 * Custom annotation so that WildFly's dependency injection framework (Weld)
 * can proxy our Kotlin classes when they have our custom annotation.
 * Because by default Kotlin classes are final.
 */
@nl.info.zac.util.AllOpen
annotation class AllOpen
