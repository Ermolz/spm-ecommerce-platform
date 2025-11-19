package contracts.inventory

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return stock information when product exists"
    
    request {
        method GET()
        url("/api/inventory/stock/1")
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            productId: 1,
            availableQuantity: 50,
            exists: true
        ])
    }
}

