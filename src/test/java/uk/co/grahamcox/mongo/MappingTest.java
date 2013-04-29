/*
 * Copyright (C) 29/04/13 graham
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
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the MongoTestBase class to test the mapping functionality
 */
@Sources({
    @Source(collection = "test", data = "/uk/co/grahamcox//mongo/testFile.json", mapper = MappingTest.TestMapper.class)
})
public class MappingTest extends MongoTestBase {
    /**
     * The mapper to use for this test
     */
    public static class TestMapper implements Mapper {
        /**
         * Map the provided input value to the new output value
         *
         * @param key   the key of the input value
         * @param input the actual input value
         * @return the value to store in the datastore
         */
        @Override
        public Object map(String key, Object input) {
            final Object result;
            if (key.equals("answer")) {
                result = ((Integer)input) * 2;
            } else if (key.equals("hello")) {
                result = "dlrow";
            } else {
                result = input;
            }
            return result;
        }
    }

    /**
     * Test that we can retrieve data from the MongoDB server that was seeded there from our Source files
     */
    @Test
    public void testLoadData() {
        DB mongoDB = getMongoDB();

        DBCollection test = mongoDB.getCollection("test");
        DBObject answer = test.findOne(new BasicDBObject("answer", 84));
        Assert.assertNotNull(answer);
        Assert.assertTrue(answer instanceof BasicDBObject);
        BasicDBObject answerAsObject = (BasicDBObject)answer;
        Assert.assertEquals("dlrow", answerAsObject.getString("hello"));
    }
}
