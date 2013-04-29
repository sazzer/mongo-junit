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
     * @throws IOException if an error occurs loading the test data
     * @throws IllegalAccessError if a security error occurs creating a mapper
     * @throws InstantiationException if a mapper can't be instantiated
     */
    @Before
    public final void setupDatabase() throws IOException, IllegalAccessException, InstantiationException {
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
        Map<String, Mapper> mappers = new HashMap<String, Mapper>();

        Sources sourcesAnnotation = getClass().getAnnotation(Sources.class);
        if (sourcesAnnotation != null) {
            for (Source source : sourcesAnnotation.value()) {
                sources.put(source.collection(), source.data());
                if (!source.mapper().equals(Mapper.class)) {
                    Mapper mapper = source.mapper().newInstance();
                    mappers.put(source.collection(), mapper);
                }
            }
        }
        Source sourceAnnotation = getClass().getAnnotation(Source.class);
        if (sourceAnnotation != null) {
            sources.put(sourceAnnotation.collection(), sourceAnnotation.data());
            if (!sourceAnnotation.mapper().equals(Mapper.class)) {
                Mapper mapper = sourceAnnotation.mapper().newInstance();
                mappers.put(sourceAnnotation.collection(), mapper);
            }
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
                Mapper mapper = mappers.get(entry.getKey());
                Map toUse = o;
                if (mapper != null) {
                    toUse = new HashMap();
                    for (Object key : o.keySet()) {
                        Object realValue = mapper.map(key.toString(), o.get(key));
                        toUse.put(key, realValue);
                    }
                }
                collection.insert(new BasicDBObject(toUse));
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