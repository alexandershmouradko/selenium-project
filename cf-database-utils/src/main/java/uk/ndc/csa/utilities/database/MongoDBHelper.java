package uk.ndc.csa.utilities.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.bson.Document;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.ThreadContext;

/** Modern MongoDB synchronous-driver facade with thread-confined connections. */
public final class MongoDBHelper {
    private static final ThreadLocal<State> STATE = new ThreadLocal<>();

    private MongoDBHelper() {
    }

    public static void createConn() {
        FrameworkProperties props = ThreadContext.getInstance().getEnvironmentProps();
        createConn(props.getString("DBurl"), props.getString("DBName"));
    }

    public static void createConn(String uri, String databaseName) {
        closeConn();
        MongoClient client = MongoClients.create(uri);
        STATE.set(new State(client, client.getDatabase(databaseName)));
    }

    public static MongoCollection<Document> getCollection(String collection) {
        return state().database().getCollection(collection);
    }

    public static void closeConn() {
        State state = STATE.get();
        if (state != null) {
            state.client().close();
            STATE.remove();
        }
    }

    public static void dropCollection(List<String> collections) {
        collections.forEach(name -> getCollection(name).drop());
    }

    public static void insertOne(String collection, Document document) {
        getCollection(collection).insertOne(document);
    }

    public static void insertMany(String collection, List<? extends Document> documents) {
        getCollection(collection).insertMany(List.copyOf(documents));
    }

    public static String findOne(String collection, BasicDBObject query) {
        Document result = getCollection(collection).find(query).first();
        return result == null ? null : result.toJson();
    }

    public static FindIterable<Document> findMany(String collection, BasicDBObject query) {
        return getCollection(collection).find(query);
    }

    public static FindIterable<Document> getDoc(String collection) {
        return getCollection(collection).find();
    }

    public static void updateOne(String collection, BasicDBObject query, BasicDBObject update) {
        getCollection(collection).updateOne(query, update);
    }

    public static void updateMany(String collection, BasicDBObject query, BasicDBObject update) {
        getCollection(collection).updateMany(query, update);
    }

    public static void deleteOne(String collection, BasicDBObject query) {
        getCollection(collection).deleteOne(query);
    }

    public static void deleteMany(String collection, BasicDBObject query) {
        getCollection(collection).deleteMany(query);
    }

    private static State state() {
        State state = STATE.get();
        if (state == null) {
            createConn();
            state = STATE.get();
        }
        return state;
    }

    private record State(MongoClient client, MongoDatabase database) {
    }
}
