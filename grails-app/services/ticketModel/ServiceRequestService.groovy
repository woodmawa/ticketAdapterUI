package ticketModel

import grails.gorm.services.Service

@Service(ServiceRequest)
interface ServiceRequestService {

    ServiceRequest get(Serializable id)

    List<ServiceRequest> list(Map args)

    Long count()

    void delete(Serializable id)

    ServiceRequest save(ServiceRequest serviceRequest)

}