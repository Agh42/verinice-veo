/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package org.veo.service.ie;

import java.util.Properties;

import de.sernet.sync.data.SyncLink;

import org.veo.model.Element;
import org.veo.model.Link;

/**
 * A context to import one link from a VNA to database.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkImportContext {

    private SyncLink syncLink;
    private Element source;
    private Element destination;
    private Link link;
    private Properties missingMappingProperties;

    public LinkImportContext() {
        super();
    }

    public LinkImportContext(SyncLink syncLink, Element source, Element destination) {
        super();
        this.syncLink = syncLink;
        this.source = source;
        this.destination = destination;
        this.missingMappingProperties = new Properties();
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public SyncLink getSyncLink() {
        return syncLink;
    }

    public Element getSource() {
        return source;
    }

    public Element getDestination() {
        return destination;
    }

    public Properties getMissingMappingProperties() {
        return missingMappingProperties;
    }

    public void addMissingMappingProperty(String key) {
        getMissingMappingProperties().put(key, "");
    }

    public void setMissingMappingProperties(Properties missingMappingProperties) {
        this.missingMappingProperties = missingMappingProperties;
    }

}