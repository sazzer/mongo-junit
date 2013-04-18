/*
 * Copyright (C) 18/04/13 graham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.grahamcox.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for MongoDB tests
 */
public class MongoTestBase {
    /** The Mongo executable */
    private MongodExecutable mongodExecutable;
    /** The Mongo process */
    private MongodProcess mongodProcess;
    /** A Mongo Client */
    private MongoClient mongoClient;
    /** A Mongo Connection */
    private DB mongoDB;
    /**
     * Set up the database to use
     */
    @Before
    public final void setupDatabase() throws IOException {
        int port;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
        }
        finally {
            serverSocket.close();
        }
        String dbName = "unittest";
        Version.Main version = Version.Main.PRODUCTION;

        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodConfig config = new MongodConfig(version, port, false);
        mongodExecutable = runtime.prepare(config);
        mongodProcess = mongodExecutable.start();

        mongoClient = new MongoClient("localhost", port);
        mongoDB = mongoClient.getDB(dbName);

        Map<String, String> sources = new HashMap<String, String>();
        Sources sourcesAnnotation = getClass().getAnnotation(Sources.class);
        if (sourcesAnnotation != null) {
            for (Source source : sourcesAnnotation.value()) {
                sources.put(source.collection(), source.data());
            }
        }
        Source sourceAnnotation = getClass().getAnnotation(Source.class);
        if (sourceAnnotation != null) {
            sources.put(sourceAnnotation.collection(), sourceAnnotation.data());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            DBCollection collection = mongoDB.getCollection(entry.getKey());
            InputStream inputStream = getClass().getResourceAsStream(entry.getValue());
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + entry.getValue());
            }
            List<Map> dataList = objectMapper.readValue(inputStream, List.class);
            for (Map o : dataList) {
                collection.insert(new BasicDBObject(o));
            }
        }
    }

    /**
     * Tear down the database afterwards
     */
    @After
    public final void teardownDatabase() {
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    /**
     * Get the database name used
     * @return the database name
     */
    public String getDatabaseName() {
        return "unittest";
    }

    /**
     * Get the Mongo Client to use
     * @return the Mongo Client
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Get the Mongo DB connection
     * @return the connection
     */
    public DB getMongoDB() {
        return mongoDB;
    }
}