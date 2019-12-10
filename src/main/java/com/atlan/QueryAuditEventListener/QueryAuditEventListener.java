package com.atlan.QueryAuditEventListener;

import io.prestosql.spi.PrestoWarning;
import io.prestosql.spi.eventlistener.*;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONTokener;
import io.prestosql.spi.eventlistener.EventListener;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import io.airlift.units.DataSize;

public class QueryAuditEventListener implements EventListener {
   Logger logger;
   final String loggerName = "QueryLog";
   final String loggerVersion = "v1.1";
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
         jsonMap.put("loggerVersion", this.loggerVersion);
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
         Map<String, Object> jsonMap = new HashMap<>();
         String esId = queryCompletedEvent.getMetadata().getQueryId();

         //metadata
         QueryMetadata metadata = queryCompletedEvent.getMetadata();
         jsonMap.put("state", metadata.getQueryState());
         if (metadata.getPlan().isPresent()) {
            jsonMap.put("queryPlan", metadata.getPlan().get());
         }
         if (metadata.getPayload().isPresent()) {
            jsonMap.put("queryPayload", metadata.getPayload().get());
         }
         if (metadata.getTransactionId().isPresent()) {
            jsonMap.put("queryTransactionId", metadata.getTransactionId().get());
         }
         // stats
         QueryStatistics stats = queryCompletedEvent.getStatistics();
         jsonMap.put("cpuTime", stats.getCpuTime().getSeconds());
         jsonMap.put("wallTime", stats.getWallTime().getSeconds());
         jsonMap.put("queuedTime", stats.getQueuedTime().getSeconds());
         if (stats.getResourceWaitingTime().isPresent()) {
            jsonMap.put("waitingTime", stats.getResourceWaitingTime().get().getSeconds());
         }
         if (stats.getAnalysisTime().isPresent()) {
            jsonMap.put("analysisTime", stats.getAnalysisTime().get().getSeconds());
         }
         if (stats.getDistributedPlanningTime().isPresent()) {
            jsonMap.put("distributedPlanningTime", stats.getDistributedPlanningTime().get().getSeconds());
         }
         jsonMap.put("peakUserMemoryBytes", stats.getPeakUserMemoryBytes());
         jsonMap.put("peakTotalNonRevocableMemoryBytes", stats.getPeakTotalNonRevocableMemoryBytes());
         jsonMap.put("peakTaskUserMemory", stats.getPeakTaskUserMemory());
         jsonMap.put("peakTaskTotalMemory", stats.getPeakTaskTotalMemory());
         jsonMap.put("physicalInputBytes", stats.getPhysicalInputBytes());
         jsonMap.put("physicalInputRows", stats.getPhysicalInputRows());
         jsonMap.put("internalNetworkBytes", stats.getInternalNetworkBytes());
         jsonMap.put("internalNetworkRows", stats.getInternalNetworkRows());
         jsonMap.put("totalBytes", stats.getTotalBytes());
         jsonMap.put("totalRows", stats.getTotalRows());
         jsonMap.put("outputBytes", stats.getOutputBytes());
         jsonMap.put("outputRows", stats.getOutputRows());
         jsonMap.put("writtenBytes", stats.getWrittenBytes());
         jsonMap.put("writtenRows", stats.getWrittenRows());
         jsonMap.put("cumulativeMemory", stats.getCumulativeMemory());
//         jsonMap.put("stageGcStatistics", stats.getStageGcStatistics());
         jsonMap.put("completedSplits", stats.getCompletedSplits());
//         jsonMap.put("cpuTimeDistribution", stats.getCpuTimeDistribution());
//         jsonMap.put("operatorSummaries", String.join(",", stats.getOperatorSummaries()));
         long physicalWrittenData = DataSize.valueOf("0B").toBytes();
         long spilledDataSize = DataSize.valueOf("0B").toBytes();
         ArrayList<Map<String, Object>> operatorSummaries = new ArrayList<>();
         for (String operatorSummary : stats.getOperatorSummaries()) {
            Map<String, Object> stringObjectMap = new JSONObject(new JSONTokener(operatorSummary)).toMap();
            operatorSummaries.add(stringObjectMap);
            DataSize operatorPhysicalWrittenData = DataSize.valueOf((String) stringObjectMap.get("physicalWrittenDataSize"));
            DataSize operatorSpilledDataSize = DataSize.valueOf((String) stringObjectMap.get("spilledDataSize"));
            physicalWrittenData += operatorPhysicalWrittenData.toBytes();
            spilledDataSize += operatorSpilledDataSize.toBytes();
         }
         jsonMap.put("physicalWrittenData", physicalWrittenData);
         jsonMap.put("spilledDataSize", spilledDataSize);
         jsonMap.put("operatorSummaries", stats.getOperatorSummaries());
         if (stats.getPlanNodeStatsAndCosts().isPresent()) {
            jsonMap.put("planNodeStatsAndCosts", stats.getPlanNodeStatsAndCosts().get());
         }
         // query io metadata

         QueryIOMetadata ioMetadata = queryCompletedEvent.getIoMetadata();
//         jsonMap.put("ioMetadataInput", ioMetadata.getInputs());
//         if (ioMetadata.getOutput().isPresent()) {
//            jsonMap.put("ioMetadataOutput", ioMetadata.getOutput());
//         }

         // query context
         QueryContext queryContext = queryCompletedEvent.getContext();
         jsonMap.put("serverVersion", queryContext.getServerVersion());

//         ArrayList<String> sessionProps = new ArrayList<>();
         String sessionProps = queryContext.getSessionProperties().entrySet().stream()
                 .map(
                         stringStringEntry -> stringStringEntry.getKey() + "=" + stringStringEntry.getValue()
                 ).collect(Collectors.joining(","));


//         for (Map.Entry<String, String> entry: queryContext.getSessionProperties().entrySet()) {
//            sessionProps.add(entry.getKey() + "=" + entry.getValue());
//         }
         jsonMap.put("sessionProperties", sessionProps);
         if (queryContext.getResourceEstimates().getCpuTime().isPresent()) {
            jsonMap.put("resourceEstimateCPUTimeBeta", queryContext.getResourceEstimates().getCpuTime().get().toMillis() / 1000);
         }
         if (queryContext.getResourceEstimates().getExecutionTime().isPresent()) {
            jsonMap.put("resourceEstimateExecutionTimeBeta", queryContext.getResourceEstimates().getExecutionTime().get().toMillis() / 1000);
         }
         if (queryContext.getResourceEstimates().getPeakMemory().isPresent()) {
            jsonMap.put("resourceEstimatePeakMemoryBeta", queryContext.getResourceEstimates().getPeakMemory().get().toBytes());
         }

         // presto warnings

         jsonMap.put("prestoWarnings", queryCompletedEvent.getWarnings().stream()
                 .map(PrestoWarning::toString).collect(Collectors.joining(",")));

         // misc
         jsonMap.put("createTime", queryCompletedEvent.getCreateTime().toString());
         jsonMap.put("executionStartTime", queryCompletedEvent.getExecutionStartTime().toString());
         jsonMap.put("endTime", queryCompletedEvent.getEndTime().toString());

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
