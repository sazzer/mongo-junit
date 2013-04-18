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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the MongoTestBase class
 */
@Sources({
        @Source(collection = "test", data = "/uk/co/grahamcox//mongo/testFile.json")
})
public class MongoTestBaseTest extends MongoTestBase {
    /**
     * Test that we actually have a MongoDB server that we can make use of via the provided MongoClient object
     */
    @Test
    public void testClientExists() {
        MongoClient mongoClient = getMongoClient();
        String database = getDatabaseName();

        Assert.assertNotNull(mongoClient);
        Assert.assertNotNull(database);

        DB db = mongoClient.getDB(database);
        Assert.assertNotNull(db);

        DBCollection test = db.getCollection("test");
        Assert.assertNotNull(test);

        long count = test.count();
        Assert.assertEquals(1, count);
    }
    /**
     * Test that we can retrieve data from the MongoDB server that was seeded there from our Source files
     */
    @Test
    public void testLoadData() {
        DB mongoDB = getMongoDB();

        DBCollection test = mongoDB.getCollection("test");
        DBObject answer = test.findOne(new BasicDBObject("answer", 42));
        Assert.assertNotNull(answer);
        Assert.assertTrue(answer instanceof BasicDBObject);
        BasicDBObject answerAsObject = (BasicDBObject)answer;
        Assert.assertEquals("world", answerAsObject.getString("hello"));
    }
}
