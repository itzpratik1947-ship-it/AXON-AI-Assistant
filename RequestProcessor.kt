package com.pratik.axon.core.engine

import com.pratik.axon.core.capability.AxonToolRouter
import com.pratik.axon.core.model.AxonRequest
import com.pratik.axon.core.model.AxonResult

class RequestProcessor(
    private val toolRouter: AxonToolRouter,
    private val builtInIntentHandler: BuiltInIntentHandler
) {

    fun process(
        request: AxonRequest
    ): AxonResult {

        val builtInResult =
            builtInIntentHandler.handle(
                request.text
            )

        if (builtInResult != null) {
            return builtInResult
        }

        return toolRouter.routeResult(
            request
        )
    }
}