package com.atlan.QueryAuditEventListener;

import io.prestosql.spi.eventlistener.EventListener;
import io.prestosql.spi.eventlistener.EventListenerFactory;
import java.util.Map;

public class QueryAuditEventListenerFactory implements EventListenerFactory {
   public String getName() {
      return "atlan-audit-logger";
   }

   public EventListener create(Map<String, String> config) {
      return new QueryAuditEventListener(config);
   }
}
