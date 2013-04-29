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

/**
 * Mechanism to map an Object as read from the JSON File to an alternative object to put into the Mongo store
 */
public interface Mapper {
    /**
     * Map the provided input value to the new output value
     * @param key the key of the input value
     * @param input the actual input value
     * @return the value to store in the datastore
     */
    public Object map(String key, Object input);
}
