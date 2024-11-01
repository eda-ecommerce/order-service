package org.eda.ecommerce.order.exceptions

class EmptyEventPayloadException : EventProcessingException {
    constructor() : super("Event payload is empty")
    constructor(
        eventSource: String,
        operation: String
    ) : super("Event payload is empty for event from source '$eventSource' with operation '$operation'")
}
