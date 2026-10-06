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

    /**
     * The name of the branches collection in MongoDB.
     */
    private static final String BRANCHES_COLLECTION = "branches";
    /**
     * The name of the products collection in MongoDB.
     */
    private static final String PRODUCTS_COLLECTION = "products";
    /**
     * Field name and mapping key for the branch identifier and others.
     */
    private static final String BRANCH_ID = "branchId";
    private static final String BRANCH_NAME = "branchName";
    private static final String PRODUCT_ID = "productId";
    private static final String PRODUCT_NAME = "productName";
    private static final String STOCK = "stock";
    /**
     * The reactive MongoDB template used to execute native aggregation pipelines.
     */
    private final ReactiveMongoTemplate template;

    /**
     * Creates the adapter.
     *
     * @param template reactive template used to run the aggregation
     */
    public MongoTopStockProductQueryAdapter(ReactiveMongoTemplate template) {
        this.template = template;
    }

    /**
     * Finds and returns the products with the highest stock level for each branch
     * within the specified franchise.
     *
     * @param franchiseId the unique identifier of the franchise
     * @return a {@link Flux} emitting {@link TopStockProduct} objects sorted by branch and product name
     */
    @Override
    public Flux<TopStockProduct> findByFranchiseId(String franchiseId) {
        Aggregation aggregation = Aggregation.newAggregation(pipeline(franchiseId));
        return template.aggregate(aggregation, BRANCHES_COLLECTION, Document.class)
                .map(MongoTopStockProductQueryAdapter::toTopStockProduct);
    }

    /**
     * Builds the multi-stage MongoDB aggregation pipeline to extract the top stock products.
     * <p>
     * The pipeline performs the following steps:
     * <ul>
     *   <li>Filters branches by the given franchise ID.</li>
     *   <li>Joins (looks up) products associated with each branch.</li>
     *   <li>Filters out branches that do not contain any products.</li>
     *   <li>Calculates the maximum stock value among the branch's products.</li>
     *   <li>Unwinds the products array for individual evaluation.</li>
     *   <li>Filters and retains only the product(s) matching the maximum stock value.</li>
     *   <li>Projects the final structure into flat fields.</li>
     *   <li>Sorts the results alphabetically by branch name and product name.</li>
     * </ul>
     * </p>
     *
     * @param franchiseId the target franchise identifier
     * @return a list of {@link AggregationOperation} representing the pipeline stages
     */
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

    /**
     * Helper method to dynamically construct a native MongoDB aggregation stage.
     *
     * @param operator   the MongoDB aggregation operator (e.g., "$match", "$lookup")
     * @param definition the document body defining the stage configuration
     * @return an {@link AggregationOperation} ready to be added to the pipeline
     */
    private static AggregationOperation stage(String operator, Object definition) {
        Document stage = new Document(operator, definition);
        return context -> stage;
    }

    /**
     * Maps a single BSON {@link Document} resulting from the aggregation into a domain query model.
     *
     * @param document the raw MongoDB result document
     * @return a mapped {@link TopStockProduct} instance
     */
    private static TopStockProduct toTopStockProduct(Document document) {
        return new TopStockProduct(
                document.getString(BRANCH_ID),
                document.getString(BRANCH_NAME),
                document.getString(PRODUCT_ID),
                document.getString(PRODUCT_NAME),
                document.get(STOCK, Number.class).intValue());
    }
}