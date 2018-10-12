package ticketModel

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class BillOfMaterialsServiceSpec extends Specification {

    BillOfMaterialsService billOfMaterialsService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new BillOfMaterials(...).save(flush: true, failOnError: true)
        //new BillOfMaterials(...).save(flush: true, failOnError: true)
        //BillOfMaterials billOfMaterials = new BillOfMaterials(...).save(flush: true, failOnError: true)
        //new BillOfMaterials(...).save(flush: true, failOnError: true)
        //new BillOfMaterials(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //billOfMaterials.id
    }

    void "test get"() {
        setupData()

        expect:
        billOfMaterialsService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<BillOfMaterials> billOfMaterialsList = billOfMaterialsService.list(max: 2, offset: 2)

        then:
        billOfMaterialsList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        billOfMaterialsService.count() == 5
    }

    void "test delete"() {
        Long billOfMaterialsId = setupData()

        expect:
        billOfMaterialsService.count() == 5

        when:
        billOfMaterialsService.delete(billOfMaterialsId)
        sessionFactory.currentSession.flush()

        then:
        billOfMaterialsService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        BillOfMaterials billOfMaterials = new BillOfMaterials()
        billOfMaterialsService.save(billOfMaterials)

        then:
        billOfMaterials.id != null
    }
}
