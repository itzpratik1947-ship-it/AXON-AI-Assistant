package com.pratik.axon.core.engine

import com.pratik.axon.core.model.AxonRequest
import com.pratik.axon.core.model.AxonResponse

class AxonOrchestrator(
    private val requestProcessor: RequestProcessor,
    private val responseComposer: ResponseComposer
) {

    fun process(
        request: AxonRequest
    ): AxonResponse {

        val result =
            requestProcessor.process(
                request
            )

        return responseComposer.compose(
            result
        )
    }
}