package com.kaspersky.kaspresso.autoscroll

import android.support.test.espresso.web.sugar.Web
import android.support.test.espresso.web.webdriver.DriverAtoms
import com.kaspersky.kaspresso.internal.extensions.other.isAllowed
import com.kaspersky.kaspresso.logger.UiTestLogger

/**
 * The implementation of the [AutoScrollProvider] interface for [Web.WebInteraction]
 */
class WebAutoScrollProviderImpl(
    private val params: AutoScrollParams,
    private val logger: UiTestLogger
) : AutoScrollProvider<Web.WebInteraction<*>> {

    /**
     * Invokes the given [action] and calls [scroll] if it fails. Helps in cases when test fails because of the
     * need to scroll to interacted view.
     *
     * @param interaction the instance of [Web.WebInteraction] interface to perform actions and assertions.
     * @param action the actual action on the interacted view.
     *
     * @throws Throwable if the exception caught while invoking [action] is not allowed via [params].
     * @return the result of [action] invocation.
     */
    @Throws(Throwable::class)
    override fun <T> withAutoScroll(interaction: Web.WebInteraction<*>, action: () -> T): T {
        return try {
            action.invoke()
        } catch (error: Throwable) {
            if (error.isAllowed(params.allowedExceptions)) {
                return scroll(interaction, action, error)
            }
            throw error
        }
    }

    /**
     * Performs the autoscrolling functionality. Performs scroll and re-invokes the given [action].
     *
     * @param interaction the instance of [Web.WebInteraction] interface to perform actions and assertions.
     * @param action the actual action on the interacted view.
     * @param cachedError the error to be thrown if autoscroll would not help.
     *
     * @throws cachedError if autoscroll action did not help.
     * @return the result of [action] invocation.
     */
    @Throws(Throwable::class)
    override fun <T> scroll(interaction: Web.WebInteraction<*>, action: () -> T, cachedError: Throwable): T {
        return try {
            interaction.perform(DriverAtoms.webScrollIntoView())
            logger.i("Web autoScroll successfully performed.")
            action.invoke()
        } catch (error: Throwable) {
            throw cachedError
        }
    }
}