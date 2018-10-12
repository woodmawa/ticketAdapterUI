package ticketModel

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class ServiceRequestServiceSpec extends Specification {

    ServiceRequestService serviceRequestService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ServiceRequest(...).save(flush: true, failOnError: true)
        //new ServiceRequest(...).save(flush: true, failOnError: true)
        //ServiceRequest serviceRequest = new ServiceRequest(...).save(flush: true, failOnError: true)
        //new ServiceRequest(...).save(flush: true, failOnError: true)
        //new ServiceRequest(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //serviceRequest.id
    }

    void "test get"() {
        setupData()

        expect:
        serviceRequestService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ServiceRequest> serviceRequestList = serviceRequestService.list(max: 2, offset: 2)

        then:
        serviceRequestList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        serviceRequestService.count() == 5
    }

    void "test delete"() {
        Long serviceRequestId = setupData()

        expect:
        serviceRequestService.count() == 5

        when:
        serviceRequestService.delete(serviceRequestId)
        sessionFactory.currentSession.flush()

        then:
        serviceRequestService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ServiceRequest serviceRequest = new ServiceRequest()
        serviceRequestService.save(serviceRequest)

        then:
        serviceRequest.id != null
    }
}
