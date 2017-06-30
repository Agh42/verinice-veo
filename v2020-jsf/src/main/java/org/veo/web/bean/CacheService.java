/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz<at>sernet.de - initial API and implementation
 ******************************************************************************/
package org.veo.web.bean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veo.model.Element;
import org.veo.persistence.ElementRepository;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * @author urszeidler
 *
 */
@Named( "CacheService")
@ApplicationScoped
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class.getName());

    @Inject
    private ElementRepository elementRepository;

    private LoadingCache<String, Element> elementCache;

    public void createElementCache() {
        RemovalListener<String, Element> elementRemovalListener = new RemovalListener<String, Element>() {

            @Override
            public void onRemoval(RemovalNotification<String, Element> arg0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("removing :" + arg0);
                }
            }
        };
        elementCache = CacheBuilder.newBuilder().removalListener(elementRemovalListener)
                .maximumSize(25000).build(new CacheLoader<String, Element>() {
                    public Element load(String uuid) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("loadind element with uuid:" + uuid);
                        }

                        long currentTimeMillis = System.currentTimeMillis();
                        if (logger.isDebugEnabled()) {
                            logger.debug("loadElement start: " + currentTimeMillis);
                        }
                        Element findOneWithChildren = elementRepository.findOneWithChildren(uuid);

                        if (logger.isDebugEnabled()) {
                            logger.debug("loadElement stop: "
                                    + (System.currentTimeMillis() - currentTimeMillis));
                        }

                        return findOneWithChildren;
                    }
                });
    }

    @PostConstruct
    public void initCache() {
        createElementCache();
    }

    public LoadingCache<String, Element> getElementCache() {
        return elementCache;
    }

}
