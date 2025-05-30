/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.planner.rm;


import org.apache.drill.PlanTestBase;
import org.apache.drill.exec.ExecConstants;
import org.apache.drill.exec.ops.QueryContext;
import org.apache.drill.exec.planner.PhysicalPlanReader;
import org.apache.drill.exec.planner.fragment.DistributedQueueParallelizer;
import org.apache.drill.exec.planner.fragment.Fragment;
import org.apache.drill.exec.planner.fragment.PlanningSet;
import org.apache.drill.exec.planner.fragment.SimpleParallelizer;
import org.apache.drill.exec.planner.fragment.Wrapper;
import org.apache.drill.common.DrillNode;
import org.apache.drill.exec.pop.PopUnitTestBase;
import org.apache.drill.exec.proto.CoordinationProtos.DrillbitEndpoint;
import org.apache.drill.exec.proto.UserBitShared;
import org.apache.drill.exec.proto.UserProtos;
import org.apache.drill.exec.resourcemgr.NodeResources;
import org.apache.drill.exec.resourcemgr.config.QueryQueueConfig;
import org.apache.drill.exec.resourcemgr.config.exception.QueueSelectionException;
import org.apache.drill.exec.rpc.user.UserSession;
import org.apache.drill.exec.server.DrillbitContext;
import org.apache.drill.exec.work.foreman.rm.QueryResourceManager;
import org.apache.drill.exec.work.foreman.rm.EmbeddedQueryQueue;
import org.apache.drill.shaded.guava.com.google.common.collect.Iterables;
import org.apache.drill.test.ClientFixture;
import org.apache.drill.test.ClusterFixture;
import org.apache.drill.test.ClusterFixtureBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMemoryCalculator extends PlanTestBase {

  private static final long DEFAULT_SLICE_TARGET = 100000L;
  private static final long DEFAULT_BATCH_SIZE = 16*1024*1024;

  private static final UserSession session = UserSession.Builder.newBuilder()
    .withCredentials(UserBitShared.UserCredentials.newBuilder()
      .setUserName("foo")
      .build())
    .withUserProperties(UserProtos.UserProperties.getDefaultInstance())
    .withOptionManager(bits[0].getContext().getOptionManager())
    .build();

  private static final DrillbitEndpoint N1_EP1 = newDrillbitEndpoint("node1", 30010);
  private static final DrillbitEndpoint N1_EP2 = newDrillbitEndpoint("node2", 30011);
  private static final DrillbitEndpoint N1_EP3 = newDrillbitEndpoint("node3", 30012);
  private static final DrillbitEndpoint N1_EP4 = newDrillbitEndpoint("node4", 30013);

  private static final DrillbitEndpoint[] nodeList = {N1_EP1, N1_EP2, N1_EP3, N1_EP4};

  private static final DrillbitEndpoint newDrillbitEndpoint(String address, int port) {
    return DrillbitEndpoint.newBuilder().setAddress(address).setControlPort(port).build();
  }
  private static final DrillbitContext drillbitContext = getDrillbitContext();
  private static final QueryContext queryContext = new QueryContext(session, drillbitContext,
                                                                    UserBitShared.QueryId.getDefaultInstance());

  private static Map<DrillbitEndpoint, String> onlineEndpoints;
  private Map<String, NodeResources> totalResources;

  @AfterClass
  public static void close() throws Exception {
    queryContext.close();
  }

  private QueryResourceManager mockResourceManager() throws QueueSelectionException {
    final QueryResourceManager mockRM = mock(QueryResourceManager.class);
    final QueryQueueConfig queueConfig = mock(QueryQueueConfig.class);

    when(queueConfig.getMaxQueryMemoryInMBPerNode()).thenReturn(10L);
    when(queueConfig.getQueueTotalMemoryInMB(anyInt())).thenReturn(100L);
    when(mockRM.selectQueue(any(NodeResources.class))).thenReturn(queueConfig);
    when(mockRM.minimumOperatorMemory()).thenReturn(40L);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        totalResources = (Map<String, NodeResources>) invocation.getArguments()[0];
        return null;
      }
    }).when(mockRM).setCost(any(Map.class));

    return mockRM;
  }

  private final Wrapper mockWrapper(Wrapper rootFragment,
                                    Map<DrillNode, NodeResources> resourceMap,
                                    List<DrillbitEndpoint> endpoints,
                                    Map<Fragment, Wrapper> originalToMockWrapper ) {
    final Wrapper mockWrapper = mock(Wrapper.class);
    originalToMockWrapper.put(rootFragment.getNode(), mockWrapper);
    List<Wrapper> mockdependencies = new ArrayList<>();

    for (Wrapper dependency : rootFragment.getFragmentDependencies()) {
      mockdependencies.add(mockWrapper(dependency, getNodeResources(), endpoints, originalToMockWrapper));
    }

    when(mockWrapper.getNode()).thenReturn(rootFragment.getNode());
    when(mockWrapper.getAssignedEndpoints()).thenReturn(endpoints);
    when(mockWrapper.getResourceMap()).thenReturn(resourceMap);
    when(mockWrapper.getWidth()).thenReturn(endpoints.size());
    when(mockWrapper.getFragmentDependencies()).thenReturn(mockdependencies);
    when(mockWrapper.isEndpointsAssignmentDone()).thenReturn(true);
    return mockWrapper;
  }

  private final PlanningSet mockPlanningSet(PlanningSet planningSet,
                                       List<DrillbitEndpoint> endpoints) {
    Map<Fragment, Wrapper> wrapperToMockWrapper = new HashMap<>();
    Wrapper rootFragment = mockWrapper(planningSet.getRootWrapper(), getNodeResources(), endpoints, wrapperToMockWrapper);
    PlanningSet mockPlanningSet = mock(PlanningSet.class);
    when(mockPlanningSet.getRootWrapper()).thenReturn(rootFragment);
    when(mockPlanningSet.get(any(Fragment.class))).thenAnswer(invocation -> {
      return wrapperToMockWrapper.get(invocation.getArgument(0));
    });
    return mockPlanningSet;
  }

  private String getPlanForQuery(String query) throws Exception {
    return getPlanForQuery(query, DEFAULT_BATCH_SIZE);
  }

  private String getPlanForQuery(String query, long outputBatchSize) throws Exception {
    return getPlanForQuery(query, outputBatchSize, DEFAULT_SLICE_TARGET);
  }

  private String getPlanForQuery(String query, long outputBatchSize,
                                 long slice_target) throws Exception {
    ClusterFixtureBuilder builder = ClusterFixture.builder(dirTestWatcher)
      .setOptionDefault(ExecConstants.OUTPUT_BATCH_SIZE, outputBatchSize)
      .setOptionDefault(ExecConstants.SLICE_TARGET, slice_target);
    String plan;

    try (ClusterFixture cluster = builder.build();
         ClientFixture client = cluster.clientFixture()) {
      plan = client.queryBuilder()
        .sql(query)
        .explainJson();
    }
    return plan;
  }

  private static Map<DrillbitEndpoint, String> getEndpoints(int totalMinorFragments,
                                                     Set<DrillbitEndpoint> notIn) {
    Map<DrillbitEndpoint, String> endpoints = new HashMap<>();
    Iterator drillbits = Iterables.cycle(nodeList).iterator();

    int i=0;
    while(totalMinorFragments-- > 0) {
      DrillbitEndpoint dbit = (DrillbitEndpoint) drillbits.next();
      if (!notIn.contains(dbit)) {
        endpoints.put(dbit, "drillbit" + ++i);
      }
    }
    return endpoints;
  }

  private Set<Wrapper> createSet(Wrapper... wrappers) {
    Set<Wrapper> setOfWrappers = new HashSet<>();
    for (Wrapper wrapper : wrappers) {
      setOfWrappers.add(wrapper);
    }
    return setOfWrappers;
  }

  private Fragment getRootFragmentFromPlan(DrillbitContext context,
                                           String plan) throws Exception {
    final PhysicalPlanReader planReader = context.getPlanReader();
    return PopUnitTestBase.getRootFragmentFromPlanString(planReader, plan);
  }

  private PlanningSet preparePlanningSet(List<DrillbitEndpoint> activeEndpoints, long slice_target,
                                         String sql, SimpleParallelizer parallelizer) throws Exception {
    Fragment rootFragment = getRootFragmentFromPlan(drillbitContext, getPlanForQuery(sql, 10, slice_target));
    return mockPlanningSet(parallelizer.prepareFragmentTree(rootFragment), activeEndpoints);
  }

  private Map<DrillNode, NodeResources> getNodeResources() {
    return onlineEndpoints.keySet().stream().collect(Collectors.toMap(x -> DrillNode.create(x), x -> NodeResources.create()));
  }

  @BeforeClass
  public static void setupForAllTests() {
    onlineEndpoints = getEndpoints(2, new HashSet<>());
  }

  @Test
  public void TestSingleMajorFragmentWithProjectAndScan() throws Exception {
    String sql = "SELECT * from cp.`tpch/nation.parquet`";

    SimpleParallelizer parallelizer = new DistributedQueueParallelizer(false, queryContext, mockResourceManager());
    PlanningSet planningSet = preparePlanningSet(new ArrayList<>(onlineEndpoints.keySet()), DEFAULT_SLICE_TARGET, sql, parallelizer);
    parallelizer.adjustMemory(planningSet, createSet(planningSet.getRootWrapper()), onlineEndpoints);
    assertTrue("memory requirement is different", Iterables.all(totalResources.entrySet(), (e) -> e.getValue().getMemoryInBytes() == 30));
  }


  @Test
  public void TestSingleMajorFragmentWithGroupByProjectAndScan() throws Exception {
    String sql = "SELECT dept_id, count(*) from cp.`tpch/lineitem.parquet` group by dept_id";

    SimpleParallelizer parallelizer = new DistributedQueueParallelizer(false, queryContext, mockResourceManager());
    PlanningSet planningSet = preparePlanningSet(new ArrayList<>(onlineEndpoints.keySet()), DEFAULT_SLICE_TARGET, sql, parallelizer);
    parallelizer.adjustMemory(planningSet, createSet(planningSet.getRootWrapper()), onlineEndpoints);
    assertTrue("memory requirement is different", Iterables.all(totalResources.entrySet(), (e) -> e.getValue().getMemoryInBytes() == 529570));
  }


  @Test
  public void TestTwoMajorFragmentWithSortProjectAndScan() throws Exception {
    String sql = "SELECT * from cp.`tpch/lineitem.parquet` order by dept_id";

    SimpleParallelizer parallelizer = new DistributedQueueParallelizer(false, queryContext, mockResourceManager());
    PlanningSet planningSet = preparePlanningSet(new ArrayList<>(onlineEndpoints.keySet()), 2, sql, parallelizer);
    parallelizer.adjustMemory(planningSet, createSet(planningSet.getRootWrapper()), onlineEndpoints);
    assertTrue("memory requirement is different", Iterables.all(totalResources.entrySet(), (e) -> e.getValue().getMemoryInBytes() == 481460));
  }

  @Test
  public void TestZKBasedQueue() throws Exception {
    String sql = "select * from cp.`employee.json`";
    ClusterFixtureBuilder builder = ClusterFixture.builder(dirTestWatcher).configProperty(EmbeddedQueryQueue.ENABLED, true);

    try (ClusterFixture cluster = builder.build();
         ClientFixture client = cluster.clientFixture()) {
      client
        .queryBuilder()
        .sql(sql)
        .run();
    }
  }
}
