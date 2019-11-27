package com.atlan.QueryAuditEventListener;

import io.prestosql.spi.eventlistener.EventListener;
import io.prestosql.spi.eventlistener.QueryCompletedEvent;
import io.prestosql.spi.eventlistener.QueryCreatedEvent;
import io.prestosql.spi.eventlistener.QueryFailureInfo;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;

public class QueryAuditEventListener implements EventListener {
   Logger logger;
   final String loggerName = "QueryLog";
   RestHighLevelClient client;
   final String indexName = "atlan-query-logs";
   final String typeName = "logs";

   public QueryAuditEventListener(Map<String, String> config) {
      this.initializeEsClient(config);
   }

   public void queryCreated(QueryCreatedEvent queryCreatedEvent) {
      try {
         String esId = queryCreatedEvent.getMetadata().getQueryId();
         Map<String, Object> jsonMap = new HashMap();
         String query = queryCreatedEvent.getMetadata().getQuery().toLowerCase();
         List<String> queryType = new ArrayList();
         if (query.contains("select") && !query.contains("create")) {
            queryType.add("SELECT");
         }

         if (query.contains("create") || query.contains("update")) {
            queryType.add("CREATE OR REPLACE");
         }

         if (query.contains("insert")) {
            queryType.add("INSERT");
         }

         if (query.contains("delete")) {
            queryType.add("DELETE");
         }

         if (query.contains("count")) {
            queryType.add("COUNT");
         }

         if (query.contains("select count(*) as count from")) {
            queryType.add("ROW_COUNT_FROM_UI");
         }

         if (query.contains("join") && query.contains("in")) {
            queryType.add("JOIN");
         }

         if (queryType.size() == 0) {
            queryType.add("OTHER");
         }

         jsonMap.put("user", queryCreatedEvent.getContext().getUser());
         jsonMap.put("query", queryCreatedEvent.getMetadata().getQuery());
         jsonMap.put("state", queryCreatedEvent.getMetadata().getQueryState());
         jsonMap.put("createdAt", queryCreatedEvent.getCreateTime().toString());
         jsonMap.put("serverAddress", queryCreatedEvent.getContext().getServerAddress());
         jsonMap.put("queryType", queryType);
         if (!queryCreatedEvent.getContext().getSessionProperties().isEmpty()) {
            jsonMap.put("sessionProperties", queryCreatedEvent.getContext().getSessionProperties().toString());
         }
         if (queryCreatedEvent.getContext().getPrincipal().isPresent()) {
            jsonMap.put("principal", queryCreatedEvent.getContext().getPrincipal().get());
         }

         if (queryCreatedEvent.getContext().getSource().isPresent()) {
            jsonMap.put("source", queryCreatedEvent.getContext().getSource().get());
         }

         if (queryCreatedEvent.getContext().getRemoteClientAddress().isPresent()) {
            jsonMap.put("clientIp", queryCreatedEvent.getContext().getRemoteClientAddress().get());
         }

         if (queryCreatedEvent.getContext().getUserAgent().isPresent()) {
            jsonMap.put("userAgent", queryCreatedEvent.getContext().getUserAgent().get());
         }

         if (queryCreatedEvent.getContext().getCatalog().isPresent()) {
            jsonMap.put("catalog", queryCreatedEvent.getContext().getCatalog().get());
         }

         if (queryCreatedEvent.getContext().getSchema().isPresent()) {
            jsonMap.put("schema", queryCreatedEvent.getContext().getSchema().get());
         }

         IndexRequest request = (new IndexRequest("atlan-query-logs")).type("logs").id(esId).source(jsonMap);
         request.timeout(TimeValue.timeValueSeconds(5L));
         this.client.index(request, RequestOptions.DEFAULT);
      } catch (Exception var7) {
         this.logger.warning("_____________--------Error4-------___________");
         this.logger.warning(Arrays.toString(var7.getStackTrace()));
         this.logger.warning(var7.getMessage());
         this.logger.warning("_____________--------Error4-------___________");
      }

   }

   public void queryCompleted(QueryCompletedEvent queryCompletedEvent) {
      try {
         Map<String, Object> jsonMap = new HashMap();
         String esId = queryCompletedEvent.getMetadata().getQueryId();
         jsonMap.put("state", queryCompletedEvent.getMetadata().getQueryState());
         jsonMap.put("wallTime", queryCompletedEvent.getStatistics().getWallTime().toMillis());
         jsonMap.put("cpuTime", queryCompletedEvent.getStatistics().getCpuTime().toMillis());
         jsonMap.put("cumulativeMemory", queryCompletedEvent.getStatistics().getCumulativeMemory());
         jsonMap.put("queuedTime", queryCompletedEvent.getStatistics().getQueuedTime().toMillis());
         jsonMap.put("totalRows", queryCompletedEvent.getStatistics().getTotalRows());
         jsonMap.put("outputRows", queryCompletedEvent.getStatistics().getOutputRows());
         jsonMap.put("peakTaskTotalMemory", queryCompletedEvent.getStatistics().getPeakTaskTotalMemory());
         jsonMap.put("peakTaskUserMemory", queryCompletedEvent.getStatistics().getPeakTaskUserMemory());
         jsonMap.put("physicalInputBytes", queryCompletedEvent.getStatistics().getPhysicalInputBytes());
         jsonMap.put("physicalInputRows", queryCompletedEvent.getStatistics().getPhysicalInputRows());
         jsonMap.put("writtenDataRows", queryCompletedEvent.getStatistics().getWrittenRows());
         jsonMap.put("writtenDataBytes", queryCompletedEvent.getStatistics().getWrittenBytes());

         try {
            if (queryCompletedEvent.getFailureInfo().isPresent()) {
               jsonMap.put("errorCode", ((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getErrorCode().getCode());
               jsonMap.put("errorName", ((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getErrorCode().getName());
               if (((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureType().isPresent()) {
                  jsonMap.put("errorType", ((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureType().get());
               }

               if (((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureMessage().isPresent()) {
                  jsonMap.put("errorMsg", ((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureMessage().get());
               }

               if (((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureTask().isPresent()) {
                  jsonMap.put("errorTask", ((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureTask().get());
               }

               if (((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureHost().isPresent()) {
                  jsonMap.put("errorHost", ((QueryFailureInfo)queryCompletedEvent.getFailureInfo().get()).getFailureHost().get());
               }
            }
         } catch (Exception var5) {
            this.logger.warning("_____________--------Error3-------___________");
            this.logger.warning(var5.getMessage());
            this.logger.warning("_____________--------Error3-------___________");
         }

         UpdateRequest request = (new UpdateRequest("atlan-query-logs", "logs", esId)).doc(jsonMap);
         request.docAsUpsert(true);
         request.retryOnConflict(3);
         request.timeout(TimeValue.timeValueSeconds(5L));
         this.client.update(request, RequestOptions.DEFAULT);
      } catch (Exception var6) {
         this.logger.warning("_____________--------Error2-------___________");
         this.logger.warning(var6.getMessage());
         this.logger.warning("_____________--------Error2-------___________");
      }

   }

   public void initializeEsClient(Map<String, String> config) {
      try {
         String esHost = (String)config.get("es-host");
         String esPort = (String)config.get("es-port");
         this.logger = Logger.getLogger("QueryLog");
         this.logger.info(esHost);
         this.client = new RestHighLevelClient(RestClient.builder(new HttpHost[]{new HttpHost(esHost, Integer.parseInt(esPort), "https")}));
         this.logger.info("client Initialized..");
      } catch (Exception var4) {
         this.logger.warning("_____________--------Error1-------___________");
         this.logger.warning(var4.getMessage());
         this.logger.warning("_____________--------Error1-------___________");
      }

   }
}
