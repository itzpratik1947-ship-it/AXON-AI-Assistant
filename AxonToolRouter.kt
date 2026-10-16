package com.pratik.axon.core.capability

import com.pratik.axon.core.model.AxonError
import com.pratik.axon.core.model.AxonRequest
import com.pratik.axon.core.model.AxonResult
import com.pratik.axon.core.security.CapabilityAuthorization

class AxonToolRouter(
    private val registry: AxonToolRegistry,
    private val authorization: CapabilityAuthorization? = null
) {

    /*
     * Core routing API.
     *
     * Finds the correct tool, checks security,
     * then executes the tool.
     */
    fun routeResult(
        request: AxonRequest
    ): AxonResult {

        val tool =
            registry.findTool(
                request.text
            )

        if (tool == null) {

            return AxonResult(
                success = false,
                message = "No suitable AXON tool was found.",
                error = AxonError(
                    code = "CAPABILITY_NOT_FOUND",
                    message =
                        "No AXON capability could handle this request."
                )
            )
        }

        val capability =
            registry.getCapabilityForTool(
                tool
            )

        if (capability == null) {

            return AxonResult(
                success = false,
                message =
                    "AXON could not identify the required capability.",
                error = AxonError(
                    code = "CAPABILITY_METADATA_MISSING",
                    message =
                        "The selected tool has no capability metadata."
                )
            )
        }

        /*
         * SECURITY GATE
         *
         * Every tool execution passes through
         * the authorization layer.
         */
        if (authorization != null) {

            val decision =
                authorization.authorize(
                    command = request.text,
                    capability = capability,
                    confirmationGranted =
                        request.confirmationGranted
                )

            if (!decision.allowed) {

                val errorCode =
                    if (decision.requiresConfirmation) {
                        "CONFIRMATION_REQUIRED"
                    } else {
                        "ACTION_NOT_AUTHORIZED"
                    }

                return AxonResult(
                    success = false,
                    message =
                        decision.reason.ifBlank {
                            "AXON is not authorized to perform this action."
                        },
                    error = AxonError(
                        code = errorCode,
                        message =
                            decision.reason.ifBlank {
                                "The requested action was not authorized."
                            }
                    )
                )
            }
        }

        /*
         * SECURITY PASSED
         *
         * Only now can the tool execute.
         */
        return tool.execute(
            request.text
        )
    }

    fun getCapabilityForCommand(
        command: String
    ): Capability? {

        val tool =
            registry.findTool(
                command
            )

        return if (tool != null) {

            registry.getCapabilityForTool(
                tool
            )

        } else {

            null
        }
    }

    fun getCapability(
        request: AxonRequest
    ): Capability? {

        val tool =
            registry.findTool(
                request.text
            )

        return if (tool != null) {

            registry.getCapabilityForTool(
                tool
            )

        } else {

            null
        }
    }

    /*
     * Compatibility API.
     *
     * Existing code can still receive a String
     * while AXON migrates toward structured results.
     */
    fun route(
        command: String
    ): String {

        val result =
            routeResult(
                AxonRequest(
                    text = command
                )
            )

        return result.message
    }
}