/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Ben Nasrallah <an@sernet.de> - initial API and implementation
 ******************************************************************************/
package org.veo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to persist maps wich needs the following keys:
 *
 * - parent
 */
public interface ElementMapService {

    List<Map<String, Object>> findAll() throws IOException;
    Map<String, Object> find(String id) throws IOException;
    List<Map<String, Object>> findChildren(String parentId) throws IOException;

    void save(String id, Map<String, Object> content) throws IOException;
    String saveNew(Map<String, Object> content) throws IOException;

    void delete(String id) throws IOException;
}
