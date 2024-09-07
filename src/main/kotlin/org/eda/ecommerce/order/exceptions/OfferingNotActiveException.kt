package org.eda.ecommerce.order.exceptions

import java.util.*

class OfferingNotActiveException : EventProcessingException {
    constructor() : super("Offering not active")
    constructor(offeringId: UUID, failureMode: String) : super("Offering with id $offeringId it not active. $failureMode")
}
