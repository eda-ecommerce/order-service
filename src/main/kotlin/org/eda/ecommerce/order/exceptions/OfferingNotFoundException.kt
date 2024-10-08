package org.eda.ecommerce.order.exceptions

import java.util.*

class OfferingNotFoundException : EventProcessingException {
    constructor() : super("Offering not found")
    constructor(offeringId: UUID) : super("Offering with id $offeringId not found.")

    constructor(offeringId: UUID, failureMode: String) : super("Offering with id $offeringId not found. $failureMode")
}
