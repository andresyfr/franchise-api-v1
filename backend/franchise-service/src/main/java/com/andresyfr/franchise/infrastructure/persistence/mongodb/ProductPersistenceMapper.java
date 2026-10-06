package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.model.Product;

/**
 * Converts between the product domain model and its MongoDB document.
 */
final class ProductPersistenceMapper {

    private ProductPersistenceMapper() {
    }

    static ProductDocument toDocument(Product product) {
        return new ProductDocument(product.id(), product.branchId(), product.name(), product.stock());
    }

    static Product toDomain(ProductDocument document) {
        return new Product(document.id(), document.branchId(), document.name(), document.stock());
    }
}