/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.awsqueue;

import io.jmix.awsqueue.entity.QueueInfo;
import io.jmix.awsqueue.entity.QueueStatus;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("awsqueue_QueueStatusCache")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class QueueStatusCache {
    protected Map<String, QueueInfo> creatingQueues;
    protected Set<String> deletedQueueUrls;

    @PostConstruct
    protected void init() {
        creatingQueues = new ConcurrentHashMap<>();
        deletedQueueUrls = ConcurrentHashMap.newKeySet();
    }

    public void invalidate(Map<String, QueueInfo> actualData) {
        creatingQueues.keySet().removeAll(actualData
                .values()
                .stream()
                .map(QueueInfo::getName)
                .collect(Collectors.toList()));
        deletedQueueUrls.retainAll(actualData.keySet());
    }

    public void setCreating(QueueInfo queue) {
        queue.setStatus(QueueStatus.CREATING);
        creatingQueues.put(queue.getName(), queue);
    }

    public Collection<QueueInfo> getCreatingQueues() {
        return creatingQueues.values();
    }

    public boolean isDeleting(String queueUrl) {
        return deletedQueueUrls.contains(queueUrl);
    }

    public void setDeleting(String queueUrl) {
        deletedQueueUrls.add(queueUrl);
    }

    public boolean isNotAvailable(QueueInfo queue) {
        return creatingQueues.containsKey(queue.getName()) &&
                deletedQueueUrls.contains(queue.getName());
    }
}
