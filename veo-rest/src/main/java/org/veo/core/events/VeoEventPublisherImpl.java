/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.core.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import org.veo.adapter.service.domaintemplate.events.DomainServiceReinitializeEvent;
import org.veo.core.service.DomainTemplateEventPublisher;

import lombok.Data;

/**
 * Publish veo related events.
 */
@Component
@Data
public class VeoEventPublisherImpl implements DomainTemplateEventPublisher {

    @Autowired
    ApplicationEventPublisher publisher;

    public void domainServiceReinitialize() {
        publisher.publishEvent(new DomainServiceReinitializeEvent());
    }
}