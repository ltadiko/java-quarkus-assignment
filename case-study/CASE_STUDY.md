# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

### Key Challenges in Cost Allocation and Tracking:

1. **Multi-dimensional Cost Attribution**
   - How do we allocate shared costs (e.g., facility overhead) across multiple warehouses?
   - How do we handle costs that span multiple stores (transportation routes)?
   - What's the granularity needed - per order, per product, per warehouse?

2. **Real-time vs Batch Cost Calculation**
   - Should costs be calculated in real-time or batch processed?
   - How do we handle cost adjustments retroactively?
   - What's the acceptable latency for cost visibility?

3. **Cost Category Classification**
   - How do we categorize variable vs fixed costs?
   - Labor: hourly rates, overtime, temporary workers?
   - Inventory: holding costs, shrinkage, obsolescence?
   - Transportation: per-mile, per-delivery, fuel surcharges?

### Questions I Would Ask Stakeholders:

1. What are the current pain points in cost tracking?
2. Who are the primary consumers of cost data (finance, operations, executives)?
3. What decisions will be made based on this cost data?
4. What's the required accuracy level (±1%, ±5%)?
5. Are there regulatory or compliance requirements for cost reporting?

### Technical Considerations:

- Cost center hierarchy (Company → Region → Warehouse → Department)
- Temporal tracking (effective dates, historical snapshots)
- Integration with payroll, inventory, fleet management, ERP systems

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

### Potential Cost Optimization Strategies:

1. **Warehouse Optimization** - Layout, zone-based picking, demand-based positioning → 15-25% labor cost reduction
2. **Inventory Management** - JIT practices, reduce safety stock, liquidate slow movers → 10-20% holding cost reduction
3. **Transportation Optimization** - Route optimization, shipment consolidation → 10-15% shipping cost reduction
4. **Labor Efficiency** - Workforce management, cross-training, predictive scheduling → 5-10% productivity improvement

### Prioritization Framework:

- HIGH: High ROI, quick wins, low-medium effort (1-3 months)
- MEDIUM: Good ROI, some complexity (3-6 months)  
- LOW: Long-term benefits, high effort (6-12 months)

### Implementation Approach:

1. **Identify**: Analyze current cost structure, benchmark against industry
2. **Prioritize**: Impact vs effort matrix, consider dependencies
3. **Pilot**: Test in limited scope first
4. **Measure**: Define KPIs, track continuously
5. **Scale**: Roll out successful pilots

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

### Importance of Integration:

1. **Single Source of Truth** - Eliminates data silos, reduces reconciliation
2. **Real-time Visibility** - Proactive cost management, faster decisions
3. **Audit Trail and Compliance** - Complete history, regulatory compliance

### Benefits:

- 20-30% time savings from reduced manual entry
- 90% reduction in data discrepancies
- 3-5 days reduction in month-end close
- Audit-ready data for compliance

### Integration Approach:

- API-First Architecture (REST, webhooks, message queues)
- Sync patterns: Real-time for critical transactions, batch for reconciliation
- Error handling with retry mechanisms, monitoring, alerting

### Questions to Gather:

- What financial systems are in use (SAP, Oracle, NetSuite)?
- Required data freshness (real-time, hourly, daily)?
- Existing integration patterns or middleware?
- Transaction volume expectations?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

### Importance:

1. **Resource Planning** - Staffing, inventory procurement, capacity planning
2. **Financial Control** - Spending limits, variance tracking, early course corrections
3. **Strategic Decisions** - Expansion evaluation, warehouse location assessment

### System Design Considerations:

1. **Data Inputs**: Historical costs (2-3 years), demand forecasts, seasonality, external factors
2. **Forecasting Methods**: Time series, regression models, ML for complex patterns, scenario planning
3. **Budget Structure**: Top-down, bottom-up, or zero-based budgeting
4. **Variance Analysis**: Automated detection, root cause categorization

### Questions to Gather:

- Budget cycle (annual, quarterly, rolling)?
- Budget owners and approval workflow?
- Detail level needed (cost center, product, customer)?
- Forecast error tolerance?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

### Importance of Preserving Cost History:

1. **Baseline for Budgeting** - Historical costs inform new warehouse budget
2. **Benchmark Comparison** - Compare new vs old performance, validate ROI
3. **Audit and Compliance** - Financial records retention (7+ years)
4. **Learning and Optimization** - Understand cost drivers, avoid repeating issues

### Technical Implementation (Our Code):

```java
// ReplaceWarehouseUseCase.java
archiveOldWarehouse(oldWarehouse);  // Soft delete preserves history
createNewWarehouse(businessUnitCode, newWarehouse, oldWarehouse.stock);
```

Key Design Decisions:
- Soft delete (archivedAt) preserves all historical data
- Business Unit Code reuse maintains reporting continuity
- Stock transfer ensures operational continuity
- Separate records allow cost history segregation

### Cost Control Aspects:

1. **Transition Costs** - Moving, downtime, parallel operations (track separately)
2. **Budget Carryover** - Unspent budget treatment, committed costs handling
3. **Performance Metrics Reset** - When "clock starts" for new warehouse

### Questions to Gather:

- Transition timeline and overlap period?
- How to allocate costs during transition?
- Report requirements for combined vs separate history?
- Contracts/leases tied to old warehouse?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
