package com.intellisix.service_request_management.storage;

import java.util.ArrayList;
import java.util.List;

import com.intellisix.service_request_management.model.ServiceRequest;

import org.springframework.stereotype.Repository;
/**
 * Repository class for storing and managing ServiceRequest objects.
 * Works with JSON persistence through ServiceRequestFilePersistence.
 *
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/stroage/ServiceRequestRepository.java
 */
@Repository
public class ServiceRequestRepository {

    private List<ServiceRequest> store;
    private final ServiceRequestFilePersistence filePersistence;

    public ServiceRequestRepository(ServiceRequestFilePersistence filePersistence) {
        this.filePersistence = filePersistence;
        this.store = filePersistence.loadAll();
        if (this.store == null) {
            this.store = new ArrayList<>();
        }
    }

    public void save(ServiceRequest request) {
        store.add(request);
        filePersistence.saveAll(store);
    }

    public ServiceRequest findById(String requestId) {
        for (ServiceRequest r : store) {
            if (r.getRequestId().equals(requestId)) {
                return r;
            }
        }
        return null;
    }

    public List<ServiceRequest> findAll() {
        return store;
    }

    public void update(ServiceRequest r) {
        // Since objects are stored by reference, this just persists the updated list.
        filePersistence.saveAll(store);
    }

    public void delete(String requestId) {
        boolean removed = store.removeIf(r -> r.getRequestId().equals(requestId));
        if (removed) {
            filePersistence.saveAll(store);
        }
    }

    public List<ServiceRequest> findByClientId(String clientId) {
        List<ServiceRequest> result = new ArrayList<>();
        for (ServiceRequest r : store) {
            if (r.getClientId().equals(clientId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<ServiceRequest> findByStaffId(String staffId) {
        List<ServiceRequest> result = new ArrayList<>();
        for (ServiceRequest r : store) {
            if (staffId.equals(r.getAssignedStaffId())) {
                result.add(r);
            }
        }
        return result;
    }
}
