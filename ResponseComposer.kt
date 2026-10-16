package com.pratik.axon.core.engine

import com.pratik.axon.core.model.AxonResponse
import com.pratik.axon.core.model.AxonResult

class ResponseComposer {

    fun compose(
        result: AxonResult
    ): AxonResponse {

        return AxonResponse(
            message = result.message,
            success = result.success,
            data = result.data
        )
    }
}