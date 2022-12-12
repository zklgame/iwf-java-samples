package io.github.cadenceoss.iwf.workflow.dsl;

import io.github.cadenceoss.iwf.core.StateDef;
import io.github.cadenceoss.iwf.core.Workflow;
import io.github.cadenceoss.iwf.core.communication.CommunicationMethodDef;
import io.github.cadenceoss.iwf.core.persistence.DataObjectDef;
import io.github.cadenceoss.iwf.core.persistence.PersistenceFieldDef;
import io.github.cadenceoss.iwf.workflow.dsl.utils.DynamicDslWorkflowAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicDslWorkflow implements Workflow {
    private final Map<String, DynamicDslWorkflowAdapter> adapterMap;

    public DynamicDslWorkflow(final Map<String, DynamicDslWorkflowAdapter> adapterMap) {
        this.adapterMap = adapterMap;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        Set<String> attributeDefs = adapterMap.values()
                .stream()
                .map(DynamicDslWorkflowAdapter::getStateDefsForWorkflow)
                .flatMap(Collection::stream)
                .map(s -> s.getWorkflowState().getStateId())
                .collect(Collectors.toSet());

        return attributeDefs.stream()
                .map(name -> DataObjectDef.create(Object.class, name))
                .collect(Collectors.toList());
    }

    @Override
    public List<StateDef> getStates() {
        return new ArrayList<>(adapterMap.values().stream()
                .map(DynamicDslWorkflowAdapter::getStateDefsForWorkflow)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(stateDef -> stateDef.getWorkflowState().getStateId(), Function.identity(), (p, q) -> p)).values());
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return adapterMap.values().stream()
                .map(DynamicDslWorkflowAdapter::getSignalChannelDefForWorkflow)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
