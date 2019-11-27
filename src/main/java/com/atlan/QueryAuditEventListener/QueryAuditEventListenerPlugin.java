package com.atlan.QueryAuditEventListener;

import io.prestosql.spi.Plugin;
import io.prestosql.spi.eventlistener.EventListenerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryAuditEventListenerPlugin implements Plugin {
   public Iterable<EventListenerFactory> getEventListenerFactories() {
      EventListenerFactory listenerFactory = new QueryAuditEventListenerFactory();
      List<EventListenerFactory> listenerFactoryList = new ArrayList();
      listenerFactoryList.add(listenerFactory);
      List<EventListenerFactory> immutableList = Collections.unmodifiableList(listenerFactoryList);
      return immutableList;
   }
}
