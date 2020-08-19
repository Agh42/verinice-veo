/*******************************************************************************
 * Copyright (c) 2020 Alexander Ben Nasrallah.
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
 ******************************************************************************/
package org.veo;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.core.env.Environment;

final public class SpringPropertyLogger {

    public static void logProperties(final Logger logger, Environment env) {
        Optional.ofNullable(env.getProperty("veo.logging.properties"))
                .map(it -> it.split(","))
                .map(Arrays::stream)
                .ifPresent(it -> it.forEach(propertyToLog -> {
                    logger.debug("spring property {}: {}", propertyToLog,
                                 Optional.ofNullable(env.getProperty(propertyToLog))
                                         .orElse(""));
                }));
    }

    private SpringPropertyLogger() {
    }
}