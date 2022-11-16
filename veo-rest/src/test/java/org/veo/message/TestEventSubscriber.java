/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Alexander Koderman.
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
package org.veo.message;

import static org.veo.core.events.MessageCreatorImpl.ROUTING_KEY_ELEMENT_CLIENT_CHANGE;
import static org.veo.core.events.MessageCreatorImpl.ROUTING_KEY_ELEMENT_TYPE_DEFINITION_UPDATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Use this event subscriber in tests to assert received events. */
// @Component
@Slf4j
// @Profile(value = { "testrabbit" })
public class TestEventSubscriber {

  @Getter List<EventMessage> receivedEvents = new ArrayList<>();

  @RabbitListener(
      bindings =
          @QueueBinding(
              value =
                  @Queue(
                      value = "${veo.message.consume.queue}_TestEventSubscriber",
                      exclusive = "false",
                      durable = "false",
                      autoDelete = "true"),
              exchange = @Exchange(value = "${veo.message.dispatch.exchange}", type = "topic"),
              key = {
                "${veo.message.dispatch.routing-key-prefix}"
                    + ROUTING_KEY_ELEMENT_TYPE_DEFINITION_UPDATE,
                "${veo.message.consume.subscription-routing-key-prefix}"
                    + ROUTING_KEY_ELEMENT_CLIENT_CHANGE,
                "${veo.message.dispatch.routing-key-prefix}veo.testmessage"
              }))
  void handleEntityEvent(EventMessage event) {
    log.debug("Consumed test event with content: {}", event.getContent());
    try {
      // save received events for assertions:
      this.receivedEvents.add(event);
    } catch (Exception e) {
      log.error("Error while consuming event {}: {}", event.getId(), event.getContent());
      // prevent re-queue and reprocessing:
      // replace with DLX for failing messages if retry, log or alert are required
      throw new AmqpRejectAndDontRequeueException(e);
    }
  }

  public List<EventMessage> getMessagesByRoutingKey(String key) {
    return getMessagesByRoutingKey().get(key);
  }

  private Map<String, List<EventMessage>> getMessagesByRoutingKey() {
    return receivedEvents.stream().collect(Collectors.groupingBy(t -> t.routingKey));
  }
}
