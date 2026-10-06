package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import java.util.List;
import com.andresyfr.franchise.domain.model.TopStockProduct;
import com.andresyfr.franchise.domain.port.TopStockProductQuery;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * MongoDB implementation of {@link TopStockProductQuery}.
 *
 * <p>The query runs as one aggregation over the {@code branches} collection:</p>
 * <ol>
 *   <li>{@code $match}: branches of the franchise.</li>
 *   <li>{@code $lookup}: products of each branch (uses the {@code branchId} index).</li>
 *   <li>{@code $match}: discards branches without products.</li>
 *   <li>{@code $addFields}: highest stock of each branch.</li>
 *   <li>{@code $unwind} and {@code $match}: keeps every product with that stock (ties included).</li>
 *   <li>{@code $project} and {@code $sort}: shapes and orders the result.</li>
 * </ol>
 */
@Component
public class MongoTopStockProductQueryAdapter implements TopStockProductQuery {

    private static final String BRANCHES_COLLECTION = "branches";
    private static final String PRODUCTS_COLLECTION = "products";

    private static final String BRANCH_ID = "branchId";
    private static final String BRANCH_NAME = "branchName";
    private static final String PRODUCT_ID = "productId";
    private static final String PRODUCT_NAME = "productName";
    private static final String STOCK = "stock";

    private final ReactiveMongoTemplate template;

    /**
     * Creates the adapter.
     *
     * @param template reactive template used to run the aggregation
     */
    public MongoTopStockProductQueryAdapter(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Flux<TopStockProduct> findByFranchiseId(String franchiseId) {
        Aggregation aggregation = Aggregation.newAggregation(pipeline(franchiseId));
        return template.aggregate(aggregation, BRANCHES_COLLECTION, Document.class)
                .map(MongoTopStockProductQueryAdapter::toTopStockProduct);
    }

    private static List<AggregationOperation> pipeline(String franchiseId) {
        return List.of(
                stage("$match", new Document("franchiseId", franchiseId)),
                stage("$lookup", new Document("from", PRODUCTS_COLLECTION)
                        .append("localField", "_id")
                        .append("foreignField", BRANCH_ID)
                        .append("as", "products")),
                stage("$match", new Document("products.0", new Document("$exists", true))),
                stage("$addFields", new Document("maxStock", new Document("$max", "$products.stock"))),
                stage("$unwind", "$products"),
                stage("$match", new Document("$expr",
                        new Document("$eq", List.of("$products.stock", "$maxStock")))),
                stage("$project", new Document("_id", 0)
                        .append(BRANCH_ID, "$_id")
                        .append(BRANCH_NAME, "$name")
                        .append(PRODUCT_ID, "$products._id")
                        .append(PRODUCT_NAME, "$products.name")
                        .append(STOCK, "$products.stock")),
                stage("$sort", new Document(BRANCH_NAME, 1).append(PRODUCT_NAME, 1)));
    }

    private static AggregationOperation stage(String operator, Object definition) {
        Document stage = new Document(operator, definition);
        return context -> stage;
    }

    private static TopStockProduct toTopStockProduct(Document document) {
        return new TopStockProduct(
                document.getString(BRANCH_ID),
                document.getString(BRANCH_NAME),
                document.getString(PRODUCT_ID),
                document.getString(PRODUCT_NAME),
                document.get(STOCK, Number.class).intValue());
    }
}