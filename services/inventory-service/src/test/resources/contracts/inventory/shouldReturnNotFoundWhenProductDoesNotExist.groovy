package contracts.inventory

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return 404 with exists=false when product does not exist"
    
    request {
        method GET()
        url("/api/inventory/stock/999")
    }
    
    response {
        status NOT_FOUND()
        headers {
            contentType(applicationJson())
        }
        body([
            productId: 999,
            availableQuantity: 0,
            exists: false
        ])
    }
}

