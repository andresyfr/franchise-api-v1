package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB representation of a product.
 *
 * <p>Indexes:</p>
 * <ul>
 *   <li>{@code branchId + name} (unique): name uniqueness per branch, safe under concurrency.</li>
 *   <li>{@code branchId + stock desc}: efficient lookup of the highest stock per branch.</li>
 * </ul>
 *
 * @param id       product identifier
 * @param branchId identifier of the branch that offers the product
 * @param name     normalized product name
 * @param stock    available units
 */
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "branch_product_name_uq", def = "{'branchId': 1, 'name': 1}", unique = true),
        @CompoundIndex(name = "branch_stock_desc_idx", def = "{'branchId': 1, 'stock': -1}")
})
public record ProductDocument(
        @Id String id,
        String branchId,
        String name,
        int stock) {
}